package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.Promotion;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.repositories.PromotionRepository;
import com.jlh.jlhautopambackend.repositories.AdministrateurRepository;
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
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PromotionController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class PromotionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PromotionRepository promoRepo;

    @MockitoBean
    private AdministrateurRepository adminRepo;

    @Test
    @DisplayName("GET /api/promotions ➔ 200, json list")
    void testGetAll() throws Exception {
        Promotion p1 = Promotion.builder()
                .idPromotion(1)
                .administrateur(Administrateur.builder().idAdmin(10).build())
                .imageUrl("http://img1")
                .validFrom(Instant.parse("2025-06-01T00:00:00Z"))
                .validTo(Instant.parse("2025-06-07T23:59:00Z"))
                .build();
        Promotion p2 = Promotion.builder()
                .idPromotion(2)
                .administrateur(Administrateur.builder().idAdmin(20).build())
                .imageUrl("http://img2")
                .validFrom(Instant.parse("2025-07-01T00:00:00Z"))
                .validTo(Instant.parse("2025-07-05T23:59:00Z"))
                .build();

        Mockito.when(promoRepo.findAll()).thenReturn(Arrays.asList(p1, p2));

        mvc.perform(get("/api/promotions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPromotion").value(1))
                .andExpect(jsonPath("$[0].imageUrl").value("http://img1"))
                .andExpect(jsonPath("$[1].administrateur.idAdmin").value(20));
    }

    @Test
    @DisplayName("GET /api/promotions/{id} ➔ 200 when found")
    void testGetByIdFound() throws Exception {
        Promotion p = Promotion.builder()
                .idPromotion(3)
                .administrateur(Administrateur.builder().idAdmin(30).build())
                .imageUrl("http://img3")
                .validFrom(Instant.parse("2025-08-01T00:00:00Z"))
                .validTo(Instant.parse("2025-08-02T00:00:00Z"))
                .build();
        Mockito.when(promoRepo.findById(3)).thenReturn(Optional.of(p));

        mvc.perform(get("/api/promotions/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPromotion").value(3))
                .andExpect(jsonPath("$.imageUrl").value("http://img3"));
    }

    @Test
    @DisplayName("GET /api/promotions/{id} ➔ 404 when not found")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(promoRepo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/promotions/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/promotions ➔ 201 when valid")
    void testCreateSuccess() throws Exception {
        Administrateur admin = Administrateur.builder().idAdmin(5).build();
        Promotion in = Promotion.builder()
                .administrateur(admin)
                .imageUrl("http://new")
                .validFrom(Instant.parse("2025-06-01T00:00:00Z"))
                .validTo(Instant.parse("2025-06-07T23:59:00Z"))
                .build();
        Promotion saved = Promotion.builder()
                .idPromotion(7)
                .administrateur(admin)
                .imageUrl("http://new")
                .validFrom(in.getValidFrom())
                .validTo(in.getValidTo())
                .build();

        Mockito.when(adminRepo.findById(5)).thenReturn(Optional.of(admin));
        Mockito.when(promoRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/promotions/7"))
                .andExpect(jsonPath("$.idPromotion").value(7))
                .andExpect(jsonPath("$.administrateur.idAdmin").value(5));
    }

    @Test
    @DisplayName("POST /api/promotions ➔ 400 when admin missing")
    void testCreateBadRequestAdminMissing() throws Exception {
        Promotion in = Promotion.builder()
                .administrateur(Administrateur.builder().idAdmin(8).build())
                .imageUrl("http://bad")
                .validFrom(Instant.now())
                .validTo(Instant.now().plusSeconds(3600))
                .build();
        Mockito.when(adminRepo.findById(8)).thenReturn(Optional.empty());

        mvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/promotions ➔ 400 when dates invalid")
    void testCreateBadRequestDates() throws Exception {
        Administrateur admin = Administrateur.builder().idAdmin(9).build();
        Promotion in = Promotion.builder()
                .administrateur(admin)
                .imageUrl("http://bad")
                .validFrom(Instant.parse("2025-06-10T00:00:00Z"))
                .validTo(Instant.parse("2025-06-01T00:00:00Z"))
                .build();
        Mockito.when(adminRepo.findById(9)).thenReturn(Optional.of(admin));

        mvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/promotions/{id} ➔ 200 when found")
    void testUpdateFound() throws Exception {
        Promotion existing = Promotion.builder()
                .idPromotion(11)
                .administrateur(Administrateur.builder().idAdmin(11).build())
                .imageUrl("http://old")
                .validFrom(Instant.parse("2025-06-01T00:00:00Z"))
                .validTo(Instant.parse("2025-06-05T00:00:00Z"))
                .build();
        Promotion updates = Promotion.builder()
                .imageUrl("http://upd")
                .validFrom(Instant.parse("2025-06-02T00:00:00Z"))
                .validTo(Instant.parse("2025-06-06T00:00:00Z"))
                .build();
        Promotion saved = Promotion.builder()
                .idPromotion(11)
                .administrateur(existing.getAdministrateur())
                .imageUrl("http://upd")
                .validFrom(updates.getValidFrom())
                .validTo(updates.getValidTo())
                .build();

        Mockito.when(promoRepo.findById(11)).thenReturn(Optional.of(existing));
        Mockito.when(promoRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(put("/api/promotions/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("http://upd"))
                .andExpect(jsonPath("$.validTo").value("2025-06-06T00:00:00Z"));
    }

    @Test
    @DisplayName("PUT /api/promotions/{id} ➔ 400 when dates invalid")
    void testUpdateBadRequestDates() throws Exception {
        Promotion existing = Promotion.builder()
                .idPromotion(12)
                .administrateur(Administrateur.builder().idAdmin(12).build())
                .imageUrl("http://old")
                .validFrom(Instant.parse("2025-06-01T00:00:00Z"))
                .validTo(Instant.parse("2025-06-05T00:00:00Z"))
                .build();
        Promotion updates = Promotion.builder()
                .imageUrl("http://bad")
                .validFrom(Instant.parse("2025-06-10T00:00:00Z"))
                .validTo(Instant.parse("2025-06-01T00:00:00Z"))
                .build();

        Mockito.when(promoRepo.findById(12)).thenReturn(Optional.of(existing));

        mvc.perform(put("/api/promotions/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/promotions/{id} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        Promotion updates = Promotion.builder()
                .imageUrl("http://none")
                .validFrom(Instant.now())
                .validTo(Instant.now().plusSeconds(3600))
                .build();
        Mockito.when(promoRepo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(put("/api/promotions/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/promotions/{id} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(promoRepo.existsById(13)).thenReturn(true);

        mvc.perform(delete("/api/promotions/13"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/promotions/{id} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(promoRepo.existsById(99)).thenReturn(false);

        mvc.perform(delete("/api/promotions/99"))
                .andExpect(status().isNotFound());
    }
}
