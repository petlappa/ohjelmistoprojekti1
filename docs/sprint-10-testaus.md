# Sprint 10 — testaus ja lipunmyynnin sivu

Luento: [luentomateriaali/testaus-junit-ja-tasot.md](luentomateriaali/testaus-junit-ja-tasot.md). Viikon muistiinpanot: [luentomateriaali/viikko-10-testaus.md](luentomateriaali/viikko-10-testaus.md).

Tuoteomistaja kysyy, onko palvelinta testattu. Client-tiimi on selittänyt aikataulua palvelimelta löytyneillä virheillä. Asiakas pyytää myös lipunmyynnistä pientä sivua, samaan tapaan kuin lipuntarkastuksesta.

## Testauksen tasot

Testattavat kohteet ja valmis luokitus ovat luennossa. Tähän ratkaisuun kuuluu:

| Taso | Missä | Miksi juuri tämä taso |
| --- | --- | --- |
| Yksikkö | Uusi `LippuServiceTest` | Yksi luokka. Repositorio on Mockito-vale. `spring-boot:run` ei käynnisty, eikä Springin kontekstia luoda. Maven ajaa JUnitin |
| Integraatio | `LippuApiIntegrationTest`, `MyyntiApiIntegrationTest` | Kontrolleri, Basic, palvelu ja H2. `MockMvc` ei avaa porttia |
| E2E | Uusi `LippuE2ETest` | `RANDOM_PORT` ja `RestTestClient`, sama HTTP-polku kuin Postmanilla |

Työkaluja ei asenneta erikseen. Ne ovat jo testiriippuvuuksina. Kaikki tasot ajetaan kansiosta `backend` komennolla `./mvnw test`. Komentoa `spring-boot:run` ei käytetä. Yksikkötesti ei käynnistä Spring Bootia. Integraatio- ja E2E-testissä Maven käynnistää Springin testin sisällä. Komennot tasoittain ovat luennon luvussa Miten testit ajetaan.

## Lipunmyynti

Sivu on [../client/myynti.html](../client/myynti.html). Ei kansiota `static/`. Myyjä hakee tapahtumat, valitsee lipputyypin ja lähettää `POST /api/sales`. Sivu näyttää kuitin koodit ja statuskoodin mukaisen virheen. Sopimus: [api/sales.md](api/sales.md). Avaus kuten sprintissä 9: Python, Node tai Live Server, backend erikseen. Osoite on `http://localhost:5500/myynti.html`.
