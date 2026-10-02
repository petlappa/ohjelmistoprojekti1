# Viikko 2 — luentomuistiinpanot

Päivämäärä: 2026-09 (Sprint 2)

Luennon materiaali:

- *Käsiteanalyysista tietokantarakenteeseen* (Ruonavaara)
- *Tietokanta projektissa* (Ruonavaara)

**Opiskelijaohje (yleinen, ei TicketGuru-tuotemalli):** [tietokanta-spring-bootissa.md](tietokanta-spring-bootissa.md)

Tämä tiedosto on tiimin viikkotarkennus. TicketGurun tuotevaatimukset: [dokumentaatio.md](../dokumentaatio.md). Kooditehtävät haarassa `viikko-2`.

## Luennon aihe

1. Käsiteanalyysi: käsite vs attribuutti, yhteydet, 1:N ja N:M, vierasavaimet.
2. Tietokanta projektissa: ORM (JPA/Hibernate), DAO, kolme vaihetta (Entity → suhteet → Repository).
3. Spring Bootissa haut JPA Query Method -nimeämisellä.

## Keskeiset käsitteet

| Käsite | Oma tarkennus | Esimerkki TicketGurussa (myöhemmin) |
| --- | --- | --- |
| Käsite / Entity | Taulun olioesitys | Tapahtuma, lipputyyppi, myynti, lippu |
| Attribuutti | Sarake; ei ole itsenäinen olio | tapahtuman nimi, lipun koodi, hinta |
| 1:N | FK N-puolelle, `@ManyToOne` | lippu → myynti |
| N:M | välikäsite, jos yhteydellä on dataa | myynti ↔ lipputyyppi lipun kautta |
| Repository | Springin DAO | `TapahtumaRepository extends JpaRepository<…>` |
| Query method | haku metodin nimestä | `findByTapahtuma`, `findByKoodi` |

## Mitä otetaan käyttöön tällä viikolla

- [x] Käsitekaavio TicketGurulle (luennon kaavalla) — [dokumentaatio 4.3](../dokumentaatio.md#43-tietokantakaavio)
- [x] Entityt ja repositoryt Spring Bootissa ohjeen kolmen vaiheen mukaan
- [x] H2-konsolilla tarkistus, että taulut syntyvät (`DemoDataLoader` + testit)

## Mitä ei tehdä vielä

- REST-rajapintaa joka entiteetille ennen kuin malli on kunnossa
- `@ManyToMany` yhteyksille, joilla on omaa dataa (hinta, koodi, kpl)

## Avoimet kysymykset

- [x] Tapahtumakohtaiset vai globaalit lipputyypit → **tapahtumakohtaiset** (dokumentaatio 4.2)
- [ ] Ennakkomyynnin päättymisaika (M11) — ei vielä saraketta

## Lähteet

- [tietokanta-spring-bootissa.md](tietokanta-spring-bootissa.md)
- [Spring ORM](https://docs.spring.io/spring-framework/reference/data-access/orm.html)
- [DAO-malli (Baeldung)](https://www.baeldung.com/java-dao-pattern)
- [JPA Query Methods](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html)
