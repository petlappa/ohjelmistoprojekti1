# TicketGuru REST API — Tickets

Client-tiimille: lipun haku koodilla ja merkintä käytetyksi. Myynti luo koodin: [sales.md](sales.md).

Tunnistus on HTTP Basic, sama kuin [events.md](events.md#authentication). Tunnus `myyja` / `salasana` tai `koordinaattori` / `salasana`.

## URI scheme

| | |
| --- | --- |
| **Host** | `localhost:8080` kehityksessä, julkaistuna oma Render-osoite |
| **Base path** | `/api` |
| **Content-Type** | `application/json; charset=utf-8` |
| **CORS** | `Access-Control-Allow-Origin: *` kehityksessä (`/api/tickets`) |

## Endpoints

### Get ticket by code

```
GET /api/tickets?koodi=3er454aa
```

#### Success response

**Code:** `200 OK`

```json
{
  "id": 1,
  "koodi": "3er454aa",
  "hinta": 15.00,
  "lipputyypinKuvaus": "Aikuinen",
  "kaytetty": false
}
```

`id` tarvitaan merkintään. Esimerkkidatan koodi `3er454ac` on jo käytetty.

#### Error responses

| Code | Milloin |
| --- | --- |
| `400` | `koodi` puuttuu |
| `401` | Basic-otsikko puuttuu tai salasana on väärä |
| `404` | Koodia ei ole |

### Mark ticket used

```
PATCH /api/tickets/1
```

#### Request body

| Kenttä | Tyyppi | Pakollinen | Kuvaus |
| --- | --- | --- | --- |
| `kaytetty` | boolean | kyllä | Vain `true`. Lippu merkitään käytetyksi. |

```json
{ "kaytetty": true }
```

#### Success response

**Code:** `200 OK` — sama runko kuin haussa, `kaytetty` on `true`.

#### Error responses

| Code | Milloin |
| --- | --- |
| `400` | Runko puuttuu, ei ole JSON:ia, tai `kaytetty` ei ole `true` |
| `401` | Basic-otsikko puuttuu tai salasana on väärä |
| `404` | Id:tä ei ole |
| `409` | Lippu on jo käytetty |

Virherunko on sama kuin tapahtumilla: `status`, `error`, `messages`.

Selain toisesta osoitteesta (localhost tai Reactin kehitysportti) lähettää ensin `OPTIONS`-kyselyn, koska otsikko on `Authorization`.
