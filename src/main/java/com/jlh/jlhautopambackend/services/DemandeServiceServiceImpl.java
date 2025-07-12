package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DemandeServiceRequest;
import com.jlh.jlhautopambackend.dto.DemandeServiceResponse;
import com.jlh.jlhautopambackend.mapper.DemandeServiceMapper;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.DemandeServiceKey;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.Service;
import com.jlh.jlhautopambackend.repositories.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repositories.DemandeRepository;
import com.jlh.jlhautopambackend.repositories.ServiceRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// On précise l’annotation en full-qualifier pour lever l’ambiguïté avec com.jlh.jlhautopambackend.modeles.Service
@org.springframework.stereotype.Service
public class DemandeServiceServiceImpl implements DemandeServiceService {

    private final DemandeServiceRepository dsRepo;
    private final DemandeRepository demandeRepo;
    private final ServiceRepository serviceRepo;
    private final DemandeServiceMapper mapper;

    public DemandeServiceServiceImpl(DemandeServiceRepository dsRepo,
                                     DemandeRepository demandeRepo,
                                     ServiceRepository serviceRepo,
                                     DemandeServiceMapper mapper) {
        this.dsRepo = dsRepo;
        this.demandeRepo = demandeRepo;
        this.serviceRepo = serviceRepo;
        this.mapper = mapper;
    }

    @Override
    public List<DemandeServiceResponse> findAll() {
        return dsRepo.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DemandeServiceResponse> findByKey(Integer demandeId, Integer serviceId) {
        DemandeServiceKey key = new DemandeServiceKey(demandeId, serviceId);
        return dsRepo.findById(key)
                .map(mapper::toResponse);
    }

    @Override
    public DemandeServiceResponse create(DemandeServiceRequest request) {
        // construction de la clé composite
        DemandeServiceKey key = new DemandeServiceKey(
                request.getIdDemande(),
                request.getIdService()
        );
        DemandeService ent = mapper.toEntity(request);
        ent.setId(key);

        Demande demande = demandeRepo.findById(request.getIdDemande())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        ent.setDemande(demande);

        Service service = serviceRepo.findById(request.getIdService())
                .orElseThrow(() -> new IllegalArgumentException("Service introuvable"));
        ent.setService(service);

        DemandeService saved = dsRepo.save(ent);
        return mapper.toResponse(saved);
    }

    @Override
    public Optional<DemandeServiceResponse> update(Integer demandeId, Integer serviceId, DemandeServiceRequest request) {
        DemandeServiceKey key = new DemandeServiceKey(demandeId, serviceId);
        return dsRepo.findById(key)
                .map(existing -> {
                    existing.setQuantite(request.getQuantite());
                    DemandeService saved = dsRepo.save(existing);
                    return mapper.toResponse(saved);
                });
    }

    @Override
    public boolean delete(Integer demandeId, Integer serviceId) {
        DemandeServiceKey key = new DemandeServiceKey(demandeId, serviceId);
        if (!dsRepo.existsById(key)) {
            return false;
        }
        dsRepo.deleteById(key);
        return true;
    }
}