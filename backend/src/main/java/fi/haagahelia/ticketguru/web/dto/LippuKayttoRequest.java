package fi.haagahelia.ticketguru.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record LippuKayttoRequest(
        @NotNull @AssertTrue(message = "pitää olla true") Boolean kaytetty) {
}
