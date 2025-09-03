package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.payload.RegisterRequest;
import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.services.ClientService;
import com.jlh.jlhautopambackend.services.EmailVerificationService;
import com.jlh.jlhautopambackend.dto.ChangePasswordRequest;
import com.jlh.jlhautopambackend.dto.ForgotPasswordRequest;
import com.jlh.jlhautopambackend.dto.ResetPasswordRequest;
import com.jlh.jlhautopambackend.services.PasswordResetService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.jlh.jlhautopambackend.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    private final ClientService clientService;
    private final ClientRepository clientRepo;
    private final EmailVerificationService emailVerificationService;

    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;

    // ===== DTO login =====
    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    // ===== Endpoints =====

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest creds) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getEmail(), creds.getPassword()
                    )
            );

            // Si c'est un client non vérifié => on refuse proprement
            clientRepo.findByEmail(creds.getEmail()).ifPresent(cli -> {
                if (!cli.isEmailVerified()) {
                    throw new BadCredentialsException("Veuillez vérifier votre e‑mail pour activer votre compte.");
                }
            });

            String token = jwtUtil.generateToken((UserDetails) auth.getPrincipal());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(403).body(Map.of("message", ex.getMessage()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(Map.of("message", "Identifiants invalides"));
        }
    }

    /** Inscription publique : crée le client et ENVOIE l'e‑mail de vérification */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        // anti‑doublon email (on garde, même si contrainte unique existe)
        if (clientRepo.findByEmail(req.email()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Un compte existe déjà avec cet e‑mail.",
                    "errors", Map.of("email", "déjà utilisé")
            ));
        }

        // ✅ mapping minimal vers ClientRequest pour NE PAS casser ClientService
        ClientRequest toCreate = ClientRequest.builder()
                .nom(req.nom())
                .prenom(req.prenom())
                .email(req.email())
                .motDePasse(req.motDePasse())
                .telephone(req.telephone())
                .immatriculation(req.immatriculation())
                .adresse(req.adresse())
                .build();

        // Centralisé : hash mdp + flags vérif + envoi email (sendVerification=true)
        try {
            clientService.create(toCreate, true);
        } catch (DataIntegrityViolationException dup) {
            // garde‑fou si contrainte unique déclenche quand même
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "Conflit de données",
                    "errors", Map.of("email", "déjà utilisé")
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Inscription reçue. Vérifiez votre e‑mail."
        ));
    }

    /** Renvoi du mail de vérification pour l'utilisateur authentifié (non vérifié) */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resend(Authentication auth) {
        String email = auth.getName();
        Client c = clientRepo.findByEmail(email).orElseThrow();
        if (c.isEmailVerified()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Adresse e‑mail déjà vérifiée."));
        }
        emailVerificationService.sendVerificationForClient(c.getIdClient());
        return ResponseEntity.ok(Map.of("message", "E‑mail de vérification renvoyé."));
    }

    /** Callback de vérification par token */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verify(@RequestParam("token") String token) {
        boolean ok = emailVerificationService.verify(token);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("message","Lien invalide ou expiré"));
        return ResponseEntity.status(302)
                .header("Location", "http://localhost:4200/login?verified=1")
                .build();
    }

    // === Demande de réinitialisation (public)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest req) {
        try {
            passwordResetService.requestReset(req.email());
        } catch (IllegalArgumentException e) {
            // Pour ne pas divulguer l’existence d’un compte, on répond OK quand même
        }
        return ResponseEntity.ok(Map.of("message", "Si un compte existe pour cet e-mail, un message a été envoyé."));
    }

    // === Réinitialisation (public)
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        try {
            passwordResetService.resetPassword(req.token(), req.newPassword(), req.confirmPassword());
            return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé. Vous pouvez vous connecter."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
