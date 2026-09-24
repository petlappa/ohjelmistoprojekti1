# TicketGuru REST API — Ticket types

Client-tiimille: tapahtuman lipputyypit. Valinnat: [../arkkitehtuuri-sprint-4.md](../arkkitehtuuri-sprint-4.md).

Sprint 4. Kirjautumista ei ole: rajapinta on avoin kehityskäytössä.

Tapahtuman id on **polussa**. Bodyyn ei laiteta `tapahtumaId`:tä. Vastaus ei upota tapahtumaa.

## Version

1.0 (Sprint 4)

## URI scheme

Sama kuin [events.md](events.md): `http://localhost:8080/api`, JSON, ISO-8601.

---

## Endpoints

### List ticket types

```
GET /api/events/:id/ticket-types
```

#### Success response

**Code:** `200 OK`

```json
[
  { "id": 3, "kuvaus": "Aikuinen", "hinta": 15.00 },
  { "id": 4, "kuvaus": "Lapsi", "hinta": 7.50 }
]
```

Lista on id:n mukaan nouseva. Tyhjä taulukko, jos tyyppejä ei ole.

#### Error response

**Code:** `404 Not Found` — tapahtumaa ei ole.

```bash
curl http://localhost:8080/api/events/1/ticket-types
```

---

### Create ticket type

```
POST /api/events/:id/ticket-types
```

#### Request body

| Kenttä | Tyyppi | Pakollinen | Kuvaus |
| --- | --- | --- | --- |
| `kuvaus` | string | kyllä | Enintään 80 merkkiä, ei tyhjä |
| `hinta` | number | kyllä | `0.00` tai suurempi, kaksi desimaalia |

```json
{ "kuvaus": "Opiskelija", "hinta": 10.00 }
```

#### Success response

**Code:** `201 Created`

**Headers:** `Location: http://localhost:8080/api/events/{tapahtumaId}/ticket-types/{id}`

Yksittäistä GET-osoitetta lipputyypille ei ole. `Location` kertoo luodun rivin id:n. Lista haetaan kokoelmasta.

```json
{ "id": 9, "kuvaus": "Opiskelija", "hinta": 10.00 }
```

#### Error responses

| Code | Milloin |
| --- | --- |
| `400` | Puuttuva kuvaus, negatiivinen hinta tai väärä JSON |
| `404` | Tapahtumaa ei ole |

```bash
curl -i -X POST http://localhost:8080/api/events/1/ticket-types \
  -H 'Content-Type: application/json' \
  -d '{"kuvaus":"Opiskelija","hinta":10.00}'
```

## Virherunko

Sama kuin [events.md](events.md): `status`, `error`, `messages`.
