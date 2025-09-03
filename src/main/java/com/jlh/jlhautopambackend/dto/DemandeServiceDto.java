package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DemandeServiceDto {
    private Integer idService;
    private String libelle;
    private Integer quantite;
    private Double prixUnitaire; // si null côté entity, restera null
}
