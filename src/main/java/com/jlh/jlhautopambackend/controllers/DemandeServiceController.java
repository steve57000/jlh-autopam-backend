package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.DemandeServiceRequest;
import com.jlh.jlhautopambackend.dto.DemandeServiceResponse;
import com.jlh.jlhautopambackend.mapper.DemandeServiceMapper;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.DemandeServiceKey;
import com.jlh.jlhautopambackend.repositories.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repositories.DemandeRepository;
import com.jlh.jlhautopambackend.repositories.ServiceRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/demandes-services")
@CrossOrigin
public class DemandeServiceController {

    private final DemandeServiceRepository repo;
    private final DemandeRepository demandeRepo;
    private final ServiceRepository serviceRepo;
    private final DemandeServiceMapper mapper;

    public DemandeServiceController(DemandeServiceRepository repo,
                                    DemandeRepository demandeRepo,
                                    ServiceRepository serviceRepo,
                                    DemandeServiceMapper mapper) {
        this.repo = repo;
        this.demandeRepo = demandeRepo;
        this.serviceRepo = serviceRepo;
        this.mapper = mapper;
    }

    @GetMapping
    public List<DemandeServiceResponse> getAll() {
        return repo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<DemandeServiceResponse> create(
            @Valid @RequestBody DemandeServiceRequest req) {
        DemandeService ent = mapper.toEntity(req);

        // lier clef composite
        ent.setId(new DemandeServiceKey(req.getIdDemande(), req.getIdService()));
        ent.setDemande(demandeRepo.findById(req.getIdDemande())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable")));
        ent.setService(serviceRepo.findById(req.getIdService())
                .orElseThrow(() -> new IllegalArgumentException("Service introuvable")));

        DemandeService saved = repo.save(ent);
        return ResponseEntity
                .created(URI.create("/api/demandes-services/" + saved.getId()))
                .body(mapper.toResponse(saved));
    }

    @DeleteMapping("/{demandeId}/{serviceId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer demandeId,
            @PathVariable Integer serviceId) {
        DemandeServiceKey key = new DemandeServiceKey(demandeId, serviceId);
        if (!repo.existsById(key)) return ResponseEntity.notFound().build();
        repo.deleteById(key);
        return ResponseEntity.noContent().build();
    }
}
