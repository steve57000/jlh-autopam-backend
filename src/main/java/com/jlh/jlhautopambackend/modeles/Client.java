package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "client")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Client {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idClient;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    @Column(name="email_verified", nullable=false)
    private boolean emailVerified = false;

    @Column(name="email_verified_at")
    private Instant emailVerifiedAt;

    @Column(nullable = false)
    private String immatriculation;

    @Column(length = 20, nullable = false)
    private String telephone;

    @Column(columnDefinition = "TEXT")
    private String adresse;
}
