# TicketGuru

Haaga-Helia **Ohjelmistoprojekti 1** -kurssin tiimityö. TicketGuru on lipputoimiston myyntipisteeseen tarkoitettu lipunmyyntijärjestelmä.

## Palautuslinkit (Sprint 1)

| Artefakti | Linkki |
| --- | --- |
| GitHub-repositorio | *täytetään repon luonnin jälkeen* |
| Dokumentaatio | [docs/dokumentaatio.md](docs/dokumentaatio.md) |
| Tuotteen työjono (Product Backlog) | *Issues-näkymä, täytetään repon luonnin jälkeen* |
| Scrum-taulu | *GitHub Project, täytetään repon luonnin jälkeen* |
| Scrum-ohje | [docs/scrum.md](docs/scrum.md) |
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

Vaatimukset: **JDK 17** ja Git. Mavenia ei tarvitse asentaa erikseen (projekti sisältää Maven Wrapperin).

```bash
git clone <repo-url>
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
backend/     Spring Boot REST-palvelin (Java 17)
docs/        Projektidokumentaatio
.github/     CI-työnkulku
```

Käyttöliittymä (React tai vastaava) lisätään myöhemmässä sprintissä. Sprint 1:n tavoite on kehitysympäristö, dokumentaation alku ja alustettu backend.
