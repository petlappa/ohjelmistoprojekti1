package fi.haagahelia.ticketguru.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.haagahelia.ticketguru.domain.Rooli;

public interface RooliRepository extends JpaRepository<Rooli, Long> {

    Optional<Rooli> findByNimi(String nimi);
}
