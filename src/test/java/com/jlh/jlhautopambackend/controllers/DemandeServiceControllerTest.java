package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.DemandeServiceKey;
import com.jlh.jlhautopambackend.modeles.Service;
import com.jlh.jlhautopambackend.repositories.DemandeRepository;
import com.jlh.jlhautopambackend.repositories.DemandeServiceRepository;
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

import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    private DemandeServiceRepository dsRepo;

    @MockitoBean
    private DemandeRepository demandeRepo;

    @MockitoBean
    private ServiceRepository serviceRepo;

    @Test
    @DisplayName("GET /api/demandes-services ➔ 200, json list")
    void testGetAll() throws Exception {
        DemandeService ds1 = DemandeService.builder()
                .id(new DemandeServiceKey(1, 10))
                .quantite(2)
                .build();
        DemandeService ds2 = DemandeService.builder()
                .id(new DemandeServiceKey(2, 20))
                .quantite(5)
                .build();

        Mockito.when(dsRepo.findAll()).thenReturn(Arrays.asList(ds1, ds2));

        mvc.perform(get("/api/demandes-services").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id.idDemande").value(1))
                .andExpect(jsonPath("$[1].id.idService").value(20));
    }

    @Test
    @DisplayName("GET /api/demandes-services/{demandeId}/{serviceId} ➔ 200")
    void testGetByIdFound() throws Exception {
        DemandeServiceKey key = new DemandeServiceKey(3, 30);
        DemandeService ds = DemandeService.builder()
                .id(key)
                .quantite(7)
                .build();
        Mockito.when(dsRepo.findById(key)).thenReturn(Optional.of(ds));

        mvc.perform(get("/api/demandes-services/3/30").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantite").value(7));
    }

    @Test
    @DisplayName("GET /api/demandes-services/{demandeId}/{serviceId} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        DemandeServiceKey key = new DemandeServiceKey(9, 90);
        Mockito.when(dsRepo.findById(key)).thenReturn(Optional.empty());

        mvc.perform(get("/api/demandes-services/9/90").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/demandes-services ➔ 201 when both exist")
    void testCreateSuccess() throws Exception {
        int demandeId = 5, serviceId = 50;
        DemandeService input = DemandeService.builder()
                .demande(Demande.builder().idDemande(demandeId).build())
                .service(Service.builder().idService(serviceId).build())
                .quantite(3)
                .build();

        Demande demande = Demande.builder().idDemande(demandeId).build();
        Service service = Service.builder().idService(serviceId).build();
        DemandeService saved = DemandeService.builder()
                .id(new DemandeServiceKey(demandeId, serviceId))
                .demande(demande)
                .service(service)
                .quantite(3)
                .build();

        Mockito.when(demandeRepo.findById(demandeId)).thenReturn(Optional.of(demande));
        Mockito.when(serviceRepo.findById(serviceId)).thenReturn(Optional.of(service));
        Mockito.when(dsRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/demandes-services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/demandes-services/5/50"))
                .andExpect(jsonPath("$.id.idDemande").value(5))
                .andExpect(jsonPath("$.id.idService").value(50));
    }

    @Test
    @DisplayName("POST /api/demandes-services ➔ 400 when missing linked entity")
    void testCreateBadRequest() throws Exception {
        int demandeId = 6, serviceId = 60;
        DemandeService input = DemandeService.builder()
                .demande(Demande.builder().idDemande(demandeId).build())
                .service(Service.builder().idService(serviceId).build())
                .quantite(1)
                .build();

        Mockito.when(demandeRepo.findById(demandeId)).thenReturn(Optional.empty());

        mvc.perform(post("/api/demandes-services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/demandes-services/{demandeId}/{serviceId} ➔ 200 when exists")
    void testUpdateFound() throws Exception {
        DemandeServiceKey key = new DemandeServiceKey(7, 70);
        DemandeService existing = DemandeService.builder()
                .id(key)
                .quantite(4)
                .build();
        DemandeService dto = DemandeService.builder()
                .quantite(9)
                .build();
        DemandeService updated = DemandeService.builder()
                .id(key)
                .quantite(9)
                .build();

        Mockito.when(dsRepo.findById(key)).thenReturn(Optional.of(existing));
        Mockito.when(dsRepo.save(Mockito.any())).thenReturn(updated);

        mvc.perform(put("/api/demandes-services/7/70")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantite").value(9));
    }

    @Test
    @DisplayName("PUT /api/demandes-services/{demandeId}/{serviceId} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        DemandeServiceKey key = new DemandeServiceKey(8, 80);
        DemandeService dto = DemandeService.builder()
                .quantite(2)
                .build();
        Mockito.when(dsRepo.findById(key)).thenReturn(Optional.empty());

        mvc.perform(put("/api/demandes-services/8/80")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/demandes-services/{demandeId}/{serviceId} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        DemandeServiceKey key = new DemandeServiceKey(11, 110);
        Mockito.when(dsRepo.existsById(key)).thenReturn(true);

        mvc.perform(delete("/api/demandes-services/11/110"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/demandes-services/{demandeId}/{serviceId} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        DemandeServiceKey key = new DemandeServiceKey(12, 120);
        Mockito.when(dsRepo.existsById(key)).thenReturn(false);

        mvc.perform(delete("/api/demandes-services/12/120"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
