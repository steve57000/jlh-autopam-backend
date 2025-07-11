package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.Devis;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.repositories.DevisRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = DevisController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class DevisControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DevisRepository devisRepo;

    @MockitoBean
    private DemandeRepository demandeRepo;

    @Test
    @DisplayName("GET /api/devis ➔ 200, json list")
    void testGetAll() throws Exception {
        Demande d1 = Demande.builder().idDemande(1).build();
        Demande d2 = Demande.builder().idDemande(2).build();
        Devis dv1 = Devis.builder()
                .idDevis(10)
                .demande(d1)
                .dateDevis(Instant.parse("2025-01-01T10:00:00Z"))
                .montantTotal(new BigDecimal("100.00"))
                .build();
        Devis dv2 = Devis.builder()
                .idDevis(20)
                .demande(d2)
                .dateDevis(Instant.parse("2025-01-02T11:00:00Z"))
                .montantTotal(new BigDecimal("200.00"))
                .build();

        Mockito.when(devisRepo.findAll()).thenReturn(Arrays.asList(dv1, dv2));

        mvc.perform(get("/api/devis").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDevis").value(10))
                .andExpect(jsonPath("$[1].montantTotal").value(200.00));
    }

    @Test
    @DisplayName("GET /api/devis/{id} ➔ 200")
    void testGetByIdFound() throws Exception {
        Demande d = Demande.builder().idDemande(3).build();
        Devis dv = Devis.builder()
                .idDevis(30)
                .demande(d)
                .dateDevis(Instant.parse("2025-01-03T12:00:00Z"))
                .montantTotal(new BigDecimal("300.50"))
                .build();

        Mockito.when(devisRepo.findById(30)).thenReturn(Optional.of(dv));

        mvc.perform(get("/api/devis/30").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDevis").value(30))
                .andExpect(jsonPath("$.montantTotal").value(300.50));
    }

    @Test
    @DisplayName("GET /api/devis/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(devisRepo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/devis/99").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/devis ➔ 201 when demande exists and no conflict")
    void testCreateSuccess() throws Exception {
        int demandeId = 4;
        Devis input = Devis.builder()
                .demande(Demande.builder().idDemande(demandeId).build())
                .dateDevis(Instant.parse("2025-01-04T08:00:00Z"))
                .montantTotal(new BigDecimal("400.00"))
                .build();
        Devis saved = Devis.builder()
                .idDevis(40)
                .demande(input.getDemande())
                .dateDevis(input.getDateDevis())
                .montantTotal(input.getMontantTotal())
                .build();

        Mockito.when(demandeRepo.findById(demandeId)).thenReturn(Optional.of(input.getDemande()));
        Mockito.when(devisRepo.findAll()).thenReturn(Collections.emptyList());
        Mockito.when(devisRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/devis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/devis/40"))
                .andExpect(jsonPath("$.idDevis").value(40));
    }

    @Test
    @DisplayName("POST /api/devis ➔ 400 when demande missing")
    void testCreateBadRequest() throws Exception {
        int demandeId = 5;
        Devis input = Devis.builder()
                .demande(Demande.builder().idDemande(demandeId).build())
                .dateDevis(Instant.now())
                .montantTotal(new BigDecimal("500.00"))
                .build();

        Mockito.when(demandeRepo.findById(demandeId)).thenReturn(Optional.empty());

        mvc.perform(post("/api/devis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/devis ➔ 409 when conflict")
    void testCreateConflict() throws Exception {
        int demandeId = 6;
        Devis input = Devis.builder()
                .demande(Demande.builder().idDemande(demandeId).build())
                .dateDevis(Instant.now())
                .montantTotal(new BigDecimal("600.00"))
                .build();
        Devis existing = Devis.builder()
                .idDevis(60)
                .demande(input.getDemande())
                .dateDevis(Instant.parse("2025-01-01T00:00:00Z"))
                .montantTotal(BigDecimal.ZERO)
                .build();

        Mockito.when(demandeRepo.findById(demandeId)).thenReturn(Optional.of(input.getDemande()));
        Mockito.when(devisRepo.findAll()).thenReturn(Collections.singletonList(existing));

        mvc.perform(post("/api/devis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/devis/{id} ➔ 200 when exists and no conflict")
    void testUpdateSuccess() throws Exception {
        int id = 7;
        int oldDemId = 7, newDemId = 8;
        Devis existing = Devis.builder()
                .idDevis(id)
                .demande(Demande.builder().idDemande(oldDemId).build())
                .dateDevis(Instant.parse("2025-01-07T07:00:00Z"))
                .montantTotal(new BigDecimal("700.00"))
                .build();
        Devis dto = Devis.builder()
                .demande(Demande.builder().idDemande(newDemId).build())
                .dateDevis(Instant.parse("2025-02-07T07:00:00Z"))
                .montantTotal(new BigDecimal("800.00"))
                .build();
        Devis updated = Devis.builder()
                .idDevis(id)
                .demande(dto.getDemande())
                .dateDevis(dto.getDateDevis())
                .montantTotal(dto.getMontantTotal())
                .build();

        Mockito.when(devisRepo.findById(id)).thenReturn(Optional.of(existing));
        Mockito.when(demandeRepo.findById(newDemId)).thenReturn(Optional.of(dto.getDemande()));
        Mockito.when(devisRepo.findAll()).thenReturn(Collections.emptyList());
        Mockito.when(devisRepo.save(Mockito.any())).thenReturn(updated);

        mvc.perform(put("/api/devis/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateDevis").value(dto.getDateDevis().toString()))
                .andExpect(jsonPath("$.montantTotal").value(800.00));
    }

    @Test
    @DisplayName("PUT /api/devis/{id} ➔ 400 when new demande missing")
    void testUpdateBadRequest() throws Exception {
        int id = 8;
        int newDemId = 9;
        Devis existing = Devis.builder()
                .idDevis(id)
                .demande(Demande.builder().idDemande(8).build())
                .dateDevis(Instant.now())
                .montantTotal(BigDecimal.ZERO)
                .build();
        Devis dto = Devis.builder()
                .demande(Demande.builder().idDemande(newDemId).build())
                .dateDevis(Instant.now())
                .montantTotal(BigDecimal.ZERO)
                .build();

        Mockito.when(devisRepo.findById(id)).thenReturn(Optional.of(existing));
        Mockito.when(demandeRepo.findById(newDemId)).thenReturn(Optional.empty());

        mvc.perform(put("/api/devis/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/devis/{id} ➔ 409 when conflict on update")
    void testUpdateConflict() throws Exception {
        int id = 9;
        int oldDemId = 9, newDemId = 10;
        Devis existing = Devis.builder()
                .idDevis(id)
                .demande(Demande.builder().idDemande(oldDemId).build())
                .dateDevis(Instant.now())
                .montantTotal(BigDecimal.ZERO)
                .build();
        Devis dto = Devis.builder()
                .demande(Demande.builder().idDemande(newDemId).build())
                .dateDevis(Instant.now())
                .montantTotal(BigDecimal.ZERO)
                .build();
        Devis conflict = Devis.builder()
                .idDevis(100)
                .demande(dto.getDemande())
                .dateDevis(Instant.now())
                .montantTotal(BigDecimal.ZERO)
                .build();

        Mockito.when(devisRepo.findById(id)).thenReturn(Optional.of(existing));
        Mockito.when(demandeRepo.findById(newDemId)).thenReturn(Optional.of(dto.getDemande()));
        Mockito.when(devisRepo.findAll()).thenReturn(Collections.singletonList(conflict));

        mvc.perform(put("/api/devis/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/devis/{id} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        Mockito.when(devisRepo.findById(42)).thenReturn(Optional.empty());

        mvc.perform(put("/api/devis/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/devis/{id} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(devisRepo.existsById(11)).thenReturn(true);

        mvc.perform(delete("/api/devis/11"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/devis/{id} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(devisRepo.existsById(99)).thenReturn(false);

        mvc.perform(delete("/api/devis/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
