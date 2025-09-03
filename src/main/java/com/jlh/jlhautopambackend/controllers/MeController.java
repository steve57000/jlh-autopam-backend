package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.ChangePasswordRequest;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
@CrossOrigin
@RequiredArgsConstructor
public class MeController {
    private final ClientRepository clientRepo;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication auth,
                                            @RequestBody @Valid ChangePasswordRequest req) {
        String email = auth.getName();
        Client cli = clientRepo.findByEmail(email).orElseThrow();

        if (!req.newPassword().equals(req.confirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "La confirmation ne correspond pas."));
        }
        if (!passwordEncoder.matches(req.oldPassword(), cli.getMotDePasse())) {
            return ResponseEntity.status(403).body(Map.of("message", "Ancien mot de passe incorrect."));
        }
        cli.setMotDePasse(passwordEncoder.encode(req.newPassword()));
        clientRepo.save(cli);
        return ResponseEntity.ok(Map.of("message", "Mot de passe mis à jour."));
    }
}
