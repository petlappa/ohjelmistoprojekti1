package fi.haagahelia.ticketguru.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import fi.haagahelia.ticketguru.domain.Lippu;
import fi.haagahelia.ticketguru.domain.Lipputyyppi;
import fi.haagahelia.ticketguru.repository.LippuRepository;
import fi.haagahelia.ticketguru.web.dto.LippuResponse;

@ExtendWith(MockitoExtension.class)
class LippuServiceTest {

    @Mock
    private LippuRepository lippuRepository;

    @InjectMocks
    private LippuService lippuService;

    @Test
    void tyhjaKoodiOn400() {
        assertThatThrownBy(() -> {
            lippuService.findByKoodi("  ");
        })
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(400);
        verify(lippuRepository, never()).findByKoodi(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void tuntematonKoodiOn404() {
        when(lippuRepository.findByKoodi("ei-ole")).thenReturn(Optional.empty());

        assertStatus(() -> {
            lippuService.findByKoodi("ei-ole");
        }, HttpStatus.NOT_FOUND);
    }

    @Test
    void koodiLoytyyJaValilyonnitKarsitaan() {
        Lippu lippu = lippu("abc", false);
        when(lippuRepository.findByKoodi("abc")).thenReturn(Optional.of(lippu));

        LippuResponse vastaus = lippuService.findByKoodi("  abc  ");

        assertThat(vastaus.koodi()).isEqualTo("abc");
        assertThat(vastaus.lipputyypinKuvaus()).isEqualTo("Aikuinen");
        assertThat(vastaus.kaytetty()).isFalse();
    }

    @Test
    void kaytettyFalseOn400() {
        assertStatus(() -> {
            lippuService.merkitseKaytetyksi(1L, false);
        }, HttpStatus.BAD_REQUEST);
        verify(lippuRepository, never()).findById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void tuntematonIdOn404() {
        when(lippuRepository.findById(9L)).thenReturn(Optional.empty());

        assertStatus(() -> {
            lippuService.merkitseKaytetyksi(9L, true);
        }, HttpStatus.NOT_FOUND);
    }

    @Test
    void joKaytettyLippuOn409() {
        when(lippuRepository.findById(1L)).thenReturn(Optional.of(lippu("abc", true)));

        assertStatus(() -> {
            lippuService.merkitseKaytetyksi(1L, true);
        }, HttpStatus.CONFLICT);
        verify(lippuRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void merkintaAsettaaKaytettyTrue() {
        Lippu lippu = lippu("abc", false);
        when(lippuRepository.findById(1L)).thenReturn(Optional.of(lippu));
        when(lippuRepository.save(lippu)).thenReturn(lippu);

        LippuResponse vastaus = lippuService.merkitseKaytetyksi(1L, true);

        assertThat(lippu.isKaytetty()).isTrue();
        assertThat(vastaus.kaytetty()).isTrue();
        assertThat(vastaus.koodi()).isEqualTo("abc");
    }

    private static void assertStatus(org.junit.jupiter.api.function.Executable call, HttpStatus status) {
        assertThatThrownBy(call::execute)
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode().value())
                .isEqualTo(status.value());
    }

    private static Lippu lippu(String koodi, boolean kaytetty) {
        Lippu lippu = new Lippu(koodi, new BigDecimal("15.00"), null, new Lipputyyppi("Aikuinen", new BigDecimal("15.00"), null));
        lippu.setKaytetty(kaytetty);
        return lippu;
    }
}
