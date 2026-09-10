package fi.haagahelia.ticketguru;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import fi.haagahelia.ticketguru.domain.Kayttaja;
import fi.haagahelia.ticketguru.domain.Lippu;
import fi.haagahelia.ticketguru.domain.Lipputyyppi;
import fi.haagahelia.ticketguru.domain.Myyntitapahtuma;
import fi.haagahelia.ticketguru.domain.Rooli;
import fi.haagahelia.ticketguru.domain.Tapahtuma;
import fi.haagahelia.ticketguru.repository.KayttajaRepository;
import fi.haagahelia.ticketguru.repository.LippuRepository;
import fi.haagahelia.ticketguru.repository.LipputyyppiRepository;
import fi.haagahelia.ticketguru.repository.MyyntitapahtumaRepository;
import fi.haagahelia.ticketguru.repository.RooliRepository;
import fi.haagahelia.ticketguru.repository.TapahtumaRepository;

@Component
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final RooliRepository rooliRepository;
    private final KayttajaRepository kayttajaRepository;
    private final TapahtumaRepository tapahtumaRepository;
    private final LipputyyppiRepository lipputyyppiRepository;
    private final MyyntitapahtumaRepository myyntitapahtumaRepository;
    private final LippuRepository lippuRepository;

    public DemoDataLoader(
            RooliRepository rooliRepository,
            KayttajaRepository kayttajaRepository,
            TapahtumaRepository tapahtumaRepository,
            LipputyyppiRepository lipputyyppiRepository,
            MyyntitapahtumaRepository myyntitapahtumaRepository,
            LippuRepository lippuRepository) {
        this.rooliRepository = rooliRepository;
        this.kayttajaRepository = kayttajaRepository;
        this.tapahtumaRepository = tapahtumaRepository;
        this.lipputyyppiRepository = lipputyyppiRepository;
        this.myyntitapahtumaRepository = myyntitapahtumaRepository;
        this.lippuRepository = lippuRepository;
    }

    @Override
    public void run(String... args) {
        if (tapahtumaRepository.count() > 0) {
            return;
        }

        Rooli myyjaRooli = rooliRepository.save(new Rooli("MYYJA"));
        Rooli koordinaattoriRooli = rooliRepository.save(new Rooli("TAPAHTUMAKOORDINAATTORI"));
        rooliRepository.save(new Rooli("PAAKAYTTAJA"));

        Kayttaja myyja = kayttajaRepository.save(
                new Kayttaja("myyja", "salasana", "Maija", "Myyjä", myyjaRooli));
        kayttajaRepository.save(
                new Kayttaja("koordinaattori", "salasana", "Kalle", "Koordinaattori", koordinaattoriRooli));

        Tapahtuma tapahtumaA = tapahtumaRepository.save(new Tapahtuma(
                "Tapahtuma A",
                LocalDateTime.of(2026, 10, 2, 17, 0),
                "Helsinki",
                "Kulttuuritalo",
                200));
        Tapahtuma tapahtumaB = tapahtumaRepository.save(new Tapahtuma(
                "Tapahtuma B",
                LocalDateTime.of(2026, 11, 15, 19, 30),
                "Tampere",
                "Tampere-talo",
                80));

        Lipputyyppi aikuinen = lipputyyppiRepository.save(
                new Lipputyyppi("Aikuinen", new BigDecimal("15.00"), tapahtumaA));
        Lipputyyppi lapsi = lipputyyppiRepository.save(
                new Lipputyyppi("Lapsi", new BigDecimal("7.50"), tapahtumaA));
        lipputyyppiRepository.save(
                new Lipputyyppi("Aikuinen", new BigDecimal("22.00"), tapahtumaB));

        Myyntitapahtuma myynti = myyntitapahtumaRepository.save(new Myyntitapahtuma(
                LocalDateTime.of(2026, 9, 10, 12, 15),
                new BigDecimal("37.50"),
                tapahtumaA,
                myyja));

        lippuRepository.save(new Lippu("3er454aa", new BigDecimal("15.00"), myynti, aikuinen));
        lippuRepository.save(new Lippu("3er454ab", new BigDecimal("15.00"), myynti, aikuinen));
        Lippu lapsenLippu = new Lippu("3er454ac", new BigDecimal("7.50"), myynti, lapsi);
        lapsenLippu.setKaytetty(true);
        lippuRepository.save(lapsenLippu);

        log.info("Esimerkkidata ladattu H2-kantaan ({} tapahtumaa, {} lippua).",
                tapahtumaRepository.count(), lippuRepository.count());
    }
}
