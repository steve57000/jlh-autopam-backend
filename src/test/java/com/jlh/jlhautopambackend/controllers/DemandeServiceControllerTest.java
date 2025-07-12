package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.dto.DemandeServiceRequest;
import com.jlh.jlhautopambackend.dto.DemandeServiceResponse;
import com.jlh.jlhautopambackend.dto.DemandeServiceKeyDto;
import com.jlh.jlhautopambackend.services.DemandeServiceService;
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

import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = DemandeServiceController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class DemandeServiceControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DemandeServiceService service;

    // mocks pour JWT
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/demandes-services ➔ 200, json list")
    void testGetAll() throws Exception {
        DemandeServiceResponse ds1 = DemandeServiceResponse.builder()
                .id(DemandeServiceKeyDto.builder().idDemande(1).idService(10).build())
                .quantite(2)
                .build();
        DemandeServiceResponse ds2 = DemandeServiceResponse.builder()
                .id(DemandeServiceKeyDto.builder().idDemande(2).idService(20).build())
                .quantite(5)
                .build();

        Mockito.when(service.findAll()).thenReturn(Arrays.asList(ds1, ds2));

        mvc.perform(get("/api/demandes-services")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id.idDemande").value(1))
                .andExpect(jsonPath("$[1].id.idService").value(20));
    }

    @Test
    @DisplayName("GET /api/demandes-services/{demandeId}/{serviceId} ➔ 200")
    void testGetByIdFound() throws Exception {
        DemandeServiceResponse ds = DemandeServiceResponse.builder()
                .id(DemandeServiceKeyDto.builder().idDemande(3).idService(30).build())
                .quantite(7)
                .build();

        Mockito.when(service.findByKey(3, 30)).thenReturn(Optional.of(ds));

        mvc.perform(get("/api/demandes-services/3/30")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantite").value(7));
    }

    @Test
    @DisplayName("GET /api/demandes-services/{demandeId}/{serviceId} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(service.findByKey(9, 90)).thenReturn(Optional.empty());

        mvc.perform(get("/api/demandes-services/9/90")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/demandes-services ➔ 201 when both exist")
    void testCreateSuccess() throws Exception {
        DemandeServiceRequest req = new DemandeServiceRequest(5, 50, 3);

        DemandeServiceResponse created = DemandeServiceResponse.builder()
                .id(DemandeServiceKeyDto.builder().idDemande(5).idService(50).build())
                .quantite(3)
                .build();

        Mockito.when(service.create(Mockito.any(DemandeServiceRequest.class)))
                .thenReturn(created);

        mvc.perform(post("/api/demandes-services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/demandes-services/5/50"))
                .andExpect(jsonPath("$.id.idDemande").value(5))
                .andExpect(jsonPath("$.id.idService").value(50));
    }

    @Test
    @DisplayName("POST /api/demandes-services ➔ 400 when missing linked entity")
    void testCreateBadRequest() throws Exception {
        DemandeServiceRequest req = new DemandeServiceRequest(6, 60, 1);

        Mockito.when(service.create(Mockito.any(DemandeServiceRequest.class)))
                .thenThrow(new IllegalArgumentException("Demande ou Service introuvable"));

        mvc.perform(post("/api/demandes-services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/demandes-services/{demandeId}/{serviceId} ➔ 200 when exists")
    void testUpdateFound() throws Exception {
        DemandeServiceRequest req = new DemandeServiceRequest(null, null, 9);
        DemandeServiceResponse updated = DemandeServiceResponse.builder()
                .id(DemandeServiceKeyDto.builder().idDemande(7).idService(70).build())
                .quantite(9)
                .build();

        Mockito.when(service.update(7, 70, req))
                .thenReturn(Optional.of(updated));

        mvc.perform(put("/api/demandes-services/7/70")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantite").value(9));
    }

    @Test
    @DisplayName("PUT /api/demandes-services/{demandeId}/{serviceId} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        DemandeServiceRequest req = new DemandeServiceRequest(null, null, 2);

        Mockito.when(service.update(8, 80, req))
                .thenReturn(Optional.empty());

        mvc.perform(put("/api/demandes-services/8/80")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/demandes-services/{demandeId}/{serviceId} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(service.delete(11, 110)).thenReturn(true);

        mvc.perform(delete("/api/demandes-services/11/110"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/demandes-services/{demandeId}/{serviceId} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(service.delete(12, 120)).thenReturn(false);

        mvc.perform(delete("/api/demandes-services/12/120"))
                .andExpect(status().isNotFound());
    }
}
