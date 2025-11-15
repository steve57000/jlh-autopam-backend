package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.DemandeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeDocumentRepository extends JpaRepository<DemandeDocument, Long> {
}
