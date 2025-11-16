package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ClientStatsDto;
import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import com.jlh.jlhautopambackend.dto.ProchainRdvDto;
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
    private final DemandeTimelineService timelineService;

    public DemandeServiceImpl(DemandeRepository repository,
                              ClientRepository clientRepo,
                              TypeDemandeRepository typeRepo,
                              StatutDemandeRepository statutRepo,
                              DemandeMapper mapper,
                              DemandeTimelineService timelineService) {
        this.repository = repository;
        this.clientRepo = clientRepo;
        this.typeRepo = typeRepo;
        this.statutRepo = statutRepo;
        this.mapper = mapper;
        this.timelineService = timelineService;
    }

    @Override
    public ClientStatsDto findStatsByClientId(Integer clientId) {
        return null;
    }

    @Override
    public Optional<ProchainRdvDto> findProchainRdvByClientId(Integer clientId) {
        return Optional.empty();
    }

    @Override
    public DemandeResponse create(DemandeRequest request) {
        Demande entity = mapper.toEntity(request);
        Client client = clientRepo.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable : " + request.getClientId()));
        TypeDemande type = typeRepo.findById(request.getCodeType())
                .orElseThrow(() -> new IllegalArgumentException("TypeDemande introuvable: " + request.getCodeType()));
        StatutDemande statut = statutRepo.findById(request.getCodeStatut())
                .orElseThrow(() -> new IllegalArgumentException("StatutDemande introuvable: " + request.getCodeStatut()));
        entity.setClient(client);
        entity.setTypeDemande(type);
        entity.setStatutDemande(statut);
        Demande saved = repository.save(entity);
        timelineService.logStatusChange(saved, statut, null, null, "ADMIN");
        return mapper.toResponse(saved);
    }

    @Override
    public DemandeResponse createPublic() {
        return null;
    }

    @Override
    public DemandeResponse createForClient(Integer clientId, DemandeRequest request) {
        return null;
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
    public List<DemandeResponse> findByClientId(Integer clientId) {
        return List.of();
    }

    @Override
    public Optional<DemandeResponse> update(Integer id, DemandeRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setDateDemande(request.getDateDemande());
                    String previousStatut = existing.getStatutDemande() != null
                            ? existing.getStatutDemande().getCodeStatut()
                            : null;
                    Client client = clientRepo.findById(request.getClientId())
                            .orElseThrow(() -> new IllegalArgumentException("Client introuvable : " + request.getClientId()));
                    TypeDemande type = typeRepo.findById(request.getCodeType())
                            .orElseThrow(() -> new IllegalArgumentException("TypeDemande introuvable: " + request.getCodeType()));
                    StatutDemande statut = statutRepo.findById(request.getCodeStatut())
                            .orElseThrow(() -> new IllegalArgumentException("StatutDemande introuvable: " + request.getCodeStatut()));
                    existing.setClient(client);
                    existing.setTypeDemande(type);
                    existing.setStatutDemande(statut);
                    Demande saved = repository.save(existing);
                    timelineService.logStatusChange(saved, statut, previousStatut, null, null);
                    return mapper.toResponse(saved);
                });
    }

    @Override
    public boolean delete(Integer id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    @Override
    public Optional<String> buildProchainRendezVousIcs(Integer clientId) {
        return Optional.empty();
    }

    @Override
    public Optional<String> buildRendezVousIcs(Integer rdvId, Integer clientIdOrNullIfAdmin) {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> findCurrentIdForClient(Integer clientId) {
        return Optional.empty();
    }

    @Override
    public Optional<DemandeResponse> findCurrentForClient(Integer clientId) {
        return Optional.empty();
    }

    @Override
    public DemandeResponse getOrCreateForClient(Integer clientId) {
        return null;
    }
}
