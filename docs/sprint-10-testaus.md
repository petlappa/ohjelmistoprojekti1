# Sprint 10 — testaus ja lipunmyynnin sivu

Luento: [luentomateriaali/testaus-junit-ja-tasot.md](luentomateriaali/testaus-junit-ja-tasot.md). Viikon muistiinpanot: [luentomateriaali/viikko-10-testaus.md](luentomateriaali/viikko-10-testaus.md).

Tuoteomistaja kysyy, onko palvelinta testattu. Client-tiimi on selittänyt aikataulua palvelimelta löytyneillä virheillä. Asiakas pyytää myös lipunmyynnistä pientä sivua, samaan tapaan kuin lipuntarkastuksesta.

## Testauksen tasot

Testattavat kohteet ja valmis luokitus ovat luennossa. Tähän ratkaisuun kuuluu:

| Taso | Missä | Miksi juuri tämä taso |
| --- | --- | --- |
| Yksikkö | Uusi `LippuServiceTest` | Yksi luokka, repositorio on Mockito-vale, Spring ei käynnisty |
| Integraatio | `LippuApiIntegrationTest`, `MyyntiApiIntegrationTest` | Kontrolleri, Basic, palvelu ja H2. `MockMvc` ei avaa porttia |
| E2E | Uusi `LippuE2ETest` | `RANDOM_PORT` ja `RestTestClient`, sama HTTP-polku kuin Postmanilla |

Työkaluja ei asenneta erikseen. Ne ovat jo testiriippuvuuksina. Ajo on `./mvnw test` kansiosta `backend`.

## Lipunmyynti

MVP-sivu `client/myynti.html`. Ei kansiota `static/`. Myyjä valitsee tapahtuman ja lipputyypin ja lähettää `POST /api/sales`. Sivu näyttää kuitin koodit ja statuskoodin mukaisen virheen. Sopimus: [api/sales.md](api/sales.md). Avaus kuten sprintissä 9: Python, Node tai Live Server, backend erikseen.
