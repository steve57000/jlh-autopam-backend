package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeServiceRequest {
    private Integer idDemande;
    private Integer idService;
    private Integer quantite;
}
