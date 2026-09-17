package fi.haagahelia.ticketguru.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fi.haagahelia.ticketguru.domain.Tapahtuma;
import fi.haagahelia.ticketguru.repository.LipputyyppiRepository;
import fi.haagahelia.ticketguru.repository.MyyntitapahtumaRepository;
import fi.haagahelia.ticketguru.repository.TapahtumaRepository;
import fi.haagahelia.ticketguru.web.dto.TapahtumaRequest;
import fi.haagahelia.ticketguru.web.dto.TapahtumaResponse;

@Service
@Transactional
public class TapahtumaService {

    private final TapahtumaRepository tapahtumaRepository;
    private final LipputyyppiRepository lipputyyppiRepository;
    private final MyyntitapahtumaRepository myyntitapahtumaRepository;

    public TapahtumaService(
            TapahtumaRepository tapahtumaRepository,
            LipputyyppiRepository lipputyyppiRepository,
            MyyntitapahtumaRepository myyntitapahtumaRepository) {
        this.tapahtumaRepository = tapahtumaRepository;
        this.lipputyyppiRepository = lipputyyppiRepository;
        this.myyntitapahtumaRepository = myyntitapahtumaRepository;
    }

    @Transactional(readOnly = true)
    public List<TapahtumaResponse> findAll(String kaupunki) {
        List<Tapahtuma> tapahtumat = (kaupunki == null || kaupunki.isBlank())
                ? tapahtumaRepository.findAllByOrderByAikaAsc()
                : tapahtumaRepository.findByKaupunkiIgnoreCaseOrderByAikaAsc(kaupunki.trim());
        return tapahtumat.stream().map(TapahtumaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TapahtumaResponse findById(Long id) {
        return TapahtumaResponse.from(requireTapahtuma(id));
    }

    public TapahtumaResponse create(TapahtumaRequest request) {
        Tapahtuma tallennettu = tapahtumaRepository.save(request.toEntity());
        return TapahtumaResponse.from(tallennettu);
    }

    public TapahtumaResponse update(Long id, TapahtumaRequest request) {
        Tapahtuma tapahtuma = requireTapahtuma(id);
        request.applyTo(tapahtuma);
        return TapahtumaResponse.from(tapahtumaRepository.save(tapahtuma));
    }

    public void delete(Long id) {
        Tapahtuma tapahtuma = requireTapahtuma(id);
        if (lipputyyppiRepository.existsByTapahtumaId(id)
                || myyntitapahtumaRepository.existsByTapahtumaId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tapahtumaa ei voi poistaa, koska siihen liittyy lipputyyppejä tai myyntejä");
        }
        tapahtumaRepository.delete(tapahtuma);
    }

    private Tapahtuma requireTapahtuma(Long id) {
        return tapahtumaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tapahtumaa ei löydy: " + id));
    }
}
