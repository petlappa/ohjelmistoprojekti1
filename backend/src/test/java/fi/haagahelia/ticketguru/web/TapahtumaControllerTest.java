package fi.haagahelia.ticketguru.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import fi.haagahelia.ticketguru.service.TapahtumaService;
import fi.haagahelia.ticketguru.web.dto.TapahtumaResponse;

@WebMvcTest(TapahtumaController.class)
class TapahtumaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TapahtumaService tapahtumaService;

    private static final TapahtumaResponse TAPAHTUMA_A = new TapahtumaResponse(
            1L,
            "Tapahtuma A",
            LocalDateTime.of(2026, 10, 2, 17, 0),
            "Helsinki",
            "Kulttuuritalo",
            200);

    @Test
    void listReturnsEvents() throws Exception {
        when(tapahtumaService.findAll(null)).thenReturn(List.of(TAPAHTUMA_A));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nimi").value("Tapahtuma A"))
                .andExpect(jsonPath("$[0].kaupunki").value("Helsinki"));
    }

    @Test
    void listFiltersByKaupunki() throws Exception {
        when(tapahtumaService.findAll("Helsinki")).thenReturn(List.of(TAPAHTUMA_A));

        mockMvc.perform(get("/api/events").param("kaupunki", "Helsinki"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nimi").value("Tapahtuma A"));
    }

    @Test
    void getReturnsEvent() throws Exception {
        when(tapahtumaService.findById(1L)).thenReturn(TAPAHTUMA_A);

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paikka").value("Kulttuuritalo"))
                .andExpect(jsonPath("$.lippujaKpl").value(200));
    }

    @Test
    void getUnknownIdReturns404() throws Exception {
        when(tapahtumaService.findById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Tapahtumaa ei löydy: 99"));

        mockMvc.perform(get("/api/events/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.messages[0]").value("Tapahtumaa ei löydy: 99"));
    }

    @Test
    void createReturns201AndLocation() throws Exception {
        when(tapahtumaService.create(any())).thenReturn(TAPAHTUMA_A);

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nimi": "Tapahtuma A",
                          "aika": "2026-10-02T17:00:00",
                          "kaupunki": "Helsinki",
                          "paikka": "Kulttuuritalo",
                          "lippujaKpl": 200
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/events/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createInvalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nimi": "",
                          "kaupunki": "Helsinki"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void updateReturns200() throws Exception {
        TapahtumaResponse updated = new TapahtumaResponse(
                1L,
                "Tapahtuma A muokattu",
                LocalDateTime.of(2026, 10, 2, 18, 0),
                "Espoo",
                "Sellosali",
                150);
        when(tapahtumaService.update(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nimi": "Tapahtuma A muokattu",
                          "aika": "2026-10-02T18:00:00",
                          "kaupunki": "Espoo",
                          "paikka": "Sellosali",
                          "lippujaKpl": 150
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nimi").value("Tapahtuma A muokattu"))
                .andExpect(jsonPath("$.kaupunki").value("Espoo"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteWithChildrenReturns409() throws Exception {
        doThrow(new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Tapahtumaa ei voi poistaa, koska siihen liittyy lipputyyppejä tai myyntejä"))
                .when(tapahtumaService).delete(1L);

        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
