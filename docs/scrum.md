# Scrum GitHubissa

Tiimin roolit ja Definition of Done. **Taulun rakentaminen klikki kerrallaan:** [github-projects-scrum-ohje.md](github-projects-scrum-ohje.md).

| Artefakti | Linkki |
| --- | --- |
| Product Backlog | https://github.com/users/petlappa/projects/1/views/1 |
| Current sprint | https://github.com/users/petlappa/projects/1/views/9 |
| Sprint 2 | https://github.com/users/petlappa/projects/1/views/4 |
| Sprint 1 | https://github.com/users/petlappa/projects/1/views/3 |

GitHubissa:

1. **Issues** = käyttäjätarinat ja tehtävät
2. **GitHub Project** = product backlog (Table) ja sprinttitaulu (Board)
3. **Iteration-kenttä (Sprint)** = sprintit — ei Milestones

## Roolit tässä projektissa

| Scrum-rooli | Kuka |
| --- | --- |
| Product Owner | Opettaja / asiakas: priorisoi työjonon |
| Scrum Master (sprintit 1–2) | Petteri Lappalainen |
| Developers | Koko tiimi (toistaiseksi yksi GitHub-käyttäjä; työpaketit #13–#17 on silti jaettu) |

Scrum Master ei ole pomo. Hän huolehtii, että Daily Scrum pidetään, taulu vastaa totuutta ja esteet nousevat esiin.

## Miten palaset liittyvät toisiinsa

- **Issue** = tarina tai tehtävä (`#1`). Luodaan Issues-välilehdellä.
- **Product Backlog** = Project-näkymä: issuet ilman Sprint-arvoa.
- **Sprint 1 … 6** = Project-näkymät: issuet, joiden kenttä **Sprint** on kyseinen viikko.
- **Status** (Todo / In Progress / Done) = onko työ käynnissä, ei mikä viikko.

Siirto backlogista sprinttiin: Product Backlog → sarake **Sprint**. Ei milestonea, ei sprint-labelia.

## Taulun sarakkeet (Board)

Board ryhmittelee **Statuksen** mukaan:

| Sarake | Mitä siinä on |
| --- | --- |
| **Todo** | Sprintissä, ei vielä aloitettu |
| **In Progress** | Työn alla |
| **Done** | Definition of Done täyttyy |

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

## Sprint 2 — työnjako (versionhallintaharjoitus)

Kurssin vaatimus: jokainen tiimiläinen ohjelmoi ja tekee committeja. Sprint 2 on siksi jaettu viiteen issueen:

| Paketti | Issue | Sisältö |
| --- | --- | --- |
| 1/5 | #13 | Tietokantakaavio + dokumentaation luku Tietokanta |
| 2/5 | #14 | Entityt ja repositoryt: Rooli, Kayttaja |
| 3/5 | #15 | Entityt ja repositoryt: Tapahtuma, Lipputyyppi |
| 4/5 | #16 | Entityt ja repositoryt: Myyntitapahtuma, Lippu |
| 5/5 | #17 | Testdata H2:een + repository-testit |

Kun tiimiin tulee lisää GitHub-käyttäjiä, seuraavat vastaavat paketit assigneeitaan heille. Tässä sprintissä commitit on tehty paketeittain, jotta historia näyttää jaon.
