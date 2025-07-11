package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.Service;
import com.jlh.jlhautopambackend.repositories.ServiceRepository;
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
import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ServiceController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class ServiceControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServiceRepository repo;

    @Test
    @DisplayName("GET /api/services ➔ 200, json list")
    void testGetAll() throws Exception {
        Service s1 = Service.builder()
                .idService(1)
                .libelle("S1")
                .description("Desc1")
                .prixUnitaire(new BigDecimal("12.34"))
                .build();
        Service s2 = Service.builder()
                .idService(2)
                .libelle("S2")
                .description("Desc2")
                .prixUnitaire(new BigDecimal("56.78"))
                .build();

        Mockito.when(repo.findAll()).thenReturn(Arrays.asList(s1, s2));

        mvc.perform(get("/api/services").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idService").value(1))
                .andExpect(jsonPath("$[1].libelle").value("S2"));
    }

    @Test
    @DisplayName("GET /api/services/{id} ➔ 200")
    void testGetByIdFound() throws Exception {
        Service s = Service.builder()
                .idService(1)
                .libelle("S1")
                .description("Desc1")
                .prixUnitaire(new BigDecimal("12.34"))
                .build();
        Mockito.when(repo.findById(1)).thenReturn(Optional.of(s));

        mvc.perform(get("/api/services/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Desc1"));
    }

    @Test
    @DisplayName("GET /api/services/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(repo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/services/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/services ➔ 200 with saved entity")
    void testCreate() throws Exception {
        Service in = Service.builder()
                .libelle("New")
                .description("NewDesc")
                .prixUnitaire(new BigDecimal("99.99"))
                .build();
        Service saved = Service.builder()
                .idService(3)
                .libelle("New")
                .description("NewDesc")
                .prixUnitaire(new BigDecimal("99.99"))
                .build();
        Mockito.when(repo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idService").value(3))
                .andExpect(jsonPath("$.prixUnitaire").value(99.99));
    }

    @Test
    @DisplayName("PUT /api/services/{id} ➔ 200 when exists")
    void testUpdateFound() throws Exception {
        Service existing = Service.builder()
                .idService(1)
                .libelle("Old")
                .description("OldDesc")
                .prixUnitaire(new BigDecimal("10.00"))
                .build();
        Service updates = Service.builder()
                .libelle("Updated")
                .description("UpdDesc")
                .prixUnitaire(new BigDecimal("20.00"))
                .build();
        Service saved = Service.builder()
                .idService(1)
                .libelle("Updated")
                .description("UpdDesc")
                .prixUnitaire(new BigDecimal("20.00"))
                .build();

        Mockito.when(repo.findById(1)).thenReturn(Optional.of(existing));
        Mockito.when(repo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(put("/api/services/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Updated"))
                .andExpect(jsonPath("$.prixUnitaire").value(20.00));
    }

    @Test
    @DisplayName("PUT /api/services/{id} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        Service updates = Service.builder()
                .libelle("X")
                .description("X")
                .prixUnitaire(new BigDecimal("1.00"))
                .build();
        Mockito.when(repo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(put("/api/services/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/services/{id} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Service s = Service.builder().idService(1).build();
        Mockito.when(repo.findById(1)).thenReturn(Optional.of(s));

        mvc.perform(delete("/api/services/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/services/{id} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(repo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(delete("/api/services/99"))
                .andExpect(status().isNotFound());
    }
}
