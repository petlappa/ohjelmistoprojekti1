# Viikko 3 — luentomuistiinpanot

Päivämäärä: 2026-09 (Sprint 3)

## Luennon aihe

REST-rajapinta client-tiimille: tapahtumien lisäys, muokkaus, haku ja poisto. Dokumentointi (restapidocs-tyyli) ja Postman-demo katselmukseen.

## TicketGurussa viikolla 3

- Base-URL: `http://localhost:8080/api`
- Endpointit: `GET/POST /api/events`, `GET/PUT/DELETE /api/events/{id}`
- Query: `kaupunki`
- H2 muistissa, ei persistenssiä
- Client-dokumentti: [../api/events.md](../api/events.md)

## Mitä ei tehdä vielä

- Lipputyyppien, myynnin ja lippujen REST
- Spring Security / kirjautuminen
- Persistentti tuotantokanta
