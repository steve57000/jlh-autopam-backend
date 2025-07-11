package com.jlh.jlhautopambackend.controllers;

import java.net.URI;
import java.util.List;

import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import com.jlh.jlhautopambackend.mapper.DemandeMapper;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repositories.ClientRepository;
import com.jlh.jlhautopambackend.repositories.StatutDemandeRepository;
import com.jlh.jlhautopambackend.repositories.TypeDemandeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.repositories.DemandeRepository;

@RestController
@RequestMapping("/api/demandes")
@CrossOrigin
public class DemandeController {

    private final DemandeRepository repo;
    private final ClientRepository clientRepo;
    private final TypeDemandeRepository typeRepo;
    private final StatutDemandeRepository statutRepo;
    private final DemandeMapper mapper;

    public DemandeController(DemandeRepository repo,
                             ClientRepository clientRepo,
                             TypeDemandeRepository typeRepo,
                             StatutDemandeRepository statutRepo,
                             DemandeMapper mapper) {
        this.repo = repo;
        this.clientRepo = clientRepo;
        this.typeRepo = typeRepo;
        this.statutRepo = statutRepo;
        this.mapper = mapper;
    }

    @GetMapping
    public List<DemandeResponse> getAll() {
        return repo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandeResponse> getById(@PathVariable Integer id) {
        return repo.findById(id)
                .map(e -> ResponseEntity.ok(mapper.toResponse(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DemandeResponse> create(@Valid @RequestBody DemandeRequest req) {
        Demande ent = mapper.toEntity(req);

        // lier le client
        Client client = clientRepo.findById(req.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable"));
        ent.setClient(client);

        // lier type & statut
        ent.setTypeDemande(typeRepo.findById(req.getCodeType())
                .orElseThrow(() -> new IllegalArgumentException("Type introuvable")));
        ent.setStatutDemande(statutRepo.findById(req.getCodeStatut())
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable")));

        Demande saved = repo.save(ent);
        return ResponseEntity
                .created(URI.create("/api/demandes/" + saved.getIdDemande()))
                .body(mapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DemandeResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody DemandeRequest req) {

        return repo.findById(id)
                .map(existing -> {
                    existing.setDateDemande(req.getDateDemande());

                    existing.setClient(clientRepo.findById(req.getClientId())
                            .orElseThrow(() -> new IllegalArgumentException("Client introuvable")));
                    existing.setTypeDemande(typeRepo.findById(req.getCodeType())
                            .orElseThrow(() -> new IllegalArgumentException("Type introuvable")));
                    existing.setStatutDemande(statutRepo.findById(req.getCodeStatut())
                            .orElseThrow(() -> new IllegalArgumentException("Statut introuvable")));

                    Demande updated = repo.save(existing);
                    return ResponseEntity.ok(mapper.toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}