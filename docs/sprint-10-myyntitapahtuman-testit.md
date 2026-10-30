# Sprint 10 — myyntitapahtuman testit

Luento nimeää tasot: [luentomateriaali/testaus-junit-ja-tasot.md](luentomateriaali/testaus-junit-ja-tasot.md). Sprintin päätökset: [sprint-10-testaus.md](sprint-10-testaus.md). Myynnin sopimus: [api/sales.md](api/sales.md).

Myyntitapahtuma on testattu kolmella automaattisella tasolla ja käsin HTML-sivuilla. Automaattiset testit ajetaan Mavenilla kansiosta `backend`. Komentoa `spring-boot:run` ei käytetä niihin. Käsin testatessa backend käynnistetään erikseen, ja sivut avataan omasta osoitteesta. Numeroidut askeleet ovat etukäteen kirjoitettu tarkistuslista. Kokeellinen testaus (exploratory testing) käyttää samaa sivua, ja testaaja päättää seuraavan askeleen sen mukaan, mitä edellinen vastaus näytti.

## Testityypit

| Taso | Luokka | Mitä ajo käynnistää | Mitä taso todistaa myynnistä |
| --- | --- | --- | --- |
| Yksikkö (unit) | `MyyntiServiceTest` | JUnit ja Mockito. Ei Tomcatia, ei tietokantaa, ei Springin kontekstia | `MyyntiService.create` päättää 400:n, 409:n ja kuitin summan, kun repositoriot on vaihdettu valeisiin |
| Integraatio (integration) | `MyyntiApiIntegrationTest` | Springin konteksti ja H2. Kutsu menee `MockMvc`-oliolla saman prosessin sisällä. Porttia ei avata | Polku, JSON, Basic ja tietokanta yhdessä: kuitti, kapasiteetti, vieras lipputyyppi, tuntematon myyjä |
| Päästä päähän (end-to-end, E2E) | `MyyntiE2ETest` | Upotettu palvelin satunnaiseen porttiin. `RestTestClient` lähettää HTTP-kutsun | Sama polku kuin Postmanilla: tapahtuma, lipputyyppi, myynti, käyttömerkintä ja toinen merkintä |
| Käsin, tarkistuslista | `client/myynti.html` ja `client/index.html` | Ihmisen selain ja erikseen käynnissä oleva backend portissa 8080 | Lomake, `fetch`, kuitti ja statuskoodin mukainen virheilmoitus. Askeleet on kirjoitettu etukäteen. Selainajuria ei ole |
| Kokeellinen (exploratory) | Samat sivut, istunto tehtävänannolla | Sama käynnissä oleva backend ja selain. Maven-ajoa ei ole | Alue, jota automaattinen testi ei ole rajannut. Havainnot kirjataan istunnon aikana |

Rooliraja on lisäksi integraatiotestissä `BasicAuthenticationTest.coordinatorCanCreateEventButCannotSell`: koordinaattori saa luoda tapahtuman ja saa myynnistä 403.

Lipun haulla ja merkinnällä on omat luokat samoilla tasoilla: `LippuServiceTest`, `LippuApiIntegrationTest` ja `LippuE2ETest`. Ne eivät myy lippua. Myynnin E2E luo uuden lipun ja merkitsee sen, jotta esimerkkikoodi `3er454aa` jää käyttämättömäksi.

## Miten testit ajetaan

Komennot ajetaan kansiosta `backend`. Maven lataa JUnitin. Sovellusta ei käynnistetä terminaaliin etukäteen.

```bash
cd backend
./mvnw test
./mvnw -Dtest=MyyntiServiceTest test
./mvnw -Dtest=MyyntiApiIntegrationTest test
./mvnw -Dtest=MyyntiE2ETest test
./mvnw -Dtest=MyyntiServiceTest,MyyntiApiIntegrationTest,MyyntiE2ETest test
```

| Komento | Mitä ajetaan |
| --- | --- |
| `./mvnw test` | Kaikki testit, myös lipun testit ja CI:n sama komento `./mvnw -B test` |
| `-Dtest=MyyntiServiceTest` | Vain palvelun päätökset. Spring Boot ei käynnisty |
| `-Dtest=MyyntiApiIntegrationTest` | Myyntirajapinta H2:ta vasten ilman porttia |
| `-Dtest=MyyntiE2ETest` | HTTP upotettuun palvelimeen |
| Kolme luokkaa pilkulla | Myynnin kaikki automaattiset tasot yhdellä ajolla |

Windowsilla komento on `mvnw.cmd` samoin argumentein. Yhden metodin voi rajata vielä luokan perään: `./mvnw -Dtest=MyyntiServiceTest#kapasiteetinYlitysOn409 test`.

## Yksikkötestit

Tiedosto `backend/src/test/java/fi/haagahelia/ticketguru/service/MyyntiServiceTest.java`.

`@ExtendWith(MockitoExtension.class)` rakentaa `MyyntiService`-olion. Viisi repositoriota ovat `@Mock`. Testi kertoo `when(...).thenReturn(...)`, mitä haku palauttaa. Tallennus palauttaa saman olion, jonka palvelu antoi `save`-kutsulle. Tapahtuman, myyjän ja lipputyypin id asetetaan testissä, koska tuotantoluokissa id tulee tietokannasta.

| Metodi | Syöte | Odotus |
| --- | --- | --- |
| `tuntematonTapahtumaOn400` | Tapahtumaa ei löydy | 400, myyntiä ei tallenneta |
| `tuntematonMyyjaOn400` | Tapahtuma löytyy, myyjää ei | 400 |
| `vieraanTapahtumanLipputyyppiOn400` | Lipputyyppi kuuluu toiseen tapahtumaan | 400 |
| `kapasiteetinYlitysOn409` | Kapasiteetti 1 ja yksi lippu on jo myyty, pyyntö on 1 kpl | 409, myyntiä ei tallenneta |
| `myyntiLuoLipunJaSumman` | Kaksi aikuisen lippua á 15,00, kapasiteetti riittää | Summa 30,00, kaksi lippua, kuvaus Aikuinen, `kaytetty` on false, myyjä Maija Myyjä |

## Integraatiotestit

Tiedosto `backend/src/test/java/fi/haagahelia/ticketguru/web/MyyntiApiIntegrationTest.java`.

`@SpringBootTest` ja `@AutoConfigureMockMvc`. Tunnus tulee `BasicAuth.myyja()` tai `BasicAuth.koordinaattori()` (`myyja` / `salasana`, `koordinaattori` / `salasana`). Testi luo tapahtuman ja lipputyypin rajapinnan kautta, jotta se ei ole sidottu esimerkkirivien id-lukuihin.

| Metodi | Odotus |
| --- | --- |
| `demoEventListsTicketTypes` | Helsingin esimerkkilipputyypit, vähintään kaksi, `kuvaus` ja `hinta` |
| `createTicketTypeThenSaleReturnsReceipt` | Kaksi opiskelijalippua á 10,00. Vastaus 201, `Location`, summa 20,00, kaksi koodia, myyjä Maija Myyjä. `GET /api/sales/{id}` palauttaa saman kuitin |
| `saleOverCapacityReturnsConflict` | Kapasiteetti 1 ja pyyntö 2 kpl on 409 |
| `ticketTypeFromAnotherEventReturnsBadRequest` | Toisen tapahtuman lipputyyppi on 400 |
| `unknownSellerOrSaleReturnsClientError` | Myyjä 999999 on 400. Tuntematon myynti ja tuntematon tapahtuman lipputyyppilista ovat 404 |

`BasicAuthenticationTest` tarkistaa saman myyntipolun roolin: koordinaattorin `POST /api/sales` on 403.

## E2E-testit

Tiedosto `backend/src/test/java/fi/haagahelia/ticketguru/web/MyyntiE2ETest.java`.

`@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)` ja `@AutoConfigureRestTestClient`. Kutsu on HTTP. Basic-otsikko kootaan testissä Base64-koodauksella tunnuksesta `myyja:salasana`.

| Metodi | Odotus |
| --- | --- |
| `myyjaSaaKuitin` | Koordinaattori luo tapahtuman ja lipputyypin 12,50. Myyjä myy yhden. Vastaus 201, summa 12,50, koodi ei ole tyhjä, `kaytetty` on false |
| `myytyLippuMerkitaanKaytetyksi` | Myynnin jälkeen `PATCH /api/tickets/{id}` rungolla `{ "kaytetty": true }` on 200. Haku koodilla näyttää `kaytetty: true`. Toinen merkintä on 409 |
| `koordinaattoriEiVoiMyyda` | Koordinaattorin `POST /api/sales` on 403 |

Esimerkkikoodeja `3er454aa` ja `3er454ab` ei merkitä näissä testeissä.

## HTML-sivujen käyttö käsin

Automaattinen testi ei avaa selainta. Käsin testataan kahta sivua kansiossa `client/`. Niitä ei kopioida Bootin `static`-kansioon.

Backend käynnistetään ensin, kansiosta `backend`:

```bash
./mvnw spring-boot:run
```

Lokissa Tomcat kuuntelee porttia 8080. Sivu tarvitsee oman `http`-osoitteen. Tiedoston avaaminen levyltä (`file://`) näyttää lomakkeen, mutta `fetch` epäonnistuu ennen palvelimen vastausta. Sivun teksti on silloin: yhteys epäonnistui.

Sivu avataan jollakin näistä, kansiosta `client/`:

```bash
cd client
python3 -m http.server 5500
```

Sivu on osoitteessa `http://localhost:5500/myynti.html`. Node-vaihtoehto on `npx serve -l 5500`. Cursorin tai VS Coden Live Server avaa yleensä `http://127.0.0.1:5500/myynti.html`. Kenttään **Rajapinnan osoite** jätetään backend, oletus `http://localhost:8080`.

### Myynti

Sivu `myynti.html`. Oletustunnus on `myyja` / `salasana`. Kenttä **Myyjän id** on `1`. Tuoreessa H2-kannassa se on Maija Myyjä, jonka `DemoDataLoader` lisää käynnistyksessä.

1. Paina **Hae tapahtumat**. Onnistunut vastaus on `200 — Tapahtumia N`. Valikkoon tulevat tapahtuman nimi ja kaupunki. Lipputyypit täyttyvät valitun tapahtuman mukaan, esimerkiksi `Aikuinen 15`.
2. Valitse tapahtuma ja lipputyyppi, jätä kappalemääräksi 1 ja paina **Myy**. Onnistunut vastaus on `201 — Liput myyty`, tapahtuman nimi, myyjän nimi, summa ja lippukoodi.
3. Vaihda tunnus kenttään `koordinaattori` ja paina **Myy**. Sivu näyttää `403 — Tunnuksella ei ole oikeutta myydä`.
4. Vaihda salasana vääräksi ja paina **Hae tapahtumat**. Sivu näyttää `401 — Tunnus tai salasana on väärä`.
5. Palauta tunnus `myyja` ja salasana `salasana`. Aseta kappalemäärä suuremmaksi kuin tapahtumaan mahtuu jäljellä olevia lippuja ja paina **Myy**. Sivu näyttää `409 — Lippuja ei ole enää jäljellä tähän tapahtumaan`.
6. Aseta myyjän id:ksi luku, jota kannassa ei ole, esimerkiksi `999999`, ja paina **Myy**. Sivu näyttää `400 — Pyyntö on kelvoton`.

Nappien käsittelijät ovat HTML-attribuuteissa: `onclick="haeTapahtumat()"` ja `onclick="myy()"`. Tapahtumavalikon `onchange` kutsuu `haeLipputyypit()`.

### Myydyn lipun tarkastus

Kopioi kuitin koodi. Avaa samasta osoitteesta `http://localhost:5500/index.html`. Tunnus voi olla `myyja` tai `koordinaattori`.

1. Liitä koodi ja paina **Hae**. `200` näyttää hinnan, lipputyypin ja että lippu ei ole käytetty.
2. Paina **Merkitse käytetyksi**. Sivu lähettää `PATCH /api/tickets/{id}` rungolla `{ "kaytetty": true }`. Id tulee haun vastauksesta.
3. Paina **Hae** uudelleen. Teksti kertoo, että lippu on käytetty.
4. Paina **Merkitse käytetyksi** toisen kerran. Sivu näyttää `409`.

Esimerkkikoodi `3er454aa` on käyttämätön aikuisen lippu. `3er454ac` on jo käytetty, joten sen merkintä on heti 409. Tyhjä koodi on 400. Koodi, jota kannassa ei ole, on 404.

Jos backend ei ole käynnissä tai sivu on auki levyltä, kumpikin sivu näyttää yhteyden epäonnistumisen eikä HTTP-statuskoodia.

## Kokeellinen testaus

Kokeellisessa testauksessa (exploratory testing) askellistaa ei kirjoiteta valmiiksi. Testaajalla on tehtävänanto (charter), aikaraja ja muistiinpanot. Yksi istunto kestää noin 25 minuuttia. Seuraava kokeilu valitaan siitä, mitä ruutu, statuskoodi tai kuitti juuri näytti.

Backend ja sivu käynnistetään samalla tavalla kuin tarkistuslistassa: `./mvnw spring-boot:run` kansiosta `backend`, ja sivu osoitteesta `http://localhost:5500/myynti.html`.

Istunnon alussa kirjataan neljä riviä:

| Kenttä | Esimerkki |
| --- | --- |
| Tehtävänanto | Myy lippuja ja selvitä, mitä tapahtuu kun kappalemäärä, myyjän id tai tapahtuma vaihtuu kesken session |
| Aika | 25 minuuttia |
| Sivut | `myynti.html` ja `index.html`, rajapinta `http://localhost:8080` |
| Tunnukset | `myyja` / `salasana` ja `koordinaattori` / `salasana` |

Istunnon aikana joka kokeilusta jää yksi rivi: mitä tehtiin, mitä sivu näytti, ja mitä kokeillaan seuraavaksi. Lopuksi kirjataan löydetyt virheet, avoimet kysymykset ja ne kohdat, joista kannattaa tehdä JUnit-testi.

Tehtävänanto rajaa alueen. Nämä käyvät tämän sprintin istuntoihin. Yhteen istuntoon riittää yksi tehtävänanto.

1. **Myynti ja kuitti.** Hae tapahtumat, vaihda tapahtumaa, myy yksi ja monta, vaihda lipputyyppiä myyntien välissä. Katso, päivittyykö valikko, täsmääkö summa hintaan kertaa kappalemäärä, ja kelpaako kuitin koodi heti tarkastussivulla.
2. **Kentät, joita lomake ei estä.** Kokeile kappalemäärää 0, tyhjää, negatiivista ja hyvin suurta. Tyhjennä myyjän id. Vaihda rajapinnan osoitteeksi `http://127.0.0.1:8080` ja takaisin. Kirjaa statuskoodi ja se, jäikö vanha virheilmoitus näkyviin.
3. **Rooli ja tunnukset.** Hae tapahtumat koordinaattorilla ja yritä myydä. Palaa myyjään, vaihda salasana kesken haun ja myynnin. Kirjaa, mikä nappi onnistuu kummallakin tunnuksella.
4. **Sama lippu kahdessa paikassa.** Myy lippu, avaa tarkastus toiseen välilehteen, merkitse lippu käytetyksi ja yritä samaa koodia uudelleen. Myy tapahtuman kapasiteetti täyteen yhdellä välilehdellä ja yritä myyntiä toisella.
5. **Koodin muoto.** Liitä kuitin koodi välilyönnein, vaihda kirjainkokoa ja kokeile koodia `3er454aa` sekä jo käytettyä `3er454ac`. Katso, mitä sivu sanoo ennen kuin painat merkintää.

Havainto, joka toistuu, kirjataan testiksi sille tasolle, jolle se kuuluu. Palvelun päätös, kuten kapasiteetin 409, kuuluu `MyyntiServiceTest`-luokkaan. Polku ja JSON kuuluvat `MyyntiApiIntegrationTest`-luokkaan. Koko HTTP-polku kuuluu `MyyntiE2ETest`-luokkaan. Lomakkeen teksti ja nappien järjestys jäävät kokeelliseen istuntoon, koska selainajuria ei tällä viikolla lisätä.
