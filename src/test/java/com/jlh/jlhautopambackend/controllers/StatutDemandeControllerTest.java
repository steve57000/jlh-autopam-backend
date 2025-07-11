package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.StatutDemande;
import com.jlh.jlhautopambackend.repositories.StatutDemandeRepository;
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

import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = StatutDemandeController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class StatutDemandeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StatutDemandeRepository statutRepo;

    @Test
    @DisplayName("GET /api/statuts-demande ➔ 200, json list")
    void testGetAll() throws Exception {
        StatutDemande s1 = StatutDemande.builder()
                .codeStatut("A")
                .libelle("Alpha")
                .build();
        StatutDemande s2 = StatutDemande.builder()
                .codeStatut("B")
                .libelle("Beta")
                .build();

        Mockito.when(statutRepo.findAll()).thenReturn(Arrays.asList(s1, s2));

        mvc.perform(get("/api/statuts-demande").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codeStatut").value("A"))
                .andExpect(jsonPath("$[1].libelle").value("Beta"));
    }

    @Test
    @DisplayName("GET /api/statuts-demande/{code} ➔ 200 if exists")
    void testGetByCodeFound() throws Exception {
        StatutDemande s = StatutDemande.builder()
                .codeStatut("X")
                .libelle("Xray")
                .build();
        Mockito.when(statutRepo.findById("X")).thenReturn(Optional.of(s));

        mvc.perform(get("/api/statuts-demande/X").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Xray"));
    }

    @Test
    @DisplayName("GET /api/statuts-demande/{code} ➔ 404 if not found")
    void testGetByCodeNotFound() throws Exception {
        Mockito.when(statutRepo.findById("Z")).thenReturn(Optional.empty());

        mvc.perform(get("/api/statuts-demande/Z").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/statuts-demande ➔ 201 when new")
    void testCreateSuccess() throws Exception {
        StatutDemande in = StatutDemande.builder()
                .codeStatut("C")
                .libelle("Charlie")
                .build();
        StatutDemande saved = StatutDemande.builder()
                .codeStatut("C")
                .libelle("Charlie")
                .build();

        Mockito.when(statutRepo.existsById("C")).thenReturn(false);
        Mockito.when(statutRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/statuts-demande")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/statuts-demande/C"))
                .andExpect(jsonPath("$.codeStatut").value("C"));
    }

    @Test
    @DisplayName("POST /api/statuts-demande ➔ 409 when exists")
    void testCreateConflict() throws Exception {
        StatutDemande in = StatutDemande.builder()
                .codeStatut("D")
                .libelle("Delta")
                .build();

        Mockito.when(statutRepo.existsById("D")).thenReturn(true);

        mvc.perform(post("/api/statuts-demande")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/statuts-demande/{code} ➔ 200 when exists")
    void testUpdateFound() throws Exception {
        StatutDemande existing = StatutDemande.builder()
                .codeStatut("E")
                .libelle("Echo")
                .build();
        StatutDemande updates = StatutDemande.builder()
                .libelle("EchoUpdated")
                .build();
        StatutDemande saved = StatutDemande.builder()
                .codeStatut("E")
                .libelle("EchoUpdated")
                .build();

        Mockito.when(statutRepo.findById("E")).thenReturn(Optional.of(existing));
        Mockito.when(statutRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(put("/api/statuts-demande/E")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("EchoUpdated"));
    }

    @Test
    @DisplayName("PUT /api/statuts-demande/{code} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        StatutDemande updates = StatutDemande.builder()
                .libelle("Nope")
                .build();
        Mockito.when(statutRepo.findById("F")).thenReturn(Optional.empty());

        mvc.perform(put("/api/statuts-demande/F")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/statuts-demande/{code} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(statutRepo.existsById("G")).thenReturn(true);

        mvc.perform(delete("/api/statuts-demande/G"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/statuts-demande/{code} ➔ 404 if not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(statutRepo.existsById("H")).thenReturn(false);

        mvc.perform(delete("/api/statuts-demande/H"))
                .andExpect(status().isNotFound());
    }
}
