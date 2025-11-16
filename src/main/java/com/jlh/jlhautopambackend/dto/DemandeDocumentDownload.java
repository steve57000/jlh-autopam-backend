package com.jlh.jlhautopambackend.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class DemandeDocumentDownload {
    Long idDocument;
    Integer demandeId;
    String nomFichier;
    String urlPublic;
    String typeContenu;
    Long tailleOctets;
    boolean visibleClient;
    String creePar;
    String creeParRole;
    Instant creeLe;
}
