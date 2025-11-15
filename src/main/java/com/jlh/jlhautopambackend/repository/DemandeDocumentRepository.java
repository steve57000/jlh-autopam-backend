package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.DemandeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeDocumentRepository extends JpaRepository<DemandeDocument, Long> {
    List<DemandeDocument> findByDemande_IdDemandeOrderByCreatedAtDesc(Integer demandeId);
    Optional<DemandeDocument> findByIdAndDemande_IdDemande(Long idDocument, Integer demandeId);
}
