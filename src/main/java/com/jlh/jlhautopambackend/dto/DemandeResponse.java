package com.jlh.jlhautopambackend.dto;

import lombok.*;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeResponse {
    private Integer idDemande;
    private Instant dateDemande;
    private Integer clientId;
    private TypeDemandeDto typeDemande;
    private StatutDemandeDto statutDemande;
    private List<DemandeServiceKeyDto> services;
}