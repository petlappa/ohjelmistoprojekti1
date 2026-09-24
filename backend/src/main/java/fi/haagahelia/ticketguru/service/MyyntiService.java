package fi.haagahelia.ticketguru.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fi.haagahelia.ticketguru.domain.Kayttaja;
import fi.haagahelia.ticketguru.domain.Lippu;
import fi.haagahelia.ticketguru.domain.Lipputyyppi;
import fi.haagahelia.ticketguru.domain.Myyntitapahtuma;
import fi.haagahelia.ticketguru.domain.Tapahtuma;
import fi.haagahelia.ticketguru.repository.KayttajaRepository;
import fi.haagahelia.ticketguru.repository.LippuRepository;
import fi.haagahelia.ticketguru.repository.LipputyyppiRepository;
import fi.haagahelia.ticketguru.repository.MyyntitapahtumaRepository;
import fi.haagahelia.ticketguru.repository.TapahtumaRepository;
import fi.haagahelia.ticketguru.web.dto.LippuResponse;
import fi.haagahelia.ticketguru.web.dto.MyyntiRequest;
import fi.haagahelia.ticketguru.web.dto.MyyntiResponse;
import fi.haagahelia.ticketguru.web.dto.MyyntiRiviRequest;

@Service
@Transactional
public class MyyntiService {

    private final MyyntitapahtumaRepository myyntitapahtumaRepository;
    private final LippuRepository lippuRepository;
    private final TapahtumaRepository tapahtumaRepository;
    private final KayttajaRepository kayttajaRepository;
    private final LipputyyppiRepository lipputyyppiRepository;

    public MyyntiService(
            MyyntitapahtumaRepository myyntitapahtumaRepository,
            LippuRepository lippuRepository,
            TapahtumaRepository tapahtumaRepository,
            KayttajaRepository kayttajaRepository,
            LipputyyppiRepository lipputyyppiRepository) {
        this.myyntitapahtumaRepository = myyntitapahtumaRepository;
        this.lippuRepository = lippuRepository;
        this.tapahtumaRepository = tapahtumaRepository;
        this.kayttajaRepository = kayttajaRepository;
        this.lipputyyppiRepository = lipputyyppiRepository;
    }

    @Transactional(readOnly = true)
    public MyyntiResponse findById(Long id) {
        Myyntitapahtuma myynti = myyntitapahtumaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Myyntitapahtumaa ei löydy: " + id));
        List<LippuResponse> liput = lippuRepository.findByMyyntitapahtuma(myynti).stream()
                .map(LippuResponse::from)
                .toList();
        return MyyntiResponse.from(myynti, liput);
    }

    public MyyntiResponse create(MyyntiRequest request) {
        Tapahtuma tapahtuma = tapahtumaRepository.findById(request.tapahtumaId())
                .orElseThrow(() -> badRequest("Tapahtumaa ei ole: " + request.tapahtumaId()));
        Kayttaja myyja = kayttajaRepository.findById(request.myyjaId())
                .orElseThrow(() -> badRequest("Myyjää ei ole: " + request.myyjaId()));

        List<VarattuRivi> rivit = new ArrayList<>();
        long uusiaLippuja = 0;
        BigDecimal summa = BigDecimal.ZERO;
        for (MyyntiRiviRequest rivi : request.rivit()) {
            Lipputyyppi tyyppi = lipputyyppiRepository.findById(rivi.lipputyyppiId())
                    .orElseThrow(() -> badRequest("Lipputyyppiä ei ole: " + rivi.lipputyyppiId()));
            if (!tyyppi.getTapahtuma().getId().equals(tapahtuma.getId())) {
                throw badRequest("Lipputyyppi " + tyyppi.getId() + " ei kuulu tapahtumaan " + tapahtuma.getId());
            }
            uusiaLippuja += rivi.kpl();
            summa = summa.add(tyyppi.getHinta().multiply(BigDecimal.valueOf(rivi.kpl())));
            rivit.add(new VarattuRivi(tyyppi, rivi.kpl()));
        }

        long myytyja = lippuRepository.countByLipputyyppiTapahtumaId(tapahtuma.getId());
        if (myytyja + uusiaLippuja > tapahtuma.getLippujaKpl()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tapahtuman kapasiteetti ylittyisi: jäljellä "
                            + Math.max(0, tapahtuma.getLippujaKpl() - myytyja)
                            + " lippua");
        }

        Myyntitapahtuma myynti = myyntitapahtumaRepository.save(
                new Myyntitapahtuma(LocalDateTime.now(), summa, tapahtuma, myyja));

        List<LippuResponse> liput = new ArrayList<>();
        for (VarattuRivi rivi : rivit) {
            for (int i = 0; i < rivi.kpl(); i++) {
                Lippu lippu = lippuRepository.save(new Lippu(
                        uusiKoodi(),
                        rivi.tyyppi().getHinta(),
                        myynti,
                        rivi.tyyppi()));
                liput.add(LippuResponse.from(lippu));
            }
        }
        return MyyntiResponse.from(myynti, liput);
    }

    private String uusiKoodi() {
        String koodi;
        do {
            koodi = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        } while (lippuRepository.findByKoodi(koodi).isPresent());
        return koodi;
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private record VarattuRivi(Lipputyyppi tyyppi, int kpl) {
    }
}
