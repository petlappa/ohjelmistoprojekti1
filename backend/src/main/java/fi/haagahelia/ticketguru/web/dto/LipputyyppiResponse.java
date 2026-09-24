package fi.haagahelia.ticketguru.web.dto;

import java.math.BigDecimal;

import fi.haagahelia.ticketguru.domain.Lipputyyppi;

public record LipputyyppiResponse(Long id, String kuvaus, BigDecimal hinta) {

    public static LipputyyppiResponse from(Lipputyyppi lipputyyppi) {
        return new LipputyyppiResponse(lipputyyppi.getId(), lipputyyppi.getKuvaus(), lipputyyppi.getHinta());
    }
}
