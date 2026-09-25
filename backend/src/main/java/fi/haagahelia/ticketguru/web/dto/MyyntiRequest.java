package fi.haagahelia.ticketguru.web.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MyyntiRequest(
        @NotNull Long tapahtumaId,
        @NotNull Long myyjaId,
        @NotEmpty @Size(max = 20) @Valid List<MyyntiRiviRequest> rivit) {
}
