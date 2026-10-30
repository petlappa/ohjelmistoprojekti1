# Viikko 10 — luentomuistiinpanot

Päivämäärä: 2026-10-30

## Luennon aihe

Testaus. Sitä ei ole nimetty yhdelle tekijälle, eikä se aina näy työjonossa. Se kuuluu silti jokaiseen muutokseen. Tuoteomistaja kysyy, onko palvelinta testattu ja miten, kun client-tiimi selittää viivettä palvelimen virheillä.

Kooste: [testaus-junit-ja-tasot.md](testaus-junit-ja-tasot.md).

## Mitä luento suosittelee

- Työkalut ovat jo `backend/pom.xml`-tiedostossa: `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`, `spring-boot-starter-validation-test` ja `spring-security-test`. Uutta JUnit-riippuvuutta ei lisätä. `@Test` on `org.junit.jupiter.api.Test` (JUnit Jupiter 6).
- Yksikkötesti on yksi luokka ilman Springiä. Malli on `LippuService` ja Mockito-vale repositoriolle. Tiedosto `LippuServiceTest` puuttuu vielä.
- Integraatiotesti on `@SpringBootTest` ja `MockMvc`. Niitä on jo: `LippuApiIntegrationTest`, `MyyntiApiIntegrationTest`. Palvelinporttia ei avata.
- Päästä päähän -testi käynnistää palvelimen `RANDOM_PORT`-ympäristöön ja kutsuu sitä `RestTestClient`-kirjastolla. Selainajuria ei lisätä.
- Testit ajetaan kansiosta `backend` komennolla `./mvnw test`. CI ajaa saman komennon.
- Lipunmyynnistä tehdään MVP-sivu `client/myynti.html`. Sitä ei laiteta Bootin `static`-kansioon. Sopimus on [../api/sales.md](../api/sales.md).

## Mitä ei tehdä vielä

- JUnit 4 -kirjastoa tai `@RunWith`-ajoa ei lisätä.
- Automaattista selaintestiä ei kirjoiteta. Lipuntarkastuksen ja myyntisivun klikkaus jää käsin tehtäväksi.
- Tuotantokoodia ei muuteta testin vuoksi, ellei testi löydä virhettä.
