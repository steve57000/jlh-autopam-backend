package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.repositories.DemandeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = DemandeController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class DemandeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DemandeRepository repo;

    @Test
    @DisplayName("GET /api/demandes ➔ 200, json list")
    void testGetAll() throws Exception {
        Demande d1 = Demande.builder()
                .idDemande(1)
                .dateDemande(Instant.parse("2025-01-01T10:00:00Z"))
                .services(Collections.emptyList())
                .build();
        Demande d2 = Demande.builder()
                .idDemande(2)
                .dateDemande(Instant.parse("2025-01-02T11:00:00Z"))
                .services(Collections.emptyList())
                .build();

        Mockito.when(repo.findAll()).thenReturn(Arrays.asList(d1, d2));

        mvc.perform(get("/api/demandes").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDemande").value(1))
                .andExpect(jsonPath("$[1].idDemande").value(2));
    }

    @Test
    @DisplayName("GET /api/demandes/{id} ➔ 200")
    void testGetByIdFound() throws Exception {
        Demande d = Demande.builder()
                .idDemande(1)
                .dateDemande(Instant.parse("2025-01-03T12:00:00Z"))
                .services(Collections.emptyList())
                .build();
        Mockito.when(repo.findById(1)).thenReturn(Optional.of(d));

        mvc.perform(get("/api/demandes/1").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDemande").value(1))
                .andExpect(jsonPath("$.dateSoumission").value("2025-01-03T12:00:00Z"));
    }

    @Test
    @DisplayName("GET /api/demandes/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(repo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/demandes/99").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/demandes ➔ 201 with nested client/type/statut")
    void testCreateWithNested() throws Exception {
        Client client = Client.builder().idClient(10).build();
        TypeDemande type = TypeDemande.builder().codeType("T1").build();
        StatutDemande statut = StatutDemande.builder().codeStatut("S1").build();

        Demande input = Demande.builder()
                .dateDemande(Instant.parse("2025-01-04T08:00:00Z"))
                .client(client)
                .typeDemande(type)
                .statutDemande(statut)
                .services(Collections.emptyList())
                .build();

        Demande saved = Demande.builder()
                .idDemande(3)
                .dateDemande(input.getDateDemande())
                .client(client)
                .typeDemande(type)
                .statutDemande(statut)
                .services(Collections.emptyList())
                .build();

        Mockito.when(repo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/demandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDemande").value(3))
                .andExpect(jsonPath("$.client.idClient").value(10))
                .andExpect(jsonPath("$.typeDemande.codeType").value("T1"))
                .andExpect(jsonPath("$.statutDemande.codeStatut").value("S1"))
                // le champ services n'existe pas dans la réponse
                .andExpect(jsonPath("$.services").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/demandes/{id} ➔ 200 when exists")
    void testUpdateFound() throws Exception {
        Demande existing = Demande.builder()
                .idDemande(4)
                .dateDemande(Instant.parse("2025-01-05T09:00:00Z"))
                .services(Collections.emptyList())
                .build();
        Demande updates = Demande.builder()
                .dateDemande(Instant.parse("2025-02-05T09:00:00Z"))
                .services(Collections.emptyList())
                .build();
        Demande saved = Demande.builder()
                .idDemande(4)
                .dateDemande(updates.getDateDemande())
                .services(Collections.emptyList())
                .build();

        Mockito.when(repo.findById(4)).thenReturn(Optional.of(existing));
        Mockito.when(repo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(put("/api/demandes/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDemande").value(4))
                .andExpect(jsonPath("$.dateSoumission").value("2025-02-05T09:00:00Z"))
                .andExpect(jsonPath("$.services").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/demandes/{id} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        Demande updates = Demande.builder()
                .dateDemande(Instant.parse("2025-03-01T07:00:00Z"))
                .services(Collections.emptyList())
                .build();
        Mockito.when(repo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(put("/api/demandes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/demandes/{id} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Demande d = Demande.builder().idDemande(5).build();
        Mockito.when(repo.findById(5)).thenReturn(Optional.of(d));

        mvc.perform(delete("/api/demandes/5"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/demandes/{id} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(repo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(delete("/api/demandes/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
