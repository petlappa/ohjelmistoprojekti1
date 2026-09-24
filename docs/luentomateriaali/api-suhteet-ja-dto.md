# REST: miten suhde merkitään API:ssa (TicketGuru)

Luento: valitse **GET-vastauksen muoto** ja **POST-pyynnön muoto** erikseen. DTO ei ole kolmas kilpailija näiden rinnalla — se on työkalu molempiin suuntiin.

Opettajan PDF (`linkitetyt_resurssit`) käyttää nimiä `User` ja `Transaction`. TicketGurussa ne ovat **Kayttaja** (kassalla: **myyjä**) ja **Myyntitapahtuma**. Sama idea, meidän nimet.

Sprint 3: tapahtumilla (`/api/events`) ei ole suhdetta JSON:ssa. Sprint 4 valitsi linjan, joka on kirjattu tiedostoon [../arkkitehtuuri-sprint-4.md](../arkkitehtuuri-sprint-4.md). Tämä luento kuvaa vaihtoehdot; valinta ei korvaa luentoa.

**Opetusjärjestys:** diat 1–13 antavat työkalut. Tiimi valitsee itse, miten merkitsee suhteen lipputyypille, myynnille ja lipulle. Mallipohdinta (“miksi myynti on se kohta”) on **dioissa 14–** — älä näytä niitä ennen kuin tiimi on ensin yrittänyt. Väärä valinta saa jäädä; sen korjaa myöhemmin, kun kassa ei mahdu URL:ään.

---

## Dia 1 — Kaksi eri kysymystä

Älä sekoita näitä:

| Kysymys | Milloin | Esimerkki |
| --- | --- | --- |
| Miten **kerrotaan** mihin uusi rivi kuuluu? | POST / PUT | Uusi lipputyyppi tapahtumalle 5 |
| Miten **näytetään** liittyvä rivi? | GET | Myynti + kuka myi |

URL:n hierarkia (`/events/5/ticket-types`) on **osoite**. JSON:n sisäkkäisyys (`"tapahtuma": { ... }`) on **vastauksen muoto**. Ne ovat eri valintoja.

REST ei vaadi samaa kaavaa joka endpointille. Sama sovellus voi käyttää eri tapoja rinnakkain; valinta on per endpoint (dia 9).

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

Vertaa **samaa operaatiota**: uusi lipputyyppi tapahtumalle 5. Tapahtumaa ei luoda uudelleen. Ero on vain siinä, **missä id 5 on**.

Opettajan PDF sekoittaa kaksi eri POST:ia (lippu myyntiin 56 vs. uusi myynti + `userId`). Se ei ole Tapa 1 vs Tapa 2. Alla molemmat tavat samalle TicketGuru-resurssille.

### Tapa 1 — id URL:ssa (hierarkia)

```
POST /api/events/5/ticket-types

{ "nimi": "Normaali", "hinta": 25.00 }
```

Controller lukee `5` polusta, hakee `Tapahtuma`, asettaa `lipputyyppi.setTapahtuma(...)`. Bodyyn ei tarvita `tapahtumaId`:tä.

Luonteva kun aliresurssi **aina** kuuluu yhdelle isännälle.

### Tapa 2 — id bodyssa, Request DTO

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

Sama rivi tietokantaan. Eri osoite, eri JSON.

| | Tapa 1 | Tapa 2 | Tapa 3 |
| --- | --- | --- | --- |
| Mitä luodaan | Lipputyyppi tapahtumalle 5 | Lipputyyppi tapahtumalle 5 | Lipputyyppi tapahtumalle 5 |
| Missä id 5 | polussa `/events/5/...` | bodyssa `"tapahtumaId": 5` | bodyssa `"tapahtuma": { "id": 5 }` |
| JSON body | `{ "nimi": "Normaali", "hinta": 25.00 }` | `{ "nimi": "Normaali", "hinta": 25.00, "tapahtumaId": 5 }` | `{ "nimi": "Normaali", "hinta": 25.00, "tapahtuma": { "id": 5 } }` |
| Luokka kontrollerissa | polun id + runko | `LipputyyppiRequest` (DTO) | suoraan `Lipputyyppi` (entity) |

Tapahtuman **muita kenttiä ei ole missään näistä pyynnöistä** (`nimi`, `aika`, `kaupunki`…). Niitä ei luoda eikä päivitetä. Viite riittää:

- Tapa 1: id on URL:ssa → JSON:ssa ei `tapahtuma`-kenttää ollenkaan
- Tapa 2: vain numero `tapahtumaId: 5` → ei oliota, ei nimeä
- Tapa 3: näyttää oliolta, mutta siinäkin on **vain** `id` (vajaa entity). `getTapahtuma().getNimi()` on silti `null`

Opettajan diassa Tapa 1:n Ticket-laatikko ja Tapa 2:n tyhjä `{}` tarkoittavat samaa: **uuden rivin omat kentät**, ei vanhemman attribuutteja. `userId: 27` / `{ "id": 27 }` ei täytä Teijan sähköpostia pyyntöön.

Opettajan dia: `POST /transactions/56/ticket` luo **lipun**, `POST /transactions` + `userId` luo **myynnin**. Eri resurssi, eri suhde — siksi siellä näkyy `Ticket` vs `userId`. Älä opi “kahta API:a”; opi **mihin id kirjoitetaan**. Sama PDF:n Entity Trick `{ "user": { "id": 27 } }` on Tapa 3 myynnille; alla se samalle lipputyypille kuin tavat 1 ja 2.

### Tapa 3 — Entity Trick (Spring Boot -erikoisuus, ei DTO)

Jackson upottaa id:n suoraan olioksi. Syntyy **vajaa entity**: `Tapahtuma` jossa on vain `id: 5`, muut kentät `null`.

```
POST /api/ticket-types

{
  "nimi": "Normaali",
  "hinta": 25.00,
  "tapahtuma": { "id": 5 }
}
```

```java
@PostMapping("/api/ticket-types")
public Lipputyyppi luo(@RequestBody Lipputyyppi lipputyyppi) {
    return repo.save(lipputyyppi);
}
```

JPA/Hibernate käyttää tuota id:tä vierasavaimena (`tapahtuma_id = 5`), vaikka nimeä ja paikkaa ei ole oliossa. Erillistä DTO-luokkaa ei ole: entityä käytetään kuin se olisi pyyntö.

Vaara: `lipputyyppi.getTapahtuma().getNimi()` ennen latausta → `null` / NPE. Jos sama luokka palautetaan GET:ssä, salasanat ja listat vuotavat ja kehä voi syntyä. Kevyt demo; ei TicketGurun julkiseen API:in.

---

## Dia 9 — Sama API, eri tavat rinnakkain

Kolmea POST-tapaa **saa** käyttää samassa TicketGurussa. REST ei pakota yhtä kaavaa kaikille endpointeille. Suunnittelija valitsee sen, mikä on kyseiselle toiminnolle selkein.

| Tapa | Esimerkki TicketGurussa | Milloin |
| --- | --- | --- |
| 1 — id URL:ssa | `POST /api/events/{id}/ticket-types` | Front on jo tapahtuman sivulla; lapsi kuuluu yhdelle isännälle |
| 2 — Request DTO | `POST /api/ticket-types` body `{ "nimi": "Normaali", "hinta": 25.00, "tapahtumaId": 5 }` | Useita kenttiä / viite bodyssa, validointi, vakaa sopimus clientille |
| 3 — Entity Trick | `{ "tapahtuma": { "id": 5 } }` suoraan entiteettiin | Vain pika-demo / sisäinen kokeilu; ei TicketGurun julkiseen API:in |

**Johdonmukaisuus** tekee rajapinnasta käyttökelpoisen. Jos jokainen endpoint noudattaa eri periaatetta, React-tiimin on vaikea arvata, onko id polussa vai bodyssa.

Hyvä linja: **pääarkkitehtuuri on DTO** (kuten Sprint 3:n `TapahtumaRequest` / `TapahtumaResponse`). Tapa 1 täydentää, kun hierarkia on ilmeinen. Tapa 3 ei ole tuotantolinja.

Päätössääntö (työkalu, ei valmista TicketGuru-karttaa): dia 12. Tiimin oma valinta ensin; mallipohdinta dioissa 14–.

---

## Dia 10 — DTO ei ole vain GET

| Suunta | Luokka TicketGurussa | Tehtävä |
| --- | --- | --- |
| Sisään (POST/PUT) | `TapahtumaRequest` / myöhemmin `LipputyyppiRequest` | vain syöte + id-viitteet, `@NotBlank` |
| Ulos (GET/201) | `TapahtumaResponse` / `LipputyyppiResponse` | ei salasanaa, ei kehää, vakaat kenttänimet |

Sama jako myöhemmin muille resursseille: pyynnössä id:t, vastauksessa nimet, taulussa oliot. Esimerkki myynnistä on mallidioissa 14– — älä kopioi sitä ennen tiimin omaa luonnosta.

Miksi: API ei rikkoudu jos sarake nimetään uudelleen; hash ei vuoda; Jackson ei kierrä.

---

## Dia 11 — Miksi tämä tuntuu raskaammalta kuin Express

| Express | Spring + JPA |
| --- | --- |
| Body on vapaa JSON | Body pitää sitoutua luokkaan |
| Rivi ≈ objekti | Rivi = `@Entity`, vierasavaimet olioina |
| Palauta rivi | Palauta rivi → Jackson seuraa `@OneToMany` |
| URL:ssä id riittää pieneen CRUD:iin | Useampi suhde → body + DTO selkeämpi |

Kurinalaisuus on tarkoituksellista (tyypit, kerrokset, vuodot). TicketGuru Sprint 3: `TapahtumaRequest` → service → `Tapahtuma` → `TapahtumaResponse` on jo tämä malli, ilman toista taulua.

---

## Dia 12 — Päätössääntö tiimille

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

TicketGuru — **tiimin tehtävä** (älä avaa vielä dioja 14–):

| Endpoint | POST-tapamme | GET-tapamme | Miksi |
| --- | --- | --- | --- |
| Lipputyyppi | | | |
| Myynti | | | |
| Lippu (kassalla / tarkastus) | | | |

Älä palauta `@Entity` suoraan JSON:na kun relaatiot ovat kaksisuuntaisia.

Tapoja **saa** sekoittaa (dia 9). Johdonmukaisuus: React-tiimin pitää arvata, onko id polussa vai bodyssa. Mallipohdinta vasta dioissa 14–.

---

## Dia 13 — Mitä koodissa jo on vs. luennon PDF

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

## Diat 14– — mallipohdinta (näytä vasta kun tiimi on valinnut)

Nämä diat eivät ole “oikea vastaus tentissä”. Ne ovat yksi perusteltu linja. Jos tiimi valitsi toisin ja kassa alkaa kipeästi, palataan tähän ja korjataan.

---

## Dia 14 — Puu mahtuu URL:ään, tähti ei

Lipputyypillä on **yksi isäntä**. Myynnillä on **monta liitosta yhdessä kassatapahtumassa**.

```text
Puu (helppo URL):
  Tapahtuma 5  →  Lipputyyppi “Normaali”

Tähti (URL loppuu kesken):
                    Tapahtuma 5
                         ↘
  Kayttaja 27 (myyjä) →  Myyntitapahtuma  →  Lippu, Lippu, Lippu
                                              ↘ lipputyyppi 3, 3, 8
```

URI-id (`POST /api/events/5/ticket-types`) toimii puussa. Tähti tarvitsee bodyn, jossa on useita id:itä ja lista.

---

## Dia 15 — Lipputyyppi: sama kuvio kuin JS-sovelluksissa

Vierasavain on vain `tapahtuma_id`. Front on jo tapahtumasivulla.

```
POST /api/events/5/ticket-types

{ "nimi": "Normaali", "hinta": 25.00 }
```

Selkeä isäntä–lapsi. Bodyyn ei tarvita `tapahtumaId`:tä. Tämä ei ole “väärä JavaScript-tapa” — se riittää, kun suhde on puu.

---

## Dia 16 — Myynti: kaksi vanhempaa ja lista lippuja

Yksi `Myyntitapahtuma` sitoo kerralla:

- **mihin tapahtumaan** (`tapahtuma_id`)
- **kuka myi** (`myyja_id`)
- **mitä lippuja** (2 × Normaali, 1 × VIP → jokainen `Lippu` viittaa `lipputyyppi_id`:hen)

URL:ään ei mahdu siististi kahta vanhempaa ja listaa:

```text
POST /api/events/5/users/27/tickets
```

Kuka on isäntä, tapahtuma vai myyjä? Mihin laitetaan määrät per lipputyyppi?

---

## Dia 17 — Yksi kuitti, ei kolmea POST:ia

Asiakas ostaa **yhden myynnin** ja yhden summan. Kolme erillistä `POST …/tickets` -kutsua rikkoo kuitin (kolme myyntinumeroa, kolme aikaa).

Siksi yksi pyyntö, DTO bodyssa:

```json
{
  "tapahtumaId": 5,
  "myyjaId": 27,
  "rivit": [
    { "lipputyyppiId": 3, "kpl": 2 },
    { "lipputyyppiId": 8, "kpl": 1 }
  ]
}
```

Myyjän voi myöhemmin ottaa tokenista; tapahtuma + rivit jäävät silti bodyyn. Tämä on DTO, koska pyynnössä on useita id:itä ja lista — ei siksi että myynti olisi “erityisen Spring-juttu”.

---

## Dia 18 — Yksi linja, jos palataan korjaamaan

| Endpoint | POST | GET |
| --- | --- | --- |
| Lipputyyppi | Tapa 1 tai 2 (`tapahtumaId`) | Response DTO (`tapahtumaNimi`) |
| Myynti | Tapa 2 (`tapahtumaId`, `myyjaId`, rivit) | Response DTO (`myyjanNimi`) |
| Lippu myyntiin | Rivit myynnin bodyssa (tai Tapa 1 vasta kun myynti on jo olemassa) | DTO, ei koko `Kayttaja`-puuta |

```java
public record MyyntiRequest(Long tapahtumaId, Long myyjaId, List<Rivi> rivit) {}

public record MyyntiResponse(
        Long id,
        LocalDateTime myyntiaika,
        BigDecimal summa,
        String myyjanNimi,
        String tapahtumanNimi) {}
```

Jos tiimi teki myynnin pelkällä URL:llä ja se alkaa kipeästi: tämä on se korjaus, ei häpeä.

---

## Lähteet

- Ruonavaara, linkitetyt resurssit (GET: upotus vs URI; POST: id URL:ssa / id bodyssa / URI bodyssa / Entity Trick; DTO).
- [Baeldung: DTO pattern](https://www.baeldung.com/java-dto-pattern) — DTO = tiedonsiirto ilman logiikkaa, irti entiteetistä.
- TicketGuru: `domain/Myyntitapahtuma.java` (`myyja`, `tapahtuma`), `web/dto/TapahtumaRequest.java` + `TapahtumaResponse.java`.
