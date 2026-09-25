package fi.haagahelia.ticketguru.web;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import fi.haagahelia.ticketguru.service.MyyntiService;
import fi.haagahelia.ticketguru.web.dto.MyyntiRequest;
import fi.haagahelia.ticketguru.web.dto.MyyntiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "*")
public class MyyntiController {

    private final MyyntiService myyntiService;

    public MyyntiController(MyyntiService myyntiService) {
        this.myyntiService = myyntiService;
    }

    @GetMapping("/{id}")
    public MyyntiResponse get(@PathVariable Long id) {
        return myyntiService.findById(id);
    }

    @PostMapping
    public ResponseEntity<MyyntiResponse> create(@Valid @RequestBody MyyntiRequest request) {
        MyyntiResponse created = myyntiService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
