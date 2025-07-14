package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
import com.jlh.jlhautopambackend.mapper.ClientMapper;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository repository;

    @Mock
    private ClientMapper mapper;

    @InjectMocks
    private ClientServiceImpl service;

    private ClientRequest request;
    private Client entity;
    private Client savedEntity;
    private ClientResponse response;

    @BeforeEach
    void setUp() {
        request = ClientRequest.builder()
                .nom("Doe")
                .prenom("John")
                .email("john.doe@example.com")
                .telephone("0123456789")
                .adresse("123 Main St")
                .build();

        entity = Client.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .adresse(request.getAdresse())
                .build();

        savedEntity = Client.builder()
                .idClient(1)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .adresse(request.getAdresse())
                .build();

        response = ClientResponse.builder()
                .idClient(savedEntity.getIdClient())
                .nom(savedEntity.getNom())
                .prenom(savedEntity.getPrenom())
                .email(savedEntity.getEmail())
                .telephone(savedEntity.getTelephone())
                .adresse(savedEntity.getAdresse())
                .build();
    }

    @Test
    void testCreate_ShouldSaveAndReturnResponse() {
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        ClientResponse result = service.create(request);

        assertEquals(response, result);
        verify(mapper).toEntity(request);
        verify(repository).save(entity);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testFindById_WhenFound() {
        when(repository.findById(1)).thenReturn(Optional.of(savedEntity));
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        Optional<ClientResponse> result = service.findById(1);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
        verify(repository).findById(1);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    void testFindById_WhenNotFound() {
        when(repository.findById(2)).thenReturn(Optional.empty());

        Optional<ClientResponse> result = service.findById(2);

        assertFalse(result.isPresent());
        verify(repository).findById(2);
        verifyNoInteractions(mapper);
    }

    @Test
    void testFindAll_ShouldReturnListOfResponses() {
        Client other = Client.builder()
                .idClient(2)
                .nom("Smith")
                .prenom("Jane")
                .email("jane.smith@example.com")
                .telephone("0987654321")
                .adresse("456 Elm St")
                .build();
        ClientResponse otherResp = ClientResponse.builder()
                .idClient(2)
                .nom("Smith")
                .prenom("Jane")
                .email("jane.smith@example.com")
                .telephone("0987654321")
                .adresse("456 Elm St")
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(savedEntity, other));
        when(mapper.toResponse(savedEntity)).thenReturn(response);
        when(mapper.toResponse(other)).thenReturn(otherResp);

        List<ClientResponse> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(response, results.get(0));
        assertEquals(otherResp, results.get(1));
        verify(repository).findAll();
        verify(mapper).toResponse(savedEntity);
        verify(mapper).toResponse(other);
    }

    @Test
    void testUpdate_WhenExists() {
        ClientRequest updateReq = ClientRequest.builder()
                .nom("Updated")
                .prenom("User")
                .email("updated.user@example.com")
                .telephone("0112233445")
                .adresse("789 Oak St")
                .build();

        Client existing = Client.builder()
                .idClient(3)
                .nom("Old")
                .prenom("Old")
                .email("old@example.com")
                .telephone("0000000000")
                .adresse("Old Addr")
                .build();

        Client updatedEntity = Client.builder()
                .idClient(3)
                .nom(updateReq.getNom())
                .prenom(updateReq.getPrenom())
                .email(updateReq.getEmail())
                .telephone(updateReq.getTelephone())
                .adresse(updateReq.getAdresse())
                .build();

        ClientResponse updatedResp = ClientResponse.builder()
                .idClient(3)
                .nom(updateReq.getNom())
                .prenom(updateReq.getPrenom())
                .email(updateReq.getEmail())
                .telephone(updateReq.getTelephone())
                .adresse(updateReq.getAdresse())
                .build();

        when(repository.findById(3)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(updatedEntity);
        when(mapper.toResponse(updatedEntity)).thenReturn(updatedResp);

        Optional<ClientResponse> result = service.update(3, updateReq);

        assertTrue(result.isPresent());
        assertEquals(updatedResp, result.get());
        verify(repository).findById(3);
        verify(repository).save(existing);
        verify(mapper).toResponse(updatedEntity);
    }

    @Test
    void testUpdate_WhenNotExists() {
        when(repository.findById(4)).thenReturn(Optional.empty());

        Optional<ClientResponse> result = service.update(4, request);

        assertFalse(result.isPresent());
        verify(repository).findById(4);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    void testDelete_WhenExists() {
        when(repository.existsById(5)).thenReturn(true);

        boolean result = service.delete(5);

        assertTrue(result);
        verify(repository).existsById(5);
        verify(repository).deleteById(5);
    }

    @Test
    void testDelete_WhenNotExists() {
        when(repository.existsById(6)).thenReturn(false);

        boolean result = service.delete(6);

        assertFalse(result);
        verify(repository).existsById(6);
        verify(repository, never()).deleteById(anyInt());
    }
}
