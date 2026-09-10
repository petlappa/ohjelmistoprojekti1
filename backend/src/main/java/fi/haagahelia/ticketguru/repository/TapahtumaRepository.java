package fi.haagahelia.ticketguru.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.haagahelia.ticketguru.domain.Tapahtuma;

public interface TapahtumaRepository extends JpaRepository<Tapahtuma, Long> {

    List<Tapahtuma> findByKaupunki(String kaupunki);

    List<Tapahtuma> findByAikaAfterOrderByAikaAsc(LocalDateTime aika);
}
