package fi.haagahelia.ticketguru.web;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class TapahtumaApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void demoDataIsListedAndSingleEventCanBeFetched() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].nimi").exists());

        mockMvc.perform(get("/api/events").param("kaupunki", "Tampere"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].kaupunki").value("Tampere"));
    }

    @Test
    void createUpdateAndDeleteRoundTripPersistsToDatabase() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nimi": "Sprint 3 demo",
                          "aika": "2026-12-01T18:00:00",
                          "kaupunki": "Espoo",
                          "paikka": "Sellosali",
                          "lippujaKpl": 80
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nimi").value("Sprint 3 demo"))
                .andReturn();

        long id = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/api/events/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paikka").value("Sellosali"));

        mockMvc.perform(put("/api/events/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nimi": "Sprint 3 demo muokattu",
                          "aika": "2026-12-01T19:30:00",
                          "kaupunki": "Vantaa",
                          "paikka": "Flamingo",
                          "lippujaKpl": 90
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nimi").value("Sprint 3 demo muokattu"))
                .andExpect(jsonPath("$.kaupunki").value("Vantaa"));

        mockMvc.perform(delete("/api/events/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/events/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingEventWithSalesReturnsConflict() throws Exception {
        MvcResult list = mockMvc.perform(get("/api/events").param("kaupunki", "Helsinki"))
                .andExpect(status().isOk())
                .andReturn();
        long id = ((Number) JsonPath.read(list.getResponse().getContentAsString(), "$[0].id")).longValue();

        mockMvc.perform(delete("/api/events/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
