package com.jlh.jlhautopambackend.modeles;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "demande")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Demande {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande")
    private Integer idDemande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @Column(name = "date_demande", nullable = false)
    private Instant dateDemande;

    @ManyToOne
    @JoinColumn(name = "code_type", referencedColumnName = "code_type", nullable = false)
    private TypeDemande typeDemande;

    @ManyToOne
    @JoinColumn(name = "code_statut", referencedColumnName = "code_statut", nullable = false)
    private StatutDemande statutDemande;

    // Si RDV :
    @OneToOne(mappedBy = "demande") // ou @OneToOne @JoinColumn(name="id_demande") selon ton modèle
    private RendezVous rendezVous;

    @OneToMany(mappedBy = "demande") // via DemandeService.id.idDemande
    private List<DemandeService> services;
}
