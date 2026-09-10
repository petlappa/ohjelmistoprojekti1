package fi.haagahelia.ticketguru.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.haagahelia.ticketguru.domain.Kayttaja;
import fi.haagahelia.ticketguru.domain.Myyntitapahtuma;
import fi.haagahelia.ticketguru.domain.Tapahtuma;

public interface MyyntitapahtumaRepository extends JpaRepository<Myyntitapahtuma, Long> {

    List<Myyntitapahtuma> findByTapahtuma(Tapahtuma tapahtuma);

    List<Myyntitapahtuma> findByMyyja(Kayttaja myyja);

    List<Myyntitapahtuma> findByTapahtumaIdOrderByMyyntiaikaDesc(Long tapahtumaId);
}
