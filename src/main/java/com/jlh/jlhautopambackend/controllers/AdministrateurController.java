package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.AdministrateurRequest;
import com.jlh.jlhautopambackend.dto.AdministrateurResponse;
import com.jlh.jlhautopambackend.mapper.AdministrateurMapper;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.repositories.AdministrateurRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/administrateurs")
public class AdministrateurController {

    private final AdministrateurRepository repo;
    private final AdministrateurMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public AdministrateurController(AdministrateurRepository repo,
                                    AdministrateurMapper mapper,
                                    PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<AdministrateurResponse> getAll() {
        return repo.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministrateurResponse> getById(@PathVariable Integer id) {
        return repo.findById(id)
                .map(entity -> ResponseEntity.ok(mapper.toResponse(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AdministrateurResponse> create(
            @Valid @RequestBody AdministrateurRequest request) {
        Administrateur entity = mapper.toEntity(request);
        // hash du mot de passe avant persistance
        entity.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        Administrateur saved = repo.save(entity);
        return ResponseEntity
                .created(URI.create("/api/administrateurs/" + saved.getIdAdmin()))
                .body(mapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministrateurResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody AdministrateurRequest request) {

        return repo.findById(id)
                .map(existing -> {
                    // mapping partiel : mettre à jour champs propres
                    existing.setUsername(request.getUsername());
                    existing.setNom(request.getNom());
                    existing.setPrenom(request.getPrenom());
                    if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
                        existing.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
                    }
                    Administrateur updated = repo.save(existing);
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
