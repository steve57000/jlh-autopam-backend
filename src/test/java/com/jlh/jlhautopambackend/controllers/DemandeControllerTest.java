package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import com.jlh.jlhautopambackend.services.DemandeService;
import com.jlh.jlhautopambackend.utils.JwtUtil;
import com.jlh.jlhautopambackend.config.JwtAuthenticationFilter;
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
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private DemandeService service;

    // **Ajout des beans pour désactiver la sécurité JWT**
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/demandes ➔ 200, json list")
    void testGetAll() throws Exception {
        DemandeResponse r1 = DemandeResponse.builder()
                .idDemande(1)
                .dateDemande(Instant.parse("2025-01-01T10:00:00Z"))
                .services(List.of())
                .build();
        DemandeResponse r2 = DemandeResponse.builder()
                .idDemande(2)
                .dateDemande(Instant.parse("2025-01-02T11:00:00Z"))
                .services(List.of())
                .build();
        Mockito.when(service.findAll()).thenReturn(List.of(r1, r2));

        mvc.perform(get("/api/demandes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDemande").value(1))
                .andExpect(jsonPath("$[1].idDemande").value(2));
    }

    @Test
    @DisplayName("GET /api/demandes/{id} ➔ 200")
    void testGetByIdFound() throws Exception {
        DemandeResponse resp = DemandeResponse.builder()
                .idDemande(3)
                .dateDemande(Instant.parse("2025-01-03T12:00:00Z"))
                .services(List.of())
                .build();
        Mockito.when(service.findById(3)).thenReturn(Optional.of(resp));

        mvc.perform(get("/api/demandes/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDemande").value(3));
    }

    @Test
    @DisplayName("GET /api/demandes/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(service.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/demandes/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/demandes ➔ 201, JSON retourné")
    void testCreate() throws Exception {
        DemandeRequest req = DemandeRequest.builder()
                .dateDemande(Instant.parse("2025-01-04T08:00:00Z"))
                .clientId(10)
                .codeType("T1")
                .codeStatut("S1")
                .build();
        DemandeResponse created = DemandeResponse.builder()
                .idDemande(10)
                .dateDemande(req.getDateDemande())
                .clientId(10)
                .typeDemande(null)
                .statutDemande(null)
                .services(List.of())
                .build();
        Mockito.when(service.create(Mockito.any(DemandeRequest.class))).thenReturn(created);

        mvc.perform(post("/api/demandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/demandes/10"))
                .andExpect(jsonPath("$.idDemande").value(10));
    }

    @Test
    @DisplayName("PUT /api/demandes/{id} ➔ 200, JSON mis à jour")
    void testUpdateFound() throws Exception {
        DemandeRequest req = DemandeRequest.builder()
                .dateDemande(Instant.parse("2025-02-05T09:00:00Z"))
                .clientId(4)
                .codeType("T4")
                .codeStatut("S4")
                .build();
        DemandeResponse updated = DemandeResponse.builder()
                .idDemande(5)
                .dateDemande(req.getDateDemande())
                .clientId(4)
                .typeDemande(null)
                .statutDemande(null)
                .services(List.of())
                .build();
        Mockito.when(service.update(Mockito.eq(5), Mockito.any(DemandeRequest.class)))
                .thenReturn(Optional.of(updated));

        mvc.perform(put("/api/demandes/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateDemande").value("2025-02-05T09:00:00Z"));
    }

    @Test
    @DisplayName("PUT /api/demandes/{id} ➔ 404")
    void testUpdateNotFound() throws Exception {
        DemandeRequest req = DemandeRequest.builder()
                .dateDemande(Instant.parse("2025-03-01T07:00:00Z"))
                .clientId(1)
                .codeType("T1")
                .codeStatut("S1")
                .build();
        Mockito.when(service.update(Mockito.eq(99), Mockito.any(DemandeRequest.class)))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/demandes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/demandes/{id} ➔ 204")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete(1)).thenReturn(true);

        mvc.perform(delete("/api/demandes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/demandes/{id} ➔ 404")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete(99)).thenReturn(false);

        mvc.perform(delete("/api/demandes/99"))
                .andExpect(status().isNotFound());
    }
}
