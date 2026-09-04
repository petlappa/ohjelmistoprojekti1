# Scrum GitHubissa

Tiimin roolit ja Definition of Done. **Taulun rakentaminen klikki kerrallaan:** [github-projects-scrum-ohje.md](github-projects-scrum-ohje.md).

| Artefakti | Linkki |
| --- | --- |
| Product Backlog | https://github.com/users/petlappa/projects/1/views/1 |
| Current sprint | https://github.com/users/petlappa/projects/1/views/2 |

GitHubissa:

1. **Issues** = käyttäjätarinat ja tehtävät
2. **GitHub Project** = product backlog (Table) ja sprinttitaulu (Board)
3. **Iteration-kenttä (Sprint)** = sprintit — ei Milestones

## Roolit tässä projektissa

| Scrum-rooli | Kuka |
| --- | --- |
| Product Owner | Opettaja / asiakas: priorisoi työjonon |
| Scrum Master (sprintit 1–2) | *Sovitaan tiimissä Sprint Planningissa* |
| Developers | Koko viisihenkinen tiimi |

Scrum Master ei ole pomo. Hän huolehtii, että Daily Scrum pidetään, taulu vastaa totuutta ja esteet nousevat esiin.

## Miten palaset liittyvät toisiinsa

```text
Product Backlog     Sprint Planning      Sprint Backlog         Daily          Review
(GitHub Issues,     valitaan milestone   (taulun sarake         kortit         suljetaan
 ilman milestonea    + siirretään         "Sprintissä")          liikkuvat      Done-sarakkeeseen
 tai Backlog-sarakkeessa)  tauluun
```

- **Product Backlog:** kaikki tiedossa olevat käyttäjätarinat. Osa odottaa myöhempiä sprinttejä.
- **Sprint Backlog:** ne issuet, joihin tiimi sitoutui tässä sprintissä (milestone = nykyinen sprintti).
- **Increment:** sprintin lopussa `main`-haarassa toimiva, testattu lisäys + päivitetty dokumentaatio.

## Taulun sarakkeet

Suositeltu Scrum-taulu (GitHub Project, Board-näkymä):

| Sarake | Mitä siinä on |
| --- | --- |
| **Backlog** | Tuotteen työjono, ei vielä tässä sprintissä |
| **Sprintissä** | Tämän sprintin sitoumus |
| **Doing** | Työn alla (yksi kortti / henkilö mieluiten) |
| **Review** | PR auki tai odottaa demoa / koodikatselmusta |
| **Done** | Yhdistetty `main`-haaraan, Definition of Done täyttyy |

Lisäkenttä **Sprint** (Sprint 1 / 2 / 3) mahdollistaa suodatuksen. Milestone issuen päällä tekee saman Issues-listassa.

### Definition of Done (alustava)

Issue on Done vasta kun:

- koodi on `main`-haarassa
- testit menevät läpi (paikallisesti ja CI)
- dokumentaatio on päivitetty, jos muutos vaikuttaa määrittelyyn tai käynnistykseen
- tiimi voi esitellä muutoksen Sprint Review’ssa

## Scrum-taulun rakentaminen

Klikkausohjeet opiskelijoille: **[github-projects-scrum-ohje.md](github-projects-scrum-ohje.md)**.

## Sprintin rytmi tässä kurssissa

1. **Sprint Planning**  
   Product Backlog -näkymä. Valituille tarinoille kenttä **Sprint** = nykyinen iteraatio, Estimate ja Assignee. Status jää Todoon.
2. **Daily Scrum**  
   Current sprint -näkymä. Kortti *In Progress* kun työ alkaa.
3. **Kehitys**  
   Feature-haara → PR (`Closes #n`) → merge → *Done*.
4. **Sprint Review**  
   Demo incrementistä. Kesken jääneet: Sprint-kenttä seuraavaan iteraatioon, ei Doneksi.
5. **Retrospective**  
   1–3 parannusta uusina `task`-issueina backlogin.

## Käyttäjätarinan issue-pohja

```markdown
## Käyttäjätarina
**Roolina** haluan ..., jotta ...

## Hyväksymiskriteerit
- [ ] Given ... When ... Then ...
```

Sprint merkitään Projectin **Sprint**-kenttään Planningissa, ei Issuen milestonena.
