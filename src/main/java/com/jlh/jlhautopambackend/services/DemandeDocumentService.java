package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DemandeDocumentDownload;
import com.jlh.jlhautopambackend.dto.DemandeDocumentDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface DemandeDocumentService {
    DemandeDocumentDto addDocument(Integer demandeId, MultipartFile file) throws IOException;

    List<DemandeDocumentDto> listDocuments(Integer demandeId);

    Optional<DemandeDocumentDownload> loadDocument(Integer demandeId, Long documentId);

    boolean deleteDocument(Integer demandeId, Long documentId);

    boolean isOwnedByClient(Integer demandeId, Integer clientId);
}
