package com.jlh.jlhautopambackend.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class DemandeDocumentDownload {
    Long idDocument;
    Integer demandeId;
    String filename;
    String contentType;
    Long fileSize;
    Instant createdAt;
    byte[] data;
}
