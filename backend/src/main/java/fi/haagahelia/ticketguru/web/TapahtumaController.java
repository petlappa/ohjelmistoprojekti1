package fi.haagahelia.ticketguru.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import fi.haagahelia.ticketguru.service.TapahtumaService;
import fi.haagahelia.ticketguru.web.dto.TapahtumaRequest;
import fi.haagahelia.ticketguru.web.dto.TapahtumaResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class TapahtumaController {

    private final TapahtumaService tapahtumaService;

    public TapahtumaController(TapahtumaService tapahtumaService) {
        this.tapahtumaService = tapahtumaService;
    }

    @GetMapping
    public List<TapahtumaResponse> list(@RequestParam(required = false) String kaupunki) {
        return tapahtumaService.findAll(kaupunki);
    }

    @GetMapping("/{id}")
    public TapahtumaResponse get(@PathVariable Long id) {
        return tapahtumaService.findById(id);
    }

    @PostMapping
    public ResponseEntity<TapahtumaResponse> create(@Valid @RequestBody TapahtumaRequest request) {
        TapahtumaResponse created = tapahtumaService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public TapahtumaResponse update(@PathVariable Long id, @Valid @RequestBody TapahtumaRequest request) {
        return tapahtumaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tapahtumaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
