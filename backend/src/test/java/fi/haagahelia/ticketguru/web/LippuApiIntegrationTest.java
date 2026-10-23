package fi.haagahelia.ticketguru.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import fi.haagahelia.ticketguru.repository.KayttajaRepository;

@SpringBootTest
@AutoConfigureMockMvc
class LippuApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KayttajaRepository kayttajaRepository;

    private long myyjaId;

    @BeforeEach
    void loadSeller() {
        myyjaId = kayttajaRepository.findByKayttajanimi("myyja").orElseThrow().getId();
    }

    @Test
    void missingCredentialsAre401() throws Exception {
        mockMvc.perform(get("/api/tickets").param("koodi", "3er454aa"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sellerAndCoordinatorCanReadTicketByCode() throws Exception {
        mockMvc.perform(get("/api/tickets").param("koodi", "3er454aa").with(BasicAuth.myyja()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.koodi").value("3er454aa"))
                .andExpect(jsonPath("$.kaytetty").value(false))
                .andExpect(jsonPath("$.lipputyypinKuvaus").value("Aikuinen"));

        mockMvc.perform(get("/api/tickets").param("koodi", "3er454aa").with(BasicAuth.koordinaattori()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.koodi").value("3er454aa"));
    }

    @Test
    void missingOrUnknownCodeIsClientError() throws Exception {
        mockMvc.perform(get("/api/tickets").with(BasicAuth.myyja()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(get("/api/tickets").param("koodi", "ei-ole").with(BasicAuth.myyja()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void patchMarksTicketUsedAndSecondMarkConflicts() throws Exception {
        long tapahtumaId = createEvent();
        long tyyppiId = createType(tapahtumaId);
        MvcResult sale = mockMvc.perform(post("/api/sales").with(BasicAuth.myyja())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "tapahtumaId": %d,
                          "myyjaId": %d,
                          "rivit": [ { "lipputyyppiId": %d, "kpl": 1 } ]
                        }
                        """.formatted(tapahtumaId, myyjaId, tyyppiId)))
                .andExpect(status().isCreated())
                .andReturn();
        String body = sale.getResponse().getContentAsString();
        long lippuId = ((Number) JsonPath.read(body, "$.liput[0].id")).longValue();
        String koodi = JsonPath.read(body, "$.liput[0].koodi");

        mockMvc.perform(patch("/api/tickets/" + lippuId).with(BasicAuth.myyja())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "kaytetty": true }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lippuId))
                .andExpect(jsonPath("$.kaytetty").value(true));

        mockMvc.perform(get("/api/tickets").param("koodi", koodi).with(BasicAuth.koordinaattori()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kaytetty").value(true));

        mockMvc.perform(patch("/api/tickets/" + lippuId).with(BasicAuth.koordinaattori())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "kaytetty": true }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void alreadyUsedDemoTicketAndInvalidBody() throws Exception {
        MvcResult found = mockMvc.perform(get("/api/tickets").param("koodi", "3er454ac").with(BasicAuth.myyja()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kaytetty").value(true))
                .andReturn();
        long id = ((Number) JsonPath.read(found.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(patch("/api/tickets/" + id).with(BasicAuth.myyja())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "kaytetty": true }
                        """))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/tickets/" + id).with(BasicAuth.myyja())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "kaytetty": false }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(patch("/api/tickets/999999").with(BasicAuth.myyja())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "kaytetty": true }
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void browserPreflightFromLocalhostIsAllowed() throws Exception {
        mockMvc.perform(options("/api/tickets")
                .header("Origin", "http://localhost:5500")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }

    private long createEvent() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/events").with(BasicAuth.koordinaattori())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nimi": "Lipuntarkastus",
                          "aika": "2026-12-24T18:00:00",
                          "kaupunki": "Oulu",
                          "paikka": "Madetoja",
                          "lippujaKpl": 10
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long createType(long tapahtumaId) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/events/" + tapahtumaId + "/ticket-types")
                .with(BasicAuth.koordinaattori())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "kuvaus": "Ovi", "hinta": 12.50 }
                        """))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();
    }
}
