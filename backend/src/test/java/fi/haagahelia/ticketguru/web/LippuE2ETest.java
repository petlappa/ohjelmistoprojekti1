package fi.haagahelia.ticketguru.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class LippuE2ETest {

    @Autowired
    private RestTestClient client;

    @Test
    void tuntematonKoodiOn404() {
        client.get()
                .uri("/api/tickets?koodi={koodi}", "ei-ole")
                .header("Authorization", basic("myyja", "salasana"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void esimerkkikoodiLoytyy() {
        String body = client.get()
                .uri("/api/tickets?koodi={koodi}", "3er454aa")
                .header("Authorization", basic("myyja", "salasana"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat((String) JsonPath.read(body, "$.koodi")).isEqualTo("3er454aa");
        assertThat((Boolean) JsonPath.read(body, "$.kaytetty")).isFalse();
    }

    @Test
    void ilmanTunnustaOn401() {
        client.get()
                .uri("/api/tickets?koodi={koodi}", "3er454aa")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private static String basic(String tunnus, String salasana) {
        String token = Base64.getEncoder().encodeToString(
                (tunnus + ":" + salasana).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}
