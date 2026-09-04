# Scrum GitHub Projectsilla

Ohje Ohjelmistoprojekti 1 -tiimeille. Näin rakennat **product backlogin** ja **sprinttitaulun** GitHubiin samalla mallilla, jota kurssilla käytetään.

Aika: noin 15–20 minuuttia. Tarvitset tiimin GitHub-repositorion ja oikeuden luoda Project.

GitHubin käyttöliittymä on yleensä englanniksi. Painikkeiden nimet on merkitty **lihavoituna**.

---

## 1. Miksi Project, ei pelkkä Issues-lista?

| Scrum | GitHubissa |
| --- | --- |
| Käyttäjätarina / tehtävä | **Issue** |
| Product Backlog | Projectin **Table**-näkymä |
| Sprintin Scrum-taulu | Projectin **Board**-näkymä, suodatin `iteration:@current` |
| Sprintit | Projectin **Iteration**-kenttä (nimellä Sprint) |

**Issues** on työjonon rivi (tarina, tehtävä, bugi).  
**Project** on taulu, jolla nuo rivit järjestetään sprintteihin ja sarakkeisiin.

Pelkkä Issues-välilehti ei ole Scrum-taulu. Pelkkä Project ilman Issueja on tyhjä taulu. Molemmat tarvitaan.

Älä käytä **Milestones**-toimintoa sprinteiksi, jos käytät Iteration-kenttää. Milestone sopii korkeintaan palautuspäivään, ei Daily Scrumin tauluun.

---

## 2. Tavoitetila

Kun ohje on tehty, tiimillä on **yksi** GitHub Project, jossa on:

1. Kenttä **Sprint** (tyyppi Iteration, **1 viikko**): Sprint 1, Sprint 2, …
2. Kenttä **Priority**: High, Medium, Low
3. Kenttä **Estimate**: numero (esim. story pointit)
4. Näkymä **Product Backlog** (taulukko): issuet, joita ei ole vielä merkitty mihinkään sprinttiin
5. Näkymät **Current sprint** sekä **Sprint 1 … Sprint 6** (kanban): yhden viikon kortit

Esimerkki (julkinen):

- Product Backlog: https://github.com/users/petlappa/projects/1/views/1
- Current sprint: https://github.com/users/petlappa/projects/1/views/2

---

## 3. Luo Project

Voit luoda Projectin repositoriosta tai omasta profiilista. Repositoriosta luominen liittää taulun automaattisesti repoon.

### 3.1 Repositoriosta (suositus)

1. Avaa tiimin repositorio GitHubissa.
2. Yläreunan välilehti **Projects**.
3. **New project**.
4. Kohdasta *Start from scratch* valitse **Board** (tai **Table** — näkymän voi vaihtaa myöhemmin).
5. Nimeksi esim. `TicketGuru Scrum` tai `[TiiminNimi] Scrum`.
6. **Create project**.

### 3.2 Profiilista (jos Projects-välilehteä ei näy repossasi)

1. Klikkaa GitHubissa profiilikuvaa → **Your profile**.
2. Välilehti **Projects** → **New project**.
3. Sama valinta ja nimi kuin yllä.
4. Liitä repo myöhemmin: Project → oikean yläkulman **⋯** → **Settings** → **Linked repositories** → lisää tiimin repo.

### 3.3 Kuvaus ja näkyvyys

Kurssipalautuksessa opettajan pitää päästä linkillä sisään.

1. Projectissa oikean yläkulman **⋯** → **Settings**.
2. *Add a description*: esim. `Product backlog ja sprinttitaulu`.
3. **Visibility** → **Public** (tai pidä Private ja lisää opettaja sekä kaikki tiimiläiset **Manage access** -kohdasta).
4. Tallenna.

Julkinen Project ei tee repositoriosta julkista. Vain taulu näkyy linkillä.

---

## 4. Kentät (custom fields)

Avaa Project **Table**-asettelussa (helpoin kenttien luontiin). Jos näet kanbanin: näkymän **View**-valikko (suodattimen vieressä) → **Layout** → **Table**.

Kentät luodaan taulukon oikean reunan otsikkoriviltä: **+** → **New field**.

### 4.1 Sprint (Iteration)

Tämä on sprintti. Älä tee sprinttejä Status-sarakkeiksi.

1. **+** → **New field**.
2. Nimi: `Sprint`.
3. Tyyppi: **Iteration**.
4. Kesto: **7** **days** (yhden viikon sprintti).
5. Aloituspäivä: sprintin 1. päivä (esim. viikon maanantai tai Sprint Planningin päivä).
6. **Save**.

GitHub luo iteraatioita automaattisesti. Nimeä ne:

1. **⋯** → **Settings** → kenttä **Sprint** (tai taulukon Sprint-sarakkeen valikko → kentän asetukset).
2. Muuta otsikoiksi `Sprint 1`, `Sprint 2`, `Sprint 3`, … (yksi viikko per sprintti).
3. Tarkista päivämäärät. Lisää iteraatioita, jotta koko kurssi mahtuu (esim. 6 × 1 vk).

`iteration:@current` tarkoittaa sitä iteraatiota, jonka aikaväliin kuluva päivä osuu. Pidä päivämäärät ajan tasalla, muuten Current sprint -näkymä on tyhjä.

### 4.2 Priority

1. **+** → **New field**.
2. Nimi: `Priority`.
3. Tyyppi: **Single select**.
4. Options: `High`, `Medium`, `Low` (**Add option** kahdesti).
5. **Save**.

### 4.3 Estimate

1. **+** → **New field**.
2. Nimi: `Estimate`.
3. Tyyppi: **Number**.
4. **Save**.

Sprint Planningissa merkitään tähän esim. 1, 2, 3, 5, 8.

### 4.4 Status (valmis kenttä)

Projectissa on valmiina **Status**: tyypillisesti *Todo*, *In Progress*, *Done*. Nämä riittävät.

| Status | Merkitys |
| --- | --- |
| Todo | Ei vielä työn alla (backlogissa tai sprintissä odottamassa) |
| In Progress | Joku tekee nyt |
| Done | Definition of Done täyttyy |

Halutessasi lisää Status-kenttään vaihtoehto `Review` (PR auki / odottaa demoa): Status-sarakkeen otsikko → kentän asetukset → **Add option**.

Sprinttiin kuuluminen **ei** ole Status. Se merkitään kenttään **Sprint**.

---

## 5. Näkymät (välilehdet)

Näkymät ovat saman Projectin eri suodattimia. Kortti on olemassa kerran; se näkyy niissä näkymissä, joihin suodatin osuu. Sprintti merkitään **Sprint**-sarakkeessa, ei Issuen labelissa eikä milestonessa.

Tallenna aina muutokset: **View** → **Save changes**.

| Välilehti | Layout | Suodatin | Käyttö |
| --- | --- | --- | --- |
| Product Backlog | Table | `-status:Done no:Sprint` | Kaikki, joita ei ole otettu sprinttiin |
| Current sprint | Board | `Sprint:@current` | Daily: tämä viikko automaattisesti |
| Sprint 1 … Sprint 6 | Board | `Sprint:"Sprint 1"` jne. | Yhden viikon lista |

Siirto: Product Backlog → klikkaa **Sprint**-solua → Sprint 2. Kortti katoaa backlogin ja ilmestyy Sprint 2 -välilehdelle.

### 5.1 Product Backlog (Table)

1. **View** → **Layout** → **Table**.
2. **View** → **Rename view** → `Product Backlog`.
3. Suodatin:

   ```text
   -status:Done no:Sprint
   ```

4. Näytä sarakkeet: Title, Status, Sprint, Priority, Estimate, Assignees, Labels.
5. **View** → **Save changes**.

Palautuslinkki: `.../projects/<n>/views/1`

### 5.2 Current sprint (Board)

1. **New view** → **Layout** → **Board**.
2. Nimi: `Current sprint`.
3. Suodatin: `Sprint:@current`
4. **Save changes**.

Daily Scrumissa avaa tämä välilehti.

### 5.3 Sprint 1–6 (Board)

Jokaiselle viikolle oma välilehti:

1. **New view** → Board.
2. Nimi: `Sprint 2`.
3. Suodatin: `Sprint:"Sprint 2"` (lainausmerkit mukaan, koska nimessä on välilyönti).
   Kentän nimi on **Sprint**, ei `iteration`.
4. **Save changes**.
5. Toista Sprint 1, 3, 4, …

---

## 6. Issues = backlog-kohteet

Jokainen käyttäjätarina on Issue. Draft-kortti Projectissa (pelkkä otsikko taululla) ei ole hyvä palautukseen: siihen ei saa numeroa, assigneeja eikä PR-linkkiä yhtä siististi.

### 6.1 Tarrat (Labels)

Repositorio → **Issues** → **Labels** → **New label**:

| Label | Käyttö |
| --- | --- |
| `user-story` | Käyttäjätarina |
| `task` | Tekninen tehtävä (ympäristö, CI, refaktorointi) |
| `documentation` | Dokumentaatio |
| `bug` | Bugi |

### 6.2 Luo käyttäjätarina

1. Repositorio → **Issues** → **New issue**.
2. Otsikko: `M4 Myyjänä haluan valita useita lippuja samaan myyntiin`
3. Leipäteksti:

   ```markdown
   ## Käyttäjätarina
   **Myyjänä** haluan valita yhteen myyntiin useita lippuja, jotta asiakas voi ostaa kerralla esim. kaksi aikuista ja yhden lapsen.

   ## Hyväksymiskriteerit
   - [ ] Given tapahtuma on valittu When asetan kappalemäärät Then ne kuuluvat samaan myyntitapahtumaan
   ```

4. Label: `user-story`.
5. **älä** aseta vielä Sprinttiä / Iteraatiota — se tehdään Planningissa Projectissa.
6. **Submit new issue**.

### 6.3 Lisää Issue Projectiin

Tavat, helpoin ensin:

**A. Issuesta**

1. Avaa Issue.
2. Oikea palsta **Projects** → valitse tiimin Project.
3. Status ja Sprint voi asettaa samasta palstasta, jos kentät näkyvät.

**B. Projectin Table-näkymästä**

1. Alin rivi, kohta **+**.
2. Liitä Issuen URL ja paina Enter.

**C. Automaatio (suositus, kerran)**

1. Project → **⋯** → **Workflows**.
2. **Auto-add to project** → **Edit**.
3. Valitse tiimin repositorio.
4. Filteriksi esim. `is:issue`.
5. **Save and turn on workflow**.

Toinen workflow: **Item added to project** → aseta Status = Todo.

---

## 7. Miten tiimi käyttää taulua

### Sprint Planning

1. Avaa **Product Backlog**.
2. Product Owner / tiimi valitsee tarinat Sprinttiin.
3. Aseta kullekin valitulle riville **Sprint** = `Sprint 1` (tai nykyinen viikko).
4. Aseta **Estimate** ja **Assignee**.
5. Status jää *Todo*-tilaan, kunnes työ alkaa.

Kortti katoaa backlogin ja ilmestyy **Sprint n** -välilehdelle sekä **Current sprint** -näkymään, jos viikko on käynnissä.

### Daily Scrum

1. Avaa **Current sprint**.
2. Siirrä kortti *In Progress* -sarakkeeseen, kun työ alkaa (yksi kortti per henkilö mieluiten).
3. Kun PR on auki: Status *Review* (jos loitte sen) tai kommentti issueen.
4. Kun tarina on valmis: *Done* **ja** Issue suljetaan.

### Sprint Review

- *Done* = voidaan demota. Definition of Done: koodi `main`-haarassa, testit vihreät, dokumentaatio ajan tasalla.
- Kesken jäänyt: Sprint-kenttä → seuraava sprintti (älä merkitse Doneksi). Table-näkymässä voit ryhmitellä Sprintin mukaan ja siirtää ryhmän: ryhmän otsikko → **Move items to...**.

### Retrospective

Kirjaa 1–3 parannusta uusina `task`-issueina ja lisää ne backlogin.

---

## 8. Definition of Done (alustava)

Issue on Done vasta kun:

- muutos on `main`-haarassa (PR yhdistetty)
- testit menevät läpi
- dokumentaatio on päivitetty, jos muutos vaikuttaa käyttöön tai määrittelyyn
- tiimi voi esitellä muutoksen Review’ssa

---

## 9. Palautuslinkit (laita README.md:hen)

```markdown
| Artefakti | Linkki |
| --- | --- |
| GitHub-repositorio | https://github.com/<omistaja>/<repo> |
| Tuotteen työjono | https://github.com/users/<kayttaja>/projects/<n>/views/1 |
| Scrum-taulu | https://github.com/users/<kayttaja>/projects/<n>/views/2 |
```

Tarkista ennen palautusta:

- [ ] Project on **Public** tai opettaja on lisätty
- [ ] Product Backlog näyttää vain sprinttiin kuulumattomat avoimet tarinat
- [ ] Current sprint näyttää tämän viikon; Sprint 2 -välilehti vain Sprint 2:n kortit
- [ ] Sprint-kentän päivämäärät osuvat kuluvaan viikkoon (`@current`)

---

## 10. Tyypilliset virheet

| Oire | Syy | Korjaus |
| --- | --- | --- |
| Current sprint on tyhjä | Kortilla ei ole Sprint-arvoa, tai iteraation päivämäärät eivät kata tänään | Aseta Sprint; korjaa iteraation start/duration |
| Backlogissa näkyy valmiit työt | Suodatin puuttuu | `-status:Done` |
| Opettaja saa 404 | Project on Private | Visibility → Public tai Manage access |
| Tiimiläinen ei näe sarakkeita | Näkymää ei ole tallennettu | **View** → **Save changes** |
| Sprintit ovat Issues-milestonet | Vanha malli | Käytä Iteration-kenttää; milestoneja ei tarvita |
| Draft-kortteja ilman numeroa | Lisätty vain Projectiin | Luo Issue ja liitä se Projectiin |

---

## 11. Git-työ ja taulu

Taulu ei korvaa GitHub-flow’ta:

1. Issue auki, Assignee sinä, Status = In Progress.
2. `git checkout -b feature/lyhyt-kuvaus`
3. Pull request, joka viittaa issueen (`Closes #12`).
4. Merge `main`-haaraan → Status = Done.

---

## 12. Pika-muistilista (tarkistuslista)

1. [ ] New project (Board tai Table), nimi `… Scrum`
2. [ ] Public (tai opettaja + tiimi access)
3. [ ] Linked repositories = tiimin repo
4. [ ] Field **Sprint** = Iteration, **7 days**, nimet Sprint 1, Sprint 2, …
5. [ ] Field **Priority** = High / Medium / Low
6. [ ] Field **Estimate** = Number
7. [ ] Näkymä **Product Backlog**: Table, suodatin `-status:Done no:Sprint`
8. [ ] Näkymä **Current sprint**: Board, `Sprint:@current`
8b. [ ] Näkymät **Sprint 1–6**: Board, `Sprint:"Sprint n"`
9. [ ] Molemmat näkymät **Save changes**
10. [ ] Workflow: Auto-add `is:issue` tiimin reposta
11. [ ] Käyttäjätarinat Issueina (`user-story`), ei pelkkinä draft-kortteina
12. [ ] README:n kaksi URL:a (views/1 ja views/2)
