package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.services.DemandeServiceService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.*;

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
    public ResponseEntity<DemandeServiceResponse> getByKey(
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId) {
        return service.findByKey(demandeId, serviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DemandeServiceResponse> create(
            @Valid @RequestBody DemandeServiceRequest req) {
        DemandeServiceResponse resp = service.create(req);
        String path = String.format("/api/demandes-services/%d/%d",
                resp.getId().getIdDemande(),
                resp.getId().getIdService());
        return ResponseEntity
                .created(URI.create(path))
                .body(resp);
    }

    @PutMapping("/{demandeId}/{serviceId}")
    public ResponseEntity<DemandeServiceResponse> update(
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId,
            @Valid @RequestBody DemandeServiceRequest req) {
        return service.update(demandeId, serviceId, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{demandeId}/{serviceId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId) {
        return service.delete(demandeId, serviceId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
