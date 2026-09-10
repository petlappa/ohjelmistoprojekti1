package fi.haagahelia.ticketguru.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import fi.haagahelia.ticketguru.domain.Kayttaja;
import fi.haagahelia.ticketguru.domain.Lippu;
import fi.haagahelia.ticketguru.domain.Lipputyyppi;
import fi.haagahelia.ticketguru.domain.Myyntitapahtuma;
import fi.haagahelia.ticketguru.domain.Rooli;
import fi.haagahelia.ticketguru.domain.Tapahtuma;

@DataJpaTest
class TicketGuruRepositoryTests {

    @Autowired
    private RooliRepository rooliRepository;

    @Autowired
    private KayttajaRepository kayttajaRepository;

    @Autowired
    private TapahtumaRepository tapahtumaRepository;

    @Autowired
    private LipputyyppiRepository lipputyyppiRepository;

    @Autowired
    private MyyntitapahtumaRepository myyntitapahtumaRepository;

    @Autowired
    private LippuRepository lippuRepository;

    @Test
    void kayttajaLoytyyKayttajanimellaJaRoolilla() {
        Rooli myyjaRooli = rooliRepository.save(new Rooli("MYYJA"));
        kayttajaRepository.save(new Kayttaja("myyja", "salasana", "Maija", "Myyjä", myyjaRooli));

        Optional<Kayttaja> loydetty = kayttajaRepository.findByKayttajanimi("myyja");
        List<Kayttaja> myyjat = kayttajaRepository.findByRooli(myyjaRooli);

        assertThat(loydetty).isPresent();
        assertThat(loydetty.get().getRooli().getNimi()).isEqualTo("MYYJA");
        assertThat(myyjat).hasSize(1);
        assertThat(rooliRepository.findByNimi("MYYJA")).isPresent();
    }

    @Test
    void tapahtumanLipputyypitJaTulevatTapahtumatLoytyvat() {
        Tapahtuma tapahtuma = tapahtumaRepository.save(new Tapahtuma(
                "Tapahtuma A",
                LocalDateTime.of(2026, 10, 2, 17, 0),
                "Helsinki",
                "Kulttuuritalo",
                200));
        lipputyyppiRepository.save(new Lipputyyppi("Aikuinen", new BigDecimal("15.00"), tapahtuma));
        lipputyyppiRepository.save(new Lipputyyppi("Lapsi", new BigDecimal("7.50"), tapahtuma));

        List<Lipputyyppi> tyypit = lipputyyppiRepository.findByTapahtuma(tapahtuma);
        List<Tapahtuma> tulevat = tapahtumaRepository.findByAikaAfterOrderByAikaAsc(
                LocalDateTime.of(2026, 9, 1, 0, 0));

        assertThat(tyypit).extracting(Lipputyyppi::getKuvaus).containsExactlyInAnyOrder("Aikuinen", "Lapsi");
        assertThat(tapahtumaRepository.findByKaupunki("Helsinki")).hasSize(1);
        assertThat(tulevat).extracting(Tapahtuma::getNimi).contains("Tapahtuma A");
    }

    @Test
    void lippuLoytyyKoodillaJaKuuluuMyyntiin() {
        Rooli rooli = rooliRepository.save(new Rooli("MYYJA"));
        Kayttaja myyja = kayttajaRepository.save(
                new Kayttaja("myyja", "salasana", "Maija", "Myyjä", rooli));
        Tapahtuma tapahtuma = tapahtumaRepository.save(new Tapahtuma(
                "Tapahtuma A",
                LocalDateTime.of(2026, 10, 2, 17, 0),
                "Helsinki",
                "Kulttuuritalo",
                200));
        Lipputyyppi aikuinen = lipputyyppiRepository.save(
                new Lipputyyppi("Aikuinen", new BigDecimal("15.00"), tapahtuma));
        Myyntitapahtuma myynti = myyntitapahtumaRepository.save(new Myyntitapahtuma(
                LocalDateTime.of(2026, 9, 10, 12, 15),
                new BigDecimal("30.00"),
                tapahtuma,
                myyja));
        lippuRepository.save(new Lippu("3er454aa", new BigDecimal("15.00"), myynti, aikuinen));
        lippuRepository.save(new Lippu("3er454ab", new BigDecimal("15.00"), myynti, aikuinen));

        Optional<Lippu> lippu = lippuRepository.findByKoodi("3er454aa");
        List<Lippu> myynninLiput = lippuRepository.findByMyyntitapahtuma(myynti);

        assertThat(lippu).isPresent();
        assertThat(lippu.get().isKaytetty()).isFalse();
        assertThat(lippu.get().getLipputyyppi().getKuvaus()).isEqualTo("Aikuinen");
        assertThat(myynninLiput).hasSize(2);
        assertThat(lippuRepository.countByLipputyyppiTapahtumaId(tapahtuma.getId())).isEqualTo(2);
        assertThat(myyntitapahtumaRepository.findByTapahtuma(tapahtuma)).hasSize(1);
        assertThat(myyntitapahtumaRepository.findByMyyja(myyja)).hasSize(1);
    }
}
