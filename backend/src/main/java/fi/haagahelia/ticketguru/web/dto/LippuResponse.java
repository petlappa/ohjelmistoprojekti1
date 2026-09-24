package fi.haagahelia.ticketguru.web.dto;

import java.math.BigDecimal;

import fi.haagahelia.ticketguru.domain.Lippu;

public record LippuResponse(
        Long id,
        String koodi,
        BigDecimal hinta,
        String lipputyypinKuvaus,
        boolean kaytetty) {

    public static LippuResponse from(Lippu lippu) {
        return new LippuResponse(
                lippu.getId(),
                lippu.getKoodi(),
                lippu.getHinta(),
                lippu.getLipputyyppi().getKuvaus(),
                lippu.isKaytetty());
    }
}
