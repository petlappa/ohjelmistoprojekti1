# Sprint 4 — arkkitehtuurivalinnat (linkitetyt resurssit)

Tämä dokumentti kertoo **vain tämän sprintin päätökset**: miten lipputyyppi ja myyntitapahtuma näkyvät rajapinnassa, ja miksi. Client-sopimus (URL, JSON, paluukoodit) on tiedostoissa [api/ticket-types.md](api/ticket-types.md) ja [api/sales.md](api/sales.md).

Lähteet: Ruonavaara, *Linkitetyt resurssit API:ssa*; [Baeldung: DTO pattern](https://www.baeldung.com/java-dto-pattern); luento [luentomateriaali/api-suhteet-ja-dto.md](luentomateriaali/api-suhteet-ja-dto.md). Sprint 3:n tapahtumat ovat jo DTO-linjalla (`TapahtumaRequest` / `TapahtumaResponse`).

## Mitä sprintissä tehdään

Tuoteomistaja halusi myyntitapahtuman ensimmäisen version. Siihen tarvitaan lipputyyppi (muuten kassalla ei ole myytävää) ja yksi kutsu, joka luo kuitin ja liput.

| Endpoint | Rooli |
| --- | --- |
| `GET /api/events/{id}/ticket-types` | Myyjä näkee hinnat (M3) |
| `POST /api/events/{id}/ticket-types` | Koordinaattori lisää tyypin (TK5) |
| `POST /api/sales` | Yksi kuitti: tapahtuma, myyjä, rivit (M4, M6, M7, J1, J2) |
| `GET /api/sales/{id}` | Kuitti uudelleen numerolla (M10, TK9) |

Kirjautuminen, myyntiraportti, ovitarkastus ja tulostus eivät kuulu tähän sprinttiin.

## Kaksi eri kysymystä

POST kertoo, **mihin uusi rivi kuuluu**. GET **näyttää** liittyvän rivin. URL:n hierarkia ja JSON:n muoto ovat eri valintoja. DTO on työkalu molempiin suuntiin, ei kolmas kilpailija niiden rinnalla.

## POST: lipputyyppi on puu, myynti on tähti

Lipputyypillä on yksi isäntä. Front on jo tapahtuman sivulla, joten tapahtuman id on polussa ja bodyyn jäävät vain omat kentät:

```http
POST /api/events/5/ticket-types
{ "kuvaus": "Opiskelija", "hinta": 10.00 }
```

Myynti sitoo kerralla tapahtuman, myyjän ja listan lippuja. URL ei mahdu siististi (`/events/5/users/27/tickets`): kuka on isäntä, ja mihin kappalemäärät laitetaan. Kolme erillistä lipun POST:ia tekisi kolme kuittia. Siksi yksi pyyntö, Request DTO, viitteet bodyssa:

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

Palvelin asettaa `myyntiaika`-kentän, laskee `summa`-kentän lipputyyppien hinnoista ja luo jokaiselle kappaleelle `Lippu`-rivin sekä koodin. Hinta kopioidaan lipulle myyntihetkellä.

| Vaihtoehto | Lipputyyppi | Myynti |
| --- | --- | --- |
| Id URL:ssa | Valittu. Yksi isäntä. | Ei. Kaksi vanhempaa ja rivilista. |
| Id bodyssa, Request DTO | Ei tarvita: id on jo polussa. | Valittu. |
| Entity Trick `{ "tapahtuma": { "id": 5 } }` | Ei. Vajaa olio, ei julkiseen API:in. | Ei. |

Entity Trick on luennon pikaesimerkki: Jackson rakentaa olion, jossa on vain id ja muut kentät ovat `null`. Sitä ei käytetä.

## GET: Response DTO, ei entiteettiä eikä pelkkää URI:a

`Myyntitapahtuma` viittaa myyjään (`Kayttaja`), ja `Kayttaja` listaa myynnit takaisin. Jos GET palauttaa JPA-entiteetin, Jackson kiertää kehää ja `salasana` lähtisi mukaan.

Pelkkä URI (`"myyja": "http://localhost:8080/api/users/27"`) välttää kehän, mutta kuitti tarvitsee nimen ja lippukoodit samassa vastauksessa. Client tekisi lisäkutsuja jokaista myyntiä kohti.

Response DTO poimii kuitin kentät:

```json
{
  "id": 12,
  "myyntiaika": "2026-09-24T18:05:00",
  "summa": 20.00,
  "tapahtumanNimi": "Tapahtuma A",
  "myyjanNimi": "Maija Myyjä",
  "liput": [
    { "id": 40, "koodi": "3er454aa", "hinta": 10.00, "lipputyypinKuvaus": "Opiskelija", "kaytetty": false }
  ]
}
```

Lipputyypin lista tapahtuman alla on sama idea pienemmin: `id`, `kuvaus`, `hinta`. Tapahtumaa ei upoteta, koska polku sen jo kertoo.

Baeldungin malli: DTO on tasainen tiedonsiirto-olio ilman liiketoimintalogiikkaa. Säännöt (kapasiteetti, vierasavaimen olemassaolo, summa) ovat servicessä.

## Virheet

| Tilanne | Koodi | Miksi |
| --- | --- | --- |
| Puuttuva tai väärä JSON-kenttä | 400 | Bean Validation |
| Myynnin bodyn id:tä ei ole, tai lipputyyppi kuuluu toiseen tapahtumaan | 400 | Viite on pyynnön dataa, ei polun resurssi |
| `GET /api/sales/{id}` tai tapahtumaa ei ole polussa | 404 | Sama malli kuin `GET /api/events/{id}` |
| Uudet liput ylittäisivät `lippujaKpl` | 409 | Sama konfliktimalli kuin tapahtuman poistossa |

`myyjaId` tulee bodysta, kunnes kirjautuminen on olemassa. Myöhemmin myyjä voidaan ottaa tokenista; tapahtuma ja rivit jäävät silti bodyyn.

## Koodissa

```text
POST /api/sales
  → MyyntiController
  → MyyntiRequest (tapahtumaId, myyjaId, rivit)
  → MyyntiService (hae viitteet, kapasiteetti, summa, liput)
  → Myyntitapahtuma + Lippu
  ← MyyntiResponse
```

Luokat: `web/dto/LipputyyppiRequest`, `LipputyyppiResponse`, `MyyntiRequest`, `MyyntiRiviRequest`, `MyyntiResponse`, `LippuResponse`. Kontrollerit eivät palauta `@Entity`-luokkia.
