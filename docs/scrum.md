# Scrum GitHubissa

Kurssilla käytetään Scrumia. GitHubissa se toteutetaan kolmella asialla:

1. **Issues** = tuotteen työjonon (Product Backlog) kohteet, yleensä käyttäjätarinat
2. **Milestones** = sprintit (Sprint 1, Sprint 2, Sprint 3)
3. **GitHub Project** = Scrum-taulu, jossa kortit liikkuvat sarakkeesta toiseen

Tämä tiedosto kertoo, miten taulu ja työjono pidetään ajan tasalla, ja miten uuden tiimin jäsen luo saman rakenteen tarvittaessa uudelleen.

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

## Näin luot Scrum-taulun GitHubiin (käsin)

Tee tämä **kerran** repositorion luonnin jälkeen. Alla klikkailtava tapa; CLI-vaihtoehto on tiedoston lopussa.

### A. Product Backlog = Issues

1. Avaa repositorio GitHubissa.
2. **Issues** → **Labels** → luo tarrat esimerkiksi:
   - `user-story` (käyttäjätarina)
   - `task` (tekninen tehtävä, esim. ympäristö)
   - `documentation`
   - `bug`
   - `priority:high` / `priority:medium` / `priority:low`
3. **Issues** → **Milestones** → **New milestone**:
   - Sprint 1
   - Sprint 2
   - Sprint 3
4. **Issues** → **New issue** jokaiselle käyttäjätarinalle (M1, TK2, …).
   - Otsikko: `M4 Myyjänä haluan valita useita lippuja samaan myyntiin`
   - Leipäteksti: hyväksymiskriteerit (Given / When / Then)
   - Label: `user-story`
   - Milestone: vasta Sprint Planningin jälkeen

**Tuotteen työjonon linkki palautukseen:**  
`https://github.com/<omistaja>/<repo>/issues`

Voit myös luoda tallennetun haun: `is:issue is:open label:user-story`.

### B. Scrum-taulu = GitHub Project

1. Repositoriossa **Projects** → **New project**.
2. Valitse malli **Board** (Kanban).
3. Nimeä esim. `TicketGuru Scrum`.
4. Muokkaa sarakkeet: Backlog, Sprintissä, Doing, Review, Done.
5. **Settings** (projektissa) → **Custom fields** → lisää:
   - `Sprint` tyyppiä Single select: Sprint 1, Sprint 2, Sprint 3
   - valinnainen `Story points` tyyppiä Number
6. **Add item** → **Add from repository** (tai raahaa Issues). Kaikki backlog-issuet tauluun.
7. Liitä projekti repositorioon: Project → **...** → **Settings** → **Linked repositories**.

**Scrum-taulun linkki palautukseen:** projektin URL, muotoa  
`https://github.com/users/<kayttaja>/projects/<numero>`

### C. Näkymät viikoittaisille sprinteille

Samassa Projectissa voi olla useita näkymiä (Views):

| Näkymä | Suodatin | Käyttö |
| --- | --- | --- |
| Product Backlog | Status = Backlog, ryhmitelty prioriteetin mukaan | PO ja Sprint Planning |
| Sprint 1 | Sprint = Sprint 1, Board | Nykyisen sprintin taulu |
| Sprint 2 | Sprint = Sprint 2 | Seuraava sprintti |
| All issues | Table, kentät Status + Sprint + Assignee | Kokonaiskuva |

Viikoittainen työ: Daily Scrumissa katsotaan **nykyisen sprintin Board-näkymää**, ei koko backlogia.

## Sprintin rytmi tässä kurssissa

1. **Sprint Planning (sprintin 1. session alku)**  
   Valitaan tarinat Product Backlogista. Asetetaan milestone (esim. Sprint 1), siirretään kortit sarakkeeseen *Sprintissä*, merkitään vastuuhenkilö (assignee).
2. **Daily Scrum** (lyhyt, esim. 10 min lähi- tai etäpalaveri)  
   Mitä tein, mitä teen, mikä estää. Kortti *Doing*-sarakkeeseen kun työ alkaa.
3. **Kehitys**  
   Feature-haara → PR → *Review* → merge → *Done*.
4. **Sprint Review**  
   Demo toimivasta incrementistä. Valmiit issuet jäävät Doneen; kesken jääneet palaavat Backlogiin (älä merkitse valmiiksi).
5. **Retrospective**  
   Mitä parannetaan prosessissa. Kirjaa 1–3 toimenpidettä vaikka uudeksi `task`-issueksi.

## Käyttäjätarinan issue-pohja

Kopioi uuteen issueen:

```markdown
## Käyttäjätarina
**Roolina** haluan ..., jotta ...

## Hyväksymiskriteerit
- [ ] Given ... When ... Then ...
- [ ] Given ... When ... Then ...

## Teknisiä huomioita
-

## Sprint
Sprint Planningissa: milestone + taulun sarake
```

## CLI: taulun luonti `gh`-komennolla

Tokenissa tarvitaan scope `project` (`gh auth refresh -s project,read:project`).

```bash
# Projekti
gh project create --owner @me --title "TicketGuru Scrum"

# Kenttä sprinteille (korvaa NUMERO create-komennon numerolla)
gh project field-create NUMERO --owner @me --name "Sprint" \
  --data-type SINGLE_SELECT \
  --single-select-options "Sprint 1,Sprint 2,Sprint 3"

# Liitä repo
gh project link NUMERO --owner @me --repo ohjelmistoprojekti1

# Lisää olemassa oleva issue tauluun
gh project item-add NUMERO --owner @me --url https://github.com/OWNER/REPO/issues/1
```

Issues ja milestones:

```bash
gh label create user-story --description "Käyttäjätarina" --color 0E8A16
gh api repos/OWNER/REPO/milestones -f title="Sprint 1"
gh issue create --title "M2 Myyjänä haluan nähdä tulevat tapahtumat" \
  --label user-story --body "..."
```

## Linkit palautukseen

Pidä README.md:n taulukko ajan tasalla:

- GitHub-repositorio
- Issues (Product Backlog)
- Project (Scrum-taulu)
- Tämä dokumentti ja [dokumentaatio](dokumentaatio.md)
