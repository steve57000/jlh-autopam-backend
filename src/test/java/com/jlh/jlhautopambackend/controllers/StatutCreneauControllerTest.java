package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.StatutCreneau;
import com.jlh.jlhautopambackend.repositories.StatutCreneauRepository;
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
        controllers = StatutCreneauController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class StatutCreneauControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StatutCreneauRepository statutRepo;

    @Test
    @DisplayName("GET /api/statuts-creneau ➔ 200, json list")
    void testGetAll() throws Exception {
        StatutCreneau s1 = StatutCreneau.builder()
                .codeStatut("A")
                .libelle("Alpha")
                .build();
        StatutCreneau s2 = StatutCreneau.builder()
                .codeStatut("B")
                .libelle("Beta")
                .build();

        Mockito.when(statutRepo.findAll()).thenReturn(Arrays.asList(s1, s2));

        mvc.perform(get("/api/statuts-creneau").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codeStatut").value("A"))
                .andExpect(jsonPath("$[1].libelle").value("Beta"));
    }

    @Test
    @DisplayName("GET /api/statuts-creneau/{code} ➔ 200")
    void testGetByIdFound() throws Exception {
        StatutCreneau s = StatutCreneau.builder()
                .codeStatut("X")
                .libelle("Xray")
                .build();
        Mockito.when(statutRepo.findById("X")).thenReturn(Optional.of(s));

        mvc.perform(get("/api/statuts-creneau/X").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Xray"));
    }

    @Test
    @DisplayName("GET /api/statuts-creneau/{code} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(statutRepo.findById("Z")).thenReturn(Optional.empty());

        mvc.perform(get("/api/statuts-creneau/Z").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/statuts-creneau ➔ 201 when new")
    void testCreateSuccess() throws Exception {
        StatutCreneau in = StatutCreneau.builder()
                .codeStatut("C")
                .libelle("Charlie")
                .build();
        StatutCreneau saved = StatutCreneau.builder()
                .codeStatut("C")
                .libelle("Charlie")
                .build();

        Mockito.when(statutRepo.existsById("C")).thenReturn(false);
        Mockito.when(statutRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/statuts-creneau")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/statuts-creneau/C"))
                .andExpect(jsonPath("$.codeStatut").value("C"));
    }

    @Test
    @DisplayName("POST /api/statuts-creneau ➔ 409 when exists")
    void testCreateConflict() throws Exception {
        StatutCreneau in = StatutCreneau.builder()
                .codeStatut("D")
                .libelle("Delta")
                .build();

        Mockito.when(statutRepo.existsById("D")).thenReturn(true);

        mvc.perform(post("/api/statuts-creneau")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/statuts-creneau/{code} ➔ 200 when exists")
    void testUpdateFound() throws Exception {
        StatutCreneau existing = StatutCreneau.builder()
                .codeStatut("E")
                .libelle("Echo")
                .build();
        StatutCreneau updates = StatutCreneau.builder()
                .libelle("EchoUpdated")
                .build();
        StatutCreneau saved = StatutCreneau.builder()
                .codeStatut("E")
                .libelle("EchoUpdated")
                .build();

        Mockito.when(statutRepo.findById("E")).thenReturn(Optional.of(existing));
        Mockito.when(statutRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(put("/api/statuts-creneau/E")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("EchoUpdated"));
    }

    @Test
    @DisplayName("PUT /api/statuts-creneau/{code} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        StatutCreneau updates = StatutCreneau.builder()
                .libelle("Nope")
                .build();
        Mockito.when(statutRepo.findById("F")).thenReturn(Optional.empty());

        mvc.perform(put("/api/statuts-creneau/F")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/statuts-creneau/{code} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(statutRepo.existsById("G")).thenReturn(true);

        mvc.perform(delete("/api/statuts-creneau/G"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/statuts-creneau/{code} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(statutRepo.existsById("H")).thenReturn(false);

        mvc.perform(delete("/api/statuts-creneau/H"))
                .andExpect(status().isNotFound());
    }
}
