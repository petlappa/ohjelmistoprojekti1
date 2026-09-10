package fi.haagahelia.ticketguru.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.haagahelia.ticketguru.domain.Lipputyyppi;
import fi.haagahelia.ticketguru.domain.Tapahtuma;

public interface LipputyyppiRepository extends JpaRepository<Lipputyyppi, Long> {

    List<Lipputyyppi> findByTapahtuma(Tapahtuma tapahtuma);

    List<Lipputyyppi> findByTapahtumaId(Long tapahtumaId);
}
