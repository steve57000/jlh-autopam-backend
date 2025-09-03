package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.services.DemandeServiceService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/demandes-services")
@CrossOrigin
public class DemandeServiceController {

    private final DemandeServiceService service;
    private final ClientRepository clientRepo;
    private final DemandeRepository demandeRepo;

    public DemandeServiceController(
            DemandeServiceService service,
            ClientRepository clientRepo,
            DemandeRepository demandeRepo
    ) {
        this.service = service;
        this.clientRepo = clientRepo;
        this.demandeRepo = demandeRepo;
    }

    /* ==================== ADMIN ==================== */

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<DemandeServiceResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{demandeId}/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandeServiceResponse> getByKey(
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId) {
        return service.findByKey(demandeId, serviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* ==================== CLIENT (ownership) ==================== */

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DemandeServiceResponse> create(
            Authentication auth,
            @Valid @RequestBody DemandeServiceRequest req) {

        Integer clientId = getClientIdFromAuth(auth);
        // ownership: la demande doit appartenir au client
        boolean ok = demandeRepo.findById(req.getDemandeId())
                .map(d -> d.getClient() != null && Objects.equals(d.getClient().getIdClient(), clientId))
                .orElse(false);
        if (!ok) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        DemandeServiceResponse resp = service.create(req);
        String path = String.format("/api/demandes-services/%d/%d",
                resp.getId().getIdDemande(),
                resp.getId().getIdService());
        return ResponseEntity.created(URI.create(path)).body(resp);
    }

    @PutMapping("/{demandeId}/{serviceId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DemandeServiceResponse> update(
            Authentication auth,
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId,
            @Valid @RequestBody DemandeServiceRequest req) {

        Integer clientId = getClientIdFromAuth(auth);
        boolean ok = demandeRepo.findById(demandeId)
                .map(d -> d.getClient() != null && Objects.equals(d.getClient().getIdClient(), clientId))
                .orElse(false);
        if (!ok) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return service.update(demandeId, serviceId, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{demandeId}/{serviceId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> delete(
            Authentication auth,
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId) {

        Integer clientId = getClientIdFromAuth(auth);
        boolean ok = demandeRepo.findById(demandeId)
                .map(d -> d.getClient() != null && Objects.equals(d.getClient().getIdClient(), clientId))
                .orElse(false);
        if (!ok) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return service.delete(demandeId, serviceId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /* ==================== Utils ==================== */

    private Integer getClientIdFromAuth(Authentication auth) {
        String email = auth.getName();
        Client client = clientRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable"));
        return client.getIdClient();
    }
}
