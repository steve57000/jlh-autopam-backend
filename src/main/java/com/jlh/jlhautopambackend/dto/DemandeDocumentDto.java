package com.jlh.jlhautopambackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeDocumentDto {
    private Long idDocument;
    private String filename;
    private String contentType;
    private Long fileSize;
    private Instant createdAt;
}
