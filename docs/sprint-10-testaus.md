# Sprint 10 — testaus ja lipunmyynnin sivu

Luento: [luentomateriaali/testaus-junit-ja-tasot.md](luentomateriaali/testaus-junit-ja-tasot.md). Viikon muistiinpanot: [luentomateriaali/viikko-10-testaus.md](luentomateriaali/viikko-10-testaus.md). Myyntitapahtuman testit, ajokomennot, HTML-sivujen tarkistuslista ja kokeellinen testaus: [sprint-10-myyntitapahtuman-testit.md](sprint-10-myyntitapahtuman-testit.md).

Tuoteomistaja kysyy, onko palvelinta testattu. Client-tiimi on selittänyt aikataulua palvelimelta löytyneillä virheillä. Asiakas pyytää myös lipunmyynnistä pientä sivua, samaan tapaan kuin lipuntarkastuksesta.

## Testauksen tasot

Testattavat kohteet ja valmis luokitus ovat luennossa. Tähän ratkaisuun kuuluu:

| Taso | Missä | Miksi juuri tämä taso |
| --- | --- | --- |
| Yksikkö | `LippuServiceTest`, `MyyntiServiceTest` | Yksi luokka. Repositorio on Mockito-vale. `spring-boot:run` ei käynnisty, eikä Springin kontekstia luoda. Maven ajaa JUnitin |
| Integraatio | `LippuApiIntegrationTest`, `MyyntiApiIntegrationTest` | Kontrolleri, Basic, palvelu ja H2. `MockMvc` ei avaa porttia |
| E2E | `LippuE2ETest`, `MyyntiE2ETest` | `RANDOM_PORT` ja `RestTestClient`, sama HTTP-polku kuin Postmanilla |

Työkaluja ei asenneta erikseen. Ne ovat jo testiriippuvuuksina. Kaikki tasot ajetaan kansiosta `backend` komennolla `./mvnw test`. Komentoa `spring-boot:run` ei käytetä. Yksikkötesti ei käynnistä Spring Bootia. Integraatio- ja E2E-testissä Maven käynnistää Springin testin sisällä. Komennot tasoittain ovat luennon luvussa Miten testit ajetaan.

## Lipunmyynti

Sivu on [../client/myynti.html](../client/myynti.html). Ei kansiota `static/`. Myyjä hakee tapahtumat, valitsee lipputyypin ja lähettää `POST /api/sales`. Sivu näyttää kuitin koodit ja statuskoodin mukaisen virheen. Sopimus: [api/sales.md](api/sales.md). Avaus, myynnin kokeilu ja myydyn koodin tarkastus `index.html`-sivulla on kuvattu dokumentissa [sprint-10-myyntitapahtuman-testit.md](sprint-10-myyntitapahtuman-testit.md). Osoite on `http://localhost:5500/myynti.html`.
