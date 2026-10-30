package fi.haagahelia.ticketguru.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.jayway.jsonpath.JsonPath;

import fi.haagahelia.ticketguru.repository.KayttajaRepository;
import fi.haagahelia.ticketguru.web.dto.LippuKayttoRequest;
import fi.haagahelia.ticketguru.web.dto.LipputyyppiRequest;
import fi.haagahelia.ticketguru.web.dto.MyyntiRequest;
import fi.haagahelia.ticketguru.web.dto.MyyntiRiviRequest;
import fi.haagahelia.ticketguru.web.dto.TapahtumaRequest;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class MyyntiE2ETest {

    @Autowired
    private RestTestClient client;

    @Autowired
    private KayttajaRepository kayttajaRepository;

    @Test
    void myyjaSaaKuitin() {
        long tapahtumaId = luoTapahtuma(5);
        long tyyppiId = luoTyyppi(tapahtumaId);
        long myyjaId = kayttajaRepository.findByKayttajanimi("myyja").orElseThrow().getId();

        String body = client.post()
                .uri("/api/sales")
                .header("Authorization", basic("myyja", "salasana"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new MyyntiRequest(tapahtumaId, myyjaId, List.of(new MyyntiRiviRequest(tyyppiId, 1))))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(new BigDecimal(JsonPath.read(body, "$.summa").toString())).isEqualByComparingTo("12.50");
        assertThat((String) JsonPath.read(body, "$.liput[0].koodi")).isNotBlank();
        assertThat((Boolean) JsonPath.read(body, "$.liput[0].kaytetty")).isFalse();
    }

    @Test
    void myytyLippuMerkitaanKaytetyksi() {
        long tapahtumaId = luoTapahtuma(5);
        long tyyppiId = luoTyyppi(tapahtumaId);
        long myyjaId = kayttajaRepository.findByKayttajanimi("myyja").orElseThrow().getId();

        String kuitti = client.post()
                .uri("/api/sales")
                .header("Authorization", basic("myyja", "salasana"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new MyyntiRequest(tapahtumaId, myyjaId, List.of(new MyyntiRiviRequest(tyyppiId, 1))))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        long lippuId = ((Number) JsonPath.read(kuitti, "$.liput[0].id")).longValue();
        String koodi = JsonPath.read(kuitti, "$.liput[0].koodi");

        client.patch()
                .uri("/api/tickets/{id}", lippuId)
                .header("Authorization", basic("myyja", "salasana"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LippuKayttoRequest(true))
                .exchange()
                .expectStatus().isOk();

        String tarkastus = client.get()
                .uri("/api/tickets?koodi={koodi}", koodi)
                .header("Authorization", basic("myyja", "salasana"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        assertThat((Boolean) JsonPath.read(tarkastus, "$.kaytetty")).isTrue();

        client.patch()
                .uri("/api/tickets/{id}", lippuId)
                .header("Authorization", basic("myyja", "salasana"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LippuKayttoRequest(true))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void koordinaattoriEiVoiMyyda() {
        client.post()
                .uri("/api/sales")
                .header("Authorization", basic("koordinaattori", "salasana"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new MyyntiRequest(1L, 1L, List.of(new MyyntiRiviRequest(1L, 1))))
                .exchange()
                .expectStatus().isForbidden();
    }

    private long luoTapahtuma(int lippujaKpl) {
        String body = client.post()
                .uri("/api/events")
                .header("Authorization", basic("koordinaattori", "salasana"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TapahtumaRequest(
                        "E2E myynti",
                        LocalDateTime.of(2026, 12, 1, 18, 0),
                        "Oulu",
                        "Torilla",
                        lippujaKpl))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long luoTyyppi(long tapahtumaId) {
        String body = client.post()
                .uri("/api/events/{id}/ticket-types", tapahtumaId)
                .header("Authorization", basic("koordinaattori", "salasana"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LipputyyppiRequest("Ovi", new BigDecimal("12.50")))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private static String basic(String tunnus, String salasana) {
        String token = Base64.getEncoder().encodeToString(
                (tunnus + ":" + salasana).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}
