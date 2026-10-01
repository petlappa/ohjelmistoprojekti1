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

## Toteutus

Rajapinta tarkistettiin luentoa vasten. POST palauttaa `201`, DELETE `204`, puuttuva polun id `404`, kelvoton runko ja vääräntyyppinen id `400`. Tietokantarajoite on `409`, väärä HTTP-metodi `405`. Tuntematon osoite on `404`. Näistä ei tule `500`:aa. Virherunko on `ApiError`.
