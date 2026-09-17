# TicketGuru REST API — Events

Client-tiimille. Pohja: [REST API Documentation Templates](https://github.com/jamescooke/restapidocs) (James Cooke).

Sprint 3 toteuttaa **tapahtumien** CRUD:n. Kirjautumista ei vielä ole: rajapinta on avoin kehityskäytössä.

## Version

1.0 (Sprint 3)

## URI scheme

| | |
| --- | --- |
| **Host** | `localhost:8080` (kehitys) |
| **Base path** | `/api` |
| **Base-URL** | `http://localhost:8080/api` |
| **Schemes** | `HTTP` |
| **Content-Type** | `application/json; charset=utf-8` |
| **CORS** | `Access-Control-Allow-Origin: *` kehityksessä (`/api/events`) |

Ajan esitys on ISO-8601 ilman aikavyöhykettä, esim. `2026-10-02T17:00:00`.

---

## Endpoints

### List events

Listaa tapahtumat esitysajan mukaan nousevasti. Valinnainen suodatus kaupungilla.

```
GET /api/events
```

#### URL

`http://localhost:8080/api/events`

#### Method

`GET`

#### Path parameters

Ei ole.

#### Query parameters

| Nimi | Tyyppi | Pakollinen | Kuvaus |
| --- | --- | --- | --- |
| `kaupunki` | string | ei | Suodattaa tapahtumat kaupungin nimellä (ei kirjainkoon merkitystä). Ilman parametria palautetaan kaikki. |

#### Request body

Ei ole.

#### Success response

**Code:** `200 OK`

**Content:** JSON-taulukko `Tapahtuma`-olioita.

```json
[
  {
    "id": 1,
    "nimi": "Tapahtuma A",
    "aika": "2026-10-02T17:00:00",
    "kaupunki": "Helsinki",
    "paikka": "Kulttuuritalo",
    "lippujaKpl": 200
  }
]
```

Tyhjä tulos: `[]` ja silti `200`.

#### Error response

Ei tyypillisiä virheitä tälle pyynnölle (virheellinen JSON ei koske GET:iä).

#### Sample call

```bash
curl http://localhost:8080/api/events
curl "http://localhost:8080/api/events?kaupunki=Helsinki"
```

---

### Get event

Hakee yhden tapahtuman tunnisteella. Client käyttää tätä muokkauslomakkeen esitäyttöön.

```
GET /api/events/:id
```

#### URL

`http://localhost:8080/api/events/{id}`

#### Method

`GET`

#### Path parameters

| Nimi | Tyyppi | Pakollinen | Kuvaus |
| --- | --- | --- | --- |
| `id` | long | kyllä | Tapahtuman tunniste |

#### Query parameters

Ei ole.

#### Request body

Ei ole.

#### Success response

**Code:** `200 OK`

**Content:** yksi `Tapahtuma`.

```json
{
  "id": 1,
  "nimi": "Tapahtuma A",
  "aika": "2026-10-02T17:00:00",
  "kaupunki": "Helsinki",
  "paikka": "Kulttuuritalo",
  "lippujaKpl": 200
}
```

#### Error response

**Code:** `404 Not Found`

**Content:**

```json
{
  "status": 404,
  "error": "Not Found",
  "messages": ["Tapahtumaa ei löydy: 99"]
}
```

#### Sample call

```bash
curl http://localhost:8080/api/events/1
```

---

### Create event

Lisää uuden tapahtuman H2-tietokantaan.

```
POST /api/events
```

#### URL

`http://localhost:8080/api/events`

#### Method

`POST`

#### Path parameters

Ei ole.

#### Query parameters

Ei ole.

#### Request body

| Kenttä | Tyyppi | Pakollinen | Kuvaus |
| --- | --- | --- | --- |
| `nimi` | string (max 120) | kyllä | Tapahtuman nimi |
| `aika` | datetime | kyllä | Esitysaika, ISO-8601 |
| `kaupunki` | string (max 80) | kyllä | Kaupunki |
| `paikka` | string (max 120) | kyllä | Sali / areena |
| `lippujaKpl` | integer ≥ 1 | kyllä | Lippujen enimmäismäärä |

```json
{
  "nimi": "Tapahtuma C",
  "aika": "2026-12-01T18:00:00",
  "kaupunki": "Espoo",
  "paikka": "Sellosali",
  "lippujaKpl": 80
}
```

`id` ei lähetetä; palvelin generoi sen.

#### Success response

**Code:** `201 Created`

**Headers:** `Location: http://localhost:8080/api/events/{id}`

**Content:** luotu `Tapahtuma` tunnisteineen (sama runko kuin GET).

#### Error response

**Code:** `400 Bad Request` — validointi tai virheellinen JSON.

```json
{
  "status": 400,
  "error": "Validation failed",
  "messages": ["nimi: must not be blank", "lippujaKpl: must be greater than or equal to 1"]
}
```

#### Sample call

```bash
curl -i -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"nimi":"Tapahtuma C","aika":"2026-12-01T18:00:00","kaupunki":"Espoo","paikka":"Sellosali","lippujaKpl":80}'
```

---

### Update event

Korvaa tapahtuman tiedot. Kaikki kentät lähetetään (PUT, ei osittaista päivitystä).

```
PUT /api/events/:id
```

#### URL

`http://localhost:8080/api/events/{id}`

#### Method

`PUT`

#### Path parameters

| Nimi | Tyyppi | Pakollinen | Kuvaus |
| --- | --- | --- | --- |
| `id` | long | kyllä | Päivitettävän tapahtuman tunniste |

#### Query parameters

Ei ole.

#### Request body

Sama kuin POST (kaikki kentät pakollisia). Polun `id` määrää rivin; rungon `id`-kenttää ei lueta.

#### Success response

**Code:** `200 OK`

**Content:** päivitetty `Tapahtuma`.

#### Error response

**Code:** `400 Bad Request` — validointi (sama runko kuin POST).

**Code:** `404 Not Found` — tunnisteita ei ole (sama runko kuin GET).

#### Sample call

```bash
curl -X PUT http://localhost:8080/api/events/1 \
  -H "Content-Type: application/json" \
  -d '{"nimi":"Tapahtuma A muokattu","aika":"2026-10-02T18:00:00","kaupunki":"Espoo","paikka":"Sellosali","lippujaKpl":150}'
```

---

### Delete event

Poistaa tapahtuman, jolla ei ole lipputyyppejä eikä myyntejä.

```
DELETE /api/events/:id
```

#### URL

`http://localhost:8080/api/events/{id}`

#### Method

`DELETE`

#### Path parameters

| Nimi | Tyyppi | Pakollinen | Kuvaus |
| --- | --- | --- | --- |
| `id` | long | kyllä | Poistettavan tapahtuman tunniste |

#### Query parameters

Ei ole.

#### Request body

Ei ole.

#### Success response

**Code:** `204 No Content`

**Content:** tyhjä.

#### Error response

**Code:** `404 Not Found` — tunnisteita ei ole.

**Code:** `409 Conflict` — tapahtumaan liittyy lipputyyppejä tai myyntitapahtumia (esim. testdatan Tapahtuma A).

```json
{
  "status": 409,
  "error": "Conflict",
  "messages": ["Tapahtumaa ei voi poistaa, koska siihen liittyy lipputyyppejä tai myyntejä"]
}
```

#### Sample call

```bash
curl -i -X DELETE http://localhost:8080/api/events/51
```

Demossa: luo ensin uusi tapahtuma POST:lla ja poista sen `id`. Valmiin testdatan tapahtumia ei voi poistaa.

---

## Resource: Tapahtuma

| Kenttä | Tyyppi | Kuvaus |
| --- | --- | --- |
| `id` | long | Palvelimen generoima tunniste |
| `nimi` | string | Nimi |
| `aika` | datetime | Esitysaika |
| `kaupunki` | string | Kaupunki |
| `paikka` | string | Paikka |
| `lippujaKpl` | integer | Kapasiteetti |

Lipputyyppejä ei palauteta tässä versiossa. Ne tulevat omassa endpointissaan myöhemmässä sprintissä.

## Virherunko

Kaikki 4xx-vastaukset (paitsi 204) käyttävät:

| Kenttä | Tyyppi | Kuvaus |
| --- | --- | --- |
| `status` | int | HTTP-koodi |
| `error` | string | Lyhyt nimi (`Not Found`, `Validation failed`, …) |
| `messages` | string[] | Ihmiselle luettavat viestit |

## Persistence

Kehityskanta on **H2 muistissa** (`jdbc:h2:mem:ticketguru`). Tiedot katoavat, kun prosessi sammutetaan. Käynnistyksessä ladataan esimerkkirivit (Tapahtuma A Helsinki, Tapahtuma B Tampere).

## Postman

Kokoelma katselmusta varten: [TicketGuru-events.postman_collection.json](TicketGuru-events.postman_collection.json)

Postman → Import → valitse tiedosto → aja järjestys: List → Get → Create → Update → Delete → Delete testdata (409).
