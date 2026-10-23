# Sprint 9 — CORS ja lipuntarkastus

Backendissa on nyt CORS päällä ja lipun haku sekä käyttömerkintä. Erillinen vanilla-sivu on kansiossa [../client/index.html](../client/index.html). Sitä ei kopioida Bootin `static`-kansioon eikä käännetä mukaan. Koko testipaketti meni läpi.

HTTP Basic säilyy. Uutta `CorsConfigurationSource`-papua ei ole, jotta `@CrossOrigin` jää voimaan. Käyttöhetken aikaleimaa ei lisätty: `kaytetty` on edelleen boolean.

Luento: [luentomateriaali/web-client-cors-ja-lipuntarkastus.md](luentomateriaali/web-client-cors-ja-lipuntarkastus.md). Client-sopimus: [api/tickets.md](api/tickets.md).

## Jo valmiina, ei muutosta

`TapahtumaController`, `LipputyyppiController` ja `MyyntiController` oli merkitty `@CrossOrigin(origins = "*")`. `LippuResponse` ja `LippuRepository.findByKoodi` olivat jo olemassa.

## Muutokset tiedostoittain

### `security/SecurityConfig.java`

Ketjun alkuun `.cors(Customizer.withDefaults())`, jotta selaimen `OPTIONS` ei kaadu 401:een ennen kontrolleria. Sitä ennen `/api/**`-sääntöä: `GET /api/tickets` ja `PATCH /api/tickets/*` rooleille `MYYJA` ja `TAPAHTUMAKOORDINAATTORI`.

### `web/HealthController.java`

Ainoa vanha `/api`-kontrolleri ilman annotaatiota. Siihen `@CrossOrigin(origins = "*")`.

### `web/LippuController.java`

Uusi. `@RequestMapping("/api/tickets")` ja sama CORS-annotaatio. `GET ?koodi=` palauttaa lipun. `PATCH /{id}` rungolla `{ "kaytetty": true }` merkitsee lipun käytetyksi.

### `service/LippuService.java`

Tyhjä tai puuttuva koodi on 400, tuntematon koodi tai id on 404, jo käytetty lippu on 409. Muuten `kaytetty` asetetaan `true`.

### `web/dto/LippuKayttoRequest.java`

Uusi runko. `kaytetty` on pakollinen ja sen pitää olla `true`. `false` on 400.

### `web/ApiExceptionHandler.java`

Puuttuva kyselyparametri, kuten `GET` ilman `koodi`-kenttää, palauttaa saman `ApiError`-400:n kuin muut virheet.

### `web/LippuApiIntegrationTest.java`

Uusi. Kattaa 401:n, haun molemmilla rooleilla, 400:n ja 404:n, merkinnän ja toisen merkinnän 409:n, sekä `OPTIONS`-esikyselyn osoitteesta `http://localhost:5500` otsikolla `Access-Control-Allow-Origin: *`.

### `docs/api/tickets.md`

Client-tiimin sopimus: suomenkieliset kentät, paluukoodit, Basic. Linkki lisättiin `docs/dokumentaatio.md`- ja `docs/luentomateriaali/README.md`-tiedostoihin.

## Erillinen client

Postman kutsuu samoja osoitteita. Sivu näyttää vastauksen sanoina.

Sivu on yksi HTML-tiedosto, jossa on lomake ja `fetch`. Se tarvitsee oman osoitteen, esimerkiksi `http://localhost:5500`. Tiedoston avaaminen levyltä (`file://`) ei toimi: selaimella ei ole silloin `http`-alkuperää, ja `fetch` epäonnistuu ennen kuin palvelin ehtii vastata. Live Server tai `python3 -m http.server 5500` kansiossa `client/` riittää. Backend voi olla `http://localhost:8080` tai Renderin osoite. CORS on jo päällä, joten eri portti on sallittu.

Sivulla on kenttä lippukoodille, nappi **Hae** ja nappi **Merkitse käytetyksi**. JavaScript kokoaa tunnuksen itse otsikkoon `Authorization: Basic`. Oletus on `myyja` / `salasana`. Rajapinnan osoitteen voi vaihtaa sivun kentästä.

Haku on `GET /api/tickets?koodi=`. Onnistunut `200` näyttää `koodi`, `hinta`, `lipputyypinKuvaus` ja `kaytetty`, ja tallettaa vastauksen `id`:n. Merkintä käyttää sitä id:tä: `PATCH /api/tickets/{id}`, otsikot `Authorization` ja `Content-Type: application/json`, runko `{ "kaytetty": true }`. Käyttäjän ei tarvitse kirjoittaa id:tä.

Virhe luetaan vastauksen `status`-kentästä. Rungossa on sama `ApiError` kuin muissa osoitteissa (`messages`), ja sivu näyttää sen lisäksi oman lauseen:

| Status | Sivulla |
| --- | --- |
| `200` | Lipun tiedot. Jos `kaytetty` on jo `true`, teksti kertoo että lippu on käytetty. |
| `400` | Lippukoodi puuttuu tai pyyntö on kelvoton. |
| `401` | Tunnus tai salasana on väärä. |
| `404` | Lippua ei löydy tällä koodilla tai id:llä. |
| `409` | Lippu on jo käytetty. |

Jos `fetch` heittää poikkeuksen eikä `status`-kenttää ole, sivu kertoo että yhteys epäonnistui: sivu on auki levyltä, backend ei ole käynnissä, tai CORS esti kutsun.
