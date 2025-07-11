package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.mapper.PromotionMapper;
import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.repositories.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin
public class PromotionController {

    private final PromotionRepository      promoRepo;
    private final AdministrateurRepository adminRepo;
    private final PromotionMapper          mapper;

    public PromotionController(PromotionRepository promoRepo,
                               AdministrateurRepository adminRepo,
                               PromotionMapper mapper) {
        this.promoRepo = promoRepo;
        this.adminRepo = adminRepo;
        this.mapper   = mapper;
    }

    /** GET /api/promotions */
    @GetMapping
    public List<PromotionResponse> getAll() {
        return promoRepo.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /** GET /api/promotions/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getById(@PathVariable Integer id) {
        return promoRepo.findById(id)
                .map(p -> ResponseEntity.ok(mapper.toResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/promotions */
    @PostMapping
    public ResponseEntity<PromotionResponse> create(
            @Valid @RequestBody PromotionRequest req) {

        // 1. Valider l’admin
        Administrateur admin = adminRepo.findById(req.getAdministrateurId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Administrateur introuvable : " + req.getAdministrateurId())
                );

        // 2. Mapper Request → Entity
        Promotion toSave = mapper.toEntity(req);
        toSave.setAdministrateur(admin);

        // 3. Sauvegarder
        Promotion saved = promoRepo.save(toSave);

        // 4. Retourner DTO + 201 Created
        return ResponseEntity
                .created(URI.create("/api/promotions/" + saved.getIdPromotion()))
                .body(mapper.toResponse(saved));
    }

    /** PUT /api/promotions/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody PromotionRequest req) {

        return promoRepo.findById(id)
                .map(existing -> {
                    // 1. Valider dates
                    if (req.getValidFrom().isAfter(req.getValidTo())) {
                        throw new IllegalArgumentException("validFrom doit être avant validTo");
                    }
                    // 2. Mettre à jour les champs simples
                    existing.setImageUrl(req.getImageUrl());
                    existing.setValidFrom(req.getValidFrom());
                    existing.setValidTo(req.getValidTo());

                    // 3. (Optionnel) changer d’admin
                    if (!existing.getAdministrateur().getIdAdmin()
                            .equals(req.getAdministrateurId())) {
                        Administrateur admin = adminRepo.findById(req.getAdministrateurId())
                                .orElseThrow(() ->
                                        new IllegalArgumentException("Administrateur introuvable : " + req.getAdministrateurId())
                                );
                        existing.setAdministrateur(admin);
                    }

                    Promotion updated = promoRepo.save(existing);
                    return ResponseEntity.ok(mapper.toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/promotions/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!promoRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        promoRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}