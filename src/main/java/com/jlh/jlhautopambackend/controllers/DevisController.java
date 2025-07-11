package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.DevisRequest;
import com.jlh.jlhautopambackend.dto.DevisResponse;
import com.jlh.jlhautopambackend.mapper.DevisMapper;
import com.jlh.jlhautopambackend.modeles.Devis;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.repositories.DevisRepository;
import com.jlh.jlhautopambackend.repositories.DemandeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/devis")
@CrossOrigin
public class DevisController {
    private final DevisRepository devisRepo;
    private final DemandeRepository demandeRepo;
    private final DevisMapper mapper;

    public DevisController(DevisRepository devisRepo,
                           DemandeRepository demandeRepo,
                           DevisMapper mapper) {
        this.devisRepo = devisRepo;
        this.demandeRepo = demandeRepo;
        this.mapper = mapper;
    }

    @GetMapping
    public List<DevisResponse> getAll() {
        return devisRepo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DevisResponse> getById(@PathVariable Integer id) {
        return devisRepo.findById(id)
                .map(devis -> ResponseEntity.ok(mapper.toResponse(devis)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DevisResponse> create(
            @Valid @RequestBody DevisRequest request) {
        Devis toSave = mapper.toEntity(request);
        // Lier à la demande existante
        Demande demande = demandeRepo.findById(request.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + request.getDemandeId()));
        toSave.setDemande(demande);
        Devis saved = devisRepo.save(toSave);
        return ResponseEntity
                .created(URI.create("/api/devis/" + saved.getIdDevis()))
                .body(mapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DevisResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody DevisRequest request) {
        return devisRepo.findById(id)
                .map(existing -> {
                    existing.setDateDevis(request.getDateDevis());
                    existing.setMontantTotal(request.getMontantTotal());
                    Demande demande = demandeRepo
                            .findById(request.getDemandeId())
                            .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + request.getDemandeId()));
                    existing.setDemande(demande);
                    Devis updated = devisRepo.save(existing);
                    return ResponseEntity.ok(mapper.toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!devisRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        devisRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
