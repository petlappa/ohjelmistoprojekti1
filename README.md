# TicketGuru

Haaga-Helia **Ohjelmistoprojekti 1** -kurssin tiimityö. TicketGuru on lipputoimiston myyntipisteeseen tarkoitettu lipunmyyntijärjestelmä.

## Palautuslinkit (Sprint 2)

| Artefakti | Linkki |
| --- | --- |
| GitHub-repositorio | https://github.com/petlappa/ohjelmistoprojekti1 |
| Dokumentaatio | [docs/dokumentaatio.md](docs/dokumentaatio.md) |
| Tietokantakaavio | [docs/dokumentaatio.md#43-tietokantakaavio](docs/dokumentaatio.md#43-tietokantakaavio) |
| Tuotteen työjono (Product Backlog) | https://github.com/users/petlappa/projects/1/views/1 |
| Scrum-taulu (nykyinen sprintti) | https://github.com/users/petlappa/projects/1/views/9 |
| Sprint 2 | https://github.com/users/petlappa/projects/1/views/4 |
| Sprint 1 | https://github.com/users/petlappa/projects/1/views/3 |
| Scrum GitHubissa (tiimin muistiinpanot) | [docs/scrum.md](docs/scrum.md) |
| Scrum-taulun rakennusohje (opiskelijoille) | [docs/github-projects-scrum-ohje.md](docs/github-projects-scrum-ohje.md) |
| Kehitysympäristö | [docs/kehitysymparisto.md](docs/kehitysymparisto.md) |
| Luentomateriaalin tarkennukset | [docs/luentomateriaali/](docs/luentomateriaali/) |
| Tietokanta Spring Bootissa (luento-ohje) | [docs/luentomateriaali/tietokanta-spring-bootissa.md](docs/luentomateriaali/tietokanta-spring-bootissa.md) |

## Tiimi

| Nimi | Scrum-rooli |
| --- | --- |
| Petteri Lappalainen | Developer, Scrum Master (sprintit 1–2) |
| *tiimin jäsen 2* | Developer |
| *tiimin jäsen 3* | Developer |
| *tiimin jäsen 4* | Developer |
| *tiimin jäsen 5* | Developer |

**Product Owner:** kurssin opettaja / asiakkaan edustaja.

Sprint 2:n toteutus on jaettu viiteen työpakettiin ([#13](https://github.com/petlappa/ohjelmistoprojekti1/issues/13)–[#17](https://github.com/petlappa/ohjelmistoprojekti1/issues/17)), jotta jokainen tiimiläinen voi ottaa oman entity-/dokumentaatiopaketin ja tehdä omat commitit. Toistaiseksi vain Petteri on Collaborators-listalla, joten paketit on toteutettu samassa repossa erillisinä committeina.

## Pika-aloitus

Vaatimukset: **JDK 25** (LTS) ja Git. Mavenia ei tarvitse asentaa erikseen (projekti sisältää Maven Wrapperin).

```bash
git clone https://github.com/petlappa/ohjelmistoprojekti1.git
cd ohjelmistoprojekti1/backend
./mvnw spring-boot:run
```

Windows: `mvnw.cmd spring-boot:run`

Sovellus vastaa osoitteessa:

- Terveystarkistus: http://localhost:8080/api/health
- H2-konsoli: http://localhost:8080/h2-console  
  JDBC URL: `jdbc:h2:mem:ticketguru` · käyttäjä: `sa` · salasana tyhjä

Käynnistyksessä `DemoDataLoader` lisää esimerkkirivit (roolit, käyttäjät, tapahtumat, lipputyypit, yksi myynti ja liput). H2-konsolissa esim. `SELECT * FROM LIPPU;` — tämä on Sprint 2:n kokeiltava versio ennen REST-myyntirajapintaa.

Testit:

```bash
./mvnw test
```

## Rakenne

```
backend/     Spring Boot 4.1 REST-palvelin (Java 25)
docs/        Projektidokumentaatio (tuote) ja luentomuistiinpanot
.github/     CI-työnkulku
```

```
backend/src/main/java/fi/haagahelia/ticketguru/
  domain/        JPA-entityt ja suhteet
  repository/    Spring Data JPA -rajapinnat
  web/           REST (toistaiseksi terveystarkistus)
```

Käyttöliittymä (React tai vastaava) lisätään myöhemmässä sprintissä. Sprint 2:n tavoite on tietokantakaavio, dokumentaation luku Tietokanta sekä entityt ja repositoryt.
