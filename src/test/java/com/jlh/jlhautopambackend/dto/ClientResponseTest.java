package com.jlh.jlhautopambackend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientResponseTest {

    @Test
    @DisplayName("Builder should set all fields and getters should retrieve them")
    void builderAndGetters() {
        ClientResponse resp = ClientResponse.builder()
                .idClient(42)
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@example.com")
                .telephone("0123456789")
                .adresse("1 rue A")
                .build();

        assertEquals(42, resp.getIdClient());
        assertEquals("Dupont", resp.getNom());
        assertEquals("Jean", resp.getPrenom());
        assertEquals("jean.dupont@example.com", resp.getEmail());
        assertEquals("0123456789", resp.getTelephone());
        assertEquals("1 rue A", resp.getAdresse());
    }

    @Test
    @DisplayName("Setters should modify fields and getters should retrieve updated values")
    void settersAndGetters() {
        ClientResponse resp = new ClientResponse();
        resp.setIdClient(99);
        resp.setNom("Martin");
        resp.setPrenom("Marie");
        resp.setEmail("marie.martin@example.com");
        resp.setTelephone("0987654321");
        resp.setAdresse("2 avenue B");

        assertEquals(99, resp.getIdClient());
        assertEquals("Martin", resp.getNom());
        assertEquals("Marie", resp.getPrenom());
        assertEquals("marie.martin@example.com", resp.getEmail());
        assertEquals("0987654321", resp.getTelephone());
        assertEquals("2 avenue B", resp.getAdresse());
    }

    @Test
    @DisplayName("equals and hashCode should be consistent")
    void equalsAndHashCode() {
        ClientResponse a = ClientResponse.builder()
                .idClient(1).nom("A").prenom("B")
                .email("a@b.com").telephone("111").adresse("Z").build();
        ClientResponse b = ClientResponse.builder()
                .idClient(1).nom("A").prenom("B")
                .email("a@b.com").telephone("111").adresse("Z").build();
        ClientResponse c = ClientResponse.builder()
                .idClient(2).nom("A").prenom("B")
                .email("a@b.com").telephone("111").adresse("Z").build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("toString should contain all fields")
    void toStringContainsAllFields() {
        ClientResponse resp = ClientResponse.builder()
                .idClient(7).nom("T").prenom("U")
                .email("t@u.com").telephone("000").adresse("Addr")
                .build();
        String s = resp.toString();
        assertTrue(s.contains("idClient=7"));
        assertTrue(s.contains("nom=T"));
        assertTrue(s.contains("prenom=U"));
        assertTrue(s.contains("email=t@u.com"));
        assertTrue(s.contains("telephone=000"));
        assertTrue(s.contains("adresse=Addr"));
    }
}
