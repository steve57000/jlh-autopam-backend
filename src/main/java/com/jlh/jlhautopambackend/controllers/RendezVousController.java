package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.RendezVousRequest;
import com.jlh.jlhautopambackend.dto.RendezVousResponse;
import com.jlh.jlhautopambackend.services.RendezVousService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rendezvous")
@CrossOrigin
public class RendezVousController {

    private final RendezVousService service;

    public RendezVousController(RendezVousService service) {
        this.service = service;
    }

    @GetMapping
    public List<RendezVousResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RendezVousResponse> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RendezVousResponse> create(
            @Valid @RequestBody RendezVousRequest req) {
        RendezVousResponse resp = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/rendezvous/" + resp.getIdRdv()))
                .body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RendezVousResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody RendezVousRequest req) {
        return service.update(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
