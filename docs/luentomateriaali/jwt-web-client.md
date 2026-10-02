# Yksinkertainen web-client JWT-rajapintaa vasten (TicketGuru)

Viikko 8, ennakko. Toinen tiimi on tehnyt Spring Boot -palvelimen, joka palauttaa JWT:n. Tämä luento kertoo, mitä client-tiimi kirjoittaa selaimeen. Clientia ei tässä luennossa lisätä repositorioon.

Client on tavallista JavaScriptiä. Se vastaanottaa tokenin ja lisää sen jokaiseen seuraavaan pyyntöön. Se ei rakenna JWT:tä, ei tarkista allekirjoitusta eikä päätä roolia.

Tausta palvelimella: [autentikointi-ja-auktorisointi.md](autentikointi-ja-auktorisointi.md), dia 5.

---

## Dia 1 — Työnjako

| Tiimi | Tekee |
| --- | --- |
| Backend | `POST /api/login` kirjoittaa tokenin. Spring Security tarkistaa sen seuraavilla pyynnöillä ja ajaa `hasRole`-säännöt. |
| Client | Lomake, yksi `fetch` kirjautumiseen, token `localStorage`-muistiin, otsikko `Authorization: Bearer` muihin kutsuihin. |

Backend-tiimi antaa client-tiimille:

- palvelimen osoitteen, esimerkiksi `https://ticketguru.example`
- kirjautumisosoitteen, tässä `POST /api/login`
- rungon kenttien nimet: `kayttajanimi` ja `salasana`
- vastauskentän nimen: `token`
- tunnukset rooleittain: `myyja` / `salasana` ja `koordinaattori` / `salasana`

Ilman näitä client ei arvaa, mistä merkkijono luetaan.

---

## Dia 2 — Kirjautuminen ja tokenin tallennus

Salasana lähtee vain tästä kutsusta. Esimerkki on vanilla JavaScriptiä, ilman Reactia tai muuta kehystä. Kenttien nimet ovat TicketGurun nimet. Toisen tiimin rajapinta voi käyttää eri nimiä: ne pitää lukea heidän dokumentistaan.

```javascript
async function login(kayttajanimi, salasana) {
  const response = await fetch(osoite + "/api/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ kayttajanimi, salasana })
  });

  if (response.status === 401) {
    throw new Error("Tunnus tai salasana on väärä");
  }
  if (!response.ok) {
    throw new Error("Kirjautuminen epäonnistui");
  }

  const data = await response.json();
  localStorage.setItem("jwtToken", data.token);
}
```

Onnistunut vastaus on `200` ja JSON `{ "token": "eyJhbGciOi..." }`. `localStorage` säilyttää tokenin sivun uudelleenlatauksen yli. Siellä on vain sen ihmisen token, joka juuri kirjautui. Kaikkien käyttäjien avaimia tai salasanoja ei tallenneta clienttiin.

Uloskirjautuminen on `localStorage.removeItem("jwtToken")`. Palvelimella ei ole istuntoa, jota sulkea.

---

## Dia 3 — Token jokaiseen muuhun pyyntöön

Otsikon arvo on tismalleen `Bearer`, välilyönti ja token. Ilman sanaa `Bearer` tai ilman välilyöntiä palvelin ei tunnista tokenia.

```javascript
async function haeTapahtumat() {
  const token = localStorage.getItem("jwtToken");
  if (!token) {
    return;
  }

  const response = await fetch(osoite + "/api/events", {
    headers: { Authorization: "Bearer " + token }
  });

  if (response.status === 401) {
    localStorage.removeItem("jwtToken");
    return;
  }
  if (response.status === 403) {
    return;
  }

  return response.json();
}
```

Sama otsikko `POST`-, `PUT`- ja `DELETE`-kutsuihin. Salasanaa ei lähetetä uudelleen. `401` poistaa tokenin, koska se puuttuu, on väärä tai vanhentunut: käyttäjä kirjautuu uudelleen. `403` jättää tokenin paikalleen. Käyttäjä on tunnistettu, mutta rooli ei saa tehdä tätä toimintoa. Myyjä ei luo tapahtumaa.

| Vastaus | Mitä client tekee |
| --- | --- |
| `200` tai `201` | Näyttää tuloksen. `201`:n `Location` kertoo luodun resurssin osoitteen. |
| `204` | Poisto onnistui. Runkoa ei ole. |
| `400` | Runko oli väärä. Näytä palvelimen viesti. |
| `401` | Token puuttuu, on väärä tai vanhentunut. Kirjaudutaan uudelleen. |
| `403` | Käyttäjä tunnistettiin, mutta rooli ei saa tehdä toimintoa. Myyjä ei luo tapahtumaa. |
| `404` | Polun id puuttuu. |

Clientin ei tarvitse lukea tokenin sisältöä. Rooli on tokenin sisällä palvelinta varten. Jos nappi halutaan piilottaa myyjältä, sen voi tehdä vastauksen `403`:sta tai erillisestä tiedosta. Oikeus tarkistetaan silti palvelimella.

Erillistä JWT-kirjastoa ei tarvita. Token on merkkijono.

Postmanissa sama asia tehdään ilman koodia: kokoelman tai pyynnön Authorization-välilehti, tyyppi Bearer Token, ja token liitetään kenttään. Postman kirjoittaa otsikon `Authorization: Bearer …`.

---

## Dia 4 — Miten Spring Boot käsittelee pyynnön

Clientin `fetch` päättyy tähän. Seuraava tapahtuu palvelimella, ennen kontrolleria.

1. Suodatin lukee jokaisesta pyynnöstä otsikon `Authorization`. Tiimi kirjoittaa sen usein luokaksi, joka perii `OncePerRequestFilter`-luokan, esimerkiksi `JwtRequestFilter`. Spring Securityn oma JWT-tuki tekee saman työn.
2. Suodatin tarkistaa, että arvo alkaa merkkijonolla `Bearer `.
3. Se tarkistaa allekirjoituksen palvelimen salaisuudella ja `exp`-ajan. Tokenia ei haeta kannasta. Jos allekirjoitus täsmää, suodatin asettaa käyttäjän ja roolin kontekstiin (`SecurityContextHolder`): `myyja`, `ROLE_MYYJA`.
4. Sama `AuthorizationFilter` kuin Basicissa ajaa `hasRole`-säännöt. Token puuttuu, on väärä tai vanha: `401`. Käyttäjä on oikea, mutta osoite on kielletty: `403`.

Tokenin kirjoitus `POST /api/login` -metodissa on yhä sovelluksen oma koodi. Suodatin vain tarkistaa valmiin tokenin.

---

## Dia 5 — CORS, kun client on eri osoitteessa

Selain ei lähetä pyyntöä toiselle palvelimelle, ennen kuin backend lupaa. Ensimmäinen kutsu on `OPTIONS`. Siinä selain kysyy, saako tämä sivu lähettää otsikon `Authorization`.

Lupa on backend-tiimin työtä. Tyypillinen kehitystilanne on client portissa `3000` ja Spring Boot portissa `8080`. Sallittujen listaan tulevat client-tiimin osoite, metodit ja otsikko `Authorization`. `allowedHeaders("*")` sallii otsikon, samoin pelkkä `Authorization`. Ilman lupaa selain pysäyttää kutsun, vaikka token olisi oikein ja `fetch` olisi kirjoitettu kuten diassa 3.

Postman ja curl eivät lähetä `OPTIONS`-kyselyä. Niillä rajapinta voi toimia samalla tunnuksella, jolla selain vielä estää clientin.

---

## Dia 6 — Jos toinen tiimi teki Basicin

Silloin kirjautumiskutsua ei ole. Client lähettää tunnuksen ja salasanan joka pyynnössä:

```javascript
headers: {
  Authorization: "Basic " + btoa(kayttajanimi + ":" + salasana)
}
```

Selain ei lisää otsikkoa itse, kun client on oma sivu eikä selaimen kirjautumisikkuna. `btoa` tekee saman Base64-muunnoksen, jonka Postmanin Basic Auth tekee. CORS tarvitaan tässäkin, koska otsikko on yhä `Authorization`.

TicketGurun oma suositus palvelimelle on Basic, kunnes web-client oikeasti tulee. Tämä viikko on sitä tilannetta varten, että vastaan tuleva tiimi on jo valinnut JWT:n. Clientin työmäärä on silti samaa luokkaa: muutama `fetch`.

---

## Dia 7 — Mitä clientiin ei kirjoiteta

- Tokenin allekirjoitusta tai salaisuutta. Ne ovat palvelimella.
- `hasRole`-sääntöjä. `403` tulee palvelimelta.
- Listaa muiden käyttäjien tunnuksista.
- Istuntoa tai evästettä, jolla palvelin muistaisi kirjautumisen.
- Omaa käyttäjätaulua.

Monimutkaisuus on palvelimessa, joka kirjoittaa tokenin ja tarkistaa sen. Client vastaanottaa merkkijonon ja lähettää sen takaisin.
