# Julkaiseminen: Render ja Rahti

Yleinen luento-ohje Ohjelmistoprojekti 1 -opiskelijoille. Päivitetty **2026-10-06**. 

Ohje julkaisee tämän repositorion Spring Boot -backendin (Java 25, hakemisto `backend/`). Vaihda palvelun nimi, Docker Hub -tunnus, API-polut ja tietokannan tunnukset oman tiimin mukaisiksi. Kevään ohjeen käyttäjät (`cashier`, `organizer`, …) olivat yhden demon tunnuksia.

Kaksi paikkaa, joihin sovelluksen voi viedä:

- **Render** rakentaa imagen GitHub-repositoriosta. Et koske Kubernetesiin.
- **Rahti** (CSC, [rahti.csc.fi](https://rahti.csc.fi)) on korkeakoulujen OKD-ympäristö. Lokakuussa 2026 alusta on OKD 4.22 / Kubernetes 1.35. Sinne viedään valmis Docker-image.

Molemmat antavat HTTPS-osoitteen. Valitse yksi sovellukselle. Tietokanta voi olla samassa paikassa tai, kun datan pitää säilyä kurssin loppuun, CSC:n Pukissa.

## Kumpi riittää kurssiin

Ilmaisen tietokannan kesto ratkaisee valinnan. Sovelluksen itsensä saa molempiin.

| Tavoite | Minne |
| --- | --- |
| Demo verkkoon tänään. Data saa kadota. | Render, oletusprofiili (H2) |
| Pysyvä kanta noin kuukaudeksi | Renderin ilmainen PostgreSQL. Vanhenee 30 päivää luonnista. |
| Kanta vähintään kurssin loppuun (3 kk) | Rahti + [Pukki](https://pukki.dbaas.csc.fi). Kesto on CSC-projektin kesto. Course-projekti kestää enintään kuusi kuukautta. |

Renderin ilmainen PostgreSQL on vanhentunut 30 päivässä jo 20.5.2024 alkaen. Käyttö ei pidennä aikaa. Vanhentumisen jälkeen kanta ei aukea. Armonaikaa on 14 päivää, jos nostat kannan maksulliseksi. Sen jälkeen Render poistaa datan. Yksi ilmainen Postgres per työtila.

Rahti ja Pukki pysyvät pystyssä niin kauan kuin CSC-projekti on auki. Rahti ei varmuuskopioi levyä. Pukki tekee päivittäisen varmuuskopion, jota säilytetään 90 päivää.

## Mitä koodiin lisätään ensin

Nämä koskevat sekä Renderiä että Rahtia. Ilman niitä jompikumpi alusta käynnistää kontin, joka ei kuuntele oikeaa porttia tai ei löydä PostgreSQL-ajuria.

### 1. Portti

Render asettaa ympäristömuuttujan `PORT` (oletus 10000) ja ohjaa liikenteen siihen. Rahti ei aseta `PORT`-muuttujaa, joten oletukseksi jää 8080.

Muuta `backend/src/main/resources/application.properties`:

```properties
server.port=${PORT:8080}
```

Jätä H2-asetukset tähän tiedostoon. Ne ovat paikallinen ja Renderin H2-demo. Älä laita tähän PostgreSQL-osoitetta.

### 2. PostgreSQL-ajuri

Lisää `backend/pom.xml`-tiedoston riippuvuuksiin:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 3. Tuotantoprofiili

Luo `backend/src/main/resources/application-prod.properties`:

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.h2.console.enabled=false
```

Spring Boot lukee profiilin, kun ympäristömuuttuja `SPRING_PROFILES_ACTIVE` on `prod`. Ilman sitä sovellus jää H2-muistiin. H2-konsoli suljetaan prod-profiilissa, koska oletustiedosto kytkee sen päälle kehitystä varten.

Muuttujien nimet ovat esimerkki. Samojen nimien pitää esiintyä sekä tässä tiedostossa että alustan ympäristömuuttujissa.

### 4. Dockerfile

Luo `backend/Dockerfile`. Imagessa on Java 25, sama kuin `pom.xml`-tiedoston `java.version`. Renderin kielilistassa ei ole Javaa, joten Docker on se polku, jolla Spring Boot julkaistaan.

```dockerfile
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chgrp -R 0 /app && chmod -R g=u /app
USER 1001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

`USER 1001` ja ryhmän 0 oikeudet tarvitaan Rahdissa: konttia ei saa ajaa rootina. Renderissä sama image käy.

`target/*.jar` poimii Spring Bootin paketoidun jarin. Jos hakemistoon jää kaksi `*.jar`-tiedostoa, build katkeaa. Silloin vaihda riviksi oman artefaktin nimi, tässä repositoriossa `ticketguru-0.0.1-SNAPSHOT.jar`.

Luo `backend/.dockerignore`:

```text
target/
```

Kokeile konetta ennen pilveä:

```bash
cd backend
docker build -t ticketguru:latest .
docker run --rm -p 8080:8080 ticketguru:latest
```

Selain: `http://localhost:8080/api/health`. Jos health-polkua ei ole, kokeile tiimin omaa GET-polkua, esimerkiksi `/api/events`.

## Vaihtoehto 1: Render

Ilmainen työtila riittää demoon. Palvelun compute-suunnitelmaksi valitaan erikseen **Free**. Työtilan Hobby-paketti ei poista Free-instanssin rajoja.

### Web Service

1. Koodi on GitHubissa. Tässä repositoriossa Dockerfile on hakemistossa `backend/`, ei juuressa.
2. [dashboard.render.com](https://dashboard.render.com) → **New** → **Web Service**.
3. **Build and deploy from a Git repository**. Yhdistä GitHub ja valitse repo sekä haara `main`.
4. **Root Directory:** `backend`.
5. Runtime: **Docker**. Dockerfile Path: `Dockerfile`. Build Command ja Start Command jätetään tyhjiksi. Käynnistys on Dockerfilen `ENTRYPOINT`.
6. Compute: **Free**.
7. Region: lähin tarjolla oleva (Euroopassa tyypillisesti Frankfurt). Tietokanta luodaan myöhemmin samaan alueeseen.
8. **Deploy**.

Jos lomakkeessa ei ole Root Directory -kenttää: Dockerfile Path `backend/Dockerfile` ja Docker Build Context `backend`.

### GitHubista Renderiin

Julkaisu ei kulje GitHub Actionsin kautta. Repositorion workflow `.github/workflows/backend-ci.yml` ajaa vain testit pushissa ja pull requestissa. Se ei lähetä imagea Renderiin.

Renderin oma deploy käynnistyy, kun palvelu on kerran yhdistetty GitHub-repoon. Yhteys tehdään ensimmäisellä kerralla dashboardissa kohdassa **Build and deploy from a Git repository**. Render pyytää luvan GitHub-tiliin. Sen jälkeen valittu haara (tässä `main`) on oletuksena **Auto-Deploy**. Uusi push siihen haaraan rakentaa imagen `backend/Dockerfile`-tiedostosta ja käynnistää palvelun uudelleen.

**Manual Deploy** tarvitaan, kun ympäristömuuttujia muutetaan ilman uutta committia, tai jos Auto-Deploy on kytketty pois palvelun asetuksista. Palvelua ei voi yhdistää, ennen kuin Dockerfile ja `prod`-profiili ovat siinä haarassa, jonka Render lukee.

Kun palvelu on Live, osoite on muotoa `https://<nimi>.onrender.com`.

| Rajoite | Mitä se tarkoittaa kurssilla |
| --- | --- |
| Nukahtaminen | 15 minuuttia ilman liikennettä ja ilmainen web-palvelu sammuu. Seuraava pyyntö herättää sen noin minuutissa. H2-data katoaa sammumiseen. |
| 750 instanssituntia / kalenterikuukausi / työtila | Vain hereillä oleva aika kuluu. Nukkuva palvelu ei kuluta tunteja. Tunnit nollautuvat kuun vaihteessa. Jos tunnit loppuvat, ilmainen web-palvelu keskeytyy loppukuuksi. |
| 512 Mt muistia | Jos loki näyttää `OutOfMemoryError`, lisää ympäristömuuttuja `JAVA_TOOL_OPTIONS` arvoksi `-XX:MaxRAMPercentage=70.0` ja deployaa uudelleen. |
| Levy | Ilmaisen web-palvelun levy on väliaikainen. Siihen kirjoitettu tiedosto katoaa sammumiseen ja uuteen deployhin. |

### H2 Renderissä

Älä aseta `SPRING_PROFILES_ACTIVE`. Sovellus käyttää `application.properties` -tiedoston H2-muistitietokantaa.

Data katoaa, kun palvelu nukahtaa, käynnistyy uudelleen tai deployataan. Tämä riittää sen tarkistamiseen, että image nousee ja API vastaa.

### PostgreSQL Renderissä

1. **New** → **PostgreSQL**. Nimi esimerkiksi `ticketguru-db`. Compute **Free**. Sama region kuin web-palvelulla.
2. Avaa kannan **Connect** / **Info**. Ota **Internal Database URL**, jos web-palvelu on samassa regionissa. Ilmainen web-palvelu saa ottaa yhteyden saman regionin tietovarastoon sisäverkossa.
3. Jaa URL osiin. Älä kopioi `jdbc:`- tai `postgresql://`-etuliitettä `DB_HOST`-kenttään.

Esimerkki sisäisestä URLista `postgresql://tguser:salasana@dpg-abc-a/ticketguru`:

| Key | Arvo |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_HOST` | `dpg-abc-a` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `ticketguru` |
| `DB_USERNAME` | `tguser` |
| `DB_PASSWORD` | kannan salasana |

4. Web Service → **Environment** → lisää rivit → **Manual Deploy**.
5. Lokissa profiili on `prod` ja JDBC-osoite alkaa `jdbc:postgresql://`. H2-osoite `jdbc:h2:mem:` tarkoittaa, että profiili ei ole päällä.

Ulkoinen host (muotoa `dpg-abc-a.frankfurt-postgres.render.com`) toimii myös. Sisäinen on se, jota käytetään, kun sovellus ja kanta ovat samassa Render-regionissa.

#### Kun 30 päivää täyttyy

Ota dumppi **ennen** vanhentumispäivää omalta koneelta. Vanhentuneeseen kantaan ei pääse.

```bash
pg_dump "postgresql://KAYTTAJA:SALASANA@ULKOINEN_HOST:5432/TIETOKANTA" --no-owner --no-acl -f ticketguru.sql
```

Ulkoinen host on Connect-näkymän External Database URL. Sisäinen host ei vastaa omalta koneelta.

Vanhentumisen jälkeen luo uusi ilmainen Postgres (yksi per työtila). Palauta data tai aja tiimin oma siemenaineisto uudestaan:

```bash
psql "postgresql://KAYTTAJA:SALASANA@UUSI_HOST:5432/TIETOKANTA" -f ticketguru.sql
```

Uusi instanssi saa uuden hostin. Päivitä `DB_HOST`, `DB_USERNAME` ja `DB_PASSWORD`, ja deployaa web-palvelu uudelleen. Kaksi tällaista uusintaa kattaa lukukauden, jos dumppi otetaan ajoissa. Data ei siirry itsestään.

Jos työtilassa on jo yksi ilmainen Postgres toisesta projektista, toista ei voi luoda ennen kuin ensimmäinen on vanhentunut.

### Kokeile

```bash
curl -sS "https://<nimi>.onrender.com/api/events"
```

Ensimmäinen kutsu nukkuneeseen palveluun voi kestää noin minuutin. Kun tiimi on kytkenyt autentikoinnin, lisää sama otsikko kuin lokaalissa kokeilussa (Basic tai `Authorization: Bearer …`). Tunnukset ovat tiimin omat. Luennon esimerkki, kun Security on toteutettu, on käyttäjä `myyja` ja salasana `salasana` ([autentikointi-ja-auktorisointi.md](autentikointi-ja-auktorisointi.md)).

### Pikaohje, Render

1. `server.port=${PORT:8080}`, PostgreSQL-ajuri, `application-prod.properties`, `backend/Dockerfile`.
2. New → Web Service → repo, haara `main`, Root Directory `backend`, runtime Docker, compute Free.
3. Deploy, kunnes tila on Live.
4. Jos kanta saa elää vain kuukauden: New → PostgreSQL, compute Free, sama region. Ympäristöön `SPRING_PROFILES_ACTIVE=prod` ja `DB_*` sisäisen URLin osista. Redeploy.
5. Jos kannan pitää elää kurssin loppuun, älä jää Renderin ilmaiseen PostgreSQLiin. Jatka Rahtiin ja Pukkiin.

## Vaihtoehto 2: Rahti

Rahti on CSC:n konttipalvelu suomalaisille korkeakouluille, myös Haaga-Helialle. Käyttö opetuksessa on maksuton CSC:n maksuttoman käytön ehtojen mukaan. Käyttöliittymä elää OKD-päivitysten mukana (vuoden 2026 aikana 4.18, 4.20, 4.21 ja nyt 4.22). Nappien teksti voi poiketa tästä ohjeesta. Polku on silti: image → Deployment → Service → Route.

Kevään ohjeen PostgreSQL-templatea ei enää käytetä uuteen kantaan. CSC merkitsee katalogin templatet vanhentuneiksi. Bitnami-imageihin nojaavat Helm-chartit on tarkoitettu kokeiluun 29.9.2025 jälkeen, koska Bitnami siirsi ylläpidetyt imaget maksulliseen katalogiin. Kurssin keston kanta tehdään Pukkiin.

Haaga-Helian omat ohjeet ovat sivustolla [CSC-palvelut Haaga-Heliassa](https://haagahelia.github.io/hh-csc-docs/). Tunnus ja projekti tehdään sivun [Opiskelijan aloitusohjeet](https://haagahelia.github.io/hh-csc-docs/aloitus_opiskelija/) mukaan. Jos opettaja on lähettänyt kutsun kurssiprojektiin, liittyminen on sivulla [Kurssiprojektiin liittyminen](https://haagahelia.github.io/hh-csc-docs/kurssiprojekti_opiskelijan_ohje/).

### CSC-tunnus

1. Avaa [my.csc.fi](https://my.csc.fi) ja kirjaudu **Haka**-tunnuksella (Haaga-Helian verkkotunnus).
2. Ensimmäisellä kerralla portaali ohjaa CSC-käyttäjätilin luontiin. CSC valitsee tunnuksen ja lähettää sen sähköpostilla.
3. Jatkossa samaan portaaliin pääsee Haka-tunnuksella tai sillä CSC-tunnuksella.

Tämän jälkeen aloitusohjeen järjestys on: Student-projekti, palvelut Rahti ja Pukki, tiimin jäsenet, sitten palvelujen käyttö.

### Tunnus ja projekti

1. Tee CSC-tunnus yllä olevan kohdan mukaan, jos sitä ei vielä ole.
2. Luo **CSC Student** -projekti, jos tiimi tarvitsee oman nimiavaruuden. Jaetussa Course-projektissa opiskelijat näkevät toistensa ympäristöt. Course-projektin enimmäiskesto on kuusi kuukautta, eikä sitä jatketa.
3. Aktivoi projektiin palvelut **Rahti** ja, kun kanta tulee Pukkiin, **Pukki**. Hyväksy käyttöehdot.
4. Avaa [rahti.csc.fi](https://rahti.csc.fi) ja luo Project, esimerkiksi `ticketguru`. MyCSC-projekti ja Rahti-projekti ovat eri asioita: ensimmäinen on hallinnollinen, toinen on se nimiavaruus, johon kontit tulevat.

### Image Docker Hubiin

Julkinen Docker Hub -repo riittää. Korvaa `yourdockeruser`.

```bash
cd backend
docker build -t ticketguru:latest .
docker tag ticketguru:latest yourdockeruser/ticketguru:latest
docker push yourdockeruser/ticketguru:latest
```

Kirjaudu ensin: `docker login`.

Jos Rahti ei saa vedettyä imagea (Docker Hubin rajoitus), työnnä se Rahtin rekisteriin. `oc` on kirjauduttu projektiin ennen näitä komentoja.

```bash
oc create imagestream ticketguru
docker login -u unused -p "$(oc whoami -t)" image-registry.apps.2.rahti.csc.fi
docker tag ticketguru:latest image-registry.apps.2.rahti.csc.fi/<rahti-projekti>/ticketguru:latest
docker push image-registry.apps.2.rahti.csc.fi/<rahti-projekti>/ticketguru:latest
```

### Deploy

Developer-näkymä → **+Add** → kontti-image (kevään käyttöliittymässä sama kohta oli **Deploy Image**).

| Kenttä | Arvo |
| --- | --- |
| Image | `docker.io/yourdockeruser/ticketguru:latest` tai Rahtin rekisterin osoite |
| Name | `ticketguru` |
| Container port | `8080` |

Rahti luo Deploymentin ja Servicen. Jos kontti jää `CrashLoopBackOff`-tilaan ja loki valittaa oikeuksista, image yrittää kirjoittaa rootin omistamaan hakemistoon. Tämän ohjeen Dockerfile ajaa käyttäjänä 1001.

### Route

**Networking** → **Routes** → **Create Route**.

| Kenttä | Arvo |
| --- | --- |
| Name | `ticketguru` |
| Service | `ticketguru` |
| Target port | `8080-tcp` |
| Secure Route | päällä, TLS edge |

Jätä hostname tyhjäksi. Rahti muodostaa osoitteen reitin nimestä ja projektista:

`https://ticketguru-<rahti-projekti>.2.rahtiapp.fi`

Kevään muoto `*.rahtiapp.fi` ilman `2.` ei ole enää oletus. Varmenne toimii sekä `*.2.rahtiapp.fi`- että `*.rahtiapp.fi`-nimille, jos nimen asettaa itse. Tyhjä hostname käyttää `.2.rahtiapp.fi`.

```bash
curl -sS "https://ticketguru-<rahti-projekti>.2.rahtiapp.fi/api/events"
```

### PostgreSQL, joka kestää kurssin: Pukki

Pukki on CSC:n tietokantapalvelu. Sitä käytetään Rahtissa ajettavan sovelluksen kantana, jotta Postgresia ei ylläpidetä itse klusterissa.

1. [pukki.dbaas.csc.fi](https://pukki.dbaas.csc.fi) → **Database** → **Instances** → **Launch Instance**.
2. Datastore: PostgreSQL, uusin tarjolla oleva versio.
3. **Allowed CIDRs** on pakollinen. Ilman sitä kantaan ei saa yhteyttä. Rahtin sovellusta varten lisää `86.50.229.150/32`. Omaa konetta varten lisää oman julkisen IP:n muodossa `x.x.x.x/32`, pilkulla erotettuna.
4. Initial database, admin-käyttäjä ja salasana. Salasana on pitkä ja uniikki. Rahtin ulospäin lähtevä osoite `86.50.229.150` on **kaikkien** Rahti-projektien yhteinen, joten pelkkä IP-rajaus ei eristä kantaa muilta Rahti-käyttäjiltä. Sovelluksen oma autentikointi ja kannan salasana ovat se raja.
5. Kun instanssi on käynnissä, julkinen IP on `DB_HOST` ja portti on aina `5432`.

Aseta TicketGuru-deploymentin ympäristömuuttujat (Deployment → ympäristö → tallenna, jolloin Pod käynnistyy uudelleen):

| Key | Arvo |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_HOST` | Pukin julkinen IP |
| `DB_PORT` | `5432` |
| `DB_NAME` | luomasi tietokannan nimi |
| `DB_USERNAME` | admin-käyttäjä |
| `DB_PASSWORD` | salasana |

Lokissa JDBC-osoite on `jdbc:postgresql://<pukin-ip>:5432/<nimi>`.

Pukin palomuuria ei pidetä auki koko internetiin. Renderin ilmaisella web-palvelulla ei ole kiinteää uloslähtevää IP:tä, jota palomuuriin voisi laittaa. Siksi Pukki paritetaan Rahtiin, ei Renderin ilmaiseen web-palveluun.

### Jos kanta tehdään silti Rahti-projektin sisään

Levy, joka säilyy Podin uudelleenkäynnistyksen yli, on **PersistentVolumeClaim**: **Storage** → **PersistentVolumeClaims** → Create. Storage class `standard-csi`, access mode ReadWriteOnce, volume mode Filesystem. `DB_HOST` on silloin Postgres-Servicen nimi projektin sisällä (esimerkiksi `postgres`) ja portti `5432`.

Tätä levyä CSC ei varmuuskopioi. Katalogin PostgreSQL-template on vanhentunut, eikä sitä luoda uudelle kurssiprojektille. Oman imagen ja PVC:n ylläpito on tiimin työtä. Kolmen kuukauden kurssille Pukki on se polku, jossa kanta on olemassa ilman omaa tietokantaimagea.

### Pikaohje, Rahti ja Pukki

1. Sama Dockerfile ja prod-profiili kuin Render-osiossa.
2. [CSC-tunnus ja Student-projekti](https://haagahelia.github.io/hh-csc-docs/aloitus_opiskelija/). Palveluiksi Rahti ja Pukki.
3. `docker build`, tag, `docker push` Docker Hubiin.
4. Rahti: +Add → image, portti 8080.
5. Route, TLS päällä. Osoite `https://ticketguru-<projekti>.2.rahtiapp.fi`.
6. Pukki: PostgreSQL, Allowed CIDR `86.50.229.150/32` ja oma IP. Ympäristömuuttujat deploymenttiin.

## Mitä kevään 2026 ohjeesta päivitettiin

| Kevään ohje | Lokakuu 2026 |
| --- | --- |
| Java 17, Dockerfile repositorion juuressa | Java 25, Dockerfile hakemistossa `backend/` |
| Renderin valikko New + | New. Compute-suunnitelma Free valitaan itse. |
| PostgreSQL Renderissä ilman kestoa | Ilmainen kanta vanhenee 30 päivässä. Kurssin loppuun tarvitaan uusinta tai Pukki. |
| JDBC-esimerkki `jdbc:postgresql://DBHOST:{DB_PORT}/…` | Oikea rivi on `jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}` |
| Rahti-osoite `*.rahtiapp.fi` | Oletusosoite on `*.2.rahtiapp.fi` |
| PostgreSQL-template Rahdissa | Templatet on merkitty vanhentuneiksi. Kurssin kanta on Pukki. |
| Käyttäjät cashier / organizer / manager / admin | Tiimin omat tunnukset. Luennon esimerkki on `myyja` / `salasana`, kun Security on kytketty. |

## Lähteet

Tarkistettu 2026-10-06.

- [Render: Deploy for Free](https://render.com/docs/free)
- [Render: Docker](https://render.com/docs/docker)
- [Render: Free PostgreSQL expires after 30 days](https://render.com/changelog/free-postgresql-instances-now-expire-after-30-days-previously-90)
- [CSC-palvelut Haaga-Heliassa](https://haagahelia.github.io/hh-csc-docs/)
- [Opiskelijan aloitusohjeet](https://haagahelia.github.io/hh-csc-docs/aloitus_opiskelija/)
- [Rahti](https://rahti.csc.fi/)
- [Rahti catalog](https://docs.csc.fi/cloud/rahti/usage/catalog/)
- [Kurssi CSC:n resursseilla](https://docs.csc.fi/fi/support/tutorials/services-for-courses/)
- [Pukin web-käyttöliittymä](https://docs.csc.fi/cloud/dbaas/web-interface/)
- [Pukin palomuuri ja Rahtin IP](https://docs.csc.fi/cloud/dbaas/firewalls/)
