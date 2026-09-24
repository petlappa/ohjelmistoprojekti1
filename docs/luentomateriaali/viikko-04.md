# Viikko 4 — luentomuistiinpanot

Päivämäärä: 2026-09-24 (Sprint 4)

## Luennon aihe

Rajapinta, kun resurssilla on yhteyksiä: GET-upotus, URI, Response DTO; POST-id polussa, id bodyssa, Entity Trick.

## TicketGurussa viikolla 4

Tiimin valinta on kirjattu tiedostoon [../arkkitehtuuri-sprint-4.md](../arkkitehtuuri-sprint-4.md).

- Lipputyyppi: `POST /api/events/{id}/ticket-types` (id polussa)
- Myynti: `POST /api/sales` Request DTO:lla (`tapahtumaId`, `myyjaId`, `rivit`)
- Kuitti: `GET /api/sales/{id}` Response DTO:lla (`myyjanNimi`, `tapahtumanNimi`, liput)
- Entity Trick ja entiteetin palautus JSON:na eivät ole julkisessa API:ssa

## Mitä ei tehdä vielä

- Kirjautuminen ja myyjän ottaminen tokenista
- Myyntiraportti, ovitarkastus, tulostus
