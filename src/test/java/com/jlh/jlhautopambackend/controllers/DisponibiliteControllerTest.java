package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Creneau;
import com.jlh.jlhautopambackend.modeles.Disponibilite;
import com.jlh.jlhautopambackend.modeles.DisponibiliteKey;
import com.jlh.jlhautopambackend.repositories.AdministrateurRepository;
import com.jlh.jlhautopambackend.repositories.CreneauRepository;
import com.jlh.jlhautopambackend.repositories.DisponibiliteRepository;
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
        controllers = DisponibiliteController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class DisponibiliteControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DisponibiliteRepository dispoRepo;

    @MockitoBean
    private AdministrateurRepository adminRepo;

    @MockitoBean
    private CreneauRepository creneauRepo;

    @Test
    @DisplayName("GET /api/disponibilites ➔ 200, json list")
    void testGetAll() throws Exception {
        DisponibiliteKey key1 = new DisponibiliteKey(1, 100);
        DisponibiliteKey key2 = new DisponibiliteKey(2, 200);
        Disponibilite d1 = Disponibilite.builder().id(key1).build();
        Disponibilite d2 = Disponibilite.builder().id(key2).build();

        Mockito.when(dispoRepo.findAll()).thenReturn(Arrays.asList(d1, d2));

        mvc.perform(get("/api/disponibilites").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id.idAdmin").value(1))
                .andExpect(jsonPath("$[1].id.idCreneau").value(200));
    }

    @Test
    @DisplayName("GET /api/disponibilites/{adminId}/{creneauId} ➔ 200")
    void testGetByIdFound() throws Exception {
        DisponibiliteKey key = new DisponibiliteKey(3, 300);
        Disponibilite d = Disponibilite.builder().id(key).build();

        Mockito.when(dispoRepo.findById(key)).thenReturn(Optional.of(d));

        mvc.perform(get("/api/disponibilites/3/300").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.idAdmin").value(3))
                .andExpect(jsonPath("$.id.idCreneau").value(300));
    }

    @Test
    @DisplayName("GET /api/disponibilites/{adminId}/{creneauId} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        DisponibiliteKey key = new DisponibiliteKey(9, 900);
        Mockito.when(dispoRepo.findById(key)).thenReturn(Optional.empty());

        mvc.perform(get("/api/disponibilites/9/900").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/disponibilites ➔ 201 when both exist")
    void testCreateSuccess() throws Exception {
        int adminId = 4, creneauId = 400;
        Administrateur admin = Administrateur.builder().idAdmin(adminId).build();
        Creneau creneau = Creneau.builder().idCreneau(creneauId).build();
        DisponibiliteKey key = new DisponibiliteKey(adminId, creneauId);
        Disponibilite saved = Disponibilite.builder()
                .id(key)
                .administrateur(admin)
                .creneau(creneau)
                .build();

        Disponibilite input = Disponibilite.builder()
                .administrateur(Administrateur.builder().idAdmin(adminId).build())
                .creneau(Creneau.builder().idCreneau(creneauId).build())
                .build();

        Mockito.when(adminRepo.findById(adminId)).thenReturn(Optional.of(admin));
        Mockito.when(creneauRepo.findById(creneauId)).thenReturn(Optional.of(creneau));
        Mockito.when(dispoRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/disponibilites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/disponibilites/4/400"))
                .andExpect(jsonPath("$.id.idAdmin").value(4))
                .andExpect(jsonPath("$.id.idCreneau").value(400));
    }

    @Test
    @DisplayName("POST /api/disponibilites ➔ 400 when admin missing")
    void testCreateBadRequestAdminMissing() throws Exception {
        int adminId = 5, creneauId = 500;
        Disponibilite input = Disponibilite.builder()
                .administrateur(Administrateur.builder().idAdmin(adminId).build())
                .creneau(Creneau.builder().idCreneau(creneauId).build())
                .build();

        Mockito.when(adminRepo.findById(adminId)).thenReturn(Optional.empty());

        mvc.perform(post("/api/disponibilites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/disponibilites ➔ 400 when creneau missing")
    void testCreateBadRequestCreneauMissing() throws Exception {
        int adminId = 6, creneauId = 600;
        Administrateur admin = Administrateur.builder().idAdmin(adminId).build();
        Disponibilite input = Disponibilite.builder()
                .administrateur(admin)
                .creneau(Creneau.builder().idCreneau(creneauId).build())
                .build();

        Mockito.when(adminRepo.findById(adminId)).thenReturn(Optional.of(admin));
        Mockito.when(creneauRepo.findById(creneauId)).thenReturn(Optional.empty());

        mvc.perform(post("/api/disponibilites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/disponibilites/{adminId}/{creneauId} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        DisponibiliteKey key = new DisponibiliteKey(7, 700);
        Mockito.when(dispoRepo.existsById(key)).thenReturn(true);

        mvc.perform(delete("/api/disponibilites/7/700"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/disponibilites/{adminId}/{creneauId} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        DisponibiliteKey key = new DisponibiliteKey(8, 800);
        Mockito.when(dispoRepo.existsById(key)).thenReturn(false);

        mvc.perform(delete("/api/disponibilites/8/800"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
