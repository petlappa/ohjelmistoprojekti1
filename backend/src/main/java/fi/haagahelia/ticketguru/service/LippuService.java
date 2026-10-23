package fi.haagahelia.ticketguru.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fi.haagahelia.ticketguru.domain.Lippu;
import fi.haagahelia.ticketguru.repository.LippuRepository;
import fi.haagahelia.ticketguru.web.dto.LippuResponse;

@Service
@Transactional
public class LippuService {

    private final LippuRepository lippuRepository;

    public LippuService(LippuRepository lippuRepository) {
        this.lippuRepository = lippuRepository;
    }

    @Transactional(readOnly = true)
    public LippuResponse findByKoodi(String koodi) {
        if (koodi == null || koodi.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "koodi puuttuu");
        }
        Lippu lippu = lippuRepository.findByKoodi(koodi.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Lippua ei löydy koodilla: " + koodi));
        return LippuResponse.from(lippu);
    }

    public LippuResponse merkitseKaytetyksi(Long id, boolean kaytetty) {
        if (!kaytetty) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kaytetty pitää olla true");
        }
        Lippu lippu = lippuRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Lippua ei löydy: " + id));
        if (lippu.isKaytetty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Lippu on jo käytetty");
        }
        lippu.setKaytetty(true);
        return LippuResponse.from(lippuRepository.save(lippu));
    }
}
