package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DemandeDocumentDownload;
import com.jlh.jlhautopambackend.dto.DemandeDocumentDto;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeDocument;
import com.jlh.jlhautopambackend.repository.DemandeDocumentRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DemandeDocumentServiceImpl implements DemandeDocumentService {

    private final DemandeRepository demandeRepository;
    private final DemandeDocumentRepository documentRepository;

    public DemandeDocumentServiceImpl(DemandeRepository demandeRepository,
                                      DemandeDocumentRepository documentRepository) {
        this.demandeRepository = demandeRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public DemandeDocumentDto addDocument(Integer demandeId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier fourni est vide ou manquant.");
        }

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + demandeId));

        String originalName = file.getOriginalFilename();
        String sanitizedName = sanitizeFilename(originalName);
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = "application/octet-stream";
        }

        DemandeDocument document = DemandeDocument.builder()
                .demande(demande)
                .nomFichier(sanitizedName)
                .typeContenu(contentType)
                .tailleOctets(file.getSize())
                .visibleClient(true)
                .creeLe(Instant.now())
                .build();

        DemandeDocument saved = documentRepository.save(document);
        if (demande.getDocuments() != null) {
            demande.getDocuments().add(saved);
        }

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeDocumentDto> listDocuments(Integer demandeId) {
        return documentRepository.findByDemande_IdDemandeOrderByCreeLeDesc(demandeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DemandeDocumentDownload> loadDocument(Integer demandeId, Long documentId) {
        return documentRepository.findByIdDocumentAndDemande_IdDemande(documentId, demandeId)
                .map(doc -> DemandeDocumentDownload.builder()
                        .idDocument(doc.getIdDocument())
                        .demandeId(demandeId)
                        .nomFichier(doc.getNomFichier())
                        .urlPublic(doc.getUrlPublic())
                        .typeContenu(doc.getTypeContenu())
                        .tailleOctets(doc.getTailleOctets())
                        .visibleClient(doc.isVisibleClient())
                        .creePar(doc.getCreePar())
                        .creeParRole(doc.getCreeParRole())
                        .creeLe(doc.getCreeLe())
                        .build());
    }

    @Override
    public boolean deleteDocument(Integer demandeId, Long documentId) {
        Optional<DemandeDocument> existing = documentRepository.findByIdDocumentAndDemande_IdDemande(documentId, demandeId);
        existing.ifPresent(documentRepository::delete);
        return existing.isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwnedByClient(Integer demandeId, Integer clientId) {
        return demandeRepository.existsByIdDemandeAndClient_IdClient(demandeId, clientId);
    }

    private DemandeDocumentDto toDto(DemandeDocument doc) {
        return DemandeDocumentDto.builder()
                .idDocument(doc.getIdDocument())
                .nomFichier(doc.getNomFichier())
                .urlPublic(doc.getUrlPublic())
                .typeContenu(doc.getTypeContenu())
                .tailleOctets(doc.getTailleOctets())
                .visibleClient(doc.isVisibleClient())
                .creePar(doc.getCreePar())
                .creeParRole(doc.getCreeParRole())
                .creeLe(doc.getCreeLe())
                .build();
    }

    private String sanitizeFilename(String originalName) {
        if (!StringUtils.hasText(originalName)) {
            return "document";
        }
        String cleaned = Paths.get(originalName).getFileName().toString();
        byte[] bytes = cleaned.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= 255) {
            return cleaned;
        }
        int maxBytes = 255;
        int end = cleaned.length();
        while (end > 0) {
            String candidate = cleaned.substring(0, end);
            if (candidate.getBytes(StandardCharsets.UTF_8).length <= maxBytes) {
                return candidate;
            }
            end--;
        }
        return cleaned.substring(0, 1);
    }
}
