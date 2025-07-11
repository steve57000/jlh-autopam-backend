package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.modeles.StatutCreneau;
import com.jlh.jlhautopambackend.repositories.CreneauRepository;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CreneauController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class CreneauControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreneauRepository creneauRepo;

    @MockitoBean
    private StatutCreneauRepository statutRepo;

    @Test
    @DisplayName("GET /api/creneaux ➔ 200, json list")
    void testGetAll() throws Exception {
        StatutCreneau okStatut = StatutCreneau.builder()
                .codeStatut("OK")
                .libelle("OK")
                .build();

        Creneau c1 = Creneau.builder()
                .idCreneau(1)
                .dateDebut(Instant.parse("2025-01-01T10:00:00Z"))
                .dateFin(Instant.parse("2025-01-01T11:00:00Z"))
                .statut(okStatut)
                .disponibilites(Collections.emptyList())
                .build();
        Creneau c2 = Creneau.builder()
                .idCreneau(2)
                .dateDebut(Instant.parse("2025-01-02T10:00:00Z"))
                .dateFin(Instant.parse("2025-01-02T11:00:00Z"))
                .statut(okStatut)
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(creneauRepo.findAll()).thenReturn(Arrays.asList(c1, c2));

        mvc.perform(get("/api/creneaux").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCreneau").value(1))
                .andExpect(jsonPath("$[1].idCreneau").value(2));
    }

    @Test
    @DisplayName("GET /api/creneaux/{id} ➔ 200")
    void testGetByIdFound() throws Exception {
        StatutCreneau okStatut = StatutCreneau.builder()
                .codeStatut("OK")
                .libelle("OK")
                .build();

        Creneau c = Creneau.builder()
                .idCreneau(1)
                .dateDebut(Instant.parse("2025-01-01T10:00:00Z"))
                .dateFin(Instant.parse("2025-01-01T11:00:00Z"))
                .statut(okStatut)
                .disponibilites(Collections.emptyList())
                .build();
        Mockito.when(creneauRepo.findById(1)).thenReturn(Optional.of(c));

        mvc.perform(get("/api/creneaux/1").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCreneau").value(1));
    }

    @Test
    @DisplayName("GET /api/creneaux/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(creneauRepo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/creneaux/99").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/creneaux ➔ 201 when statut exists")
    void testCreateSuccess() throws Exception {
        StatutCreneau newStatut = StatutCreneau.builder()
                .codeStatut("NEW")
                .libelle("Nouveau")
                .build();

        Creneau in = Creneau.builder()
                .dateDebut(Instant.parse("2025-01-03T10:00:00Z"))
                .dateFin(Instant.parse("2025-01-03T11:00:00Z"))
                .statut(StatutCreneau.builder().codeStatut("NEW").build())
                .disponibilites(Collections.emptyList())
                .build();
        Creneau saved = Creneau.builder()
                .idCreneau(5)
                .dateDebut(in.getDateDebut())
                .dateFin(in.getDateFin())
                .statut(newStatut)
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(statutRepo.findById("NEW")).thenReturn(Optional.of(newStatut));
        Mockito.when(creneauRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/creneaux")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/creneaux/5"))
                .andExpect(jsonPath("$.idCreneau").value(5))
                .andExpect(jsonPath("$.statut.codeStatut").value("NEW"));
    }

    @Test
    @DisplayName("POST /api/creneaux ➔ 400 when statut missing")
    void testCreateBadRequest() throws Exception {
        Creneau in = Creneau.builder()
                .dateDebut(Instant.parse("2025-01-04T10:00:00Z"))
                .dateFin(Instant.parse("2025-01-04T11:00:00Z"))
                .statut(StatutCreneau.builder().codeStatut("MISSING").build())
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(statutRepo.findById("MISSING")).thenReturn(Optional.empty());

        mvc.perform(post("/api/creneaux")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/creneaux/{id} ➔ 200 when exists and statut exists")
    void testUpdateSuccess() throws Exception {
        StatutCreneau oldStatut = StatutCreneau.builder()
                .codeStatut("OLD")
                .libelle("Old")
                .build();
        StatutCreneau newStatut = StatutCreneau.builder()
                .codeStatut("NEW")
                .libelle("New")
                .build();

        Creneau existing = Creneau.builder()
                .idCreneau(10)
                .dateDebut(Instant.parse("2025-01-01T10:00:00Z"))
                .dateFin(Instant.parse("2025-01-01T11:00:00Z"))
                .statut(oldStatut)
                .disponibilites(Collections.emptyList())
                .build();
        Creneau dto = Creneau.builder()
                .dateDebut(Instant.parse("2025-02-01T10:00:00Z"))
                .dateFin(Instant.parse("2025-02-01T11:00:00Z"))
                .statut(StatutCreneau.builder().codeStatut("NEW").build())
                .disponibilites(Collections.emptyList())
                .build();
        Creneau updated = Creneau.builder()
                .idCreneau(10)
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .statut(newStatut)
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(creneauRepo.findById(10)).thenReturn(Optional.of(existing));
        Mockito.when(statutRepo.findById("NEW")).thenReturn(Optional.of(newStatut));
        Mockito.when(creneauRepo.save(Mockito.any())).thenReturn(updated);

        mvc.perform(put("/api/creneaux/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateDebut").value(dto.getDateDebut().toString()))
                .andExpect(jsonPath("$.statut.codeStatut").value("NEW"));
    }

    @Test
    @DisplayName("PUT /api/creneaux/{id} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        Creneau dto = Creneau.builder()
                .dateDebut(Instant.parse("2025-03-01T10:00:00Z"))
                .dateFin(Instant.parse("2025-03-01T11:00:00Z"))
                .statut(StatutCreneau.builder().codeStatut("OK").build())
                .disponibilites(Collections.emptyList())
                .build();
        Mockito.when(creneauRepo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(put("/api/creneaux/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/creneaux/{id} ➔ 400 when new statut missing")
    void testUpdateBadRequestStatus() throws Exception {
        StatutCreneau oldStatut = StatutCreneau.builder()
                .codeStatut("OLD")
                .libelle("Old")
                .build();

        Creneau existing = Creneau.builder()
                .idCreneau(20)
                .dateDebut(Instant.parse("2025-01-05T10:00:00Z"))
                .dateFin(Instant.parse("2025-01-05T11:00:00Z"))
                .statut(oldStatut)
                .disponibilites(Collections.emptyList())
                .build();
        Creneau dto = Creneau.builder()
                .dateDebut(Instant.parse("2025-01-06T10:00:00Z"))
                .dateFin(Instant.parse("2025-01-06T11:00:00Z"))
                .statut(StatutCreneau.builder().codeStatut("MISSING").build())
                .disponibilites(Collections.emptyList())
                .build();

        Mockito.when(creneauRepo.findById(20)).thenReturn(Optional.of(existing));
        Mockito.when(statutRepo.findById("MISSING")).thenReturn(Optional.empty());

        mvc.perform(put("/api/creneaux/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/creneaux/{id} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(creneauRepo.existsById(1)).thenReturn(true);

        mvc.perform(delete("/api/creneaux/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/creneaux/{id} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(creneauRepo.existsById(99)).thenReturn(false);

        mvc.perform(delete("/api/creneaux/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
