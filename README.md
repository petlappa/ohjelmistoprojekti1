# TicketGuru

Haaga-Helia **Ohjelmistoprojekti 1** -kurssin tiimityö. TicketGuru on lipputoimiston myyntipisteeseen tarkoitettu lipunmyyntijärjestelmä.

## Palautus

| Artefakti | Linkki |
| --- | --- |
| GitHub-repositorio | https://github.com/petlappa/ohjelmistoprojekti1 |
| Tuotteen määrittely | [docs/dokumentaatio.md](docs/dokumentaatio.md) |
| Tuotteen työjono | https://github.com/users/petlappa/projects/1/views/1 |
| Scrum-taulu (nykyinen sprintti) | https://github.com/users/petlappa/projects/1/views/9 |

Sprinttien taulut ja päätökset: [docs/scrum.md](docs/scrum.md).

## Dokumentaatio

| Mitä etsit | Missä se on |
| --- | --- |
| Johdanto, roolit, käyttöliittymä, tietokanta, rajapinnan yhteenveto | [docs/dokumentaatio.md](docs/dokumentaatio.md) |
| Tietokantakaavio | [docs/dokumentaatio.md#43-tietokantakaavio](docs/dokumentaatio.md#43-tietokantakaavio) |
| Client-kuvaus: tapahtumat, lipputyypit, myynti | [events](docs/api/events.md), [ticket-types](docs/api/ticket-types.md), [sales](docs/api/sales.md) |
| Postman | [tapahtumat](docs/api/TicketGuru-events.postman_collection.json), [myynti](docs/api/TicketGuru-sales.postman_collection.json) |
| Rajapinnan rakenne | [arkkitehtuuri-sprint-4.md](docs/arkkitehtuuri-sprint-4.md), [arkkitehtuuri-tapahtuma.md](docs/arkkitehtuuri-tapahtuma.md) |
| Sprintit ja taulun rakennus | [scrum.md](docs/scrum.md), [github-projects-scrum-ohje.md](docs/github-projects-scrum-ohje.md) |
| Julkaisu, sprintti 7 | [julkaiseminen-render-ja-rahti.md](docs/luentomateriaali/julkaiseminen-render-ja-rahti.md), kirjaus [scrum.md](docs/scrum.md#sprint-7--julkaisu) |
| Käynnistys | [kehitysymparisto.md](docs/kehitysymparisto.md) |
| Luennot | [luentomateriaali/](docs/luentomateriaali/) |

## Tiimi

| Nimi | Scrum-rooli |
| --- | --- |
| Petteri Lappalainen | Developer, Scrum Master (sprintit 1–4) |
| *tiimin jäsen 2* | Developer |
| *tiimin jäsen 3* | Developer |
| *tiimin jäsen 4* | Developer |
| *tiimin jäsen 5* | Developer |

**Product Owner:** kurssin opettaja / asiakkaan edustaja.

Sprint 2:n toteutus on jaettu viiteen työpakettiin ([#13](https://github.com/petlappa/ohjelmistoprojekti1/issues/13)–[#17](https://github.com/petlappa/ohjelmistoprojekti1/issues/17)). Sprint 3: events-API ([#29](https://github.com/petlappa/ohjelmistoprojekti1/issues/29)–[#31](https://github.com/petlappa/ohjelmistoprojekti1/issues/31), [#37](https://github.com/petlappa/ohjelmistoprojekti1/issues/37)–[#41](https://github.com/petlappa/ohjelmistoprojekti1/issues/41), [#43](https://github.com/petlappa/ohjelmistoprojekti1/issues/43)). Sprint 4: lipputyypit ja myynti ([#20](https://github.com/petlappa/ohjelmistoprojekti1/issues/20), [#21](https://github.com/petlappa/ohjelmistoprojekti1/issues/21), [#23](https://github.com/petlappa/ohjelmistoprojekti1/issues/23), [#24](https://github.com/petlappa/ohjelmistoprojekti1/issues/24), [#27](https://github.com/petlappa/ohjelmistoprojekti1/issues/27), [#32](https://github.com/petlappa/ohjelmistoprojekti1/issues/32), [#34](https://github.com/petlappa/ohjelmistoprojekti1/issues/34), [#47](https://github.com/petlappa/ohjelmistoprojekti1/issues/47)).

## Pika-aloitus

Vaatimukset: **JDK 25** (LTS) ja Git. Mavenia ei tarvitse asentaa erikseen (projekti sisältää Maven Wrapperin).

```bash
git clone https://github.com/petlappa/ohjelmistoprojekti1.git
cd ohjelmistoprojekti1/backend
./mvnw spring-boot:run
```

Windows: `mvnw.cmd spring-boot:run`

Sovellus vastaa osoitteessa:

- Terveystarkistus: http://localhost:8080/api/health (Basic Auth)
- Tapahtumat (lista): http://localhost:8080/api/events

Rajapinta vaatii HTTP Basicin. Esimerkkidatan tunnukset: `myyja` / `salasana` ja `koordinaattori` / `salasana`. `curl -u myyja:salasana http://localhost:8080/api/events`
- H2-konsoli: http://localhost:8080/h2-console  
  JDBC URL: `jdbc:h2:mem:ticketguru` · käyttäjä: `sa` · salasana tyhjä

Käynnistyksessä `DemoDataLoader` lisää esimerkkirivit. Sprint 4:n kokeiltava versio on **lipputyypit ja myynti**. Client-kuvaus: [docs/api/ticket-types.md](docs/api/ticket-types.md), [docs/api/sales.md](docs/api/sales.md). Postman: [docs/api/TicketGuru-sales.postman_collection.json](docs/api/TicketGuru-sales.postman_collection.json).

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
  web/           REST (`/api/health`, `/api/events`, `/api/sales`)
  service/       Tapahtumat, lipputyypit, myynti
```

Käyttöliittymän toteuttaa toinen tiimi. Sprint 4 lisää lipputyypit tapahtuman alle ja yhden myyntikutsun, joka luo kuitin ja liput. Valinnat: [docs/arkkitehtuuri-sprint-4.md](docs/arkkitehtuuri-sprint-4.md).
