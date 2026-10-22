# Viikko 9 — luentomuistiinpanot

Päivämäärä: 2026-10-22

## Luennon aihe

Web-ohjelmointi. Jokaisen pitää osata lisätä staattinen sivusto (static content) Spring Bootiin ja lukea, miten erillinen sivu (client) kutsuu rajapintaa toisesta osoitteesta. CORS (Cross-Origin Resource Sharing) on selaimen tarkistus sille kutsulle. Sama alkuperä (same-origin) tarkoittaa, että protokolla (scheme), palvelimen nimi (host) ja portti (port) täsmäävät. Niiden yhdistelmä on alkuperä (origin).

Tämän viikon sprintissä sivustoa ei lisätä omaan Bootiin. Backend on julkaistu, esimerkiksi Renderiin. Clientin tekee toinen tiimi localhostissa, ja sivu kutsuu julkaistua rajapintaa. Tämän tiimin työ on lipun haku ja merkintä sekä selaimen luvan antaminen sivun osoitteelle. Tunnistus pysyy viikon 6 HTTP Basicinä.

Kooste: [web-client-cors-ja-lipuntarkastus.md](web-client-cors-ja-lipuntarkastus.md).

## Mitä luento suosittelee

- Rajapinta jatkaa sprintin 4 lippua: `GET /api/tickets?koodi=` ja `PATCH /api/tickets/{id}` rungolla `{ "kaytetty": true }`. Vastaus on sama `LippuResponse` kuin myynnissä. Virheet ovat viikon 5 koodeja, `ApiError`-rungolla.
- CORS-tarkistuslista: jokaiseen `/api`-kontrolleriin `@CrossOrigin(origins = "*")` luokan yläpuolelle, myös vanhoihin jos annotaatio puuttuu, ja `SecurityConfig`-luokan `filterChain`-metodiin `.cors(Customizer.withDefaults())`. Molemmat tarvitaan. Erillistä CORS-asetusoliota ei lisätä. Lippujen `GET` ja `PATCH` saavat `hasAnyRole("MYYJA", "TAPAHTUMAKOORDINAATTORI")` ennen yleistä `/api/**`-sääntöä.
- Otsikko `Authorization: Basic` aiheuttaa `OPTIONS`-kyselyn myös `GET`-kutsulle. Postmanin `200` ei kerro, että selain pääsee läpi.
- Staattinen sivusto osataan laittaa kansioon `src/main/resources/static/`. Tämän sprintin ohjelmaan ei pakata sivua. Myöhemmin oma React-client käännetään, ja käännöksen tiedostot (`dist/` tai `build/`) kopioidaan siihen kansioon. Silloin sivu ja rajapinta ovat samaa alkuperää.
- QR-koodin paikka jätetään tiimin perusteltavaksi. Luento ei valitse client- eikä palvelinpuolta.

## Mitä ei tehdä vielä

- Erillistä web-sivua ei kirjoiteta tähän repositorioon. Toinen tiimi voi tehdä sen Reactilla. `fetch` ja Basic-otsikko ovat silti samat, ja Reactin localhost-portti tarvitsee CORS-luvan.
- Tunnistusta ei vaihdeta. Basic jää.
- QR-toteutusta ei tehdä valmiiksi luentomateriaalissa.
