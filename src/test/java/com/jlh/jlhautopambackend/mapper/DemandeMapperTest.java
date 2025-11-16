package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DemandeMapperTest {

    private DemandeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(DemandeMapper.class);
    }

    @Test
    void toEntity_shouldMapDateAndIgnoreIdsAndRelations() {
        Instant now = Instant.parse("2025-07-14T12:00:00Z");
        DemandeRequest req = DemandeRequest.builder()
                .dateDemande(now)
                .clientId(42)
                .codeType("TYP")
                .codeStatut("ST1")
                .build();

        Demande ent = mapper.toEntity(req);

        // idDemande ignoré par le mapping
        assertNull(ent.getIdDemande());
        // date bien mappée
        assertEquals(now, ent.getDateDemande());
        // relations non hydratées par toEntity()
        assertNull(ent.getClient());
        assertNull(ent.getTypeDemande());
        assertNull(ent.getStatutDemande());
        assertTrue(ent.getServices() == null || ent.getServices().isEmpty());
        assertTrue(ent.getDocuments() == null || ent.getDocuments().isEmpty());
    }

    @Test
    void toResponse_shouldMapAllFields_includingClientAndServices() {
        Instant dt = Instant.parse("2025-06-01T08:30:00Z");

        // entités liées
        Client client = Client.builder()
                .idClient(7)
                .nom("Durand")
                .prenom("Alice")
                .email("alice@example.com")
                .build();

        TypeDemande td = TypeDemande.builder()
                .codeType("T1")
                .libelle("Type 1")
                .build();

        StatutDemande sd = StatutDemande.builder()
                .codeStatut("S1")
                .libelle("Statut 1")
                .build();

        // service (côté produit)
        Service service = Service.builder()
                .idService(456)
                .libelle("Vidange")
                .quantiteMax(3)
                .prixUnitaire(BigDecimal.valueOf(59.90))
                .build();

        // liaison many-to-many avec quantité
        DemandeServiceKey key = new DemandeServiceKey(123, 456);
        DemandeService ds = DemandeService.builder()
                .id(key)
                .service(service)
                .quantite(2)
                .libelleService("Vidange")
                .descriptionService("Vidange moteur")
                .prixUnitaireService(BigDecimal.valueOf(59.90))
                .build();

        DemandeDocument document = DemandeDocument.builder()
                .idDocument(321L)
                .nomFichier("devis.pdf")
                .urlPublic("https://cdn.example.com/docs/devis.pdf")
                .typeContenu("application/pdf")
                .tailleOctets(2048L)
                .visibleClient(true)
                .creePar("admin@test.fr")
                .creeParRole("ADMIN")
                .creeLe(Instant.parse("2025-06-01T08:31:00Z"))
                .build();

        Demande ent = Demande.builder()
                .idDemande(99)
                .dateDemande(dt)
                .client(client)
                .typeDemande(td)
                .statutDemande(sd)
                .services(Set.of(ds))
                .documents(List.of(document))
                .build();

        document.setDemande(ent);

        DemandeResponse resp = mapper.toResponse(ent);

        // champs simples
        assertEquals(99, resp.getIdDemande());
        assertEquals(dt, resp.getDateDemande());

        // client embarqué (plus de clientId seul)
        assertNotNull(resp.getClient());
        assertEquals(7, resp.getClient().getIdClient());
        assertEquals("Durand", resp.getClient().getNom());
        assertEquals("alice@example.com", resp.getClient().getEmail());

        // DTO imbriqués (type / statut)
        TypeDemandeDto tdDto = resp.getTypeDemande();
        assertNotNull(tdDto);
        assertEquals("T1", tdDto.getCodeType());
        assertEquals("Type 1", tdDto.getLibelle());

        StatutDemandeDto sdDto = resp.getStatutDemande();
        assertNotNull(sdDto);
        assertEquals("S1", sdDto.getCodeStatut());
        assertEquals("Statut 1", sdDto.getLibelle());

        // services détaillés
        List<DemandeServiceDto> svc = resp.getServices();
        assertNotNull(svc);
        assertEquals(1, svc.size());

        DemandeServiceDto s0 = svc.get(0);
        assertEquals(456, s0.getIdService());
        assertEquals("Vidange", s0.getLibelle());
        assertEquals("Vidange moteur", s0.getDescription());
        assertEquals(2, s0.getQuantite());
        assertEquals(BigDecimal.valueOf(59.90), s0.getPrixUnitaire());

        List<DemandeDocumentDto> docs = resp.getDocuments();
        assertNotNull(docs);
        assertEquals(1, docs.size());
        DemandeDocumentDto docDto = docs.get(0);
        assertEquals(321L, docDto.getIdDocument());
        assertEquals("devis.pdf", docDto.getNomFichier());
        assertEquals("https://cdn.example.com/docs/devis.pdf", docDto.getUrlPublic());
        assertEquals("application/pdf", docDto.getTypeContenu());
        assertEquals(2048L, docDto.getTailleOctets());
        assertTrue(docDto.isVisibleClient());
        assertEquals("admin@test.fr", docDto.getCreePar());
        assertEquals("ADMIN", docDto.getCreeParRole());
        assertEquals(Instant.parse("2025-06-01T08:31:00Z"), docDto.getCreeLe());
    }

    @Test
    void toDto_shouldProjectSimplifiedView() {
        Instant dt = Instant.parse("2025-06-01T08:30:00Z");
        Client client = Client.builder()
                .idClient(7)
                .nom("Durand")
                .prenom("Alice")
                .email("alice@example.com")
                .telephone("0102030405")
                .adresseLigne1("10 rue Victor")
                .adresseCodePostal("75000")
                .adresseVille("Paris")
                .build();

        TypeDemande td = TypeDemande.builder()
                .codeType("T1")
                .libelle("Type 1")
                .build();

        StatutDemande sd = StatutDemande.builder()
                .codeStatut("S1")
                .libelle("Statut 1")
                .build();

        DemandeService serviceLine = DemandeService.builder()
                .id(new DemandeServiceKey(99, 456))
                .quantite(2)
                .libelleService("Vidange")
                .descriptionService("Vidange moteur")
                .prixUnitaireService(BigDecimal.valueOf(59.90))
                .build();

        Demande demande = Demande.builder()
                .idDemande(99)
                .dateDemande(dt)
                .client(client)
                .typeDemande(td)
                .statutDemande(sd)
                .services(Set.of(serviceLine))
                .build();
        serviceLine.setDemande(demande);

        DemandeDto dto = mapper.toDto(demande);

        assertEquals(99, dto.getIdDemande());
        assertEquals(dt, dto.getDateSoumission());
        assertEquals("T1", dto.getCodeType());
        assertEquals("Type 1", dto.getTypeLibelle());
        assertEquals("S1", dto.getCodeStatut());
        assertEquals("Statut 1", dto.getStatutLibelle());

        assertNotNull(dto.getClient());
        assertEquals(7, dto.getClient().getIdClient());
        assertEquals("Durand", dto.getClient().getNom());
        assertEquals("10 rue Victor 75000 Paris", dto.getClient().getAdresse());

        assertNotNull(dto.getServices());
        assertEquals(1, dto.getServices().size());
        DemandeServiceDto svc = dto.getServices().get(0);
        assertEquals(99, svc.getIdDemande());
        assertEquals(456, svc.getIdService());
        assertEquals("Vidange", svc.getLibelle());
        assertEquals("Vidange", svc.getLibelleService());
        assertEquals("Vidange moteur", svc.getDescription());
        assertEquals(BigDecimal.valueOf(59.90), svc.getPrixUnitaire());
        assertEquals(BigDecimal.valueOf(59.90), svc.getPrixUnitaireService());
    }
}
