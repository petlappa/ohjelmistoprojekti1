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

## Dia 4 — Palvelimen neljä osaa

Clientin `fetch` päättyy tähän. Palvelin on tilaton: istuntoa ei luoda, ja jokainen pyyntö tarkistetaan suodattimella. Koodi ei ole vielä tässä repositoriossa. Neljä osaa riittää.

### Riippuvuudet

`spring-boot-starter-security` on sama kuin Basicissa. JWT:n allekirjoitus tulee kirjastosta JJWT. Pelkkä `jjwt-api` ei riitä ajoon, vaan mukaan tarvitaan myös toteutus ja JSON-kirjasto.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### Suodatin

`JwtRequestFilter` perii `OncePerRequestFilter`-luokan. Se lukee otsikon, tarkistaa etuliitteen `Bearer `, validoi tokenin ja asettaa käyttäjän kontekstiin. Roolia ei lueta tokenista vaan haetaan uudelleen `UserDetailsService`-palvelulla, joka lukee taulun `Kayttaja`. Näin rooli on sama rivi kuin Basicissa.

`JwtTokenProvider` on tiimin oma luokka, ei Springin. Se tekee kolme asiaa: `generateToken`, `validateToken` ja `getUsernameFromToken`.

```java
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (tokenProvider.validateToken(token)) {
                String kayttajanimi = tokenProvider.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(kayttajanimi);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

Jos otsikko puuttuu tai token on väärä, suodatin ei aseta käyttäjää. Ketju jatkuu, ja seuraava vaihe vastaa `401`.

### SecurityConfig

Istuntoa ei luoda ja CSRF on pois päältä, kuten Basicin luennossa. `POST /api/login` on julkinen. Muut osoitteet käyttävät samoja `hasRole`-sääntöjä. Suodatin ajetaan ennen Springin oletussuodatinta `UsernamePasswordAuthenticationFilter`.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/refresh-token").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/**")
                    .hasAnyRole("MYYJA", "TAPAHTUMAKOORDINAATTORI")
                .requestMatchers(HttpMethod.POST, "/api/events/**")
                    .hasRole("TAPAHTUMAKOORDINAATTORI")
                .requestMatchers(HttpMethod.POST, "/api/sales").hasRole("MYYJA")
                .anyRequest().authenticated())
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
```

`AuthorizationFilter` ajaa `hasRole`-rivit sen jälkeen, kun suodatin on täyttänyt kontekstin. Token puuttuu, on väärä tai vanha: `401`. Käyttäjä on oikea, mutta osoite on kielletty: `403`.

### Kirjautuminen

`AuthController` tarkistaa tunnuksen ja salasanan `AuthenticationManager`-oliolla. Se käyttää samaa `UserDetailsService`-palvelua ja `PasswordEncoder`-luokkaa kuin Basic. Onnistuneesta tarkistuksesta syntyy token, joka palautetaan kentässä `token`. Rekisteröintiosoitetta ei ole: käyttäjät ovat jo taulussa `Kayttaja`.

```java
@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.kayttajanimi(), request.salasana()));
        String token = tokenProvider.generateToken(authentication.getName());
        return ResponseEntity.ok(new JwtResponse(token));
    }
}
```

`LoginRequest` on record, jossa on kentät `kayttajanimi` ja `salasana`. `JwtResponse` on record, jossa on kenttä `token`. Clientin dia 2 lukee juuri tämän kentän.

### Miten token syntyy

Palvelin ei tallenna tokenia muistiin eikä tietokantaan. Se palauttaa allekirjoitetun merkkijonon. Merkkijonossa on kolme osaa, pisteellä erotettuna: `header.payload.signature`.

| Osa | Sisältö |
| --- | --- |
| Header | Algoritmi, tässä `HS256`, ja tyyppi `JWT`. |
| Payload | Käyttäjänimi kentässä `sub`, luontiaika `iat` ja vanheneminen `exp`. |
| Signature | HMAC-SHA256 laskettuna kahdesta ensimmäisestä osasta ja salaisesta avaimesta, jonka vain palvelin tietää. |

Luonti etenee näin:

1. `AuthenticationManager` on jo tarkistanut salasanan hashin taulusta `Kayttaja`.
2. Payloadiin laitetaan `kayttajanimi` ja vanheneminen. Tunnin mittainen token riittää esimerkissä. Salasanaa ei laiteta mukaan.
3. Header ja payload muutetaan Base64URL-merkkijonoiksi.
4. Allekirjoitus on `HMACSHA256(header + "." + payload, salainenAvain)`.
5. Kolme osaa palautetaan JSON-kentässä `token`.

```java
@Component
public class JwtTokenProvider {

    private final Key secretKey;
    private final long validityInMilliseconds = 3_600_000;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(String kayttajanimi) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(kayttajanimi)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
```

Avain luetaan asetuksesta `app.jwt.secret`. Se on sama joka käynnistyksellä, jotta `validateToken` tunnistaa aiemmin annetun tokenin. `Keys.secretKeyFor(...)` loisi uuden avaimen joka käynnistyksellä, ja vanhat tokenit lakkaisivat toimimasta.

Allekirjoitus ei salaa payloadia. Base64URL:n purkaa kuka tahansa, joten tokenista näkee tunnuksen ja ajat. Salasanaa tai muuta salaista ei siis kirjoiteta payloadiin. Väärennös estetään sillä, että vain palvelin tietää avaimen. Tokenia ei voi perua palvelimelta ilman erillistä listaa, joten `exp` pidetään lyhyenä. Yksinkertainen client kirjautuu tunnin jälkeen uudelleen. Virkistystoken on diassa 5, jos salasanaa ei haluta kysyä uudestaan.

---

## Dia 5 — Virkistystoken

`JwtRequestFilter` tarkistaa tavallisen pyynnön access-tokenin jo diassa 4. Virkistys on eri kutsu. Sillä tasapainotetaan kahta asiaa: varastettu token kelpaa vain vähän aikaa, mutta käyttäjän ei tarvitse kirjoittaa salasanaa varttitunnin välein.

| Token | Käyttöaika | Mihin se lähetetään |
| --- | --- | --- |
| Access token | lyhyt, esimerkiksi 15 minuuttia | jokaiseen tavalliseen pyyntöön, otsikkona `Bearer` |
| Refresh token | pitkä, esimerkiksi 7–30 päivää | vain osoitteeseen `POST /api/refresh-token` |

Kulku:

1. `POST /api/login` palauttaa molemmat: `accessToken` ja `refreshToken`.
2. Tavalliset kutsut käyttävät vain access-tokenia. `JwtRequestFilter` tarkistaa sen allekirjoituksen ja vanhenemisen.
3. Kun access-token on vanha, palvelin vastaa `401`. `403` ei ole vanheneminen: rooli ei riitä, eikä silloin virkistetä.
4. Client lähettää refresh-tokenin osoitteeseen `POST /api/refresh-token`. Palvelin tarkistaa sen ja palauttaa uuden access-tokenin.
5. Client tallentaa uuden access-tokenin ja tekee alkuperäisen pyynnön uudelleen.
6. Jos myös refresh-token on vanha, virkistys vastaa `401`. Client poistaa molemmat ja palaa kirjautumiseen.

Virkistysosoite on julkinen samalla tavalla kuin login, koska vanhentunut access-token ei läpäise `hasRole`-sääntöjä. Tarkistus tehdään kontrollerissa refresh-tokenille itselleen.

```java
@PostMapping("/refresh-token")
public ResponseEntity<JwtResponse> refresh(@RequestBody RefreshRequest request) {
    if (!tokenProvider.validateToken(request.refreshToken())) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Virkistystoken ei kelpaa");
    }
    String kayttajanimi = tokenProvider.getUsernameFromToken(request.refreshToken());
    return ResponseEntity.ok(new JwtResponse(tokenProvider.generateToken(kayttajanimi)));
}
```

Yksinkertainen client dioissa 2 ja 3 ei tee tätä. Se kirjautuu uudelleen, kun access-token vanhenee. Virkistys kannattaa vasta, kun lomaketta ei haluta näyttää kesken myyntiä.

Refresh-tokenia ei lähetetä jokaiseen pyyntöön. Jos se on vain allekirjoitettu merkkijono eikä sitä ole kannassa, sitä ei voi perua ennen `exp`-aikaa. Peruutus onnistuu, jos palvelin tallentaa refresh-tokenin ja poistaa rivin uloskirjautuessa. Access-tokenia ei silti tallenneta.

---

## Dia 6 — CORS, kun client on eri osoitteessa

Selain ei lähetä pyyntöä toiselle palvelimelle, ennen kuin backend lupaa. Ensimmäinen kutsu on `OPTIONS`. Siinä selain kysyy, saako tämä sivu lähettää otsikon `Authorization`.

Lupa on backend-tiimin työtä. Tyypillinen kehitystilanne on client portissa `3000` ja Spring Boot portissa `8080`. Sallittujen listaan tulevat client-tiimin osoite, metodit ja otsikko `Authorization`. `allowedHeaders("*")` sallii otsikon, samoin pelkkä `Authorization`. Ilman lupaa selain pysäyttää kutsun, vaikka token olisi oikein ja `fetch` olisi kirjoitettu kuten diassa 3.

Postman ja curl eivät lähetä `OPTIONS`-kyselyä. Niillä rajapinta voi toimia samalla tunnuksella, jolla selain vielä estää clientin.

---

## Dia 7 — Jos toinen tiimi teki Basicin

Silloin kirjautumiskutsua ei ole. Client lähettää tunnuksen ja salasanan joka pyynnössä:

```javascript
headers: {
  Authorization: "Basic " + btoa(kayttajanimi + ":" + salasana)
}
```

Selain ei lisää otsikkoa itse, kun client on oma sivu eikä selaimen kirjautumisikkuna. `btoa` tekee saman Base64-muunnoksen, jonka Postmanin Basic Auth tekee. CORS tarvitaan tässäkin, koska otsikko on yhä `Authorization`.

TicketGurun oma suositus palvelimelle on Basic, kunnes web-client oikeasti tulee. Tämä viikko on sitä tilannetta varten, että vastaan tuleva tiimi on jo valinnut JWT:n. Clientin työmäärä on silti samaa luokkaa: muutama `fetch`.

---

## Dia 8 — Mitä clientiin ei kirjoiteta

- Tokenin allekirjoitusta tai salaisuutta. Ne ovat palvelimella.
- `hasRole`-sääntöjä. `403` tulee palvelimelta.
- Listaa muiden käyttäjien tunnuksista.
- Istuntoa tai evästettä, jolla palvelin muistaisi kirjautumisen.
- Omaa käyttäjätaulua.

Monimutkaisuus on palvelimessa, joka kirjoittaa tokenin ja tarkistaa sen. Client vastaanottaa merkkijonon ja lähettää sen takaisin.
