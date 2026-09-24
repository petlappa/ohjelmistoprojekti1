package fi.haagahelia.ticketguru.web;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

import fi.haagahelia.ticketguru.repository.KayttajaRepository;

@SpringBootTest
@AutoConfigureMockMvc
class MyyntiApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void demoEventListsTicketTypes() throws Exception {
        long tapahtumaId = helsinkiEventId();

        mockMvc.perform(get("/api/events/" + tapahtumaId + "/ticket-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].kuvaus").exists())
                .andExpect(jsonPath("$[0].hinta").exists());
    }

    @Test
    void createTicketTypeThenSaleReturnsReceipt() throws Exception {
        long tapahtumaId = createEvent(2);

        MvcResult type = mockMvc.perform(post("/api/events/" + tapahtumaId + "/ticket-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "kuvaus": "Opiskelija", "hinta": 10.00 }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.kuvaus").value("Opiskelija"))
                .andReturn();
        long tyyppiId = idOf(type);

        MvcResult sale = mockMvc.perform(post("/api/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "tapahtumaId": %d,
                          "myyjaId": %d,
                          "rivit": [ { "lipputyyppiId": %d, "kpl": 2 } ]
                        }
                        """.formatted(tapahtumaId, myyjaId, tyyppiId)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.summa").value(20.00))
                .andExpect(jsonPath("$.tapahtumanNimi").value("Sprint 4 kapasiteetti"))
                .andExpect(jsonPath("$.myyjanNimi").value("Maija Myyjä"))
                .andExpect(jsonPath("$.myyntiaika").exists())
                .andExpect(jsonPath("$.liput", hasSize(2)))
                .andExpect(jsonPath("$.liput[0].koodi").exists())
                .andExpect(jsonPath("$.liput[0].lipputyypinKuvaus").value("Opiskelija"))
                .andReturn();

        long saleId = idOf(sale);
        mockMvc.perform(get("/api/sales/" + saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saleId))
                .andExpect(jsonPath("$.liput", hasSize(2)));
    }

    @Test
    void saleOverCapacityReturnsConflict() throws Exception {
        long tapahtumaId = createEvent(1);
        long tyyppiId = createType(tapahtumaId);

        mockMvc.perform(post("/api/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(saleBody(tapahtumaId, myyjaId, tyyppiId, 2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void ticketTypeFromAnotherEventReturnsBadRequest() throws Exception {
        long tapahtumaA = createEvent(10);
        long tapahtumaB = createEvent(10);
        long tyyppiB = createType(tapahtumaB);

        mockMvc.perform(post("/api/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(saleBody(tapahtumaA, myyjaId, tyyppiB, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unknownSellerOrSaleReturnsClientError() throws Exception {
        long tapahtumaId = createEvent(5);
        long tyyppiId = createType(tapahtumaId);

        mockMvc.perform(post("/api/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(saleBody(tapahtumaId, 999999L, tyyppiId, 1)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/sales/999999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/events/999999/ticket-types"))
                .andExpect(status().isNotFound());
    }

    private long helsinkiEventId() throws Exception {
        MvcResult list = mockMvc.perform(get("/api/events").param("kaupunki", "Helsinki"))
                .andExpect(status().isOk())
                .andReturn();
        return ((Number) JsonPath.read(list.getResponse().getContentAsString(), "$[0].id")).longValue();
    }

    @Autowired
    private KayttajaRepository kayttajaRepository;

    private long myyjaId;

    @org.junit.jupiter.api.BeforeEach
    void loadSeller() {
        myyjaId = kayttajaRepository.findByKayttajanimi("myyja").orElseThrow().getId();
    }

    private long createEvent(int kapasiteetti) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nimi": "Sprint 4 kapasiteetti",
                          "aika": "2026-12-24T18:00:00",
                          "kaupunki": "Turku",
                          "paikka": "Logomo",
                          "lippujaKpl": %d
                        }
                        """.formatted(kapasiteetti)))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(created);
    }

    private long createType(long tapahtumaId) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/events/" + tapahtumaId + "/ticket-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "kuvaus": "Aikuinen", "hinta": 12.50 }
                        """))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(created);
    }

    private static String saleBody(long tapahtumaId, long myyjaId, long tyyppiId, int kpl) {
        return """
                {
                  "tapahtumaId": %d,
                  "myyjaId": %d,
                  "rivit": [ { "lipputyyppiId": %d, "kpl": %d } ]
                }
                """.formatted(tapahtumaId, myyjaId, tyyppiId, kpl);
    }

    private static long idOf(MvcResult result) throws Exception {
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }
}
