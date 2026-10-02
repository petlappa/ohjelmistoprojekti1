# Viikko 8 — luentomuistiinpanot

Päivämäärä: 2026-10-02 (ennakko)

## Luennon aihe

Yksinkertainen selainclient toisen tiimin JWT-rajapintaa vasten. Client on vanilla JavaScript: se vastaanottaa tokenin ja lisää sen jokaiseen pyyntöön.

Kooste: [jwt-web-client.md](jwt-web-client.md).

## Mitä luento suosittelee

- `POST /api/login` kerran. Token talteen `localStorage`-muistiin. Sen jälkeen otsikko on tismalleen `Bearer`, välilyönti ja token.
- Palvelimella neljä osaa: JJWT-riippuvuudet, `JwtRequestFilter`, tilaton `SecurityFilterChain` ja `POST /api/login`, joka palauttaa kentän `token`. Rooli haetaan taulusta `Kayttaja`, ja `hasRole` ajetaan suodattimen jälkeen.
- Client ei rakenna JWT:tä, ei tarkista allekirjoitusta eikä päätä roolia.
- `401` tarkoittaa uutta kirjautumista. `403` tarkoittaa, että rooli ei riitä.
- Eri osoitteessa oleva selain tarvitsee backendin CORS-luvan otsikolle `Authorization`.
- Jos toinen tiimi teki Basicin, otsikko on `Basic` joka pyynnössä eikä erillistä login-kutsua ole.

## Mitä ei tehdä vielä

- Ei client-koodia tähän repositorioon.
- Ei oleteta, että oma palvelin on jo JWT. Viikon 6 linja on yhä Basic.
