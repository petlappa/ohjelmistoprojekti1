# Viikko 5 — luentomuistiinpanot

Päivämäärä: 2026-09-24 (Sprint 5, materiaali)

## Luennon aihe

Vastauskoodit ja hallitut virheet: `200` / `201` / `204`, puuttuva resurssi `404`, kelvoton pyyntö `400`. Odotettu virhe ei ole `500`.

Kooste vaihtoehdoista ja TicketGurun suositus: [virheet-ja-validointi.md](virheet-ja-validointi.md).

## Mitä luento suosittelee

- Onnistunut GET ja PUT: olion palautus, Springin `200`.
- Onnistunut POST: `ResponseEntity` → `201` ja `Location`.
- Onnistunut DELETE: `204`.
- Puuttuva polun id: `ResponseStatusException` → `404`.
- Rungon tarkistus: `@Valid` ja annotaatiot Request DTO:ssa, ei entityssä.
- Sama virhe-JSON kaikille koodeille: yksi `@RestControllerAdvice`.

## Mitä ei tehdä vielä tässä muistiinpanossa

Toteutusta ei muutettu. Sprintin tehtävä (oikeat koodit, ei `500`:aa, pakolliset kentät POST:ssa ja PUT:ssa) tarkistetaan luentoa vasten myöhemmin.
