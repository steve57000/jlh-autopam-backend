package com.jlh.jlhautopambackend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeServiceRequest {
    private Integer demandeId;
    private Integer serviceId;
    private Integer quantite;
    private BigDecimal prixUnitaire;
}
