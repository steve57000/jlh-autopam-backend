package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.CreneauRequest;
import com.jlh.jlhautopambackend.dto.CreneauResponse;
import com.jlh.jlhautopambackend.mapper.CreneauMapper;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.modeles.StatutCreneau;
import com.jlh.jlhautopambackend.repositories.CreneauRepository;
import com.jlh.jlhautopambackend.repositories.StatutCreneauRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/creneaux")
@CrossOrigin
public class CreneauController {

    private final CreneauRepository creneauRepo;
    private final StatutCreneauRepository statutRepo;
    private final CreneauMapper mapper;

    public CreneauController(CreneauRepository creneauRepo,
                             StatutCreneauRepository statutRepo,
                             CreneauMapper mapper) {
        this.creneauRepo = creneauRepo;
        this.statutRepo = statutRepo;
        this.mapper = mapper;
    }

    /** GET /api/creneaux */
    @GetMapping
    public List<CreneauResponse> getAll() {
        return creneauRepo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    /** GET /api/creneaux/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<CreneauResponse> getById(@PathVariable Integer id) {
        return creneauRepo.findById(id)
                .map(entity -> ResponseEntity.ok(mapper.toResponse(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/creneaux */
    @PostMapping
    public ResponseEntity<CreneauResponse> create(
            @Valid @RequestBody CreneauRequest request) {

        // 1️⃣ Charger et valider le StatutCreneau (codeStatut est une String)
        StatutCreneau statut = statutRepo.findById(request.getCodeStatut())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Statut introuvable : " + request.getCodeStatut()));

        // 2️⃣ Mapper Request → Entity (dates, etc.)
        Creneau toSave = mapper.toEntity(request);

        // 3️⃣ Injecter le statut JPA
        toSave.setStatut(statut);

        // 4️⃣ Sauvegarde
        Creneau saved = creneauRepo.save(toSave);

        // 5️⃣ Retour DTO + 201 Created
        return ResponseEntity
                .created(URI.create("/api/creneaux/" + saved.getIdCreneau()))
                .body(mapper.toResponse(saved));
    }

    /** PUT /api/creneaux/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<CreneauResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody CreneauRequest request) {

        return creneauRepo.findById(id)
                .map(existing -> {
                    existing.setDateDebut(request.getDateDebut());
                    existing.setDateFin(request.getDateFin());

                    StatutCreneau statut = statutRepo.findById(request.getCodeStatut())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Statut introuvable : " + request.getCodeStatut()));
                    existing.setStatut(statut);

                    Creneau updated = creneauRepo.save(existing);
                    return ResponseEntity.ok(mapper.toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/creneaux/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!creneauRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        creneauRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
