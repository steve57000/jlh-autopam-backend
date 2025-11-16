package com.jlh.jlhautopambackend.controllers;

import com.jlh.jlhautopambackend.dto.DemandeDocumentDownload;
import com.jlh.jlhautopambackend.dto.DemandeDocumentDto;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.services.DemandeDocumentService;
import com.jlh.jlhautopambackend.services.support.AuthenticatedClientResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/demandes/{demandeId}/documents")
public class DemandeDocumentController {

    private final DemandeDocumentService documentService;
    private final AuthenticatedClientResolver clientResolver;

    public DemandeDocumentController(DemandeDocumentService documentService,
                                     AuthenticatedClientResolver clientResolver) {
        this.documentService = documentService;
        this.clientResolver = clientResolver;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemandeDocumentDto> upload(@PathVariable Integer demandeId,
                                                     @RequestPart("file") MultipartFile file) {
        try {
            DemandeDocumentDto created = documentService.addDocument(demandeId, file);
            URI location = URI.create(String.format("/api/demandes/%d/documents/%d", demandeId, created.getIdDocument()));
            return ResponseEntity.created(location).body(created);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible d'enregistrer le fichier.", ex);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    public ResponseEntity<List<DemandeDocumentDto>> list(@PathVariable Integer demandeId,
                                                         Authentication authentication) {
        if (!hasAccess(authentication, demandeId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(documentService.listDocuments(demandeId));
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    public ResponseEntity<Void> download(@PathVariable Integer demandeId,
                                         @PathVariable Long documentId,
                                         Authentication authentication) {
        if (!hasAccess(authentication, demandeId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<DemandeDocumentDownload> download = documentService.loadDocument(demandeId, documentId);
        if (download.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DemandeDocumentDownload doc = download.get();
        if (!StringUtils.hasText(doc.getUrlPublic())) {
            return ResponseEntity.notFound().build();
        }

        URI target = URI.create(doc.getUrlPublic());
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(target)
                .build();
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer demandeId, @PathVariable Long documentId) {
        boolean deleted = documentService.deleteDocument(demandeId, documentId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private boolean hasAccess(Authentication authentication, Integer demandeId) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
        if (isAdmin) {
            return true;
        }
        if (authentication.getAuthorities().stream().noneMatch(auth -> "ROLE_CLIENT".equals(auth.getAuthority()))) {
            return false;
        }
        Client client = clientResolver.requireCurrentClient(authentication);
        return documentService.isOwnedByClient(demandeId, client.getIdClient());
    }

}
