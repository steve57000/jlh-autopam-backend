package com.jlh.jlhautopambackend.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {
    private Integer idClient;
    private String nom;
    private String prenom;
    private String email;
    private String immatriculation;
    private String telephone;
    private String adresse;

    private boolean emailVerified;
    private Instant emailVerifiedAt;
}
