package fi.haagahelia.ticketguru.web;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.haagahelia.ticketguru.service.LippuService;
import fi.haagahelia.ticketguru.web.dto.LippuKayttoRequest;
import fi.haagahelia.ticketguru.web.dto.LippuResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class LippuController {

    private final LippuService lippuService;

    public LippuController(LippuService lippuService) {
        this.lippuService = lippuService;
    }

    @GetMapping
    public LippuResponse findByKoodi(@RequestParam String koodi) {
        return lippuService.findByKoodi(koodi);
    }

    @PatchMapping("/{id}")
    public LippuResponse merkitseKaytetyksi(
            @PathVariable Long id,
            @Valid @RequestBody LippuKayttoRequest request) {
        return lippuService.merkitseKaytetyksi(id, request.kaytetty());
    }
}
