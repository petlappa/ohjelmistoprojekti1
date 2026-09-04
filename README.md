# TicketGuru

Haaga-Helia **Ohjelmistoprojekti 1** -kurssin tiimityö. TicketGuru on lipputoimiston myyntipisteeseen tarkoitettu lipunmyyntijärjestelmä.

## Palautuslinkit (Sprint 1)

| Artefakti | Linkki |
| --- | --- |
| GitHub-repositorio | https://github.com/petlappa/ohjelmistoprojekti1 |
| Dokumentaatio | [docs/dokumentaatio.md](docs/dokumentaatio.md) |
| Tuotteen työjono (Product Backlog) | https://github.com/users/petlappa/projects/1/views/1 |
| Scrum-taulu (nykyinen sprintti) | https://github.com/users/petlappa/projects/1/views/2 |
| Scrum GitHubissa (tiimin muistiinpanot) | [docs/scrum.md](docs/scrum.md) |
| Scrum-taulun rakennusohje (opiskelijoille) | [docs/github-projects-scrum-ohje.md](docs/github-projects-scrum-ohje.md) |
| Kehitysympäristö | [docs/kehitysymparisto.md](docs/kehitysymparisto.md) |

## Tiimi

| Nimi | Scrum-rooli |
| --- | --- |
| Petteri Lappalainen | Developer |
| *tiimin jäsen 2* | Developer |
| *tiimin jäsen 3* | Developer |
| *tiimin jäsen 4* | Developer |
| *tiimin jäsen 5* | Developer |

**Scrum Master (sprintit 1–2):** *sovitaan tiimissä ensimmäisessä Sprint Planningissa.*  
**Product Owner:** kurssin opettaja / asiakkaan edustaja.

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

Testit:

```bash
./mvnw test
```

## Rakenne

```
backend/     Spring Boot 4.1 REST-palvelin (Java 25)
docs/        Projektidokumentaatio
.github/     CI-työnkulku
```

Käyttöliittymä (React tai vastaava) lisätään myöhemmässä sprintissä. Sprint 1:n tavoite on kehitysympäristö, dokumentaation alku ja alustettu backend.
