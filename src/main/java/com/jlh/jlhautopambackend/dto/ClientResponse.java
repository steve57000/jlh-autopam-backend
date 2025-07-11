package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {
    private Integer idClient;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
}
