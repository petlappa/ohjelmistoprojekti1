# Testaus: tasot, työkalut ja testin kirjoitus

Päivitetty **2026-10-30**. Viikko 10, sprintti 10. Tausta: [viikko-10-testaus.md](viikko-10-testaus.md).

Testaus ei ole yhden ihmisen tehtävä. Sitä ei aina näe työjonossa, mutta se kuuluu jokaiselle muutokselle. Tuoteomistaja kysyy, onko palvelinpuolta testattu ja miten. Tässä repossa on jo integraatiotestejä, ja GitHub Actions ajaa ne haarassa `main` sekä pull requesteissa. Tällä viikolla tasot nimetään, puuttuva yksikkötesti ja palvelimen päästä päähän -testi kirjoitetaan, ja lipunmyynnistä tehdään pieni erillinen sivu samaan tapaan kuin lipuntarkastuksesta.

## Lähteet

| Aihe | Lähde |
| --- | --- |
| Spring Boot -testit | [Testing Spring Boot Applications](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html) |
| JUnit Jupiter | [JUnit 6 User Guide](https://docs.junit.org/6.0.3/user-guide/) |
| AssertJ | [AssertJ core](https://assertj.github.io/doc/) |

Moodlen tiedostot *Johdanto testaukseen*, *Testitapausten suunnittelu* ja *Yksikkötestaus ja JUnit* taustoittavat tasot. Tämän projektin annotaatiot ja riippuvuudet ovat Spring Boot 4.1:n mukaiset. `@Test` tulee paketista `org.junit.jupiter.api`. Erillistä JUnit 4 -kirjastoa ei lisätä.

## Mitä palvelimessa testataan

| Kohde | Mitä tarkistetaan | Miten |
| --- | --- | --- |
| `LippuService` | Tyhjä koodi on 400, tuntematon koodi tai id on 404, jo käytetty lippu on 409, muuten `kaytetty` on `true` | Yksikkötesti. Tietokanta on vale (mock). |
| `MyyntiService` | Kapasiteetin ylitys on 409, vieraan tapahtuman lipputyyppi on 400 | Yksikkötesti samalla tavalla, tai jo oleva integraatiotesti. |
| Kontrolleri | Polku, JSON ja statuskoodi, kun palvelu on vaihdettu valheeseen | `@WebMvcTest`. Luokka `TapahtumaControllerTest` on tämä kapea viipale. |
| Tietokanta | `findByKoodi` ja tallennus | `@DataJpaTest`. Luokka `TicketGuruRepositoryTests`. |
| Rajapinta kokonaisuutena | HTTP Basic, roolit, palvelu ja H2 yhdessä | `@SpringBootTest` ja `MockMvc`. Luokat `LippuApiIntegrationTest` ja `MyyntiApiIntegrationTest`. |
| Sama polku kuin Postman | Oikea upotettu palvelin ja HTTP-kutsu | `webEnvironment = RANDOM_PORT` ja `RestTestClient`. Tätä luokkaa ei vielä ole. |
| Selain | Lomake, `fetch` ja virheilmoitus | Käsin sivu `client/index.html`. Automaattista selaintestiä ei lisätä tällä viikolla. |

## Työkalut ovat jo classpathilla

`backend/pom.xml` ei tarvitse uutta riippuvuutta. Testit tulevat näistä, scope `test`:

| Artefakti | Mitä sillä tehdään |
| --- | --- |
| `spring-boot-starter-webmvc-test` | JUnit Jupiter 6, AssertJ, Mockito, MockMvc, `RestTestClient` |
| `spring-boot-starter-data-jpa-test` | `@DataJpaTest` ja H2 |
| `spring-boot-starter-validation-test` | Bean Validation testeissä |
| `spring-security-test` | `httpBasic` ja `@WithMockUser` |

Spring Boot 4 jakoi vanhan `spring-boot-starter-test`-paketin näihin. Toista JUnit-versiota ei laiteta `pom.xml`-tiedostoon.

Testiluokka tulee kansioon `backend/src/test/java/`. Paketti peilaa tuotantokoodia. Maven ajaa luokat, joiden nimi päättyy `Test`.

Ajo kansiosta `backend`:

```bash
./mvnw test
./mvnw -Dtest=LippuApiIntegrationTest test
```

CI on tiedostossa `.github/workflows/backend-ci.yml`. Se ajaa saman `./mvnw -B test` -komennon.

## Yksikkötesti

Yksikkötesti (unit test) kutsuu yhtä luokkaa. Springiä ei käynnistetä, H2 ei nouse, eikä HTTP-kutsuja lähde. `LippuRepository` on Mockito-vale, koska tietokanta ei kuulu tähän testiin.

Tiedosto `backend/src/test/java/fi/haagahelia/ticketguru/service/LippuServiceTest.java`:

```java
@ExtendWith(MockitoExtension.class)
class LippuServiceTest {

    @Mock
    private LippuRepository lippuRepository;

    @InjectMocks
    private LippuService lippuService;

    @Test
    void joKaytettyLippuPalauttaa409() {
        Lippu lippu = new Lippu();
        lippu.setKaytetty(true);
        when(lippuRepository.findById(1L)).thenReturn(Optional.of(lippu));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> lippuService.merkitseKaytetyksi(1L, true));

        assertThat(ex.getStatusCode().value()).isEqualTo(409);
    }
}
```

Askeleet:

1. Luo luokka testipuun samaan pakettiin kuin `LippuService`.
2. `@ExtendWith(MockitoExtension.class)` käynnistää Mockiton. `@SpringBootTest` jätetään pois.
3. `@Mock` tekee `LippuRepository`-valeen. `@InjectMocks` syöttää sen `LippuService`-konstruktoriin.
4. `@Test` on metodi, jonka nimi kertoo odotuksen. Järjestä tilanne, kutsu metodia, tarkista tulos.
5. `assertThrows` nappaa poikkeuksen. `assertThat` on AssertJ. `when(...).thenReturn(...)` on Mockito.
6. Aja `./mvnw -Dtest=LippuServiceTest test`.

Tämä on yksikkötesti, koska ajettava koodi on `LippuService` ja repositorio ei ole oikea. Sama malli käy tyhjälle koodille (400) ja tuntemattomalle id:lle (404).

## Integraatiotesti

Integraatiotesti (integration test) ajaa useita kerroksia yhdessä: kontrolleri, tietoturva, palvelu ja H2. Palvelinta ei käynnistetä porttiin. `MockMvc` kutsuu Spring MVC:tä saman prosessin sisällä.

Malli on jo luokassa `LippuApiIntegrationTest`:

```java
@SpringBootTest
@AutoConfigureMockMvc
class LippuApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void missingCredentialsAre401() throws Exception {
        mockMvc.perform(get("/api/tickets").param("koodi", "3er454aa"))
                .andExpect(status().isUnauthorized());
    }
}
```

`@SpringBootTest` ilman `webEnvironment`-arvoa on `MOCK`. Upotettu Tomcat ei kuuntele porttia. `@AutoConfigureMockMvc` on paketissa `org.springframework.boot.webmvc.test.autoconfigure`. Tunnus lisätään `BasicAuth.myyja()`-apulaisella, joka on `httpBasic("myyja", "salasana")`.

Askeleet uudelle tapaukselle:

1. Kopioi luokan annotaatiot ja `MockMvc`.
2. Kirjoita `@Test`, joka tekee yhden asian: myy lipun, merkitse se, merkitse uudelleen.
3. Odota statuskoodi `andExpect(status()...)` ja JSON `jsonPath`.
4. Älä muuta esimerkkilippua `3er454aa` käytetyksi. Se on jaettu H2, ja toinen testi lukee sen käyttämättömänä. Uusi lippu luodaan `POST /api/sales` -kutsulla, kuten `patchMarksTicketUsedAndSecondMarkConflicts` jo tekee.

Tämä on integraatiotesti, koska mukana ovat tietoturva, kontrolleri, palvelu ja tietokanta. Se ei ole päästä päähän -testi, koska kutsu ei lähde oikeaan porttiin.

Kapeammat viipaleet ovat jo repossa. `@WebMvcTest(TapahtumaController.class)` testaa vain webin, ja palvelu on `@MockitoBean`. `@DataJpaTest` testaa vain tietokannan. Ne eivät korvaa koko polun integraatiotestiä.

## Päästä päähän

Päästä päähän -testi (end-to-end, E2E) käynnistää upotetun palvelimen satunnaiseen porttiin ja lähettää HTTP-kutsun, kuten Postman. Kirjasto `RestTestClient` tulee jo `spring-boot-starter-webmvc-test`-riippuvuudesta.

Tiedosto `backend/src/test/java/fi/haagahelia/ticketguru/web/LippuE2ETest.java`:

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class LippuE2ETest {

    @Autowired
    private RestTestClient client;

    @Test
    void tuntematonKoodiOn404() {
        client.get()
                .uri("/api/tickets?koodi={koodi}", "ei-ole")
                .header("Authorization", basic("myyja", "salasana"))
                .exchange()
                .expectStatus().isNotFound();
    }

    private static String basic(String tunnus, String salasana) {
        String token = Base64.getEncoder().encodeToString(
                (tunnus + ":" + salasana).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}
```

`AutoConfigureRestTestClient` on paketissa `org.springframework.boot.resttestclient.autoconfigure`. `RestTestClient` on paketissa `org.springframework.test.web.servlet.client`.

Tämä on E2E-testi, koska palvelin kuuntelee porttia ja asiakas on HTTP-kirjasto. Selainta, Renderiä ja erillistä `client/`-sivua ei ole mukana. Sivun klikkaus käsin on manuaalinen E2E. Automaattista selainajuria ei lisätä.

`RANDOM_PORT`-testissä palvelimen tietokantatapahtuma ei peruunnu testimetodin lopussa. Älä jätä jaettua esimerkkilippua käytetyksi.

## Lipunmyynnin sivu

Asiakas pyytää lipunmyynnistä samanlaisen pienen sivun kuin lipuntarkastuksesta. Taso on MVP: myynti onnistuu, ylimääräistä ei rakenneta.

Sivu on uusi tiedosto `client/myynti.html`. Sitä ei kopioida kansioon `static/`. Avaus on sama kuin sprintissä 9: Python, Node tai Live Server, ja backend erikseen. Sopimus on [../api/sales.md](../api/sales.md).

Sivu:

1. Hakee tapahtumat `GET /api/events` ja tyypit `GET /api/events/{id}/ticket-types`.
2. Lähettää `POST /api/sales` rungolla `tapahtumaId`, `myyjaId` ja `rivit`. Tunnus on `myyja` / `salasana`. Koordinaattori saa tästä osoitteesta 403.
3. Näyttää kuitin: summa ja lippujen `koodi`. Virhe näytetään statuskoodin mukaan, kuten lipuntarkastuksessa.

`MyyntiApiIntegrationTest` kattaa jo myynnin integraationa. Sivun E2E on tällä viikolla käsin, ellei tiimi lisää `RANDOM_PORT`-testiä samalle `POST`-kutsulle.
