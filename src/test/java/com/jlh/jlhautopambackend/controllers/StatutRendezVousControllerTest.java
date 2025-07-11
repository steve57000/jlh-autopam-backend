package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.StatutRendezVous;
import com.jlh.jlhautopambackend.repositories.StatutRendezVousRepository;
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
        controllers = StatutRendezVousController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class StatutRendezVousControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StatutRendezVousRepository statutRepo;

    @Test
    @DisplayName("GET /api/statuts-rendezvous ➔ 200, json list")
    void testGetAll() throws Exception {
        StatutRendezVous s1 = StatutRendezVous.builder()
                .codeStatut("A")
                .libelle("Alpha")
                .build();
        StatutRendezVous s2 = StatutRendezVous.builder()
                .codeStatut("B")
                .libelle("Beta")
                .build();
        Mockito.when(statutRepo.findAll()).thenReturn(Arrays.asList(s1, s2));

        mvc.perform(get("/api/statuts-rendezvous").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codeStatut").value("A"))
                .andExpect(jsonPath("$[1].libelle").value("Beta"));
    }

    @Test
    @DisplayName("GET /api/statuts-rendezvous/{code} ➔ 200 if exists")
    void testGetByCodeFound() throws Exception {
        StatutRendezVous s = StatutRendezVous.builder()
                .codeStatut("X")
                .libelle("Xray")
                .build();
        Mockito.when(statutRepo.findById("X")).thenReturn(Optional.of(s));

        mvc.perform(get("/api/statuts-rendezvous/X").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Xray"));
    }

    @Test
    @DisplayName("GET /api/statuts-rendezvous/{code} ➔ 404 if not found")
    void testGetByCodeNotFound() throws Exception {
        Mockito.when(statutRepo.findById("Z")).thenReturn(Optional.empty());

        mvc.perform(get("/api/statuts-rendezvous/Z").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/statuts-rendezvous ➔ 201 when new")
    void testCreateSuccess() throws Exception {
        StatutRendezVous in = StatutRendezVous.builder()
                .codeStatut("C")
                .libelle("Charlie")
                .build();
        StatutRendezVous saved = StatutRendezVous.builder()
                .codeStatut("C")
                .libelle("Charlie")
                .build();
        Mockito.when(statutRepo.existsById("C")).thenReturn(false);
        Mockito.when(statutRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/statuts-rendezvous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/statuts-rendezvous/C"))
                .andExpect(jsonPath("$.codeStatut").value("C"));
    }

    @Test
    @DisplayName("POST /api/statuts-rendezvous ➔ 409 when already exists")
    void testCreateConflict() throws Exception {
        StatutRendezVous in = StatutRendezVous.builder()
                .codeStatut("D")
                .libelle("Delta")
                .build();
        Mockito.when(statutRepo.existsById("D")).thenReturn(true);

        mvc.perform(post("/api/statuts-rendezvous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/statuts-rendezvous/{code} ➔ 200 when exists")
    void testUpdateFound() throws Exception {
        StatutRendezVous existing = StatutRendezVous.builder()
                .codeStatut("E")
                .libelle("Echo")
                .build();
        StatutRendezVous updates = StatutRendezVous.builder()
                .libelle("EchoUpdated")
                .build();
        StatutRendezVous saved = StatutRendezVous.builder()
                .codeStatut("E")
                .libelle("EchoUpdated")
                .build();
        Mockito.when(statutRepo.findById("E")).thenReturn(Optional.of(existing));
        Mockito.when(statutRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(put("/api/statuts-rendezvous/E")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("EchoUpdated"));
    }

    @Test
    @DisplayName("PUT /api/statuts-rendezvous/{code} ➔ 404 if not found")
    void testUpdateNotFound() throws Exception {
        StatutRendezVous updates = StatutRendezVous.builder()
                .libelle("Nope")
                .build();
        Mockito.when(statutRepo.findById("F")).thenReturn(Optional.empty());

        mvc.perform(put("/api/statuts-rendezvous/F")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/statuts-rendezvous/{code} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(statutRepo.existsById("G")).thenReturn(true);

        mvc.perform(delete("/api/statuts-rendezvous/G"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/statuts-rendezvous/{code} ➔ 404 if not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(statutRepo.existsById("H")).thenReturn(false);

        mvc.perform(delete("/api/statuts-rendezvous/H"))
                .andExpect(status().isNotFound());
    }
}
