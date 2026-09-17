package fi.haagahelia.ticketguru.web.dto;

import java.util.List;

public record ApiError(int status, String error, List<String> messages) {
}
