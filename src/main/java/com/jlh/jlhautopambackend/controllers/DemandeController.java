package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import com.jlh.jlhautopambackend.services.DemandeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/demandes")
@CrossOrigin
public class DemandeController {

    private final DemandeService service;

    public DemandeController(DemandeService service) {
        this.service = service;
    }

    @GetMapping
    public List<DemandeResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandeResponse> getById(@PathVariable Integer id) {
        Optional<DemandeResponse> opt = service.findById(id);
        return opt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DemandeResponse> create(@Valid @RequestBody DemandeRequest req) {
        DemandeResponse created = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/demandes/" + created.getIdDemande()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DemandeResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody DemandeRequest req) {

        Optional<DemandeResponse> opt = service.update(id, req);
        return opt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        boolean deleted = service.delete(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
