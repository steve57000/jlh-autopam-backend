package com.jlh.jlhautopambackend.controllers;

import java.util.List;

import com.jlh.jlhautopambackend.modeles.Client;
import org.springframework.web.bind.annotation.*;
import com.jlh.jlhautopambackend.modeles.TypeDemande;
import com.jlh.jlhautopambackend.repositories.TypeDemandeRepository;

@RestController
@RequestMapping("/api/types-demande")
public class TypeDemandeController {
    private final TypeDemandeRepository repo;
    public TypeDemandeController(TypeDemandeRepository repo) { this.repo = repo; }
    @GetMapping public List<Client> getAll() { return repo.findAll(); }
    @GetMapping("/{code}") public Client getById(@PathVariable String code) {
        return repo.findById(Integer.valueOf(code)).orElse(null);
    }
}
