package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.DemandeServiceRequest;
import com.jlh.jlhautopambackend.dto.DemandeServiceResponse;
import com.jlh.jlhautopambackend.services.DemandeServiceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/demandes-services")
@CrossOrigin
public class DemandeServiceController {

    private final DemandeServiceService service;

    public DemandeServiceController(DemandeServiceService service) {
        this.service = service;
    }

    @GetMapping
    public List<DemandeServiceResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{demandeId}/{serviceId}")
    public ResponseEntity<DemandeServiceResponse> getById(
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId
    ) {
        Optional<DemandeServiceResponse> opt = service.findByKey(demandeId, serviceId);
        return opt
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DemandeServiceResponse> create(
            @Valid @RequestBody DemandeServiceRequest req
    ) {
        try {
            DemandeServiceResponse resp = service.create(req);
            String path = String.format("/api/demandes-services/%d/%d",
                    resp.getId().getIdDemande(),
                    resp.getId().getIdService());
            return ResponseEntity
                    .created(URI.create(path))
                    .body(resp);
        } catch (IllegalArgumentException ex) {
            // mapping de l'IllegalArgumentException en 400
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{demandeId}/{serviceId}")
    public ResponseEntity<DemandeServiceResponse> update(
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId,
            @Valid @RequestBody DemandeServiceRequest req
    ) {
        try {
            Optional<DemandeServiceResponse> opt = service.update(demandeId, serviceId, req);
            return opt
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{demandeId}/{serviceId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId
    ) {
        return service.delete(demandeId, serviceId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
