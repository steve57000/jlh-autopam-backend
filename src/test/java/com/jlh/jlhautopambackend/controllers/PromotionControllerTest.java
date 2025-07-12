package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.PromotionRequest;
import com.jlh.jlhautopambackend.dto.PromotionResponse;
import com.jlh.jlhautopambackend.services.PromotionService;
import com.jlh.jlhautopambackend.utils.JwtUtil;
import com.jlh.jlhautopambackend.config.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
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
    private PromotionService service;

    // Désactivation de la sécurité JWT
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/promotions ➔ 200, json list")
    void testGetAll() throws Exception {
        PromotionResponse p1 = PromotionResponse.builder()
                .idPromotion(1)
                .administrateurId(10)
                .imageUrl("http://img1")
                .validFrom(Instant.parse("2025-06-01T00:00:00Z"))
                .validTo(Instant.parse("2025-06-07T23:59:00Z"))
                .build();
        PromotionResponse p2 = PromotionResponse.builder()
                .idPromotion(2)
                .administrateurId(20)
                .imageUrl("http://img2")
                .validFrom(Instant.parse("2025-07-01T00:00:00Z"))
                .validTo(Instant.parse("2025-07-05T23:59:00Z"))
                .build();

        when(service.findAll()).thenReturn(Arrays.asList(p1, p2));

        mvc.perform(get("/api/promotions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPromotion").value(1))
                .andExpect(jsonPath("$[0].imageUrl").value("http://img1"))
                .andExpect(jsonPath("$[1].administrateurId").value(20));
    }

    @Test
    @DisplayName("GET /api/promotions/{id} ➔ 200 when found")
    void testGetByIdFound() throws Exception {
        PromotionResponse p = PromotionResponse.builder()
                .idPromotion(3)
                .administrateurId(30)
                .imageUrl("http://img3")
                .validFrom(Instant.parse("2025-08-01T00:00:00Z"))
                .validTo(Instant.parse("2025-08-02T00:00:00Z"))
                .build();
        when(service.findById(3)).thenReturn(Optional.of(p));

        mvc.perform(get("/api/promotions/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPromotion").value(3))
                .andExpect(jsonPath("$.imageUrl").value("http://img3"));
    }

    @Test
    @DisplayName("GET /api/promotions/{id} ➔ 404 when not found")
    void testGetByIdNotFound() throws Exception {
        when(service.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/promotions/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/promotions ➔ 201 when valid")
    void testCreateSuccess() throws Exception {
        PromotionRequest req = PromotionRequest.builder()
                .administrateurId(5)
                .imageUrl("http://new")
                .validFrom(Instant.parse("2025-06-01T00:00:00Z"))
                .validTo(Instant.parse("2025-06-07T23:59:00Z"))
                .build();
        PromotionResponse saved = PromotionResponse.builder()
                .idPromotion(7)
                .administrateurId(5)
                .imageUrl(req.getImageUrl())
                .validFrom(req.getValidFrom())
                .validTo(req.getValidTo())
                .build();

        when(service.create(any(PromotionRequest.class))).thenReturn(saved);

        mvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/promotions/7"))
                .andExpect(jsonPath("$.idPromotion").value(7))
                .andExpect(jsonPath("$.administrateurId").value(5));
    }

    @Test
    @DisplayName("POST /api/promotions ➔ 400 when admin missing")
    void testCreateBadRequestAdminMissing() throws Exception {
        PromotionRequest req = PromotionRequest.builder()
                .administrateurId(8)
                .imageUrl("http://bad")
                .validFrom(Instant.now())
                .validTo(Instant.now().plusSeconds(3600))
                .build();
        when(service.create(any(PromotionRequest.class)))
                .thenThrow(new IllegalArgumentException("Administrateur introuvable : 8"));

        mvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/promotions ➔ 400 when dates invalid")
    void testCreateBadRequestDates() throws Exception {
        PromotionRequest req = PromotionRequest.builder()
                .administrateurId(9)
                .imageUrl("http://bad")
                .validFrom(Instant.parse("2025-06-10T00:00:00Z"))
                .validTo(Instant.parse("2025-06-01T00:00:00Z"))
                .build();
        when(service.create(any(PromotionRequest.class)))
                .thenThrow(new IllegalArgumentException("validFrom doit être avant validTo"));

        mvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/promotions/{id} ➔ 200 when found")
    void testUpdateFound() throws Exception {
        PromotionRequest req = PromotionRequest.builder()
                .administrateurId(11) // reste le même admin
                .imageUrl("http://upd")
                .validFrom(Instant.parse("2025-06-02T00:00:00Z"))
                .validTo(Instant.parse("2025-06-06T00:00:00Z"))
                .build();
        PromotionResponse updated = PromotionResponse.builder()
                .idPromotion(11)
                .administrateurId(11)
                .imageUrl(req.getImageUrl())
                .validFrom(req.getValidFrom())
                .validTo(req.getValidTo())
                .build();

        when(service.update(eq(11), any(PromotionRequest.class)))
                .thenReturn(Optional.of(updated));

        mvc.perform(put("/api/promotions/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("http://upd"))
                .andExpect(jsonPath("$.validTo").value("2025-06-06T00:00:00Z"));
    }

    @Test
    @DisplayName("PUT /api/promotions/{id} ➔ 400 when dates invalid")
    void testUpdateBadRequestDates() throws Exception {
        PromotionRequest req = PromotionRequest.builder()
                .administrateurId(12)
                .imageUrl("http://bad")
                .validFrom(Instant.parse("2025-06-10T00:00:00Z"))
                .validTo(Instant.parse("2025-06-01T00:00:00Z"))
                .build();
        when(service.update(eq(12), any(PromotionRequest.class)))
                .thenThrow(new IllegalArgumentException("validFrom doit être avant validTo"));

        mvc.perform(put("/api/promotions/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/promotions/{id} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        PromotionRequest req = PromotionRequest.builder()
                .administrateurId(99)
                .imageUrl("http://none")
                .validFrom(Instant.now())
                .validTo(Instant.now().plusSeconds(3600))
                .build();
        when(service.update(eq(99), any(PromotionRequest.class)))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/promotions/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/promotions/{id} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        when(service.delete(13)).thenReturn(true);

        mvc.perform(delete("/api/promotions/13"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/promotions/{id} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        when(service.delete(99)).thenReturn(false);

        mvc.perform(delete("/api/promotions/99"))
                .andExpect(status().isNotFound());
    }
}
