# Vaihe 1, käännös. Tämä image jää vain rakentamiseen. Sitä ei ajeta palvelimella.
#
# maven:3.9-eclipse-temurin-25
#   3.9  = Mavenin versio imagen sisällä. Java-versio on tagin lopussa.
#   25   = JDK:n versio. Sama luku kuin pom.xml-tiedoston <java.version>.
# Java 17 -projekti: maven:3.9-eclipse-temurin-17
# AS build nimeää vaiheen, jotta valmis jar voidaan kopioida siitä alempana.
FROM maven:3.9-eclipse-temurin-25 AS build

# Työhakemisto kontin sisällä. /app on valittu nimi, ei sovelluksen artifactId.
# Jos vaihdat polun, vaihda se myös alempaan vaiheeseen ja COPY --from -riville.
WORKDIR /app

# Kopioi Maven-kuvauksen. Ilman tätä mvn ei tiedä riippuvuuksia eikä jarin nimeä.
# Polku on suhteessa hakemistoon, jossa komento docker build ajetaan
# (tässä backend/, koska Dockerfile ja pom.xml ovat siellä).
COPY pom.xml .

# Kopioi lähdekoodin (src/main/java ja src/main/resources).
COPY src ./src

# Kääntää ja paketoi. Tulostiedosto on
#   target/<artifactId>-<version>.jar
# Nimi luetaan pom.xml:stä. Tässä repositoriossa tiedosto on
#   target/ticketguru-0.0.1-SNAPSHOT.jar
# Komento ei nimeä tiedostoa app.jar:ksi. Uudelleennimeäminen on alempana.
# -B           ei kysy vahvistuksia (pakollinen, kun build ajetaan palvelimella)
# clean        tyhjentää target/ ennen paketointia
# package      tuottaa jarin
# -DskipTests  testit ajetaan GitHub Actionsissa, ei Docker-buildissa
# Sivutuote target/*.jar.original ei ole suoritettava jar, eikä seuraava COPY ota sitä.
RUN mvn -B clean package -DskipTests

# Vaihe 2, ajo. Tähän imageen jää JRE, jolla jar käynnistetään.
# eclipse-temurin:25-jre on sama Java-versio kuin build-vaiheessa, ilman JDK:ta ja Mavenia.
# Java 17 -projekti: eclipse-temurin:17-jre
# Molemmat FROM-rivit vaihdetaan yhdessä. Pelkän toisen rivin vaihto rikkoo ajon,
# jos jar on käännetty uudemmalla Javalla kuin mitä JRE osaa ajaa.
FROM eclipse-temurin:25-jre

WORKDIR /app

# Kopioi build-vaiheen jarin ja tallentaa sen nimellä app.jar.
# Tähti *.jar osuu yhteen paketoituun jariin (ei *.jar.original -tiedostoon).
# Jos target/-hakemistoon jää kaksi .jar-tiedostoa, build katkeaa.
# Kirjoita silloin tarkka nimi, jonka pom.xml määrää:
#   COPY --from=build /app/target/ticketguru-0.0.1-SNAPSHOT.jar app.jar
# Kun kohdenimi on app.jar, ENTRYPOINT pysyy samana vaikka artifactId tai versio vaihtuu.
COPY --from=build /app/target/*.jar app.jar

# Rahti kieltää root-käyttäjän. Ryhmä 0 saa samat oikeudet kuin tiedoston omistaja,
# jotta käyttäjä 1001 voi lukea jarin. Renderissä sama rivi ei haittaa.
RUN chgrp -R 0 /app && chmod -R g=u /app

# Käynnistä kontti käyttäjänä 1001, ei rootina (uid 0).
USER 1001

# Kertoo, että prosessin oletusportti kontin sisällä on 8080.
# EXPOSE ei avaa porttia eikä sido sitä isäntäkoneeseen.
# Render asettaa ympäristömuuttujan PORT (usein 10000). Sovellus lukee sen
# tiedostosta application.properties: server.port=${PORT:8080}.
# Ilman PORT-muuttujaa (paikallinen docker run, Rahti) portti on 8080.
EXPOSE 8080

# Käynnistää Spring Bootin. Nimi app.jar on edellisen COPY-rivin kohdenimi,
# ei pom.xml:n artifactId. Tätä riviä ei muuteta, kun sovelluksen nimi vaihtuu.
ENTRYPOINT ["java", "-jar", "app.jar"]
