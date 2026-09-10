package fi.haagahelia.ticketguru.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.haagahelia.ticketguru.domain.Lippu;
import fi.haagahelia.ticketguru.domain.Myyntitapahtuma;

public interface LippuRepository extends JpaRepository<Lippu, Long> {

    Optional<Lippu> findByKoodi(String koodi);

    List<Lippu> findByMyyntitapahtuma(Myyntitapahtuma myyntitapahtuma);

    List<Lippu> findByLipputyyppiTapahtumaId(Long tapahtumaId);

    long countByLipputyyppiTapahtumaId(Long tapahtumaId);
}
