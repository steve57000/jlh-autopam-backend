package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "demande_service")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DemandeService {
    @EmbeddedId
    private DemandeServiceKey id = new DemandeServiceKey();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idDemande")
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idService")
    @JoinColumn(name = "id_service", nullable = false)
    private Service service;

    @Column(name = "quantite", nullable = false)
    private Integer quantite;
}

