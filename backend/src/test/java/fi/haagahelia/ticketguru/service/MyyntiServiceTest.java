package fi.haagahelia.ticketguru.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import fi.haagahelia.ticketguru.domain.Kayttaja;
import fi.haagahelia.ticketguru.domain.Lipputyyppi;
import fi.haagahelia.ticketguru.domain.Tapahtuma;
import fi.haagahelia.ticketguru.repository.KayttajaRepository;
import fi.haagahelia.ticketguru.repository.LippuRepository;
import fi.haagahelia.ticketguru.repository.LipputyyppiRepository;
import fi.haagahelia.ticketguru.repository.MyyntitapahtumaRepository;
import fi.haagahelia.ticketguru.repository.TapahtumaRepository;
import fi.haagahelia.ticketguru.web.dto.MyyntiRequest;
import fi.haagahelia.ticketguru.web.dto.MyyntiResponse;
import fi.haagahelia.ticketguru.web.dto.MyyntiRiviRequest;

@ExtendWith(MockitoExtension.class)
class MyyntiServiceTest {

    @Mock
    private MyyntitapahtumaRepository myyntitapahtumaRepository;

    @Mock
    private LippuRepository lippuRepository;

    @Mock
    private TapahtumaRepository tapahtumaRepository;

    @Mock
    private KayttajaRepository kayttajaRepository;

    @Mock
    private LipputyyppiRepository lipputyyppiRepository;

    @InjectMocks
    private MyyntiService myyntiService;

    private Tapahtuma tapahtuma;
    private Kayttaja myyja;
    private Lipputyyppi tyyppi;

    @BeforeEach
    void fixtures() {
        tapahtuma = withId(new Tapahtuma(
                "Tapahtuma A",
                LocalDateTime.of(2026, 10, 2, 17, 0),
                "Helsinki",
                "Kulttuuritalo",
                1), 1L);
        myyja = withId(new Kayttaja("myyja", "salasana", "Maija", "Myyjä", null), 2L);
        tyyppi = withId(new Lipputyyppi("Aikuinen", new BigDecimal("15.00"), tapahtuma), 3L);
    }

    @Test
    void tuntematonTapahtumaOn400() {
        when(tapahtumaRepository.findById(1L)).thenReturn(Optional.empty());

        assertStatus(pyynto(1), 400);
        verify(myyntitapahtumaRepository, never()).save(any());
    }

    @Test
    void tuntematonMyyjaOn400() {
        when(tapahtumaRepository.findById(1L)).thenReturn(Optional.of(tapahtuma));
        when(kayttajaRepository.findById(2L)).thenReturn(Optional.empty());

        assertStatus(pyynto(1), 400);
    }

    @Test
    void vieraanTapahtumanLipputyyppiOn400() {
        Tapahtuma toinen = withId(new Tapahtuma(
                "Toinen", LocalDateTime.of(2026, 11, 1, 18, 0), "Turku", "Logomo", 10), 9L);
        Lipputyyppi vaara = withId(new Lipputyyppi("Opiskelija", new BigDecimal("10.00"), toinen), 8L);
        when(tapahtumaRepository.findById(1L)).thenReturn(Optional.of(tapahtuma));
        when(kayttajaRepository.findById(2L)).thenReturn(Optional.of(myyja));
        when(lipputyyppiRepository.findById(8L)).thenReturn(Optional.of(vaara));

        assertStatus(new MyyntiRequest(1L, 2L, List.of(new MyyntiRiviRequest(8L, 1))), 400);
    }

    @Test
    void kapasiteetinYlitysOn409() {
        when(tapahtumaRepository.findById(1L)).thenReturn(Optional.of(tapahtuma));
        when(kayttajaRepository.findById(2L)).thenReturn(Optional.of(myyja));
        when(lipputyyppiRepository.findById(3L)).thenReturn(Optional.of(tyyppi));
        when(lippuRepository.countByLipputyyppiTapahtumaId(1L)).thenReturn(1L);

        assertStatus(pyynto(1), 409);
        verify(myyntitapahtumaRepository, never()).save(any());
    }

    @Test
    void myyntiLuoLipunJaSumman() {
        tapahtuma.setLippujaKpl(10);
        when(tapahtumaRepository.findById(1L)).thenReturn(Optional.of(tapahtuma));
        when(kayttajaRepository.findById(2L)).thenReturn(Optional.of(myyja));
        when(lipputyyppiRepository.findById(3L)).thenReturn(Optional.of(tyyppi));
        when(lippuRepository.countByLipputyyppiTapahtumaId(1L)).thenReturn(0L);
        when(lippuRepository.findByKoodi(any())).thenReturn(Optional.empty());
        when(myyntitapahtumaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(lippuRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MyyntiResponse vastaus = myyntiService.create(pyynto(2));

        assertThat(vastaus.summa()).isEqualByComparingTo("30.00");
        assertThat(vastaus.tapahtumanNimi()).isEqualTo("Tapahtuma A");
        assertThat(vastaus.myyjanNimi()).isEqualTo("Maija Myyjä");
        assertThat(vastaus.liput()).hasSize(2);
        assertThat(vastaus.liput().get(0).lipputyypinKuvaus()).isEqualTo("Aikuinen");
        assertThat(vastaus.liput().get(0).kaytetty()).isFalse();
    }

    private MyyntiRequest pyynto(int kpl) {
        return new MyyntiRequest(1L, 2L, List.of(new MyyntiRiviRequest(3L, kpl)));
    }

    private void assertStatus(MyyntiRequest request, int status) {
        assertThatThrownBy(() -> myyntiService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(status);
    }

    private static <T> T withId(T entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
            return entity;
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
