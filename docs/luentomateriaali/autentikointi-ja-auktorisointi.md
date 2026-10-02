# REST: autentikointi ja auktorisointi (TicketGuru)

Sprint 6. Rajapinta vastaa jo oikeilla statuskoodeilla, mutta se on edelleen auki: kuka tahansa voi luoda tapahtuman tai myydä lippuja toisen nimissä. Tämä luento listaa **vaihtoehdot** ja merkitsee **suositellun linjan**. Koodia ei tässä vaiheessa muuteta.

Kaksi kysymystä pidetään erillään:

- **Autentikointi** tunnistaa käyttäjän. TicketGurussa se on `Kayttaja`: `myyja` tai `koordinaattori`.
- **Auktorisointi** päättää, mitä tunnistettu käyttäjä saa tehdä. Sen tieto on jo kannassa roolina `MYYJA`, `TAPAHTUMAKOORDINAATTORI` tai `PAAKAYTTAJA`.

Lähteet:

- Markku Ruonavaara: *REST API:n autentikoinnin ja auktorisoinnin perusteita* (kurssin PDF, 2020).
- [HTTP-autentikointi, MDN](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication)
- [REST ja tilattomuus](https://restfulapi.net/statelessness/)
- [JSON Web Token](https://jwt.io/introduction/)
- [Salasanan hash](https://security.blogoverflow.com/2013/09/about-secure-password-hashing/)

---

## Dia 1 — Mitä rajapinta tekee nyt

`DemoDataLoader` luo kaksi kirjautujaa, molemmille salasanan `salasana` selväkielisenä:

| kayttajanimi | rooli | nimi |
| --- | --- | --- |
| `myyja` | `MYYJA` | Maija Myyjä |
| `koordinaattori` | `TAPAHTUMAKOORDINAATTORI` | Kalle Koordinaattori |

Rooli `PAAKAYTTAJA` on kannassa, mutta sille ei ole vielä käyttäjää. Spring Security -riippuvuutta ei ole. `GET /api/events` ja `POST /api/sales` onnistuvat ilman otsakkeita.

Myyntipyyntö kertoo myyjän itse:

```json
{ "tapahtumaId": 1, "myyjaId": 1, "rivit": [{ "lipputyyppiId": 1, "kpl": 2 }] }
```

Client voi laittaa `myyjaId`:ksi kenet tahansa. Tunnistus puuttuu, joten roolitaulukko dokumentaatiossa ei vielä päde.

---

## Dia 2 — Miksi selainistunto ei ole REST-malli

Tavallisessa web-sovelluksessa palvelin avaa **istunnon**, antaa selaimelle session-id:n evästeessä ja muistaa, kuka on kirjautunut. Uloskirjautuminen tuhoaa istunnon palvelimelta. Palvelin pitää kirjaa kaikista istunnoista.

REST-rajapinta on **tilaton**. Palvelin ei muista edellistä pyyntöä. Tunnistuksen pitää tulla **jokaisen** pyynnön mukana. Siksi TicketGuru ei rakenna kirjautumista evästeen ja istunnon varaan.

Tästä seuraa kaksi käytännön tapaa, jotka luento käy läpi:

1. **Nyt:** HTTP Basic. Käyttäjätunnus ja salasana tulevat joka pyynnössä.
2. **Myöhemmin, jos web-client tulee:** JWT. Salasana lähetetään kerran, sen jälkeen client lähettää allekirjoitetun tokenin.

Kolmas esillä ollut tapa, **API-avain**, ei sovi tähän tuotteeseen. Se on mukana, jotta ero näkyy.

---

## Dia 3 — Kolme tapaa suojata rajapinta

| | API-avain | HTTP Basic | JWT |
| --- | --- | --- | --- |
| Mitä pyyntö kantaa | `X-API-Key: secret123` | `Authorization: Basic …` | `Authorization: Bearer …` |
| Kuka tunnistetaan | Sovellus tai tili, jolla on avain | Yksi `Kayttaja` | Sama `Kayttaja`, tokenin sisällä |
| Roolit | Vain jos avain on sidottu rooliin tai käyttäjään | Kyllä, kannan `Rooli` | Kyllä, tokenin claim |
| Salasana joka pyynnössä | Ei käyttäjäsalasanaa | Kyllä | Ei, vain token |
| Palvelin muistaa kirjautumisen | Ei | Ei | Ei |
| Milloin järkevä | Kone kutsuu konetta, ei ihmisiä | Kehitys ja työkalut (Postman, curl), kun käyttäjät ovat jo kannassa | Selain tai muu client, joka ei saa lähettää salasanaa joka kutsussa |

### API-avain — yksi kutsuja, ei yhteinen kassa

API-avain ei ole HTTP Basic eikä JWT. Pyyntö kuljettaa yhden salaisuuden omassa otsikossaan. Julkinen esimerkki on [The Cat API](https://docs.thecatapi.com/docs/authorization): rekisteröitynyt saa avaimen sähköpostiin ja lähettää sen joka kutsussa.

```bash
curl -H "x-api-key: YOUR-API-KEY" https://api.thecatapi.com/v1/breeds
```

Avain puuttuu tai on väärä: kutsu hylätään. Oikea avain kertoo, kenen tili rajapintaa käyttää. Cat API rajaa tällä nopeuden (ilmaisella tilillä 10 pyyntöä minuutissa) ja sen, mitkä kentät vastauksessa näkyvät. Se on tunnistus ja karkea oikeuksien rajaus tilille, ei kirjautuneelle ihmiselle.

Avain sopii, kun kutsuja on yksin liikkeellä:

- yksi taustasovellus tai kumppanin integraatio
- yksi skripti
- yksi ihminen, joka hakee rajapinnasta tietoa omalla avaimellaan

Silloin avain on sen yhden kutsujan salaisuus. Palvelin voi pitää taulua, jossa avain viittaa tiliin.

Roolikohtainen avain näyttää ensin riittävältä. Kaikille myyjille yksi avain, kaikille koordinaattoreille toinen. Palvelin hakee avaimen ja tietää roolin, joten osoitteiden tarkistus onnistuu. Myyntiä ei silti voi kirjata Maijalle: kaikki saman avaimen haltijat ovat yksi kutsuja. Avaimen vuoto vaihdetaan koko roolilta.

Käyttäjäkohtainen avain on jo henkilökohtainen salaisuus, sama idea kuin salasana. Otsikko on vain `x-api-key` eikä `Authorization: Basic`. Taulussa on silloin käyttäjä, rooli ja avain.

Yhteinen client ei voi pitää listaa näistä avaimista. Lista olisi sovelluksen koodissa tai muistissa, ja jokainen clientin avaava näkisi kaikkien tunnukset. Clientin kuuluu tietää vain sen ihmisen salaisuus, joka sitä juuri käyttää. Avainten lista pysyy palvelimella.

TicketGuru on yhteinen kassa. Sama client on monella myyjällä ja koordinaattorilla. Siksi avain jää sivuun ja tunnus on käyttäjäkohtainen Basic.

### Yksi tunnus `application.properties`-tiedostossa — vain demo

Spring Security osaa yhdessä minuutissa tämän:

```properties
spring.security.user.name=admin
spring.security.user.password=secret
```

Silloin ainoa käyttäjä on `admin`, ei taulun `Kayttaja` rivi. Maija ja Kalle katoavat, ja `myyjaId` on yhä clientin keksittävissä. Riittää ymmärtämään otsakkeen. Ei riitä tuotteeksi.

### HTTP Basic omia käyttäjiä vasten — suositus nyt

Basic on HTTP:n oma menetelmä. Otsikko on käyttäjätunnus, kaksoispiste ja salasana, Base64-koodattuna:

```text
myyja:salasana   →   Authorization: Basic bXl5amE6c2FsYXNhbmE=
```

Base64 **ei ole salausta**. Kuka tahansa purkaa merkkijonon takaisin tunnukseksi ja salasanaksi. Basic on järkevä vain, kun yhteys on HTTPS. Kurssin paikallisessa kehityksessä yhteys on HTTP, ja demosalasana `salasana` saa näkyä. Tuotantoon selväkielistä salasanaa tai pelkkää Basicia ei jätetä.

### JWT — sama käyttäjä, myöhemmin toinen otsikko

Client lähettää tunnuksen ja salasanan **kerran** kirjautumisosoitteeseen. Palvelin palauttaa allekirjoitetun tokenin. Seuraavat pyynnöt lähettävät tokenin, eivät salasanaa. Tokenia ei tallenneta palvelimelle. Palvelin tarkistaa allekirjoituksen ja vanhenemisajan.

Käyttäjätaulu ei vaihdu. Vaihtuu vain se, miten sama `Kayttaja` esitetään pyynnössä. Tämä otetaan käyttöön, kun ensimmäinen web-client tulee. Ei tässä sprintissä.

Ulkoinen tunnistuspalvelu (OpenID Connect, organisaation tunnus, Keycloak) on eri askel. Silloin käyttäjiä ei enää pidettäisi omassa kannassa. TicketGurun myyjä on tuotteen oma käsite, joten oma `Kayttaja` säilyy. Omaa yleistä kirjautumisalustaa ei rakenneta.

---

## Dia 4 — Basic Auth TicketGurussa

Suositus: Spring Security, HTTP Basic, käyttäjät taulusta `Kayttaja`, roolit taulusta `Rooli`. Rajapinta pysyy tilattomana, joten istuntoa ei luoda.

### Valmis kirjasto

Basic Authenticationia ei kirjoiteta itse. Otsikon purku, salasanan vertailu ja `401`-vastaus tulevat **Spring Securitystä**. Projektiin lisätään yksi riippuvuus, versiota ei kirjoiteta, koska parent on jo `4.1.1`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Kirjasto tuo mukanaan `BasicAuthenticationFilter`-luokan. Se lukee `Authorization: Basic …` -otsikon ja erottaa tunnuksen ja salasanan. Omaa `OncePerRequestFilter`-luokkaa, joka vertaa otsikkoa käsin, ei tarvita. API-avaimen suodatin oli eri malli.

Pelkkä riippuvuus ei vielä käytä `Kayttaja`-taulua. Spring Boot sulkee silloin koko sovelluksen yhden generoidun tunnuksen taakse: käyttäjä `user` ja satunnainen salasana, joka tulostuu käynnistyslokiin. Se on dian 3 yhden `admin`-tunnuksen versio. TicketGuru korvaa sen kahdella beanilla: oma `UserDetailsService` ja oma `SecurityFilterChain`.

### Kaksi tarkistusta

Tauluun ei kirjata sallittuja toimenpiteitä. `Rooli`-rivillä on vain nimi (`MYYJA`). Se, mitä rooli saa kutsua, on koodissa.

| Vaihe | Mistä tieto tulee | Epäonnistuu |
| --- | --- | --- |
| Tunnistus | `Kayttaja`-taulu: tunnus ja salasanan hash | `401` |
| Oikeus | Koodin `hasRole`-säännöt, roolin nimi taulusta | `403` |

Erillinen taulu “rooli saa osoitteen X” tarvitaan vasta, jos pääkäyttäjä muuttaa oikeuksia ilman uutta koodiversiota. Tässä projektissa roolit on jo päätetty, joten säännöt ovat konfiguraatiossa.

`hasRole("MYYJA")` etsii oikeutta `ROLE_MYYJA`. Etuliitteen lisää Spring, kun rooli annetaan nimellä `MYYJA`. Tauluun ei kirjoiteta merkkijonoa `ROLE_`.

### Miltä pyyntö näyttää

Postmanissa välilehti Authorization, tyyppi Basic Auth, käyttäjä `myyja`, salasana `salasana`. Curlissa sama asia:

```bash
curl -u myyja:salasana http://localhost:8080/api/events
```

`-u` kirjoittaa otsikon. Ilman otsikkoa vastaus on **401 Unauthorized**: kutsujaa ei tunnistettu. Väärä salasana on sama `401`.

Tunnistettu myyjä, jolta toiminto on kielletty, saa **403 Forbidden**. Hän on oikea käyttäjä, mutta rooli ei riitä. `401` ja `403` ovat odotettuja vastauksia, eivät `500`.

### Mistä tunnus tarkistetaan

Ei `application.properties`-käyttäjästä. Palvelu hakee tunnuksen näin:

```text
KayttajaRepository.findByKayttajanimi("myyja")
```

Löydetty rivi antaa salasanan tarkistukseen ja roolin `MYYJA` oikeustarkistukseen. Tuntematon tunnus on `401`.

Spring kutsuu tätä hakua itse, kun `UserDetailsService` on rekisteröity. Luonnos (ei vielä tiedostona projektissa):

```java
@Bean
public UserDetailsService userDetailsService(KayttajaRepository kayttajat) {
    return kayttajanimi -> {
        Kayttaja kayttaja = kayttajat.findByKayttajanimi(kayttajanimi)
                .orElseThrow(() -> new UsernameNotFoundException(kayttajanimi));
        return User.withUsername(kayttaja.getKayttajanimi())
                .password(kayttaja.getSalasana())
                .roles(kayttaja.getRooli().getNimi())
                .build();
    };
}

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

`PasswordEncoder` on sama kirjasto. Se laskee bcrypt-hashin ja vertaa pyynnön salasanaa kannan hashiin. Koodi ei tee `equals`-vertailua selväkieliseen salasanaan.

Oikeussäännöt ovat toisessa beanissa, `SecurityFilterChain`. Luonnos:

```java
http
    .csrf(csrf -> csrf.disable())
    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.GET, "/api/events/**").hasAnyRole("MYYJA", "TAPAHTUMAKOORDINAATTORI")
        .requestMatchers(HttpMethod.POST, "/api/events/**").hasRole("TAPAHTUMAKOORDINAATTORI")
        .requestMatchers(HttpMethod.POST, "/api/sales").hasRole("MYYJA")
        .anyRequest().authenticated())
    .httpBasic(Customizer.withDefaults());
```

`csrf` sammutetaan, koska REST ei käytä evästeistuntoa, jota CSRF-suojaus vartioi. `STATELESS` vastaa dian 2 sääntöä: palvelin ei avaa istuntoa.

### Salasana hashina

Nyt `salasana`-sarake on teksti `salasana`. Tunnistuksen yhteydessä sitä ei saa verrata merkkijonona, eikä uutta salasanaa saa tallentaa selväkielisenä. Salasanasta lasketaan yksisuuntainen **hash** (kurssilla bcrypt). Hashista ei päästä takaisin salasanaan, mutta annettu salasana voidaan tarkistaa sitä vasten.

Demoaineisto päivitetään samalla, kun Basic otetaan käyttöön: kantaan tallennetaan hash, Postmaniin jää edelleen selvä `salasana`. Hash ei näy vastauksissa. `MyyntiResponse` ei jo nyt palauta salasanaa, eikä mikään muukaan DTO saa alkaa palauttaa sitä.

### Kuka on myyjä

Kun pyyntö on tunnistettu, myyjä on kirjautunut `Kayttaja`, ei rungon `myyjaId`. Muuten Maija voi myydä liput Kallen nimissä. Toteutuksessa `myyjaId` poistuu `MyyntiRequest`-rungosta tai se tarkistetaan samaksi kuin kirjautunut käyttäjä. Luento vain merkitsee säännön. Kenttää ei vielä poisteta.

### Kuka saa tehdä mitä

Tuotteen roolitaulu, sidottuna nykyisiin osoitteisiin:

| Toiminto | MYYJA | TAPAHTUMAKOORDINAATTORI |
| --- | --- | --- |
| `GET /api/events`, `GET /api/events/{id}` | kyllä | kyllä |
| `POST`, `PUT`, `DELETE /api/events` | ei (`403`) | kyllä |
| `GET /api/events/{id}/ticket-types` | kyllä | kyllä |
| `POST /api/events/{id}/ticket-types` | ei (`403`) | kyllä |
| `POST /api/sales`, `GET /api/sales/{id}` | kyllä | ei myyntiä kassalla |
| Käyttäjien hallinta | ei | ei |

`PAAKAYTTAJA` ja käyttäjien luonti jäävät myöhemmäksi. Niitä ei tarvita siihen, että tapahtumat ja myynti ovat suojatut.

Esimerkit samalla aineistolla:

| Pyyntö | Tunnus | Tulos |
| --- | --- | --- |
| `GET /api/events` | ei otsikkoa | `401` |
| `GET /api/events` | `myyja` / `salasana` | `200` |
| `POST /api/events` | `myyja` / `salasana` | `403` |
| `POST /api/events` | `koordinaattori` / `salasana` | `201` ja `Location` |
| `POST /api/sales` | `myyja` / `salasana` | `201` |
| `POST /api/sales` | väärä salasana | `401` |

Onnistuneet koodit eivät muutu edellisestä sprintistä. Puuttuva id on yhä `404`, kelvoton runko yhä `400`. Ne tarkistetaan vasta, kun tunnus ja rooli on hyväksytty.

---

## Dia 5 — JWT, kun web-client tulee

Basic riittää Postmanille ja curlille. Selainclientille se on huono: salasana lähtisi joka pyynnössä, ja selain voi avata oman kirjautumisikkunan. Silloin otsikko vaihtuu, käyttäjät eivät.

### Kulku

1. Client lähettää kerran `POST /api/login` rungolla `{ "kayttajanimi": "myyja", "salasana": "salasana" }`.
2. Palvelin tarkistaa saman käyttäjän ja saman bcrypt-hashin kuin Basicissa.
3. Onnistunut vastaus on `200` ja token. Väärä tunnus on `401`. Erillistä istuntoa ei synny.
4. Client säilyttää tokenin itsellään ja lähettää sen seuraavissa pyynnöissä:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJteXlqYSJ9.allekirjoitus
```

5. Palvelin tarkistaa allekirjoituksen ja `exp`-ajan. Tokenia ei haeta kannasta.
6. Uloskirjautuminen on clientin teko: se hävittää tokenin. Palvelimella ei ole riviä, jonka voisi poistaa.

Resurssien osoitteet pysyvät. `GET /api/events` on sama kutsu. Vain `Authorization`-otsikon muoto vaihtuu Basicista Bearer-tokeniin. Roolisäännöt diasta 4 pysyvät.

### Mitä token sisältää

JWT on kolme Base64-osaa pisteellä erotettuna: header, payload, allekirjoitus.

```json
{ "alg": "HS256", "typ": "JWT" }
```

```json
{ "sub": "myyja", "rooli": "MYYJA", "exp": 1760000000 }
```

Allekirjoitus lasketaan kahdesta ensimmäisestä osasta ja palvelimen salaisuudesta. Client ei voi vaihtaa `rooli`-kentäksi `TAPAHTUMAKOORDINAATTORI` ilman, että allekirjoitus rikkoutuu. Payload **ei ole salainen**: kuka tahansa lukee siitä tunnuksen ja roolin. Siksi tokeniin ei laiteta salasanaa eikä muuta, mitä clientin ei kuulu nähdä.

Token vanhenee. Ensimmäisessä versiossa client kirjautuu uudelleen. Erillistä refresh-tokenia ei tarvita, ennen kuin client oikeasti on olemassa.

### Mitä ei rakenneta JWT:n tilalle

Keycloak, Auth0 tai organisaation tunnuspalvelu tunnistavat käyttäjän muualla ja antavat sovellukselle valmiin tokenin. Se on oikea malli, kun käyttäjät tulevat organisaation hakemistosta. TicketGurun kurssiversiossa myyjä ja koordinaattori syntyvät omassa kannassa. JWT niiden päälle riittää sinä päivänä, kun selainclient tulee. Uutta käyttäjäjärjestelmää ei tehdä sitä ennen.

---

## Dia 6 — Suositus

| Kysymys | Valinta |
| --- | --- |
| Suojataanko rajapinta nyt? | Kyllä, seuraavassa toteutuksessa. Ei tässä luennossa. |
| Millä? | HTTP Basic, käyttäjät taulusta `Kayttaja`. |
| Yksi `admin` asetuksissa? | Ei. Se ei erota Maijaa ja Kallea. |
| API-avain? | Ei yhteiseen kassaan. Sopii yhdelle sovellukselle, skriptille tai yksin rajapintaa kutsualle ihmiselle. |
| Istunto ja eväste? | Ei. Rajapinta pysyy tilattomana. |
| Salasana kannassa? | Bcrypt-hash, ei selväkielistä `salasana`-saraketta. |
| Mistä myyjä tiedetään? | Kirjautuneesta käyttäjästä, ei vapaasta `myyjaId`-kentästä. |
| Missä sallitut toimenpiteet ovat? | Koodin `hasRole`-säännöissä. Roolitaulussa on vain nimi. |
| Mistä kirjasto? | `spring-boot-starter-security`. Omaa Basic-suodatinta ei kirjoiteta. |
| Web-client tai JWT? | Ei vielä. Kun client tulee, sama käyttäjä, otsikko `Bearer`. |
| Ulkoinen tunnistuspalvelu? | Ei tällä kurssilla. |

Sprintin tarkistuslista, kun toteutus myöhemmin tehdään:

- Ilman tunnusta `GET /api/events` palauttaa `401`, ei `200`.
- `myyja` saa listata tapahtumat ja luoda myynnin.
- `myyja` ei saa luoda tapahtumaa eikä lipputyyppiä (`403`).
- `koordinaattori` saa luoda tapahtuman (`201`).
- Väärä salasana on `401`, ei `500`.
- Kannassa ei ole selväkielistä salasanaa, eikä mikään vastaus palauta sitä.
- Myynti kirjautuu sille käyttäjälle, joka pyynnön lähetti.
- JWT-kirjautumista ja selainclienttiä ei lisätä tässä sprintissä.

---

## Lähteet

- Ruonavaara: autentikointi ja auktorisointi, istunto, Basic, token, hash, JWT:n kolme osaa.
- [MDN: HTTP authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication)
- [REST statelessness](https://restfulapi.net/statelessness/)
- [JWT introduction](https://jwt.io/introduction/)
- [The Cat API: Authorization](https://docs.thecatapi.com/docs/authorization) (esimerkki otsikosta `x-api-key`)
- [Spring Security: HTTP Basic](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/basic.html)
- [Spring Boot: Security](https://docs.spring.io/spring-boot/reference/web/spring-security.html)
- TicketGuru nyt: `domain/Kayttaja.java`, `domain/Rooli.java`, `DemoDataLoader.java`, `web/dto/MyyntiRequest.java`. Security-konfiguraatiota ei ole.
