# TicketGuru

Haaga-Helia **Ohjelmistoprojekti 1** -kurssin tiimityö. TicketGuru on lipputoimiston myyntipisteeseen tarkoitettu lipunmyyntijärjestelmä.

## Palautuslinkit (Sprint 3)

| Artefakti | Linkki |
| --- | --- |
| GitHub-repositorio | https://github.com/petlappa/ohjelmistoprojekti1 |
| Dokumentaatio | [docs/dokumentaatio.md](docs/dokumentaatio.md) |
| API-dokumentaatio (events, client-tiimille) | [docs/api/events.md](docs/api/events.md) |
| Pyynnön kulku Springissä (arkkitehtuuri) | [docs/arkkitehtuuri-tapahtuma.md](docs/arkkitehtuuri-tapahtuma.md) |
| Postman-kokoelma | [docs/api/TicketGuru-events.postman_collection.json](docs/api/TicketGuru-events.postman_collection.json) |
| Tietokantakaavio | [docs/dokumentaatio.md#43-tietokantakaavio](docs/dokumentaatio.md#43-tietokantakaavio) |
| Tuotteen työjono (Product Backlog) | https://github.com/users/petlappa/projects/1/views/1 |
| Scrum-taulu (nykyinen sprintti) | https://github.com/users/petlappa/projects/1/views/9 |
| Sprint 3 | https://github.com/users/petlappa/projects/1/views/5 |
| Sprint 2 | https://github.com/users/petlappa/projects/1/views/4 |
| Sprint 1 | https://github.com/users/petlappa/projects/1/views/3 |
| Scrum GitHubissa (tiimin muistiinpanot) | [docs/scrum.md](docs/scrum.md) |
| Scrum-taulun rakennusohje (opiskelijoille) | [docs/github-projects-scrum-ohje.md](docs/github-projects-scrum-ohje.md) |
| Kehitysympäristö | [docs/kehitysymparisto.md](docs/kehitysymparisto.md) |
| Luentomateriaalin tarkennukset | [docs/luentomateriaali/](docs/luentomateriaali/) |

## Tiimi

| Nimi | Scrum-rooli |
| --- | --- |
| Petteri Lappalainen | Developer, Scrum Master (sprintit 1–3) |
| *tiimin jäsen 2* | Developer |
| *tiimin jäsen 3* | Developer |
| *tiimin jäsen 4* | Developer |
| *tiimin jäsen 5* | Developer |

**Product Owner:** kurssin opettaja / asiakkaan edustaja.

Sprint 2:n toteutus on jaettu viiteen työpakettiin ([#13](https://github.com/petlappa/ohjelmistoprojekti1/issues/13)–[#17](https://github.com/petlappa/ohjelmistoprojekti1/issues/17)). Sprint 3: events-API client-tiimille ([#29](https://github.com/petlappa/ohjelmistoprojekti1/issues/29), [#30](https://github.com/petlappa/ohjelmistoprojekti1/issues/30), [#31](https://github.com/petlappa/ohjelmistoprojekti1/issues/31), [#37](https://github.com/petlappa/ohjelmistoprojekti1/issues/37)–[#41](https://github.com/petlappa/ohjelmistoprojekti1/issues/41)).

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
- Tapahtumat (lista): http://localhost:8080/api/events
- H2-konsoli: http://localhost:8080/h2-console  
  JDBC URL: `jdbc:h2:mem:ticketguru` · käyttäjä: `sa` · salasana tyhjä

Käynnistyksessä `DemoDataLoader` lisää esimerkkirivit. Sprint 3:n kokeiltava versio on **events-API**. Client-kuvaus: [docs/api/events.md](docs/api/events.md). Postman: [docs/api/TicketGuru-events.postman_collection.json](docs/api/TicketGuru-events.postman_collection.json) (Import Postmanissa).

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
  web/           REST (`/api/health`, `/api/events`)
  service/       Tapahtumien käyttötapaukset
```

Käyttöliittymän toteuttaa toinen tiimi. Sprint 3:n tavoite on tapahtumien REST-CRUD, API-dokumentaatio ja Postman-demo.
