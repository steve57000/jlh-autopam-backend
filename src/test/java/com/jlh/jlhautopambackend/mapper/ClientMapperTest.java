package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
import com.jlh.jlhautopambackend.modeles.Client;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ClientMapperTest {
    private final ClientMapper mapper = Mappers.getMapper(ClientMapper.class);

    @Test
    void shouldMapRequestToEntityAndBack() {
        // GIVEN: un DTO complet (tous les champs requis)
        ClientRequest dto = ClientRequest.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("jean@dupont.fr")
                .motDePasse("secret123")         // ajouté
                .immatriculation("AA-123-AA")    // ajouté
                .telephone("0102030405")
                .adresse("1 rue X")
                .build();

        // WHEN: mapping vers l'entité
        Client ent = mapper.toEntity(dto);

        // THEN: id ignoré et champs copiés
        assertNull(ent.getIdClient());
        assertEquals("Dupont", ent.getNom());
        assertEquals("Jean", ent.getPrenom());
        assertEquals("jean@dupont.fr", ent.getEmail());
        assertEquals("0102030405", ent.getTelephone());
        assertEquals("1 rue X", ent.getAdresse());
        assertEquals("AA-123-AA", ent.getImmatriculation());
        // selon ton mapper, tu peux hasher/saisir le mot de passe ailleurs,
        // ici on vérifie juste que le champ est bien passé si mappé tel quel :
        // assertEquals("secret123", ent.getMotDePasse());

        // WHEN: mapping retour vers la réponse
        ent.setIdClient(7);
        ClientResponse res = mapper.toResponse(ent);

        // THEN: réponse cohérente
        assertEquals(7, res.getIdClient());
        assertEquals("Dupont", res.getNom());
        assertEquals("Jean", res.getPrenom());
        assertEquals("jean@dupont.fr", res.getEmail());
        assertEquals("0102030405", res.getTelephone());
        assertEquals("1 rue X", res.getAdresse());
        assertEquals("AA-123-AA", res.getImmatriculation());
    }
}
