package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
public class DemandeServiceKey implements Serializable {
    private Integer idDemande;
    private Integer idService;
}