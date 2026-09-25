package fi.haagahelia.ticketguru.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MyyntiRiviRequest(
        @NotNull Long lipputyyppiId,
        @NotNull @Min(1) @Max(500) Integer kpl) {
}
