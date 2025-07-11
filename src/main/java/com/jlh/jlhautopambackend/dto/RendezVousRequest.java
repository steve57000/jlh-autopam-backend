package com.jlh.jlhautopambackend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVousRequest {
    /** Identifiant de la demande associée */
    private Integer demandeId;
    /** Identifiant du créneau réservé */
    private Integer creneauId;
    /** Identifiant de l’administrateur */
    private Integer administrateurId;
    /** Code du statut du rendez-vous (clé primaire de StatutRendezVous) */
    private String codeStatut;
}
