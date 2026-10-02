# Viikko 6 — luentomuistiinpanot

Päivämäärä: 2026-10-01 (Sprint 6, materiaali)

## Luennon aihe

REST-rajapinnan autentikointi ja auktorisointi. Rajapinta on tilaton, joten tunnistus tulee joka pyynnössä. TicketGurussa käyttäjät ja roolit ovat jo kannassa (`myyja`, `koordinaattori`).

Kooste vaihtoehdoista ja suositus: [autentikointi-ja-auktorisointi.md](autentikointi-ja-auktorisointi.md).

## Mitä luento suosittelee

- Nyt: HTTP Basic omia `Kayttaja`-rivejä vasten, roolit `Rooli`-taulusta.
- Sallitut toimenpiteet ovat koodin `hasRole`-säännöissä. Roolitauluun ei kirjata osoitteita.
- Kirjasto on `spring-boot-starter-security`. Omaa Basic-suodatinta ei kirjoiteta. `hasRole` ajetaan suodattimessa ennen kontrolleria.
- Myöhemmin JWT: otsikko on `Authorization: Bearer`. Tokenin kirjoittaa oma kirjautumiskoodi. Spring Security tarkistaa sen seuraavilla pyynnöillä, ja samat `hasRole`-säännöt sekä osoitteet jäävät.
- Jos tiimi tekee JWT:n ennen selainclienttiä, kokeilu on Postmanissa: `POST /api/login` ja sen jälkeen Bearer Token. `401` on puuttuva tai kelvoton tunnus, `403` on väärä rooli. Ohje on luennon diassa 5.
- Ei yhtä `admin`-tunnusta asetuksissa. API-avain sopii yhdelle kutsujalle, ei yhteiseen kassaan, jossa moni kirjautuu samaan clienttiin.
- Salasana bcrypt-hashina. Myyjä luetaan kirjautuneesta käyttäjästä.
- Ulkoista tunnistuspalvelua ei rakenneta.

## Mitä ei tehdä vielä

- Ei Spring Security -riippuvuutta, ei suodatinta, ei login-osoitetta.
- Ei JWT:tä eikä selainclienttiä.
- Rajapinta jää tämän luennon jälkeen yhä auki.
