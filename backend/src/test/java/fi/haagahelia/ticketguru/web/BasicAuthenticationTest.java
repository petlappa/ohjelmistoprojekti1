package fi.haagahelia.ticketguru.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BasicAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String TAPAHTUMA = """
            {
              "nimi": "Basic demo",
              "aika": "2026-12-01T18:00:00",
              "kaupunki": "Espoo",
              "paikka": "Sellosali",
              "lippujaKpl": 40
            }
            """;

    @Test
    void missingCredentialsAre401() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIs401() throws Exception {
        mockMvc.perform(get("/api/events").with(httpBasic("myyja", "vaarin")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sellerCanListEventsButCannotCreateOne() throws Exception {
        mockMvc.perform(get("/api/events").with(BasicAuth.myyja()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/events")
                .with(BasicAuth.myyja())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TAPAHTUMA))
                .andExpect(status().isForbidden());
    }

    @Test
    void coordinatorCanCreateEventButCannotSell() throws Exception {
        mockMvc.perform(post("/api/events")
                .with(BasicAuth.koordinaattori())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TAPAHTUMA))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/sales")
                .with(BasicAuth.koordinaattori())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "tapahtumaId": 1, "myyjaId": 1, "rivit": [] }
                        """))
                .andExpect(status().isForbidden());
    }
}
