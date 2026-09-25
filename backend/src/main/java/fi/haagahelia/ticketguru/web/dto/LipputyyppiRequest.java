package fi.haagahelia.ticketguru.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LipputyyppiRequest(
        @NotBlank @Size(max = 80) String kuvaus,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal hinta) {
}
