package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
import com.jlh.jlhautopambackend.modeles.Client;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClientMapperTest {
    private final ClientMapper mapper = Mappers.getMapper(ClientMapper.class);

    @Test
    void shouldMapRequestToEntityAndBack() {
        ClientRequest dto = ClientRequest.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("jean@dupont.fr")
                .telephone("0102030405")
                .adresse("1 rue X")
                .build();
        // toEntity
        Client ent = mapper.toEntity(dto);
        assertNull(ent.getIdClient());
        assertEquals("Dupont", ent.getNom());
        // toResponse
        ent.setIdClient(7);
        ClientResponse res = mapper.toResponse(ent);
        assertEquals(7, res.getIdClient());
        assertEquals("Jean", res.getPrenom());
    }
}
