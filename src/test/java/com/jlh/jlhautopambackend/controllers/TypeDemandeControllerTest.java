package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.TypeDemande;
import com.jlh.jlhautopambackend.repositories.TypeDemandeRepository;
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
        controllers = TypeDemandeController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class TypeDemandeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TypeDemandeRepository typeRepo;

    @Test
    @DisplayName("GET /api/types-demande ➔ 200, liste non vide")
    void testGetAll() throws Exception {
        TypeDemande t1 = TypeDemande.builder().codeType("A").libelle("Alpha").build();
        TypeDemande t2 = TypeDemande.builder().codeType("B").libelle("Beta").build();
        Mockito.when(typeRepo.findAll()).thenReturn(Arrays.asList(t1, t2));

        mvc.perform(get("/api/types-demande").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codeType").value("A"))
                .andExpect(jsonPath("$[1].libelle").value("Beta"));
    }

    @Test
    @DisplayName("GET /api/types-demande/{code} ➔ 200 si existant")
    void testGetByCodeFound() throws Exception {
        TypeDemande t = TypeDemande.builder().codeType("X").libelle("Xray").build();
        Mockito.when(typeRepo.findById("X")).thenReturn(Optional.of(t));

        mvc.perform(get("/api/types-demande/X").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("Xray"));
    }

    @Test
    @DisplayName("GET /api/types-demande/{code} ➔ 404 si absent")
    void testGetByCodeNotFound() throws Exception {
        Mockito.when(typeRepo.findById("Z")).thenReturn(Optional.empty());

        mvc.perform(get("/api/types-demande/Z").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/types-demande ➔ 201 quand nouveau")
    void testCreateSuccess() throws Exception {
        TypeDemande in = TypeDemande.builder().codeType("C").libelle("Charlie").build();
        TypeDemande saved = TypeDemande.builder().codeType("C").libelle("Charlie").build();
        Mockito.when(typeRepo.existsById("C")).thenReturn(false);
        Mockito.when(typeRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/types-demande")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/types-demande/C"))
                .andExpect(jsonPath("$.codeType").value("C"));
    }

    @Test
    @DisplayName("POST /api/types-demande ➔ 409 si déjà existant")
    void testCreateConflict() throws Exception {
        TypeDemande in = TypeDemande.builder().codeType("D").libelle("Delta").build();
        Mockito.when(typeRepo.existsById("D")).thenReturn(true);

        mvc.perform(post("/api/types-demande")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/types-demande/{code} ➔ 200 si existant")
    void testUpdateFound() throws Exception {
        TypeDemande existing = TypeDemande.builder().codeType("E").libelle("Echo").build();
        TypeDemande updates  = TypeDemande.builder().libelle("EchoUpdated").build();
        TypeDemande saved    = TypeDemande.builder().codeType("E").libelle("EchoUpdated").build();
        Mockito.when(typeRepo.findById("E")).thenReturn(Optional.of(existing));
        Mockito.when(typeRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(put("/api/types-demande/E")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("EchoUpdated"));
    }

    @Test
    @DisplayName("PUT /api/types-demande/{code} ➔ 404 si absent")
    void testUpdateNotFound() throws Exception {
        TypeDemande updates = TypeDemande.builder().libelle("Nope").build();
        Mockito.when(typeRepo.findById("F")).thenReturn(Optional.empty());

        mvc.perform(put("/api/types-demande/F")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/types-demande/{code} ➔ 204 si existant")
    void testDeleteFound() throws Exception {
        Mockito.when(typeRepo.existsById("G")).thenReturn(true);

        mvc.perform(delete("/api/types-demande/G"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/types-demande/{code} ➔ 404 si absent")
    void testDeleteNotFound() throws Exception {
        Mockito.when(typeRepo.existsById("H")).thenReturn(false);

        mvc.perform(delete("/api/types-demande/H"))
                .andExpect(status().isNotFound());
    }
}
