package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import com.jlh.jlhautopambackend.mapper.DemandeMapper;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.StatutDemande;
import com.jlh.jlhautopambackend.modeles.TypeDemande;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.StatutDemandeRepository;
import com.jlh.jlhautopambackend.repository.TypeDemandeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DemandeServiceImpl implements DemandeService {
    private final DemandeRepository repository;
    private final ClientRepository clientRepo;
    private final TypeDemandeRepository typeRepo;
    private final StatutDemandeRepository statutRepo;
    private final DemandeMapper mapper;

    public DemandeServiceImpl(DemandeRepository repository,
                              ClientRepository clientRepo,
                              TypeDemandeRepository typeRepo,
                              StatutDemandeRepository statutRepo,
                              DemandeMapper mapper) {
        this.repository = repository;
        this.clientRepo = clientRepo;
        this.typeRepo = typeRepo;
        this.statutRepo = statutRepo;
        this.mapper = mapper;
    }

    @Override
    public DemandeResponse create(DemandeRequest request) {
        Demande entity = mapper.toEntity(request);
        Client client = clientRepo.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable"));
        TypeDemande type = typeRepo.findById(request.getCodeType())
                .orElseThrow(() -> new IllegalArgumentException("Type introuvable"));
        StatutDemande statut = statutRepo.findById(request.getCodeStatut())
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable"));
        entity.setClient(client);
        entity.setTypeDemande(type);
        entity.setStatutDemande(statut);
        Demande saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DemandeResponse> findById(Integer id) {
        return repository.findById(id).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DemandeResponse> update(Integer id, DemandeRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setDateDemande(request.getDateDemande());
                    Client client = clientRepo.findById(request.getClientId())
                            .orElseThrow(() -> new IllegalArgumentException("Client introuvable"));
                    TypeDemande type = typeRepo.findById(request.getCodeType())
                            .orElseThrow(() -> new IllegalArgumentException("Type introuvable"));
                    StatutDemande statut = statutRepo.findById(request.getCodeStatut())
                            .orElseThrow(() -> new IllegalArgumentException("Statut introuvable"));
                    existing.setClient(client);
                    existing.setTypeDemande(type);
                    existing.setStatutDemande(statut);
                    Demande saved = repository.save(existing);
                    return mapper.toResponse(saved);
                });
    }

    @Override
    public boolean delete(Integer id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
