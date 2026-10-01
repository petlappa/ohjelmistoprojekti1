package fi.haagahelia.ticketguru.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class StatusCodeApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String TAPAHTUMA = """
            {
              "nimi": "Statuskoodi",
              "aika": "2026-12-01T18:00:00",
              "kaupunki": "Oulu",
              "paikka": "Tullisali",
              "lippujaKpl": 40
            }
            """;

    @Test
    void missingPathIdIs404() throws Exception {
        mockMvc.perform(get("/api/events/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(put("/api/events/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TAPAHTUMA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(delete("/api/events/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(get("/api/sales/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(get("/api/events/999999/ticket-types"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void invalidPathIdAndBodyAre400() throws Exception {
        mockMvc.perform(get("/api/events/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"));

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON"));

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nimi": "Rikki",
                          "aika": "ei-pvm",
                          "kaupunki": "Oulu",
                          "paikka": "Tullisali",
                          "lippujaKpl": 10
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(put("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "nimi": "Puuttuu loput" }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unknownPathIs404AndWrongMethodIs405() throws Exception {
        mockMvc.perform(get("/api/ei-ole"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(patch("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void successfulWritesUse201And204() throws Exception {
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TAPAHTUMA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }
}
