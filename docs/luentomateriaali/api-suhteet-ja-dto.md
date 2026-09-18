# REST: miten suhde merkitään API:ssa (TicketGuru)

Luento: valitse **GET-vastauksen muoto** ja **POST-pyynnön muoto** erikseen. DTO ei ole kolmas kilpailija näiden rinnalla — se on työkalu molempiin suuntiin.

Opettajan PDF (`linkitetyt_resurssit`) käyttää nimiä `User` ja `Transaction`. TicketGurussa ne ovat **Kayttaja** (kassalla: **myyjä**) ja **Myyntitapahtuma**. Sama idea, meidän nimet.

Sprint 3: tapahtumilla (`/api/events`) ei vielä ole suhdetta JSON:ssa. Tämä luento on seuraavaa sprinttiä varten (lipputyyppi, lippu, myynti).

---

## Dia 1 — Kaksi eri kysymystä

Älä sekoita näitä:

| Kysymys | Milloin | Esimerkki |
| --- | --- | --- |
| Miten **kerrotaan** mihin uusi rivi kuuluu? | POST / PUT | Uusi lipputyyppi tapahtumalle 5 |
| Miten **näytetään** liittyvä rivi? | GET | Myynti + kuka myi |

URL:n hierarkia (`/events/5/ticket-types`) on **osoite**. JSON:n sisäkkäisyys (`"tapahtuma": { ... }`) on **vastauksen muoto**. Ne ovat eri valintoja.

JavaScriptissä (Express) laitat usein kaiken URL:ään ja palautat SQL-rivin sellaisenaan. Spring Bootissa JPA-entiteetti on tietokantarivi olioksi — sen palauttaminen suoraan JSON:na on eri asia kuin API-sopimus.

---

## Dia 2 — TicketGurun taulut (ei “User”)

```text
Rooli  ←  Kayttaja  ←  Myyntitapahtuma  →  Tapahtuma
                              ↓
                            Lippu  →  Lipputyyppi  →  Tapahtuma
```

| Taulu | Mitä se on | Vierasavain |
| --- | --- | --- |
| `Tapahtuma` | Konsertti / ottelu | — |
| `Lipputyyppi` | “Normaali 25 €” tälle tapahtumalle | `tapahtuma_id` |
| `Kayttaja` | Kirjautuva henkilö | `rooli_id` |
| `Myyntitapahtuma` | Yksi kassatapahtuma | `tapahtuma_id`, `myyja_id` |
| `Lippu` | Yksi myyty kappale | `myyntitapahtuma_id`, `lipputyyppi_id` |

**Kayttaja** ei ole asiakas tiskillä. Asiakas ei kirjaudu. Kassahenkilö on `Kayttaja`, jonka kenttä myynnissä on `myyja` (`myyja_id`).

Roolit TicketGurussa: `MYYJA`, `TAPAHTUMAKOORDINAATTORI`, `PAAKAYTTAJA`. Yksi taulu, eri oikeudet.

Opettajan PDF:n `GET … user` upotettuna = meillä `GET /api/sales/{id}` ja kenttä **`myyja`**, ei `user`.

---

## Dia 3 — GET: pääresurssi ensin

Haetaan **myyntitapahtuma**, ei käyttäjää.

```
GET /api/sales/1
```

Päätaulu: `Myyntitapahtuma` (`myyntiaika`, `summa`).  
Suhde: `myyja` → `Kayttaja` (kuka myi).

Tästä eteenpäin kolme tapaa **muotoilla vastaus**.

---

## Dia 4 — GET tapa A: upotus (embedding)

Koko liittyvä olio JSON:n sisällä.

```json
{
  "id": 1,
  "myyntiaika": "2026-10-02T17:05:00",
  "summa": 50.00,
  "myyja": {
    "id": 27,
    "sahkoposti": "teija@ticketguru.fi",
    "nimi": "Teija Tiilikainen"
  }
}
```

Hyvä: yksi HTTP-kutsu, client näkee nimen heti.

Huono:

- Vastaus turpoaa (`myyja` → `rooli` → kaikki myynnit…).
- **Kehäviittaus**, jos molemmat suunnat serialisoidaan (alla Jackson).

Sama idea lipulle:

```
GET /api/tickets/10
```

```json
{
  "id": 10,
  "koodi": "TKT-1001",
  "kaytetty": false,
  "lipputyyppi": {
    "id": 3,
    "nimi": "Normaali",
    "hinta": 25.00
  }
}
```

---

## Dia 5 — Jackson ja kehäviittaus

**Jackson** = kirjasto, joka muuttaa Java-olion JSON:ksi (sarjallistus) ja JSON:n olioksi (deserialisointi). Spring Boot käyttää sitä `@RequestBody` / paluuarvoon.

Kehä syntyy **vain jos GET upottaa oikean JPA-entiteetin** ja relaatio on kaksisuuntainen:

```text
Myyntitapahtuma.myyja  →  Kayttaja
Kayttaja.myynnit       →  List<Myyntitapahtuma>
```

Jackson: myynti → myyjä → myynnit → myynti → … → pino täyteen.

Ei synny, jos:

- vastauksessa on vain URI tai id,
- vastaus on **Response DTO**, jossa ei ole paluulistaa,
- tai silmukka katkaistaan (`@JsonIgnore` / `@JsonManagedReference` + `@JsonBackReference`).

Entity Trick POST:ssa ei aiheuta tätä silmukkaa (siellä ei serialisoida puuta ulos samalla tavalla). Vaara on **GET + upotettu entiteetti**.

---

## Dia 6 — GET tapa B: linkki (URI)

```json
{
  "id": 1,
  "myyntiaika": "2026-10-02T17:05:00",
  "summa": 50.00,
  "myyja": "http://localhost:8080/api/users/27"
}
```

Hyvä: kevyt, ei kehää.

Huono: **N+1** — client tekee vielä `GET /api/users/27` nimen saamiseksi. Listassa 50 myyntiä → 50 lisäkutsua.

---

## Dia 7 — GET tapa C: Response DTO (valitut kentät)

Ei koko `Kayttaja`-oliota, ei salasanaa, ei listaa myynneistä:

```json
{
  "id": 1,
  "myyntiaika": "2026-10-02T17:05:00",
  "summa": 50.00,
  "myyjanNimi": "Teija Tiilikainen"
}
```

Tämä on se mitä TicketGuru jo tekee tapahtumille: `Tapahtuma` (taulu) ≠ `TapahtumaResponse` (JSON). Sprint 3:ssä ei vain ole vielä toista taulua vastauksessa.

---

## Dia 8 — POST: kolme tapaa merkitä “kuuluu tähän”

Uusi **lipputyyppi** kuuluu jo olemassa olevaan **tapahtumaan**. Tapahtumaa ei luoda uudelleen.

### Tapa 1 — id URL:ssa (hierarkia)

```
POST /api/events/5/ticket-types
Content-Type: application/json

{ "nimi": "Normaali", "hinta": 25.00 }
```

Controller lukee `5` polusta, hakee `Tapahtuma`, asettaa `lipputyyppi.setTapahtuma(...)`. Bodyyn ei tarvita `tapahtumaId`:tä.

Luonteva kun aliresurssi **aina** kuuluu yhdelle isännälle (lipputyyppi tapahtumalle, lippu myynnille).

### Tapa 2 — id bodyssa, Request DTO (suositus)

```
POST /api/ticket-types

{ "nimi": "Normaali", "hinta": 25.00, "tapahtumaId": 5 }
```

```java
public record LipputyyppiRequest(
        @NotBlank String nimi,
        @NotNull @DecimalMin("0.00") BigDecimal hinta,
        @NotNull Long tapahtumaId) {}
```

Controller: `findById(dto.tapahtumaId())` → 400 jos puuttuu → tallenna. Tämä **on DTO**. Julkinen kenttä `tapahtumaId`, tietokannassa `Tapahtuma`-olio.

### Tapa 3 — Entity Trick (ei DTO)

```
POST /api/ticket-types

{ "nimi": "Normaali", "hinta": 25.00, "tapahtuma": { "id": 5 } }
```

Controller ottaa suoraan `@RequestBody Lipputyyppi`. Jackson tekee `Tapahtuma`-kuoren jossa vain `id`. Hibernate käyttää sitä vierasavaimena.

Ei erillistä DTO-luokkaa. Kevyt demossa. Vaara: `lipputyyppi.getTapahtuma().getNimi()` ennen latausta → `null` / NPE. Entiteetin kentät (salasana, sisäiset listat) vuotavat helposti GET:ssä jos sama luokka palautetaan.

---

## Dia 9 — DTO ei ole vain GET

| Suunta | Luokka TicketGurussa | Tehtävä |
| --- | --- | --- |
| Sisään (POST/PUT) | `TapahtumaRequest` / myöhemmin `LipputyyppiRequest` | vain syöte + id-viitteet, `@NotBlank` |
| Ulos (GET/201) | `TapahtumaResponse` / `LipputyyppiResponse` | ei salasanaa, ei kehää, vakaat kenttänimet |

Sama malli myynnille:

```java
public record MyyntiRequest(Long tapahtumaId, Long myyjaId, List<Rivi> rivit) {}

public record MyyntiResponse(
        Long id,
        LocalDateTime myyntiaika,
        BigDecimal summa,
        String myyjanNimi,
        String tapahtumanNimi) {}
```

Pyynnössä id:t. Vastauksessa nimet. Taulussa oliot.

Miksi: API ei rikkoudu jos sarake nimetään uudelleen; hash ei vuoda; Jackson ei kierrä.

---

## Dia 10 — Miksi tämä tuntuu raskaammalta kuin Express

| Express | Spring + JPA |
| --- | --- |
| Body on vapaa JSON | Body pitää sitoutua luokkaan |
| Rivi ≈ objekti | Rivi = `@Entity`, vierasavaimet olioina |
| Palauta rivi | Palauta rivi → Jackson seuraa `@OneToMany` |
| URL:ssä id riittää pieneen CRUD:iin | Useampi suhde → body + DTO selkeämpi |

Kurinalaisuus on tarkoituksellista (tyypit, kerrokset, vuodot). TicketGuru Sprint 3: `TapahtumaRequest` → service → `Tapahtuma` → `TapahtumaResponse` on jo tämä malli, ilman toista taulua.

---

## Dia 11 — Päätössääntö tiimille

```text
Onko kyseessä LUONTI (POST)?
  ├── Selkeä isäntä–lapsi?     → Tapa 1: POST /api/events/{id}/ticket-types
  ├── Useita viitteitä / vakaa sopimus? → Tapa 2: Request DTO + tapahtumaId
  └── Pika-demo ilman DTO:ta?  → Tapa 3: Entity Trick (älä tuotantoon)

Onko kyseessä HAKU (GET)?
  ├── Tarvitaan yksi kutsu + vähän kenttiä? → Response DTO (suositus)
  ├── Halutaan koko sisäolio?              → upotus (varo kehä + koko)
  └── Halutaan mahdollisimman kevyt?       → URI / pelkkä id
```

TicketGuru-ehdotus:

| Endpoint | POST | GET |
| --- | --- | --- |
| Lipputyyppi | Tapa 1 tai 2 (`tapahtumaId`) | Response DTO (`tapahtumaNimi`) |
| Myynti | Tapa 2 (`tapahtumaId`, `myyjaId`, rivit) | Response DTO (`myyjanNimi`) |
| Lippu myyntiin | Tapa 1 `POST /api/sales/{id}/tickets` tai rivit myynnin bodyssa | DTO, ei koko `Kayttaja`-puuta |

Älä palauta `@Entity` suoraan JSON:na kun relaatiot ovat kaksisuuntaisia.

---

## Dia 12 — Mitä koodissa jo on vs. luennon PDF

| | Nyt repossa | Luennon idea |
| --- | --- | --- |
| `POST /api/events` | `TapahtumaRequest` (DTO, ei suhdetta) | Tapa 2 ilman vierasavainta |
| `GET /api/events/{id}` | `TapahtumaResponse` | GET tapa C |
| `User` JSON:ssa | ei käytetä | opettajan nimi → meillä `myyja` |
| Entity Trick | ei | vältä |
| Upotettu `Kayttaja` | ei | älä tee ilman DTO/ignore |

Pyyntöketju: [../arkkitehtuuri-tapahtuma.md](../arkkitehtuuri-tapahtuma.md).  
JSON-sopimus: [../api/events.md](../api/events.md).

---

## Lyhyt koodivertailu (lipputyyppi)

Tapa 1 — id URL:ssa:

```java
@PostMapping("/{tapahtumaId}/ticket-types")
public ResponseEntity<LipputyyppiResponse> luo(
        @PathVariable Long tapahtumaId,
        @Valid @RequestBody LipputyyppiRunko body) { /* find tapahtuma, set, save */ }
```

Tapa 2 — DTO:

```java
@PostMapping("/api/ticket-types")
public ResponseEntity<LipputyyppiResponse> luo(
        @Valid @RequestBody LipputyyppiRequest dto) {
    Tapahtuma t = tapahtumaRepository.findById(dto.tapahtumaId())
            .orElseThrow(() -> new IllegalArgumentException("Tapahtumaa ei ole"));
    // ...
}
```

Tapa 3 — ei DTO:ta:

```java
@PostMapping("/api/ticket-types")
public Lipputyyppi luo(@RequestBody Lipputyyppi lipputyyppi) {
    return repo.save(lipputyyppi); // body: { "tapahtuma": { "id": 5 } }
}
```

---

## Lähteet

- Ruonavaara, linkitetyt resurssit (GET: upotus vs URI; POST: id URL:ssa / id bodyssa / URI bodyssa / Entity Trick; DTO).
- [Baeldung: DTO pattern](https://www.baeldung.com/java-dto-pattern) — DTO = tiedonsiirto ilman logiikkaa, irti entiteetistä.
- TicketGuru: `domain/Myyntitapahtuma.java` (`myyja`, `tapahtuma`), `web/dto/TapahtumaRequest.java` + `TapahtumaResponse.java`.
