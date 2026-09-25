package fi.haagahelia.ticketguru.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fi.haagahelia.ticketguru.domain.Lipputyyppi;
import fi.haagahelia.ticketguru.domain.Tapahtuma;
import fi.haagahelia.ticketguru.repository.LipputyyppiRepository;
import fi.haagahelia.ticketguru.repository.TapahtumaRepository;
import fi.haagahelia.ticketguru.web.dto.LipputyyppiRequest;
import fi.haagahelia.ticketguru.web.dto.LipputyyppiResponse;

@Service
@Transactional
public class LipputyyppiService {

    private final LipputyyppiRepository lipputyyppiRepository;
    private final TapahtumaRepository tapahtumaRepository;

    public LipputyyppiService(
            LipputyyppiRepository lipputyyppiRepository,
            TapahtumaRepository tapahtumaRepository) {
        this.lipputyyppiRepository = lipputyyppiRepository;
        this.tapahtumaRepository = tapahtumaRepository;
    }

    @Transactional(readOnly = true)
    public List<LipputyyppiResponse> findByTapahtuma(Long tapahtumaId) {
        requireTapahtuma(tapahtumaId);
        return lipputyyppiRepository.findByTapahtumaIdOrderByIdAsc(tapahtumaId).stream()
                .map(LipputyyppiResponse::from)
                .toList();
    }

    public LipputyyppiResponse create(Long tapahtumaId, LipputyyppiRequest request) {
        Tapahtuma tapahtuma = requireTapahtuma(tapahtumaId);
        Lipputyyppi lipputyyppi = new Lipputyyppi(request.kuvaus().trim(), request.hinta(), tapahtuma);
        return LipputyyppiResponse.from(lipputyyppiRepository.save(lipputyyppi));
    }

    private Tapahtuma requireTapahtuma(Long id) {
        return tapahtumaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tapahtumaa ei löydy: " + id));
    }
}
