package com.jlh.jlhautopambackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.repositories.*;
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
        controllers = RendezVousController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class RendezVousControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RendezVousRepository rvRepo;

    @MockitoBean
    private DemandeRepository demandeRepo;

    @MockitoBean
    private AdministrateurRepository adminRepo;

    @MockitoBean
    private CreneauRepository creneauRepo;

    @MockitoBean
    private StatutRendezVousRepository statutRepo;

    @Test
    @DisplayName("GET /api/rendezvous ➔ 200, json list")
    void testGetAll() throws Exception {
        Demande d = Demande.builder().idDemande(1).build();
        Administrateur a = Administrateur.builder().idAdmin(2).username("u").motDePasse("p").build();
        Creneau c = Creneau.builder().idCreneau(3).build();
        StatutRendezVous s = StatutRendezVous.builder().codeStatut("S").libelle("lib").build();

        RendezVous r1 = RendezVous.builder()
                .idRdv(10)
                .demande(d)
                .administrateur(a)
                .creneau(c)
                .statut(s)
                .build();
        RendezVous r2 = RendezVous.builder()
                .idRdv(20)
                .demande(d)
                .administrateur(a)
                .creneau(c)
                .statut(s)
                .build();

        Mockito.when(rvRepo.findAll()).thenReturn(Arrays.asList(r1, r2));

        mvc.perform(get("/api/rendezvous").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idRdv").value(10))
                .andExpect(jsonPath("$[1].idRdv").value(20));
    }

    @Test
    @DisplayName("GET /api/rendezvous/{id} ➔ 200")
    void testGetByIdFound() throws Exception {
        RendezVous r = RendezVous.builder().idRdv(5).build();
        Mockito.when(rvRepo.findById(5)).thenReturn(Optional.of(r));

        mvc.perform(get("/api/rendezvous/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idRdv").value(5));
    }

    @Test
    @DisplayName("GET /api/rendezvous/{id} ➔ 404")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(rvRepo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(get("/api/rendezvous/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/rendezvous ➔ 201 when all links valid")
    void testCreateSuccess() throws Exception {
        Demande d = Demande.builder().idDemande(1).build();
        Administrateur a = Administrateur.builder().idAdmin(2).build();
        Creneau c = Creneau.builder().idCreneau(3).build();
        StatutRendezVous s = StatutRendezVous.builder().codeStatut("OK").build();

        RendezVous input = RendezVous.builder()
                .demande(d).administrateur(a).creneau(c).statut(s).build();
        RendezVous saved = RendezVous.builder()
                .idRdv(100)
                .demande(d).administrateur(a).creneau(c).statut(s)
                .build();

        Mockito.when(demandeRepo.findById(1)).thenReturn(Optional.of(d));
        Mockito.when(adminRepo.findById(2)).thenReturn(Optional.of(a));
        Mockito.when(creneauRepo.findById(3)).thenReturn(Optional.of(c));
        Mockito.when(statutRepo.findById("OK")).thenReturn(Optional.of(s));
        Mockito.when(rvRepo.existsByDemandeIdDemande(1)).thenReturn(false);
        Mockito.when(rvRepo.existsByCreneauIdCreneau(3)).thenReturn(false);
        Mockito.when(rvRepo.save(Mockito.any())).thenReturn(saved);

        mvc.perform(post("/api/rendezvous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/rendezvous/100"))
                .andExpect(jsonPath("$.idRdv").value(100));
    }

    @Test
    @DisplayName("POST /api/rendezvous ➔ 400 when a linked entity missing")
    void testCreateBadRequest() throws Exception {
        RendezVous input = RendezVous.builder()
                .demande(Demande.builder().idDemande(1).build())
                .administrateur(Administrateur.builder().idAdmin(2).build())
                .creneau(Creneau.builder().idCreneau(3).build())
                .statut(StatutRendezVous.builder().codeStatut("X").build())
                .build();
        Mockito.when(demandeRepo.findById(1)).thenReturn(Optional.empty());

        mvc.perform(post("/api/rendezvous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/rendezvous ➔ 409 on uniqueness conflict")
    void testCreateConflict() throws Exception {
        Demande d = Demande.builder().idDemande(1).build();
        Administrateur a = Administrateur.builder().idAdmin(2).build();
        Creneau c = Creneau.builder().idCreneau(3).build();
        StatutRendezVous s = StatutRendezVous.builder().codeStatut("OK").build();

        RendezVous input = RendezVous.builder()
                .demande(d).administrateur(a).creneau(c).statut(s).build();

        Mockito.when(demandeRepo.findById(1)).thenReturn(Optional.of(d));
        Mockito.when(adminRepo.findById(2)).thenReturn(Optional.of(a));
        Mockito.when(creneauRepo.findById(3)).thenReturn(Optional.of(c));
        Mockito.when(statutRepo.findById("OK")).thenReturn(Optional.of(s));
        Mockito.when(rvRepo.existsByDemandeIdDemande(1)).thenReturn(true);

        mvc.perform(post("/api/rendezvous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/rendezvous/{id} ➔ 200 when update valid")
    void testUpdateSuccess() throws Exception {
        Demande oldD = Demande.builder().idDemande(1).build();
        Administrateur oldA = Administrateur.builder().idAdmin(2).build();
        Creneau oldC = Creneau.builder().idCreneau(3).build();
        StatutRendezVous oldS = StatutRendezVous.builder().codeStatut("OLD").build();
        RendezVous existing = RendezVous.builder()
                .idRdv(50).demande(oldD).administrateur(oldA).creneau(oldC).statut(oldS).build();

        Demande newD = Demande.builder().idDemande(10).build();
        Administrateur newA = Administrateur.builder().idAdmin(20).build();
        Creneau newC = Creneau.builder().idCreneau(30).build();
        StatutRendezVous newS = StatutRendezVous.builder().codeStatut("NEW").build();
        RendezVous dto = RendezVous.builder()
                .demande(newD).administrateur(newA).creneau(newC).statut(newS).build();
        RendezVous updated = RendezVous.builder()
                .idRdv(50).demande(newD).administrateur(newA).creneau(newC).statut(newS).build();

        Mockito.when(rvRepo.findById(50)).thenReturn(Optional.of(existing));
        Mockito.when(demandeRepo.findById(10)).thenReturn(Optional.of(newD));
        Mockito.when(rvRepo.existsByDemandeIdDemande(10)).thenReturn(false);
        Mockito.when(adminRepo.findById(20)).thenReturn(Optional.of(newA));
        Mockito.when(creneauRepo.findById(30)).thenReturn(Optional.of(newC));
        Mockito.when(rvRepo.existsByCreneauIdCreneau(30)).thenReturn(false);
        Mockito.when(statutRepo.findById("NEW")).thenReturn(Optional.of(newS));
        Mockito.when(rvRepo.save(Mockito.any())).thenReturn(updated);

        mvc.perform(put("/api/rendezvous/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demande.idDemande").value(10))
                .andExpect(jsonPath("$.statut.codeStatut").value("NEW"));
    }

    @Test
    @DisplayName("PUT /api/rendezvous/{id} ➔ 400 on bad request (new link missing or conflict)")
    void testUpdateBadRequest() throws Exception {
        RendezVous dto = RendezVous.builder()
                .demande(Demande.builder().idDemande(99).build())
                .administrateur(Administrateur.builder().idAdmin(2).build())
                .creneau(Creneau.builder().idCreneau(3).build())
                .statut(StatutRendezVous.builder().codeStatut("OK").build())
                .build();
        Mockito.when(rvRepo.findById(60)).thenReturn(Optional.of(
                RendezVous.builder().idRdv(60)
                        .demande(Demande.builder().idDemande(1).build())
                        .administrateur(Administrateur.builder().idAdmin(2).build())
                        .creneau(Creneau.builder().idCreneau(3).build())
                        .statut(StatutRendezVous.builder().codeStatut("OK").build())
                        .build()));
        Mockito.when(demandeRepo.findById(99)).thenReturn(Optional.empty());

        mvc.perform(put("/api/rendezvous/60")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/rendezvous/{id} ➔ 404 when not found")
    void testUpdateNotFound() throws Exception {
        Mockito.when(rvRepo.findById(999)).thenReturn(Optional.empty());

        mvc.perform(put("/api/rendezvous/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/rendezvous/{id} ➔ 204 when exists")
    void testDeleteFound() throws Exception {
        Mockito.when(rvRepo.existsById(7)).thenReturn(true);

        mvc.perform(delete("/api/rendezvous/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/rendezvous/{id} ➔ 404 when not found")
    void testDeleteNotFound() throws Exception {
        Mockito.when(rvRepo.existsById(8)).thenReturn(false);

        mvc.perform(delete("/api/rendezvous/8"))
                .andExpect(status().isNotFound());
    }
}
