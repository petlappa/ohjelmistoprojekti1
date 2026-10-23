# Lipuntarkastuksen web-client ja CORS

Viikon luentoaihe on web-ohjelmointi. Client voidaan tarjota kahdella tavalla. Full stack -sovelluksessa Spring Boot palvelee sivun kansiosta `src/main/resources/static/` samasta ohjelmasta kuin rajapinnan. Ohje on diassa 7. Erillinen sivu on oma osoitteensa ja kutsuu rajapintaa toisesta alkuperästä. Kutsun ohje on diassa 4.

Erillinen sivu (client) ei ole tämän Spring Boot -projektin kansiossa. Se voi olla yksi HTML-tiedosto tai React-sovellus. CORS (Cross-Origin Resource Sharing) on selaimen tarkistus: saako se sivu kutsua toista osoitetta. Postman ei tarkistusta tee. Erillinen client on tavallinen, kun sivun ja rajapinnan tekevät eri tiimit tai kun sivua kehitetään omalla palvelimellaan. Sama Boot on selkeä, kun yksi tiimi julkaisee yhden sovelluksen yhteen osoitteeseen.

Tämän viikon sprintissä sivustoa ei lisätä omaan Bootiin. Backend on julkaistu, esimerkiksi Renderiin osoitteeseen `https://<nimi>.onrender.com`. Clientin tekee toinen tiimi omalla koneellaan, localhostissa. Heidän erillinen sivunsa käyttää rajapintaa ja ottaa sillä yhteyden julkaistuun backendiin. Sivu itse ei ole Renderissä.

Tämän tiimin sprinttityö on lipun haku ja merkintä käytetyksi, sekä selaimen luvan antaminen tuolle sivulle. Tunnistus (authentication) pysyy viikon 6 HTTP Basicinä: tunnus ja salasana otsikossa (header) `Authorization`. Uutta kirjautumistapaa ei tehdä.

Tausta: [autentikointi-ja-auktorisointi.md](autentikointi-ja-auktorisointi.md), lipun kentät [../api/sales.md](../api/sales.md), virheet [virheet-ja-validointi.md](virheet-ja-validointi.md).

---

## Lähteet

| Aihe | Lähde |
| --- | --- |
| Staattiset tiedostot Bootissa | [Servlet applications: Static Content](https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.static-content) |
| Sama alkuperä (same-origin) | [MDN: Same-origin policy](https://developer.mozilla.org/en-US/docs/Web/Security/Same-origin_policy) |
| CORS selaimessa | [MDN: CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/CORS) |
| `fetch` | [MDN: Using Fetch](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch) |
| `PATCH` | [MDN: PATCH](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Methods/PATCH) |
| `@CrossOrigin` | [Spring Framework: CORS](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html) |
| CORS ja Spring Security | [Spring Security: CORS](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html) |
| QR-koodin luku selaimessa | [MDN: Barcode Detection API](https://developer.mozilla.org/en-US/docs/Web/API/Barcode_Detection_API) |

CORS-ohje tälle sprintille on dian 5 tarkistuslista: annotaatio kontrollereihin ja rivi luokkaan `SecurityConfig`. Uutta projektia ei perusteta.

---

## Dia 1 — Työnjako

| | Luennolla | Tämän viikon sprintissä |
| --- | --- | --- |
| Staattinen sivusto Spring Bootissa | Full stack -sovelluksessa sivun tiedostot tulevat kansioon `src/main/resources/static/`. Sivu aukeaa samasta Bootista. Dia 7. | Tämän viikon sivua ei lisätä omaan Bootiin. |
| Web-client | Jokainen ymmärtää dian 4 kutsut: otsikko `Authorization: Basic`, `GET` ja `PATCH`. | Clientin tekee toinen tiimi erillisenä sivuna. Sivu kutsuu tämän tiimin rajapintaa. |
| Tämä tiimi | Osaa kytkeä CORS:n päälle viikon 6 luokassa `SecurityConfig`. | Lipun haku koodilla ja merkintä käytetyksi. Samat virhekoodit ja sama `LippuResponse` kuin myynnissä. |

Kuka tekee sivun ja kenen julkaistua palvelinta se kutsuu:

| Clientin kirjoittaa | Backend, jota sivu kutsuu |
| --- | --- |
| Ykköset | Ryhmä 2 |
| Ryhmä 2 | Sopivasti Satunnaiset |
| Sopivasti Satunnaiset | Ykköset |

Teidän julkaistua palvelintaan kutsuu se tiimi, joka on taulukossa samalla rivillä sarakkeessa “Clientin kirjoittaa”. Sen sivun kirjoittaminen ei kuulu teidän sprinttiinne. Sivu tehdään heidän koneellaan. Tällä viikolla sitä ei lisätä kummankaan tiimin Spring Bootiin.

Toiselle tiimille kirjoitetaan auki, ennen kuin he tekevät sivun:

- julkaistu osoite, esimerkiksi `https://<nimi>.onrender.com`. Clientin sivu saa jäädä localhostiin ja kutsua tätä osoitetta.
- `GET /api/tickets?koodi=` ja `PATCH /api/tickets/{id}`
- pyynnön rungon (body) kenttä `kaytetty`, arvo `true` tai `false`
- HTTP Basic, tunnus `myyja` / `salasana`

Tuoteomistajan muistiinpanossa nimet olivat `code` ja `used`. Tässä koodissa lippu on ollut sprintistä 4 asti kentillä `koodi` ja `kaytetty`. Ne nimet jatkuvat, ja ne annetaan client-tiimille kirjallisina. Osoitteiden polut ovat englanniksi, kuten `/api/events` ja `/api/sales`. JSON-kenttien nimet ovat suomeksi, kuten myyntivastauksessa.

---

## Dia 2 — Lipun tarkastuksen rajapinta

Koodi syntyy myynnissä. `LippuRepository.findByKoodi` on jo olemassa. Uutta on kontrolleri ja palvelumetodi, jotka palauttavat saman `LippuResponse`-olion kuin myynti: `id`, `koodi`, `hinta`, `lipputyypinKuvaus`, `kaytetty`.

```http
GET /api/tickets?koodi=a1b2c3d4
Authorization: Basic bXl5amE6c2FsYXNhbmE=
```

`bXl5amE6c2FsYXNhbmE=` on merkkijono `myyja:salasana` Base64-muodossa. Se on sama tunnus kuin viikolla 6.

```http
PATCH /api/tickets/40
Authorization: Basic bXl5amE6c2FsYXNhbmE=
Content-Type: application/json

{ "kaytetty": true }
```

Onnistunut vastaus on `200` ja sama lippuolio. `PATCH` asettaa `kaytetty`-arvoksi `true`. Käyttöhetkeä ei tallenneta erillisenä aikana, koska tietokannan kenttä on tosi tai epätosi (boolean).

| Koodi | Milloin | Mistä se tulee |
| --- | --- | --- |
| `200` | Lippu löytyi, tai merkintä onnistui | Kontrolleri palauttaa `LippuResponse` |
| `400` | `koodi` puuttuu, tai JSON on väärän muotoinen | `@RequestParam` ja `@Valid`, viikko 5 |
| `401` | `Authorization`-otsikko puuttuu tai salasana on väärä | Spring Security, viikko 6 |
| `403` | Tunnus on oikea, rooli (role) ei saa kutsua polkua. Tämä on valtuutus (authorization). | `hasAnyRole` luokassa `SecurityConfig` |
| `404` | Koodia tai id:tä ei ole | `ResponseStatusException(NOT_FOUND)` |
| `409` | Lippu on jo käytetty | `ResponseStatusException(CONFLICT)`, sama sääntö kuin myynnin kapasiteetti |

Virhevastauksen runko (body) on entinen `ApiError`: `status`, `error`, `messages`.

Roolisääntö kirjoitetaan luokkaan `SecurityConfig`, metodiin `filterChain`, ennen yleistä `/api/**`-sääntöä. Sama kohta, jossa myynnin roolit jo ovat. Uutta roolia ei perusteta. `GET` ja `PATCH` osoitteeseen `/api/tickets` kuuluvat rooleille `MYYJA` ja `TAPAHTUMAKOORDINAATTORI`.

---

## Dia 3 — Sama alkuperä (same-origin)

Selain vertaa sivun ja rajapinnan alkuperää. Sääntö on same-origin: kutsu lähtee ilman CORS-tarkistusta vain, kun alkuperät ovat samat. Alkuperä (origin) on protokolla (scheme), palvelimen nimi (host) ja portti (port). Polku (path) ei kuulu siihen: `/index.html` ja `/api/tickets` voivat olla samaa alkuperää (same-origin).

`http://localhost:5500` on protokolla `http`, nimi `localhost` ja portti `5500`. `https://nimi.onrender.com` on protokolla `https`, nimi `nimi.onrender.com` ja portti `443`, vaikka porttia ei kirjoiteta osoitteeseen. `localhost` ja `127.0.0.1` ovat eri nimiä, vaikka ne olisivat sama kone.

| Sivu | Rajapinta | CORS |
| --- | --- | --- |
| `http://localhost:8080/index.html` | `http://localhost:8080/api/tickets` | Sama alkuperä. Selain lähettää kutsun ilman CORS-tarkistusta. |
| `http://localhost:5500/index.html` | `http://localhost:8080/api/tickets` | Eri portti. CORS tarvitaan. |
| `http://127.0.0.1:5500/index.html` | `http://localhost:8080/api/tickets` | Eri palvelimen nimi. CORS tarvitaan. |
| `http://localhost:5500/index.html` | `https://nimi.onrender.com/api/tickets` | Tavallinen HTML localhostissa, rajapinta julkaistu. CORS tarvitaan. |
| `http://localhost:5173/` tai `http://localhost:3000/` | `https://nimi.onrender.com/api/tickets` | Reactin kehityspalvelin, rajapinta julkaistu. CORS tarvitaan. |
| `file:///…/index.html` | `https://nimi.onrender.com/api/tickets` | Tiedosto on avattu suoraan levyltä. Sivulla ei ole osoitetta `http` tai `https`, joten selain estää kutsun. |

Selain vertaa sivun osoitetta, ei sitä konetta jossa Boot on kehitetty. Julkaistu palvelu ja localhost-sivu ovat eri alkuperiä, vaikka molemmat olisivat saman opiskelijan läppärillä auki. Selain lähettää otsikossa `Origin` sivun osoitteen, esimerkiksi `http://localhost:5500` tai Reactin `http://localhost:5173` tai `http://localhost:3000`. Kutsun kohde on `https://nimi.onrender.com`. Dia 7:n sivusto Bootin sisällä olisi taulukon ensimmäinen rivi. Sitä tiedostoa ei tällä viikolla lisätä.

---

## Dia 4 — Kutsut, jotka erillinen sivu lähettää

Tätä sivua ei kirjoiteta tähän repositorioon eikä kansioon `static/`. Toinen tiimi avaa sen localhostissa. `fetch` osoittaa julkaistuun backendiin. Selain ei kysy tunnusta itse. Otsikon (header) kokoaa sivun JavaScript, oli sivu yksi HTML-tiedosto tai React-sovellus.

```html
<!DOCTYPE html>
<html lang="fi">
<body>
  <input id="koodi" placeholder="koodi">
  <button id="hae" type="button">Hae</button>
  <input id="id" placeholder="id">
  <button id="kayta" type="button">Merkitse käytetyksi</button>
  <pre id="tulos"></pre>
  <script>
    const osoite = "https://nimi.onrender.com";
    const auth = { Authorization: "Basic " + btoa("myyja:salasana") };

    async function nayta(response) {
      document.getElementById("tulos").textContent =
        response.status + "\n" + await response.text();
    }

    document.getElementById("hae").onclick = async () => {
      const koodi = document.getElementById("koodi").value;
      const response = await fetch(
        osoite + "/api/tickets?koodi=" + encodeURIComponent(koodi),
        { headers: auth }
      );
      nayta(response);
    };

    document.getElementById("kayta").onclick = async () => {
      const id = document.getElementById("id").value;
      const response = await fetch(osoite + "/api/tickets/" + id, {
        method: "PATCH",
        headers: {
          Authorization: auth.Authorization,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ kaytetty: true })
      });
      nayta(response);
    };
  </script>
</body>
</html>
```

Toinen tiimi voi kirjoittaa saman sivun Reactilla. Silloin `fetch`, osoite, otsikko `Authorization` ja runko `{ "kaytetty": true }` ovat samat. React ei lisää otsikkoa itse eikä poista selaimen CORS-tarkistusta. Komponentti vain kutsuu samaa `fetch`-funktiota napin painalluksesta.

```jsx
import { useState } from "react";

function HaeLippu() {
  const [koodi, setKoodi] = useState("");
  const osoite = "https://nimi.onrender.com";

  async function hae() {
    const response = await fetch(
      osoite + "/api/tickets?koodi=" + encodeURIComponent(koodi),
      { headers: { Authorization: "Basic " + btoa("myyja:salasana") } }
    );
    console.log(response.status, await response.text());
  }

  return (
    <div>
      <input value={koodi} onChange={(event) => setKoodi(event.target.value)} />
      <button type="button" onClick={hae}>Hae</button>
    </div>
  );
}
```

Reactin kehityspalvelin on oma osoite, yleensä `http://localhost:5173` tai `http://localhost:3000`. Se ei ole Renderin osoite, joten dia 5 tarvitaan silloinkin. Sovellusta ei kopioida kansioon `static/`.

`btoa` muuttaa merkkijonon `myyja:salasana` Base64-muotoon, jota Basic-otsikko vaatii. `encodeURIComponent` koodaa lippukoodin osoitteen kyselyosaan (query) niin, että erikoismerkki ei katkaise `?koodi=`-osaa.

HTML-esimerkin haku ja merkintä aiheuttavat ensin `OPTIONS`-kyselyn (preflight). Selain kysyy sillä luvan ennen varsinaista kutsua. Syynä on otsikko `Authorization`. Selain ei lähetä sitä toiseen osoitteeseen kysymättä. `Content-Type: application/json` on toinen syy sille, että `PATCH`-kutsu kysyy luvan ensin. Reactin `fetch` tekee saman kyselyn. Ilman dian 5 asetuksia selain pysäyttää kutsun, vaikka Postman saisi samalla tunnuksella `200`.

Kun selain hylkää vastauksen, `fetch` heittää poikkeuksen eikä `response.status` ole olemassa. Dia 6 erottaa tämän `401`:stä.

---

## Dia 5 — CORS: tarkistuslista

Selain pääsee läpi vasta, kun listan jokainen kohta on koodissa. Annotaatio (annotation) kontrollereissa ja rivi luokassa `SecurityConfig` tarvitaan molemmat. Osalla tiimeistä annotaatio on jo aiemmalta sprintiltä, osalla ei. Tarkista oma koodi. Puuttuva annotaatio lisätään.

1. Jokaisessa `/api`-kontrollerissa on `@CrossOrigin(origins = "*")` luokan yläpuolella, samassa kohdassa kuin `@RestController`. Uusi lippukontrolleri saa sen heti. Vanhat kontrollerit (`TapahtumaController`, `LipputyyppiController`, `MyyntiController` ja muut) avataan ja annotaatio lisätään, jos sitä ei ole.
2. Luokan `SecurityConfig` metodissa `filterChain` ensimmäinen asetus on `.cors(Customizer.withDefaults())`.
3. Erillistä CORS-asetusoliota `CorsConfigurationSource` ei ole. Jos se on olemassa, Security ei lue kontrollereiden annotaatioita.
4. Lipun `GET` ja `PATCH` on lisätty `authorizeHttpRequests`-listaan ennen yleistä `/api/**`-sääntöä, rooleilla `MYYJA` ja `TAPAHTUMAKOORDINAATTORI`.
5. Tunnistus on yhä HTTP Basic. `myyja` / `salasana` toimii Postmanissa ja selaimen `Authorization`-otsikossa.
6. Toiselle tiimille on kerrottu julkaistu osoite (`https://<nimi>.onrender.com`), polut, kenttä `koodi`, kenttä `kaytetty` ja Basic-tunnus. Heidän sivunsa saa olla localhostissa.

### Annotaatio kontrolleriin

Springin annotaatio on `@CrossOrigin`. Se tulee luokan tasolle, samaan kohtaan kuin `@RestController` ja `@RequestMapping`.

```java
@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class LippuController {
```

Sama rivi jokaiseen kontrolleriin, jota selain kutsuu. Merkintää ei tarvitse toistaa jokaisen metodin yläpuolella, kun se on luokan yläpuolella. `origins = "*"` sallii sivun, joka on auki `http://localhost:5500` tai Reactin portissa `5173` tai `3000` ja kutsuu julkaistua `https://<nimi>.onrender.com`-osoitetta. Selain lähettää sivun osoitteen otsikossa `Origin`. Render-osoitteen laittaminen ainoaksi sallituksi arvoksi estää localhost-sivun, koska `Origin` ei ole Renderin osoite.

Annotaatio yksin ei vastaa selaimen `OPTIONS`-kyselyyn. `SecurityFilterChain` näkee sen ennen kontrolleria. Kyselyssä ei ole Basic-otsikkoa, joten Security vastaa `401` eikä lisää otsikkoa `Access-Control-Allow-Origin`. Siksi listan kohta 2 tarvitaan annotaation lisäksi.

### Rivi luokkaan SecurityConfig

Metodiin `filterChain`, ensimmäiseksi kutsuksi `http`-oliolle:

```java
http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        // entinen sessionManagement, authorizeHttpRequests, httpBasic
        ;
```

Kun erillistä `CorsConfigurationSource`-oliota ei ole, tämä rivi lukee kontrollereiden `@CrossOrigin`-asetukset. Ilman annotaatiota `OPTIONS`-vastauksesta puuttuu otsikko `Access-Control-Allow-Origin`. Ilman riviä annotaatio ei ehdi mukaan.

Vanhaa muotoa `http.cors().and()` ei käytetä tässä Boot-versiossa. Rivi kirjoitetaan samalla tavalla kuin nykyinen `.httpBasic(Customizer.withDefaults())`.

Lipun polut lisätään samaan listaan ennen yleistä `/api/**`-sääntöä:

```java
.requestMatchers(HttpMethod.GET, "/api/tickets")
        .hasAnyRole("MYYJA", "TAPAHTUMAKOORDINAATTORI")
.requestMatchers(HttpMethod.PATCH, "/api/tickets/*")
        .hasAnyRole("MYYJA", "TAPAHTUMAKOORDINAATTORI")
```

Kutsua `allowCredentials(true)` ei lisätä. Se tarkoittaa, että selain saisi lähettää evästeet (cookies). Basic kulkee otsikossa `Authorization`, ei evästeenä. Tähti `origins = "*"` ja evästeet yhdessä on kielletty yhdistelmä.

---

## Dia 6 — Mitä konsolissa näkyy

| Havainto | Syy |
| --- | --- |
| Postman palauttaa `200`, selaimen konsoli sanoo että CORS esti kutsun | Postman ei lähetä `OPTIONS`-kyselyä. Tarkistuslistasta puuttuu annotaatio, `.cors(Customizer.withDefaults())` tai molemmat. |
| Selaimen Network-välilehdellä `OPTIONS` ja status `401` | Spring Security vastasi ennen kuin kontrollerin annotaatio ehti mukaan. `.cors(Customizer.withDefaults())` puuttuu. |
| `OPTIONS` palautuu ilman otsikkoa `Access-Control-Allow-Origin` | Security-rivi on paikallaan, kontrollerista puuttuu `@CrossOrigin`. |
| `OPTIONS` ja viesti `Invalid CORS request` | Sivun osoite tai metodi `GET`/`PATCH` ei ole luvassa. `localhost` ja `127.0.0.1` ovat eri nimiä. |
| `fetch` palauttaa `401` ja vastauksen tekstin näkee | Selain pääsi läpi. Otsikosta puuttuu sana `Basic`, tai tunnus on väärä. |
| `403` | Tunnus meni läpi. Roolisääntö ei anna tälle tunnukselle polkua. Jos lipun sääntö on listassa vasta yleisen `/api/**`-säännön jälkeen, kutsu ei tule tähän tarkistukseen vaan läpi jokaiselta kirjautuneelta. |
| `404` | Koodia tai id:tä ei ole. Selain on jo hyväksynyt kutsun. Virhe tulee palvelimelta. |
| Konsolissa `Failed to fetch`, eikä pyyntö näy palvelimen lokissa | Sivu on avattu tiedostona levyltä, tai julkaistu palvelu ei ole käynnissä. |

---

## Dia 7 — Näin staattinen sivusto (static content) laitetaan Spring Bootiin

Näin full stack -sovellus tarjoaa sivun samasta ohjelmasta kuin rajapinnan. Tämän viikon sprintissä tiedostoa ei lisätä, koska client on erillinen sivu.

Boot palvelee tiedostoja ilman kontrolleria näistä kansioista:

- `src/main/resources/static/`
- `src/main/resources/public/`

`src/main/resources/static/index.html` aukeaa osoitteessa `http://localhost:8080/`. Kansiota `src/main/webapp` ei käytetä. Boot ei lue sitä tästä projektista.

Nykyinen `SecurityFilterChain` päästää muun kuin `/api/**`-polun läpi (`anyRequest().permitAll()`). HTML aukeaisi ilman tunnusta. Rajapinta vaatii tunnuksen yhä. Sivu samassa Bootissa olisi samaa alkuperää rajapinnan kanssa, eikä selain tekisi CORS-tarkistusta. Sprintin sivu tulee localhostista, HTML-tiedostona tai Reactin kehityspalvelimesta, ja kutsuu Renderiä. Siksi dia 5 jää voimaan.

### Myöhemmin oma React-sivu samaan ohjelmaan

Kun tämä tiimi tekee myös oman clientin, sivu ja rajapinta voivat olla sama Spring Boot. Reactin lähdekoodia ei laiteta kansioon `static`. Sinne tulee käännöksen (build) tulos.

1. React-projekti on oma kansionsa. Kehityksen aikana sivu aukeaa osoitteesta `http://localhost:5173` tai `http://localhost:3000` ja `fetch` kutsuu rajapintaa. Alkuperät eroavat, joten dian 5 CORS jää kehitykseen.
2. Julkaisuun ajetaan käännös. Vite kirjoittaa kansion `dist/`, Create React App kansion `build/`. Siinä on `index.html` sekä tuotetut JavaScript- ja CSS-tiedostot. `node_modules`-kansiota ja `.jsx`-lähteitä ei ole mukana.
3. Kansion sisältö kopioidaan hakemistoon `src/main/resources/static/`. Julkaistu sivu aukeaa osoitteesta `https://nimi.onrender.com/` ja rajapinta osoitteesta `https://nimi.onrender.com/api/tickets`. Alkuperä on sama, joten tämä julkaistu sivu ei tarvitse CORS-tarkistusta.
4. `fetch`-osoite voi olla suhteellinen (relative) `/api/tickets?koodi=`. Otsikko `Authorization: Basic` tarvitaan yhä, koska rajapinta on suojattu.

Jos Reactin osoitteissa on polkuja, jotka eivät ole tiedostoja, Boot ei löydä niitä ja vastaa `404`. Silloin tuntematon polku ohjataan tiedostoon `index.html`, jotta selain voi avata saman sivun uudelleen.

---

## Dia 8 — QR-koodi: selain (client) vai palvelin (server)

Lippukoodi on merkkijono `koodi`. Sen luo myynti. QR-koodi on sen merkkijonon kuva. Ovitarkastuksen sivu lukee kuvasta merkkijonon ja lähettää sen dian 2 `GET`-kutsuun.

Tiimi päättää itse, piirretäänkö kuva selaimessa vai palvelimella, ja kirjoittaa perustelun. Luento ei valitse puolta. Perustelussa käydään läpi ainakin nämä:

- Kuka kuvaa tarvitsee: kassanäkymä, tulostus, sähköposti, puhelin, ovitarkastus.
- Mitä rajapinta palauttaa, jos kuva tehdään palvelimella, ja mitä sivu tekee, jos kuva tehdään selaimessa.
- Miten käy sähköpostille tai kuittitulostimelle, jossa ei ole selainta.
- Mitä kirjastoja, välimuistia ja uusia osoitteita ratkaisu tuo.
- Mitä tapahtuu, jos kuvaan päätyy eri merkkijono kuin tietokantaan.

Vastaus on tiimin oma suositus ja sen perustelu.

Bonus, jos aikaa jää: pieni kokeilu siitä ratkaisusta, jonka tiimi valitsi. Dian 9 lukeminen on client-tiimin bonus.

---

## Dia 9 — Bonuksena kuvan lukeminen

Luku tapahtuu sillä laitteella, jossa kamera on. Sen tekee tiimi, joka kirjoittaa erillisen sivun. HTML ja JavaScript riittävät.

Chromessa ja Edgessä on [Barcode Detection API](https://developer.mozilla.org/en-US/docs/Web/API/Barcode_Detection_API). `BarcodeDetector` luodaan muodolla `qr_code`. Selain pyytää lupaa kameraan (`getUserMedia`), ja luettu teksti on sama `koodi` joka menisi dian 4 hakuun.

Kamera aukeaa osoitteessa `https://` tai `http://localhost`. Levyltä avattu tiedosto ei saa lupaa. Firefoxissa tätä rajapintaa ei ole. Silloin lukemiseen tarvitaan erillinen kirjasto, tai koodi kirjoitetaan kenttään.

---

## Mitä tähän repositorioon ei kirjoiteta

- Tämän viikon client-sivua. Sen tekee toinen tiimi. Myöhemmin oma React-käännös voidaan kopioida kansioon `static`, kuten dia 7 kertoo. Lähdekoodia ja `node_modules`-kansiota ei kopioida.
- Uutta tunnistusta. HTTP Basic, käyttäjätaulu `Kayttaja` ja roolit `MYYJA` sekä `TAPAHTUMAKOORDINAATTORI` jäävät.
- Toista CORS-asetusoliota. Tarkistuslistan annotaatio ja `.cors(Customizer.withDefaults())` riittävät.
- Lippukoodin luontia tarkastuksen osoitteeseen. Koodin luo myynti.
- Erillistä käyttöaikaa. Tietokannassa on kenttä `kaytetty`.
