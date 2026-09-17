package fi.haagahelia.ticketguru.web.dto;

import java.time.LocalDateTime;

import fi.haagahelia.ticketguru.domain.Tapahtuma;

public record TapahtumaResponse(
        Long id,
        String nimi,
        LocalDateTime aika,
        String kaupunki,
        String paikka,
        Integer lippujaKpl) {

    public static TapahtumaResponse from(Tapahtuma tapahtuma) {
        return new TapahtumaResponse(
                tapahtuma.getId(),
                tapahtuma.getNimi(),
                tapahtuma.getAika(),
                tapahtuma.getKaupunki(),
                tapahtuma.getPaikka(),
                tapahtuma.getLippujaKpl());
    }
}
