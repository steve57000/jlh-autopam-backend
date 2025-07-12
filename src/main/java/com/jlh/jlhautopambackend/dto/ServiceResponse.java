package com.jlh.jlhautopambackend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponse {
    private Integer idService;
    private String libelle;
    private String description;
    private BigDecimal prixUnitaire;
}