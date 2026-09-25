# TicketGuru REST API — Sales

Client-tiimille: yhden kassakaupan luonti ja kuitti. Valinnat: [../arkkitehtuuri-sprint-4.md](../arkkitehtuuri-sprint-4.md). Lipputyypit: [ticket-types.md](ticket-types.md).

Sprint 4. Kirjautumista ei ole. Myyjä annetaan bodyssa kenttänä `myyjaId` (esimerkkidata: käyttäjä `myyja`, nimi Maija Myyjä).

## Version

1.0 (Sprint 4)

## URI scheme

Sama kuin [events.md](events.md): `http://localhost:8080/api`, JSON, ISO-8601 ilman aikavyöhykettä.

---

## Endpoints

### Create sale

Yksi kutsu luo myyntitapahtuman ja kaikki liput. Palvelin asettaa ajan, laskee summan ja generoi koodit. Älä lähetä `summa`- tai `myyntiaika`-kenttää.

```
POST /api/sales
```

#### Request body

| Kenttä | Tyyppi | Pakollinen | Kuvaus |
| --- | --- | --- | --- |
| `tapahtumaId` | long | kyllä | Olemassa oleva tapahtuma |
| `myyjaId` | long | kyllä | Olemassa oleva käyttäjä |
| `rivit` | array | kyllä | 1–20 riviä |
| `rivit[].lipputyyppiId` | long | kyllä | Tyypin pitää kuulua samaan tapahtumaan |
| `rivit[].kpl` | int | kyllä | 1–500 |

```json
{
  "tapahtumaId": 1,
  "myyjaId": 1,
  "rivit": [
    { "lipputyyppiId": 3, "kpl": 2 },
    { "lipputyyppiId": 4, "kpl": 1 }
  ]
}
```

Id:t ovat esimerkkidatassa juoksevia. Hae tapahtuma ja lipputyypit ensin.

#### Success response

**Code:** `201 Created`

**Headers:** `Location: http://localhost:8080/api/sales/{id}`

```json
{
  "id": 12,
  "myyntiaika": "2026-09-24T18:05:12",
  "summa": 37.50,
  "tapahtumanNimi": "Tapahtuma A",
  "myyjanNimi": "Maija Myyjä",
  "liput": [
    {
      "id": 40,
      "koodi": "a1b2c3d4",
      "hinta": 15.00,
      "lipputyypinKuvaus": "Aikuinen",
      "kaytetty": false
    }
  ]
}
```

`summa` on rivien `hinta × kpl`. Jokainen lippu saa kopion hinnasta myyntihetkellä. `koodi` on 8 merkkiä ja yksilöllinen.

#### Error responses

| Code | Milloin |
| --- | --- |
| `400` | Validointi; tapahtumaa, myyjää tai lipputyyppiä ei ole; tyyppi kuuluu toiseen tapahtumaan |
| `409` | Uudet liput ylittäisivät tapahtuman `lippujaKpl` |

```bash
curl -i -X POST http://localhost:8080/api/sales \
  -H 'Content-Type: application/json' \
  -d '{"tapahtumaId":1,"myyjaId":1,"rivit":[{"lipputyyppiId":3,"kpl":2}]}'
```

---

### Get sale

```
GET /api/sales/:id
```

#### Success response

**Code:** `200 OK` — sama runko kuin luonnin `201`.

#### Error response

**Code:** `404 Not Found` — myyntiä ei ole.

```bash
curl http://localhost:8080/api/sales/1
```

## Resource: Myynti

| Kenttä | Tyyppi | Kuvaus |
| --- | --- | --- |
| `id` | long | Myyntinumero |
| `myyntiaika` | datetime | Palvelimen aika |
| `summa` | number | Rivien summa |
| `tapahtumanNimi` | string | Ei koko tapahtumaoliota |
| `myyjanNimi` | string | Etunimi ja sukunimi. Ei salasanaa, ei käyttäjälistaa |
| `liput` | array | Luodut liput |

## Virherunko

Sama kuin [events.md](events.md): `status`, `error`, `messages`.

## Postman

[TicketGuru-sales.postman_collection.json](TicketGuru-sales.postman_collection.json)
