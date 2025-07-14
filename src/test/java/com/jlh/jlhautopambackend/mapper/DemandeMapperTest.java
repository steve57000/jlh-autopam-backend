package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

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

        // idDemande doit être ignoré
        assertNull(ent.getIdDemande());
        // date bien mappée
        assertEquals(now, ent.getDateDemande());
        // les autres relations ignorées
        assertNull(ent.getClient());
        assertNull(ent.getTypeDemande());
        assertNull(ent.getStatutDemande());
        assertNull(ent.getServices());
    }

    @Test
    void toResponse_shouldMapAllFields() {
        Instant dt = Instant.parse("2025-06-01T08:30:00Z");
        // entités liées
        Client client = Client.builder().idClient(7).build();
        TypeDemande td = TypeDemande.builder()
                .codeType("T1")
                .libelle("Type 1")
                .build();
        StatutDemande sd = StatutDemande.builder()
                .codeStatut("S1")
                .libelle("Statut 1")
                .build();
        // liaisons many-to-many
        DemandeServiceKey key = new DemandeServiceKey(123, 456);
        DemandeService ds = DemandeService.builder().id(key).build();

        Demande ent = Demande.builder()
                .idDemande(99)
                .dateDemande(dt)
                .client(client)
                .typeDemande(td)
                .statutDemande(sd)
                .services(List.of(ds))
                .build();

        DemandeResponse resp = mapper.toResponse(ent);

        // champs simples
        assertEquals(99, resp.getIdDemande());
        assertEquals(dt, resp.getDateDemande());
        assertEquals(7, resp.getClientId());

        // DTO imbriqués
        TypeDemandeDto tdDto = resp.getTypeDemande();
        assertNotNull(tdDto);
        assertEquals("T1", tdDto.getCodeType());
        assertEquals("Type 1", tdDto.getLibelle());

        StatutDemandeDto sdDto = resp.getStatutDemande();
        assertNotNull(sdDto);
        assertEquals("S1", sdDto.getCodeStatut());
        assertEquals("Statut 1", sdDto.getLibelle());

        // mapping de la liste des services
        List<DemandeServiceKeyDto> svcKeys = resp.getServices();
        assertNotNull(svcKeys);
        assertEquals(1, svcKeys.size());
        DemandeServiceKeyDto ks = svcKeys.get(0);
        assertEquals(123, ks.getIdDemande());
        assertEquals(456, ks.getIdService());
    }
}
