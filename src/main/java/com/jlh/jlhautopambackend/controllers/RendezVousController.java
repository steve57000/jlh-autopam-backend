package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.mapper.RendezVousMapper;
import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.repositories.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rendezvous")
@CrossOrigin
public class RendezVousController {

    private final RendezVousRepository         repo;
    private final DemandeRepository             demandeRepo;
    private final CreneauRepository             creneauRepo;
    private final AdministrateurRepository      adminRepo;
    private final StatutRendezVousRepository   statutRepo;
    private final RendezVousMapper              mapper;

    public RendezVousController(RendezVousRepository repo,
                                DemandeRepository demandeRepo,
                                CreneauRepository creneauRepo,
                                AdministrateurRepository adminRepo,
                                StatutRendezVousRepository statutRepo,
                                RendezVousMapper mapper) {
        this.repo        = repo;
        this.demandeRepo = demandeRepo;
        this.creneauRepo = creneauRepo;
        this.adminRepo   = adminRepo;
        this.statutRepo  = statutRepo;
        this.mapper      = mapper;
    }

    @GetMapping
    public List<RendezVousResponse> getAll() {
        return repo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RendezVousResponse> getById(@PathVariable Integer id) {
        return repo.findById(id)
                .map(rv -> ResponseEntity.ok(mapper.toResponse(rv)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RendezVousResponse> create(
            @Valid @RequestBody RendezVousRequest req) {

        // Lier demande
        Demande demande = demandeRepo
                .findById(req.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + req.getDemandeId()));
        // Lier créneau
        Creneau creneau = creneauRepo
                .findById(req.getCreneauId())
                .orElseThrow(() -> new IllegalArgumentException("Creneau introuvable: " + req.getCreneauId()));
        // Lier administrateur
        Administrateur admin = adminRepo
                .findById(req.getAdministrateurId())
                .orElseThrow(() -> new IllegalArgumentException("Administrateur introuvable: " + req.getAdministrateurId())
                );
        // Lier statut
        StatutRendezVous statut = statutRepo
                .findById(req.getCodeStatut())
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable: " + req.getCodeStatut())
                );

        RendezVous toSave = mapper.toEntity(req);
        toSave.setDemande(demande);
        toSave.setCreneau(creneau);
        toSave.setAdministrateur(admin);
        toSave.setStatut(statut);

        RendezVous saved = repo.save(toSave);
        return ResponseEntity
                .created(URI.create("/api/rendezvous/" + saved.getIdRdv()))
                .body(mapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RendezVousResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody RendezVousRequest req) {
        return repo.findById(id)
                .map(existing -> {
                    // Mettre à jour associations si besoin
                    existing.setDemande(demandeRepo.findById(req.getDemandeId())
                            .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + req.getDemandeId())));
                    existing.setCreneau(creneauRepo.findById(req.getCreneauId())
                            .orElseThrow(() -> new IllegalArgumentException("Creneau introuvable: " + req.getCreneauId())));
                    existing.setAdministrateur(adminRepo.findById(req.getAdministrateurId())
                            .orElseThrow(() -> new IllegalArgumentException("Administrateur introuvable: " + req.getAdministrateurId())));
                    existing.setStatut(statutRepo.findById(req.getCodeStatut())
                            .orElseThrow(() -> new IllegalArgumentException("Statut introuvable: " + req.getCodeStatut())));
                    RendezVous updated = repo.save(existing);
                    return ResponseEntity.ok(mapper.toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
