package com.jlh.jlhautopambackend.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.DemandeService;
import com.jlh.jlhautopambackend.modeles.DemandeServiceKey;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.DemandeServiceRepository;
import com.jlh.jlhautopambackend.repository.ServiceRepository;
import com.jlh.jlhautopambackend.dto.DemandeServiceRequest;
import com.jlh.jlhautopambackend.dto.DemandeServiceResponse;
import com.jlh.jlhautopambackend.mapper.DemandeServiceMapper;

@Service
@Transactional
public class DemandeServiceServiceImpl implements DemandeServiceService {

    private final DemandeServiceRepository dsRepo;
    private final DemandeRepository demandeRepo;
    private final ServiceRepository serviceRepo;
    private final DemandeServiceMapper mapper;

    public DemandeServiceServiceImpl(
            DemandeServiceRepository dsRepo,
            DemandeRepository demandeRepo,
            ServiceRepository serviceRepo,
            DemandeServiceMapper mapper
    ) {
        this.dsRepo = dsRepo;
        this.demandeRepo = demandeRepo;
        this.serviceRepo = serviceRepo;
        this.mapper = mapper;
    }

    @Override
    public List<DemandeServiceResponse> findAll() {
        return dsRepo.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DemandeServiceResponse> findByKey(Integer demandeId, Integer serviceId) {
        DemandeServiceKey key = new DemandeServiceKey(demandeId, serviceId);
        return dsRepo.findById(key)
                .map(mapper::toDto);
    }

    @Override
    public DemandeServiceResponse create(DemandeServiceRequest req) {
        Demande demande = demandeRepo.findById(req.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        com.jlh.jlhautopambackend.modeles.Service serviceEntity =
                serviceRepo.findById(req.getServiceId())
                        .orElseThrow(() -> new IllegalArgumentException("Service introuvable"));

        DemandeService entity = mapper.toEntity(req);
        entity.setDemande(demande);
        entity.setService(serviceEntity);

        DemandeService saved = dsRepo.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    public Optional<DemandeServiceResponse> update(Integer demandeId, Integer serviceId, DemandeServiceRequest req) {
        DemandeServiceKey key = new DemandeServiceKey(demandeId, serviceId);
        return dsRepo.findById(key)
                .map(entity -> {
                    entity.setQuantite(req.getQuantite());
                    DemandeService saved = dsRepo.save(entity);
                    return mapper.toDto(saved);
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
