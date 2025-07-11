package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repositories.ClientRepository;
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
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ClientController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClientRepository repo;

    @Test
    @DisplayName("GET /api/clients ➔ 200, JSON list non vide")
    void testGetAllNonEmpty() throws Exception {
        Client c1 = Client.builder()
                .idClient(1)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .telephone("0123456789")
                .adresse("1 rue A")
                .build();
        Client c2 = Client.builder()
                .idClient(2)
                .nom("Martin")
                .prenom("Marie")
                .email("marie.martin@example.com")
                .telephone("0987654321")
                .adresse("2 avenue B")
                .build();

        Mockito.when(repo.findAll()).thenReturn(Arrays.asList(c1, c2));

        mvc.perform(get("/api/clients")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].idClient").value(1))
                .andExpect(jsonPath("$[1].prenom").value("Marie"));
    }

    @Test
    @DisplayName("GET /api/clients ➔ 200, JSON liste vide")
    void testGetAllEmpty() throws Exception {
        Mockito.when(repo.findAll()).thenReturn(Collections.emptyList());

        mvc.perform(get("/api/clients")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/clients/{id} ➔ 200, JSON")
    void testGetByIdFound() throws Exception {
        Client c = Client.builder()
                .idClient(5)
                .nom("Durand")
                .prenom("Luc")
                .email("luc.durand@example.com")
                .telephone("0112233445")
                .adresse("3 chemin C")
                .build();
        Mockito.when(repo.findById(5)).thenReturn(Optional.of(c));

        mvc.perform(get("/api/clients/5")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idClient").value(5))
                .andExpect(jsonPath("$.email").value("luc.durand@example.com"));
    }

    @Test
    @DisplayName("GET /api/clients/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(repo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/clients/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/clients ➔ 201, retourne JSON")
    void testCreate() throws Exception {
        Client in = Client.builder()
                .nom("Petit")
                .prenom("Anne")
                .email("anne.petit@example.com")
                .telephone("0223344556")
                .adresse("4 boulevard D")
                .build();
        Client saved = Client.builder()
                .idClient(10)
                .nom(in.getNom())
                .prenom(in.getPrenom())
                .email(in.getEmail())
                .telephone(in.getTelephone())
                .adresse(in.getAdresse())
                .build();

        Mockito.when(repo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idClient").value(10))
                .andExpect(jsonPath("$.nom").value("Petit"))
                .andExpect(jsonPath("$.adresse").value("4 boulevard D"));
    }

    @Test
    @DisplayName("PUT /api/clients/{id} ➔ 200, retourne JSON mis à jour")
    void testUpdateFound() throws Exception {
        Client existing = Client.builder()
                .idClient(7)
                .nom("OldNom")
                .prenom("OldPrenom")
                .email("old@example.com")
                .telephone("0000000000")
                .adresse("OldAdresse")
                .build();
        Client updates = Client.builder()
                .nom("NewNom")
                .prenom("NewPrenom")
                .email("new@example.com")
                .telephone("1111111111")
                .adresse("NewAdresse")
                .build();
        Client saved = Client.builder()
                .idClient(7)
                .nom("NewNom")
                .prenom("NewPrenom")
                .email("new@example.com")
                .telephone("1111111111")
                .adresse("NewAdresse")
                .build();

        Mockito.when(repo.findById(7)).thenReturn(Optional.of(existing));
        Mockito.when(repo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(put("/api/clients/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idClient").value(7))
                .andExpect(jsonPath("$.nom").value("NewNom"))
                .andExpect(jsonPath("$.adresse").value("NewAdresse"))
                .andExpect(jsonPath("$.telephone").value("1111111111"));
    }

    @Test
    @DisplayName("PUT /api/clients/{id} ➔ 404")
    void testUpdateNotFound() throws Exception {
        Client updates = Client.builder()
                .nom("X")
                .prenom("Y")
                .email("x@y.com")
                .telephone("123")
                .adresse("Z")
                .build();
        Mockito.when(repo.findById(42)).thenReturn(Optional.empty());

        mvc.perform(put("/api/clients/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/clients/{id} ➔ 204, sans contenu")
    void testDeleteFound() throws Exception {
        Client c = Client.builder().idClient(8).build();
        Mockito.when(repo.findById(8)).thenReturn(Optional.of(c));

        mvc.perform(delete("/api/clients/8"))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("DELETE /api/clients/{id} ➔ 404")
    void testDeleteNotFound() throws Exception {
        Mockito.when(repo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(delete("/api/clients/99"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
