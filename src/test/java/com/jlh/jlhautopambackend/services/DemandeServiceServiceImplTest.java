package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DemandeServiceKeyDto;
import com.jlh.jlhautopambackend.dto.DemandeServiceRequest;
import com.jlh.jlhautopambackend.dto.DemandeServiceResponse;
import com.jlh.jlhautopambackend.mapper.DemandeServiceMapper;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.DemandeServiceKey;
import com.jlh.jlhautopambackend.modeles.Service;
import com.jlh.jlhautopambackend.repositories.DemandeRepository;
import com.jlh.jlhautopambackend.repositories.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repositories.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemandeServiceServiceImplTest {

    @Mock
    private DemandeServiceRepository dsRepo;
    @Mock
    private DemandeRepository demandeRepo;
    @Mock
    private ServiceRepository serviceRepo;
    @Mock
    private DemandeServiceMapper mapper;

    @InjectMocks
    private DemandeServiceServiceImpl service;

    private DemandeServiceRequest request;
    private Demande demande;
    private Service serviceEntity;
    private DemandeService entityWithoutRel;
    private DemandeService savedEntity;
    private DemandeServiceResponse response;

    @BeforeEach
    void setUp() {
        request = DemandeServiceRequest.builder()
                .demandeId(1)
                .serviceId(2)
                .quantite(5)
                .build();

        demande = Demande.builder()
                .idDemande(1)
                .build();

        serviceEntity = Service.builder()
                .idService(2)
                .libelle("TestService")
                .description("Desc")
                .prixUnitaire(new BigDecimal("100.00"))
                .build();

        entityWithoutRel = DemandeService.builder()
                .quantite(request.getQuantite())
                .build();

        savedEntity = DemandeService.builder()
                .id(new DemandeServiceKey(1, 2))
                .demande(demande)
                .service(serviceEntity)
                .quantite(request.getQuantite())
                .build();

        response = DemandeServiceResponse.builder()
                .id(new DemandeServiceKeyDto(1, 2))
                .quantite(request.getQuantite())
                .build();
    }

    @Test
    void testFindAll_ShouldReturnListOfResponses() {
        DemandeService otherEntity = DemandeService.builder()
                .id(new DemandeServiceKey(1, 3))
                .demande(demande)
                .service(serviceEntity)
                .quantite(2)
                .build();
        DemandeServiceResponse otherResp = DemandeServiceResponse.builder()
                .id(new DemandeServiceKeyDto(1, 3))
                .quantite(2)
                .build();

        when(dsRepo.findAll()).thenReturn(Arrays.asList(savedEntity, otherEntity));
        when(mapper.toDto(savedEntity)).thenReturn(response);
        when(mapper.toDto(otherEntity)).thenReturn(otherResp);

        List<DemandeServiceResponse> results = service.findAll();

        assertEquals(2, results.size());
        assertEquals(response, results.get(0));
        assertEquals(otherResp, results.get(1));
        verify(dsRepo).findAll();
        verify(mapper).toDto(savedEntity);
        verify(mapper).toDto(otherEntity);
    }

    @Test
    void testFindByKey_WhenFound() {
        DemandeServiceKey key = new DemandeServiceKey(1, 2);
        when(dsRepo.findById(key)).thenReturn(Optional.of(savedEntity));
        when(mapper.toDto(savedEntity)).thenReturn(response);

        Optional<DemandeServiceResponse> result = service.findByKey(1, 2);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
        verify(dsRepo).findById(key);
        verify(mapper).toDto(savedEntity);
    }

    @Test
    void testFindByKey_WhenNotFound() {
        DemandeServiceKey key = new DemandeServiceKey(1, 2);
        when(dsRepo.findById(key)).thenReturn(Optional.empty());

        Optional<DemandeServiceResponse> result = service.findByKey(1, 2);

        assertFalse(result.isPresent());
        verify(dsRepo).findById(key);
        verifyNoInteractions(mapper);
    }

    @Test
    void testCreate_ShouldSetRelationsAndReturnResponse() {
        when(mapper.toEntity(request)).thenReturn(entityWithoutRel);
        when(demandeRepo.findById(1)).thenReturn(Optional.of(demande));
        when(serviceRepo.findById(2)).thenReturn(Optional.of(serviceEntity));
        when(dsRepo.save(entityWithoutRel)).thenReturn(savedEntity);
        when(mapper.toDto(savedEntity)).thenReturn(response);

        DemandeServiceResponse result = service.create(request);

        assertEquals(response, result);
        ArgumentCaptor<DemandeService> captor = ArgumentCaptor.forClass(DemandeService.class);
        verify(mapper).toEntity(request);
        verify(demandeRepo).findById(1);
        verify(serviceRepo).findById(2);
        verify(dsRepo).save(captor.capture());
        DemandeService passed = captor.getValue();
        assertEquals(demande, passed.getDemande());
        assertEquals(serviceEntity, passed.getService());
        assertEquals(request.getQuantite(), passed.getQuantite());
        verify(mapper).toDto(savedEntity);
    }

    @Test
    void testCreate_ShouldThrowWhenDemandeNotFound() {
        when(demandeRepo.findById(1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(request));
        assertEquals("Demande introuvable", ex.getMessage());
        verify(demandeRepo).findById(1);
        verifyNoMoreInteractions(serviceRepo, dsRepo, mapper);
    }

    @Test
    void testCreate_ShouldThrowWhenServiceNotFound() {
        when(demandeRepo.findById(1)).thenReturn(Optional.of(demande));
        when(serviceRepo.findById(2)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(request));
        assertEquals("Service introuvable", ex.getMessage());
        verify(demandeRepo).findById(1);
        verify(serviceRepo).findById(2);
        verifyNoMoreInteractions(dsRepo, mapper);
    }

    @Test
    void testUpdate_WhenExists() {
        DemandeServiceKey key = new DemandeServiceKey(1, 2);
        DemandeService existing = DemandeService.builder()
                .id(key)
                .quantite(10)
                .build();
        DemandeService updatedEntity = DemandeService.builder()
                .id(key)
                .quantite(8)
                .build();
        DemandeServiceResponse updatedResp = DemandeServiceResponse.builder()
                .id(new DemandeServiceKeyDto(1, 2))
                .quantite(8)
                .build();
        DemandeServiceRequest updateReq = DemandeServiceRequest.builder()
                .demandeId(1)
                .serviceId(2)
                .quantite(8)
                .build();

        when(dsRepo.findById(key)).thenReturn(Optional.of(existing));
        when(dsRepo.save(existing)).thenReturn(updatedEntity);
        when(mapper.toDto(updatedEntity)).thenReturn(updatedResp);

        Optional<DemandeServiceResponse> result = service.update(1, 2, updateReq);

        assertTrue(result.isPresent());
        assertEquals(updatedResp, result.get());
        verify(dsRepo).findById(key);
        verify(dsRepo).save(existing);
        verify(mapper).toDto(updatedEntity);
    }

    @Test
    void testUpdate_WhenNotExists() {
        DemandeServiceKey key = new DemandeServiceKey(1, 2);
        DemandeServiceRequest updateReq = DemandeServiceRequest.builder()
                .demandeId(1)
                .serviceId(2)
                .quantite(8)
                .build();
        when(dsRepo.findById(key)).thenReturn(Optional.empty());

        Optional<DemandeServiceResponse> result = service.update(1, 2, updateReq);

        assertFalse(result.isPresent());
        verify(dsRepo).findById(key);
        verifyNoMoreInteractions(dsRepo, mapper);
    }

    @Test
    void testDelete_WhenExists() {
        DemandeServiceKey key = new DemandeServiceKey(1, 2);
        when(dsRepo.existsById(key)).thenReturn(true);

        boolean result = service.delete(1, 2);

        assertTrue(result);
        verify(dsRepo).existsById(key);
        verify(dsRepo).deleteById(key);
    }

    @Test
    void testDelete_WhenNotExists() {
        DemandeServiceKey key = new DemandeServiceKey(1, 2);
        when(dsRepo.existsById(key)).thenReturn(false);

        boolean result = service.delete(1, 2);

        assertFalse(result);
        verify(dsRepo).existsById(key);
        verify(dsRepo, never()).deleteById(any());
    }
}
