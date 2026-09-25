package fi.haagahelia.ticketguru.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import fi.haagahelia.ticketguru.service.LipputyyppiService;
import fi.haagahelia.ticketguru.web.dto.LipputyyppiRequest;
import fi.haagahelia.ticketguru.web.dto.LipputyyppiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events/{tapahtumaId}/ticket-types")
@CrossOrigin(origins = "*")
public class LipputyyppiController {

    private final LipputyyppiService lipputyyppiService;

    public LipputyyppiController(LipputyyppiService lipputyyppiService) {
        this.lipputyyppiService = lipputyyppiService;
    }

    @GetMapping
    public List<LipputyyppiResponse> list(@PathVariable Long tapahtumaId) {
        return lipputyyppiService.findByTapahtuma(tapahtumaId);
    }

    @PostMapping
    public ResponseEntity<LipputyyppiResponse> create(
            @PathVariable Long tapahtumaId,
            @Valid @RequestBody LipputyyppiRequest request) {
        LipputyyppiResponse created = lipputyyppiService.create(tapahtumaId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
