# Yksinkertainen web-client JWT-rajapintaa vasten (TicketGuru)

Viikko 8, ennakko. Toinen tiimi on tehnyt Spring Boot -palvelimen, joka palauttaa JWT:n. Tämä luento kertoo, mitä client-tiimi kirjoittaa selaimeen. Clientia ei tässä luennossa lisätä repositorioon.

Client on tavallista JavaScriptiä. Se vastaanottaa tokenin ja lisää sen jokaiseen seuraavaan pyyntöön. Se ei rakenna JWT:tä, ei tarkista allekirjoitusta eikä päätä roolia.

Tausta palvelimella: [autentikointi-ja-auktorisointi.md](autentikointi-ja-auktorisointi.md), dia 5.

---

## Dia 1 — Työnjako

| Tiimi | Tekee |
| --- | --- |
| Backend | `POST /api/login` kirjoittaa tokenin. Spring Security tarkistaa sen seuraavilla pyynnöillä ja ajaa `hasRole`-säännöt. |
| Client | Lomake, yksi `fetch` kirjautumiseen, token muuttujaan, otsikko `Authorization: Bearer` muihin kutsuihin. |

Backend-tiimi antaa client-tiimille:

- palvelimen osoitteen, esimerkiksi `https://ticketguru.example`
- kirjautumisosoitteen, tässä `POST /api/login`
- rungon kenttien nimet: `kayttajanimi` ja `salasana`
- vastauskentän nimen: `token`
- tunnukset rooleittain: `myyja` / `salasana` ja `koordinaattori` / `salasana`

Ilman näitä client ei arvaa, mistä merkkijono luetaan.

---

## Dia 2 — Kirjautuminen kerran

Salasana lähtee vain tästä kutsusta.

```javascript
const vastaus = await fetch(osoite + "/api/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ kayttajanimi, salasana })
});

if (vastaus.status === 401) {
  // tunnus tai salasana on väärä
}

const { token } = await vastaus.json();
```

Onnistunut vastaus on `200` ja token. Client säilyttää sen yhdessä muuttujassa. Muuttujassa on vain sen ihmisen token, joka juuri kirjautui. Kaikkien käyttäjien avaimia tai salasanoja ei tallenneta clienttiin.

Uloskirjautuminen on tämän muuttujan tyhjennys. Palvelimella ei ole istuntoa, jota sulkea.

---

## Dia 3 — Token jokaiseen muuhun pyyntöön

```javascript
const vastaus = await fetch(osoite + "/api/events", {
  headers: { Authorization: "Bearer " + token }
});
```

Sama otsikko `POST`-, `PUT`- ja `DELETE`-kutsuihin. Salasanaa ei lähetetä uudelleen.

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

---

## Dia 4 — CORS, kun client on eri osoitteessa

Selain ei lähetä pyyntöä toiselle palvelimelle, ennen kuin backend lupaa. Ensimmäinen kutsu on `OPTIONS`. Siinä selain kysyy, saako tämä sivu lähettää otsikon `Authorization`.

Lupa on backend-tiimin työtä. Sallittujen listaan tulevat client-tiimin osoite, metodit ja otsikko `Authorization`. Ilman lupaa selain pysäyttää kutsun, vaikka token olisi oikein ja `fetch` olisi kirjoitettu kuten diassa 3.

Postman ja curl eivät lähetä `OPTIONS`-kyselyä. Niillä rajapinta voi toimia samalla tunnuksella, jolla selain vielä estää clientin.

---

## Dia 5 — Jos toinen tiimi teki Basicin

Silloin kirjautumiskutsua ei ole. Client lähettää tunnuksen ja salasanan joka pyynnössä:

```javascript
headers: {
  Authorization: "Basic " + btoa(kayttajanimi + ":" + salasana)
}
```

Selain ei lisää otsikkoa itse, kun client on oma sivu eikä selaimen kirjautumisikkuna. `btoa` tekee saman Base64-muunnoksen, jonka Postmanin Basic Auth tekee. CORS tarvitaan tässäkin, koska otsikko on yhä `Authorization`.

TicketGurun oma suositus palvelimelle on Basic, kunnes web-client oikeasti tulee. Tämä viikko on sitä tilannetta varten, että vastaan tuleva tiimi on jo valinnut JWT:n. Clientin työmäärä on silti samaa luokkaa: muutama `fetch`.

---

## Dia 6 — Mitä clientiin ei kirjoiteta

- Tokenin allekirjoitusta tai salaisuutta. Ne ovat palvelimella.
- `hasRole`-sääntöjä. `403` tulee palvelimelta.
- Listaa muiden käyttäjien tunnuksista.
- Istuntoa tai evästettä, jolla palvelin muistaisi kirjautumisen.
- Omaa käyttäjätaulua.

Monimutkaisuus on palvelimessa, joka kirjoittaa tokenin ja tarkistaa sen. Client vastaanottaa merkkijonon ja lähettää sen takaisin.
