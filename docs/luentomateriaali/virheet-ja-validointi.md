# REST: vastauskoodit, virheet ja validointi (TicketGuru)

Sprint 5. Onnistunut polku on suoraviivainen: resurssi löytyy ja data on oikeassa muodossa. Rajapinta tuntuu clientistä silti satunnaiselta, jos puuttuva id, tyhjä kenttä ja rikki mennyt JSON tulevat ulos eri näköisinä tai koodina `500`.

Tämä luento listaa **vaihtoehdot** ja merkitsee **suositellun linjan** TicketGurulle. Koodia ei tässä vaiheessa muuteta. Sprintin tehtävä on tarkistaa, että nykyinen rajapinta noudattaa linjaa: oikea onnistumiskoodi, `404` puuttuvalle resurssille, `400` kelvottomalle pyynnölle, eikä odotettu virhe ole `500`.

Lähteet kurssin Moodlesta:

- [HTTP-statuskoodit, yleisimmät](https://httpstatus.io/http-status-codes)
- [HTTP-statuskoodit, MDN](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)
- [@ResponseStatus](https://www.baeldung.com/spring-response-status)
- [ResponseStatusException](https://www.baeldung.com/spring-response-status-exception)
- [Bean Validation ja Spring Boot](https://reflectoring.io/bean-validation-with-spring-boot/)
- [Bean Validation, Baeldung](https://www.baeldung.com/javax-validation)

---

## Dia 1 — Kolme tilannetta, jotka pitää hallita

| Tilanne | Esimerkki TicketGurussa | Koodi |
| --- | --- | --- |
| Tunnistetta ei ole | `GET /api/sales/999`, `PUT /api/events/999`, `DELETE /api/events/999` | **404** |
| Pakollinen tieto puuttuu | `POST /api/events` ilman `nimi`-kenttää | **400** |
| Tieto on väärässä muodossa | `"hinta": "halpa"` tai rikki mennyt JSON | **400** |

`500` tarkoittaa, että palvelin kaatui odottamattomaan virheeseen. Client ei voi korjata pyyntöään sen perusteella. Puuttuva rivi ja huono JSON eivät ole sellaisia tilanteita.

Onnistuminen ei ole aina `200`. Spring laittaa `200 OK`, jos metodi vain palauttaa olion ja mitään poikkeusta ei tule. `POST`, joka loi rivin, on **201 Created**.

---

## Dia 2 — Onnistunut koodi: kaksi tapaa

Käytännön sääntö:

- Palautat pelkän olion → voit käyttää `@ResponseStatus`-annotaatiota.
- Tarvitset otsakkeita, vaihtelevia statuksia tai muuten enemmän kontrollia → käytä `ResponseEntity`-oliota.

Jos status on jotain muuta kuin Springin oletus `200` tai tarvitset headerit (esim. `Location`), käytä `ResponseEntity`-oliota. Muulloin pelkkä olio riittää.

### Tapa A — `@ResponseStatus` metodissa

Kiinteä koodi, jos metodi päättyy normaalisti ja palauttaa pelkän olion. Oletus on `200`, joten GET:iin tätä ei tarvita.

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public MyyntiResponse create(...) {
    return service.create(...);
}
```

Saat koodin `201`. Spring ei tiedä, mitä `Location`-otsakkeeseen laitetaan. Se pitäisi rakentaa itse, eikä tämä annotaatio tee sitä.

### Tapa B — `ResponseEntity`

Metodi päättää koodin, otsakkeet ja rungon itse.

```java
@PostMapping
public ResponseEntity<MyyntiResponse> create(...) {
    return ResponseEntity.created(location).body(created);
}
```

`created(location)` tekee molemmat: statuskoodin `201 Created` ja `Location`-otsakkeen. Poisto ilman runkoa on sama työkalu: `ResponseEntity.noContent().build()` → `204`.

Älä laita `@ResponseStatus(CREATED)` ja `ResponseEntity` samaan metodiin. `ResponseEntity` voittaa, ja kahdesta ohjeesta tulee epäselvä koodi.

### Miksi `Location`-otsake?

TicketGurussa `Location` ei ole tekninen pakko. Rungossa on jo `id`, ja client voi koota osoitteen itse. REST-käytäntö on silti palauttaa se POST-vastauksessa, koska palvelin kertoo uuden resurssin osoitteen. Clientin ei tarvitse tuntea URL-rakennetta.

```http
POST /api/sales
```

Palvelin loi myynnin, jonka id on 12:

```http
HTTP/1.1 201 Created
Location: /api/sales/12
```

Client tietää heti, että luotu resurssi on osoitteessa `/api/sales/12`. Ilman otsaketta sama tieto on vain rungossa:

```json
{ "id": 12, "summa": 37.50 }
```

Sekin toimii. Myöhemmin osoite voi muuttua, esimerkiksi `/api/v2/sales/12`. Jos client sai `Location`-arvon, sen ei tarvitse arvata polkua.

Sama tapahtumalle:

```http
POST /api/events
```

```http
HTTP/1.1 201 Created
Location: /api/events/42
```

Siksi TicketGurun POST-metodit käyttävät `ResponseEntity.created(location).body(created)`.

### Yhteenveto

| Pyyntö | Mitä palautetaan |
| --- | --- |
| GET onnistui | Pelkkä olio. Otsakkeita ei tarvita, `200` riittää. |
| PUT onnistui | Pelkkä olio, `200`. |
| POST loi resurssin | `201 Created` ja `Location`. `ResponseEntity.created(...)`. |
| DELETE onnistui | `204`, ei runkoa. `ResponseEntity.noContent()`. |

TicketGurussa tämä on jo käytössä: `POST /api/sales` ja `POST /api/events` palauttavat `201` sekä `Location`-otsakkeen. `DELETE /api/events/{id}` palauttaa `204`.

---

## Dia 3 — 404: resurssia ei ole

Polun id on osoite. Jos osoitetta ei ole, vastaus on `404`, ei tyhjä `200` eikä `500`.

### Tapa A — `ResponseStatusException` (kurssin perusratkaisu)

```java
return myyntitapahtumaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Myyntitapahtumaa ei löydy: " + id));
```

Springin oletuskäsittelijä nappaa poikkeuksen ja lähettää `404`:n sekä viestin. Erillistä poikkeusluokkaa ei tarvita. Tällä pärjää pitkälle, kuten Moodle sanoo.

### Tapa B — oma poikkeusluokka ja `@ResponseStatus`

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MyyntiaEiLoydyException extends RuntimeException {
    public MyyntiaEiLoydyException(Long id) {
        super("Myyntitapahtumaa ei löydy: " + id);
    }
}
```

Koodi on kiinni luokassa. Heitto on luettava (`throw new MyyntiaEiLoydyException(id)`). Jokainen uusi tilanne (409, 400) tarvitsee oman luokkansa.

### Tapa C — oma poikkeus ja `@ExceptionHandler`

`@RestControllerAdvice`-luokka kuuntelee kaikkia kontrollereita. `@ExceptionHandler` muuttaa yhden poikkeustyypin halutuksi JSON:ksi. Täysi kontrolli runkoon. Enemmän luokkia.

### Mitä TicketGuru tekee

Palvelu heittää **tavan A** (`ResponseStatusException`). Sen päälle yksi käsittelijä muotoilee rungon aina samaksi `ApiError`-olioksi (`status`, `error`, `messages`). Client ei joudu arvaamaan, onko virhe Springin oletus-JSON vai oma teksti.

```java
@ExceptionHandler(ResponseStatusException.class)
public ResponseEntity<ApiError> handleStatus(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    String message = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
    return ResponseEntity.status(status)
            .body(new ApiError(status.value(), status.getReasonPhrase(), List.of(message)));
}
```

Ilman tätä käsittelijää `ResponseStatusException` toimii silti: koodi on oikein, mutta runko on Springin oletusmuoto. Käsittelijä on tapa C pienimmillään, kohdistettuna valmiiseen poikkeukseen eikä kymmeneen omaan luokkaan.

`404` kuuluu **polun** id:hen (`GET /api/sales/999`, `GET /api/events/999/ticket-types`). Bodyn luku (`"tapahtumaId": 999`) on pyynnön dataa: TicketGurussa se on `400`.

---

## Dia 4 — 400: validointi ennen kuin pyyntö ajetaan loppuun

Bean Validation on Java-standardi. Annotaatiot ovat jo Spring Boot -riippuvuuksissa. Ne eivät tee mitään, ellei kontrolleri pyydä tarkistusta.

```java
public ResponseEntity<MyyntiResponse> create(
        @Valid @RequestBody MyyntiRequest request)
```

`@Valid` ilman sääntöjä ei tarkista mitään. Säännöt ilman `@Valid` eivät myöskään käynnisty. Molemmat tarvitaan.

Jos tarkistus epäonnistuu, Spring heittää `MethodArgumentNotValidException` **ennen** kuin palvelun `create` käynnistyy. Oletusvastaus on `400`.

| Annotaatio | Milloin kenttä kelpaa |
| --- | --- |
| `@NotNull` | Arvo on JSON:ssa. Luku tai olio. |
| `@NotBlank` | Teksti ei ole tyhjä eikä pelkkiä välilyöntejä. |
| `@NotEmpty` | Lista tai teksti ei ole tyhjä. |
| `@Size(max = 80)` | Pituus tai listan koko on rajattu. |
| `@Min(1)` / `@Max(500)` | Luku on välillä. |
| `@DecimalMin("0.00")` | Raha ei ole negatiivinen. |

### Minne säännöt kirjoitetaan

Moodlen esimerkki laittaa annotaatiot **entityyn**, koska entity on Java Bean. Se toimii, jos `@RequestBody` on suoraan entity.

TicketGurun julkinen sopimus on **Request DTO** (`TapahtumaRequest`, `LipputyyppiRequest`, `MyyntiRequest`). Säännöt kuuluvat siihen. Entity jää tietokannan säännöiksi (`nullable = false`). API-sopimus ja taulu saavat muuttua eri tahdissa. Tämä on sama jako kuin Sprint 4:ssä.

Väärä muoto, jota Bean Validation ei näe: JSON ei jäsenny ollenkaan (`{` puuttuu, tai `"lippujaKpl": "paljon"`). Spring heittää `HttpMessageNotReadableException` jo ennen `@Valid`-tarkistusta. Sekin on `400`. TicketGurun `ApiExceptionHandler` vastaa siihen tekstillä, että runko ei ole kelvollista JSON:ia.

### Kyselyparametrit

`GET /api/events?kaupunki=Helsinki` ei ole runko, joten `@RequestBody` ja `@Valid` eivät koske sitä. Tarvittaessa parametri tarkistetaan erikseen: joko käsin palvelussa tai `@Validated` kontrolleriluokassa ja rajoite itse parametrissa (`@Size`, `@Min`). Tyhjä valinnainen suodatin saa jäädä sallituksi. Pakollinen parametri, jota ei voi tulkita, on `400`.

---

## Dia 5 — 409 ei ole 500

Osa virheistä on ehjiä pyyntöjä, jotka törmäävät jo olemassa olevaan tilaan.

| Tilanne | Koodi |
| --- | --- |
| Tapahtumaa ei voi poistaa, koska sillä on lipputyyppejä tai myyntejä | 409 |
| Myynti ylittäisi `lippujaKpl` | 409 |

Ne heitetään samalla `ResponseStatusException`-oliolla kuin `404`, koodina `HttpStatus.CONFLICT`. Client erottaa “korjaa JSON” (`400`) ja “liiketoimintasääntö esti” (`409`).

---

## Dia 6 — Mistä 500 tulee, ja miten se vältetään

`500` syntyy, kun poikkeusta ei ole käännetty HTTP-koodiksi. Tyypillisesti `NullPointerException`, koska koodi olettaa rivin olevan olemassa, tai tietokannan oma virhe (uniikki koodi, puuttuva vierasavain), jonka palvelu päästää läpi.

Hallittu polku:

1. Puuttuva polun id → `orElseThrow` + `404` heti haun jälkeen. Älä kutsu `getNimi()` tyhjälle tulokselle.
2. Runko → `@Valid` ja DTO:n annotaatiot, plus käsittelijä rikkinäiselle JSON:lle.
3. Tunnettu sääntö (kapasiteetti, poiston esto, väärä tapahtuma lipputyypille) → `ResponseStatusException` koodilla `400` tai `409`.
4. `@Transactional` palvelussa peruu puolikkaan tallennuksen, jos heitto tulee kesken metodin.

Odottamatonta vikaa ei pidä niellä tyhjäksi `200`:ksi. Silloin client luulee, että myynti onnistui. `500` saa jäädä vain bugille, jota ei osattu ennakoida.

---

## Dia 7 — Suositus TicketGurulle

Yksi linja, joka vastaa Moodlen “selkeää tapaa” ja Sprint 4:n DTO-jakoa. Oma poikkeushierarkia ei ole tarpeen.

| Kysymys | Valinta |
| --- | --- |
| GET ja PUT onnistuivat | Palauta olio. Spring antaa `200`. |
| POST loi rivin | `ResponseEntity.created(location).body(...)` → `201` |
| DELETE onnistui | `ResponseEntity.noContent()` → `204` |
| Polun id puuttuu | `ResponseStatusException(NOT_FOUND)` palvelussa |
| Runko puutteellinen tai väärän muotoinen | `@Valid` + annotaatiot **Request DTO:ssa** → `400` |
| Sääntö estää ehjän pyynnön | `ResponseStatusException(CONFLICT)` → `409` |
| Virherungon muoto | Yksi `@RestControllerAdvice`, sama `ApiError` joka koodille |

`@ResponseStatus` metodissa jää käyttämättä, koska POST ja DELETE tarvitsevat `ResponseEntity`-oliota. Omat `EiLoydyException`-luokat jäävät käyttämättä, koska `ResponseStatusException` + yksi käsittelijä riittää.

Sprintin tarkistuslista (toteutus myöhemmin, tätä luentoa vasten):

- Jokainen POST palauttaa `201`, ei `200`.
- Jokainen puuttuva polun id palauttaa `404`.
- POST ja PUT ilman pakollista kenttää tai väärällä tyypillä palauttavat `400`.
- Mikään näistä ei palauta `500`.
- Virhe-JSON on aina `status`, `error`, `messages`.

Nykyinen koodi on jo tällä linjalla (`MyyntiController`, `MyyntiService`, `ApiExceptionHandler`, `*Request`-recordit). Sprint 5 on tarkistus ja aukkojen paikkaus, ei uuden virhekehyksen kirjoitus.

---

## Lähteet

- Kurssin Moodle, Sprint 5: vastauskoodit, `ResponseStatusException`, Bean Validation.
- [Baeldung: @ResponseStatus](https://www.baeldung.com/spring-response-status)
- [Baeldung: ResponseStatusException](https://www.baeldung.com/spring-response-status-exception)
- [Reflectoring: Bean Validation with Spring Boot](https://reflectoring.io/bean-validation-with-spring-boot/)
- [Baeldung: Java Bean Validation](https://www.baeldung.com/javax-validation)
- TicketGuru: `web/ApiExceptionHandler.java`, `web/dto/ApiError.java`, `web/MyyntiController.java`, `service/MyyntiService.java`.
