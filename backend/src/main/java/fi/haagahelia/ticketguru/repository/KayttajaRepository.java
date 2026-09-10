package fi.haagahelia.ticketguru.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.haagahelia.ticketguru.domain.Kayttaja;
import fi.haagahelia.ticketguru.domain.Rooli;

public interface KayttajaRepository extends JpaRepository<Kayttaja, Long> {

    Optional<Kayttaja> findByKayttajanimi(String kayttajanimi);

    List<Kayttaja> findByRooli(Rooli rooli);
}
