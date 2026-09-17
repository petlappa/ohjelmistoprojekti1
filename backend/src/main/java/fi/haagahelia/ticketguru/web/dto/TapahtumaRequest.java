package fi.haagahelia.ticketguru.web.dto;

import java.time.LocalDateTime;

import fi.haagahelia.ticketguru.domain.Tapahtuma;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TapahtumaRequest(
        @NotBlank @Size(max = 120) String nimi,
        @NotNull LocalDateTime aika,
        @NotBlank @Size(max = 80) String kaupunki,
        @NotBlank @Size(max = 120) String paikka,
        @NotNull @Min(1) Integer lippujaKpl) {

    public Tapahtuma toEntity() {
        return new Tapahtuma(nimi, aika, kaupunki, paikka, lippujaKpl);
    }

    public void applyTo(Tapahtuma tapahtuma) {
        tapahtuma.setNimi(nimi);
        tapahtuma.setAika(aika);
        tapahtuma.setKaupunki(kaupunki);
        tapahtuma.setPaikka(paikka);
        tapahtuma.setLippujaKpl(lippujaKpl);
    }
}
