# Kehitysympäristö

Tämä ohje on tarkoitettu kaikille tiimin jäsenille. Kun nämä askeleet on tehty, jokainen pystyy kloonaamaan repositorion, käynnistämään Spring Boot -backendin ja ajamaan testit.

## 1. Asennettavat työkalut

| Työkalu | Versio | Huomio |
| --- | --- | --- |
| JDK | **25** (LTS) | Temurin, Zulu tai Oracle. Tarkista: `java -version`. Spring Boot 4.1 tukee Java 17–26. |
| Git | uusin | `git --version` |
| GitHub-tili | — | Pyydä kutsu repositorion Collaborators-listalle |
| IDE | IntelliJ IDEA / VS Code / Cursor / Eclipse | Spring Boot -tuki suositeltava |

Mavenia **ei tarvitse asentaa**: projektissa on Maven Wrapper (`backend/mvnw`).

macOS, SDKMAN (suositus tälle tiimille):

```bash
sdk install java 25.0.4-tem
sdk use java 25.0.4-tem
java -version
```

macOS, Homebrew:

```bash
brew install git openjdk@25
```

Aseta sitten `JAVA_HOME` osoittamaan JDK 25:een (Homebrew ei vaihda oletus-Javaa automaattisesti).

VS Code / Cursor -laajennukset: Extension Pack for Java (Language Support, Debugger, Maven, Test Runner).

## 2. Repositorion kloonaus

```bash
git clone git@github.com:petlappa/ohjelmistoprojekti1.git
cd ohjelmistoprojekti1
```

HTTPS, jos SSH-avaimia ei ole:

```bash
git clone https://github.com/petlappa/ohjelmistoprojekti1.git
```

## 3. Backendin käynnistys

```bash
cd backend
./mvnw spring-boot:run
```

Windows PowerShell / cmd: `mvnw.cmd spring-boot:run`

Ensimmäisellä ajolla Maven lataa riippuvuudet. Onnistunut käynnistys näkyy lokissa: Tomcat portissa **8080**.

Tarkista selaimella tai terminaalista:

```bash
curl http://localhost:8080/api/health
```

Odotettu vastaus: `{"status":"UP","application":"TicketGuru"}`.

### H2-konsoli

1. Avaa http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:ticketguru`
3. User: `sa`
4. Password: (tyhjä)
5. Connect

H2 on muistissa: tiedot katoavat, kun sovellus sammutetaan. Tämä on tarkoituksellista Sprint 1:ssä.

## 4. Testit

```bash
cd backend
./mvnw test
```

CI ajaa samat testit jokaisesta pushista GitHub Actionsissa (`.github/workflows/backend-ci.yml`).

## 5. Työskentely Gitissä

- `main` on aina käynnistyvä versio.
- Oma työ omalle haaralle: `git checkout -b feature/tapahtumat-api`
- Pienet, selkeät commitit.
- Yhdistä `main`-haaraan **pull requestilla**, ei suoraan.
- Älä commitoi salasanoja, `.env`-tiedostoja tai `application-local.properties`-tiedostoa.

```bash
git checkout main
git pull
git checkout -b feature/lyhyt-kuvaus
# ... muutokset ...
git add .
git commit -m "Lisää tapahtumien listausrajapinta."
git push -u origin HEAD
```

Avaa sen jälkeen GitHubissa Pull request.

## 6. IntelliJ / VS Code

**IntelliJ:** File → Open → valitse `backend`-kansio (tai koko repo). Odota, että Maven importoi `pom.xml`. Run → `TicketGuruApplication`.

**VS Code / Cursor:** avaa repo. Kun Java-laajennus kysyy, valitse JDK 25. Käynnistä `TicketGuruApplication` tai käytä terminaalia (`./mvnw spring-boot:run`).

## 7. Yleisiä ongelmia

| Oire | Ratkaisu |
| --- | --- |
| `Unsupported class file major version` | Käytössä on väärä JDK. Aseta JDK 25 (`java -version`). |
| Portti 8080 varattu | Sammuta toinen Spring-sovellus tai vaihda `server.port` tilapäisesti. |
| `mvnw: Permission denied` (macOS/Linux) | `chmod +x backend/mvnw` |
| H2-konsoli ei avaudu | Varmista, että sovellus on käynnissä ja URL on `/h2-console`. |
| GitHub-push hylätään | Pyydä repo-oikeudet (Write) repositorion omistajalta. |

## 8. Tiimin jäsenen lisääminen GitHubiin

Repositorion omistaja:

1. GitHub → repo → **Settings** → **Collaborators** → **Add people**
2. Lisää tiimiläisten GitHub-käyttäjänimet
3. Jaa heille myös kutsu **GitHub Projectiin** (Scrum-taulu), jos Projects-oikeus ei seuraa automaattisesti
