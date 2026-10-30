# Viikko 10 — luentomuistiinpanot

Päivämäärä: 2026-10-30

## Luennon aihe

Testaus. Sitä ei ole nimetty yhdelle tekijälle, eikä se aina näy työjonossa. Se kuuluu silti jokaiseen muutokseen. Tuoteomistaja kysyy, onko palvelinta testattu ja miten, kun client-tiimi selittää viivettä palvelimen virheillä.

Kooste: [testaus-junit-ja-tasot.md](testaus-junit-ja-tasot.md). Myyntitapahtuman testit, ajokomennot, HTML-sivujen tarkistuslista ja kokeellinen testaus: [../sprint-10-myyntitapahtuman-testit.md](../sprint-10-myyntitapahtuman-testit.md).

## Mitä luento suosittelee

- Työkalut ovat jo `backend/pom.xml`-tiedostossa: `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`, `spring-boot-starter-validation-test` ja `spring-security-test`. Uutta JUnit-riippuvuutta ei lisätä. `@Test` on `org.junit.jupiter.api.Test` (JUnit Jupiter 6).
- Yksikkötesti kutsuu luokkaa `LippuService` tai `MyyntiService` ilman käynnissä olevaa Spring Bootia. Repositorio on Mockito-vale. Maven ajaa JUnitin komennolla `./mvnw -Dtest=LippuServiceTest,MyyntiServiceTest test`. Luokat ovat `LippuServiceTest` ja `MyyntiServiceTest`.
- Integraatiotesti on `@SpringBootTest` ja `MockMvc`. Niitä on jo: `LippuApiIntegrationTest`, `MyyntiApiIntegrationTest`. Palvelinporttia ei avata.
- Päästä päähän -testi käynnistää palvelimen `RANDOM_PORT`-ympäristöön ja kutsuu sitä `RestTestClient`-kirjastolla. Luokat ovat `LippuE2ETest` ja `MyyntiE2ETest`. Selainajuria ei lisätä.
- Kaikki testit ajetaan kansiosta `backend` komennolla `./mvnw test`. Komentoa `spring-boot:run` ei käytetä. Yksikkötesti ei käynnistä Spring Bootia. Integraatiotesti käynnistää Springin ja H2:n testin sisällä ilman porttia. E2E käynnistää upotetun palvelimen satunnaiseen porttiin. Taulukko on luennon luvussa Miten testit ajetaan.
- Lipunmyynnin MVP-sivu on `client/myynti.html`. Sitä ei laiteta Bootin `static`-kansioon. Sopimus on [../api/sales.md](../api/sales.md).

## Mitä ei tehdä vielä

- JUnit 4 -kirjastoa tai `@RunWith`-ajoa ei lisätä.
- Automaattista selaintestiä ei kirjoiteta. Lipuntarkastuksen ja myyntisivun klikkaus jää käsin tehtäväksi.
- Tuotantokoodia ei muuteta testin vuoksi, ellei testi löydä virhettä.
