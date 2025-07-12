package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.PromotionRequest;
import com.jlh.jlhautopambackend.dto.PromotionResponse;
import com.jlh.jlhautopambackend.services.PromotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin
public class PromotionController {

    private final PromotionService service;

    public PromotionController(PromotionService service) {
        this.service = service;
    }

    @GetMapping
    public List<PromotionResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PromotionResponse> create(@RequestBody PromotionRequest req) {
        try {
            PromotionResponse resp = service.create(req);
            String location = "/api/promotions/" + resp.getIdPromotion();
            return ResponseEntity.created(URI.create(location)).body(resp);
        } catch (IllegalArgumentException ex) {
            // validFrom > validTo ou admin introuvable
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> update(
            @PathVariable Integer id,
            @RequestBody PromotionRequest req) {
        try {
            return service.update(id, req)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            // validFrom > validTo
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
