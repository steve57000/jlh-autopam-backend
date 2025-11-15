package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DevisRequest;
import com.jlh.jlhautopambackend.dto.DevisResponse;
import com.jlh.jlhautopambackend.mapper.DevisMapper;
import com.jlh.jlhautopambackend.modeles.Devis;
import com.jlh.jlhautopambackend.modeles.Demande;
import com.jlh.jlhautopambackend.modeles.StatutDemande;
import com.jlh.jlhautopambackend.repository.DevisRepository;
import com.jlh.jlhautopambackend.repository.DemandeRepository;
import com.jlh.jlhautopambackend.repository.StatutDemandeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DevisServiceImpl implements DevisService {

    private final DevisRepository devisRepo;
    private final DemandeRepository demandeRepo;
    private final StatutDemandeRepository statutRepo; // ✅
    private final DevisMapper mapper;
    private final DemandeTimelineService timelineService;

    private static final String STATUT_BROUILLON  = "Brouillon";
    private static final String STATUT_EN_ATTENTE = "En_attente";

    public DevisServiceImpl(DevisRepository devisRepo,
                            DemandeRepository demandeRepo,
                            StatutDemandeRepository statutRepo, // ✅
                            DevisMapper mapper,
                            DemandeTimelineService timelineService) {
        this.devisRepo = devisRepo;
        this.demandeRepo = demandeRepo;
        this.statutRepo = statutRepo; // ✅
        this.mapper = mapper;
        this.timelineService = timelineService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DevisResponse> findAll() {
        return devisRepo.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DevisResponse> findById(Integer id) {
        return devisRepo.findById(id).map(mapper::toResponse);
    }

    @Override
    public DevisResponse create(DevisRequest request) {
        // 1) Récupère la demande
        Demande demande = demandeRepo.findById(request.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        // 2) Création du devis
        Devis entity = mapper.toEntity(request);
        entity.setDemande(demande);
        Devis saved = devisRepo.save(entity);
        timelineService.logMontantValidation(demande, saved.getMontantTotal(), "Montant du devis validé", null, "ADMIN");

        // 3) Passage Brouillon -> En_attente (propre)
        String current = demande.getStatutDemande() != null ? demande.getStatutDemande().getCodeStatut() : null;
        if (current == null || STATUT_BROUILLON.equals(current)) {
            StatutDemande enAttente = statutRepo.findById(STATUT_EN_ATTENTE)
                    .orElseThrow(() -> new IllegalStateException("Statut 'En_attente' manquant en base"));
            demande.setStatutDemande(enAttente);
            Demande updated = demandeRepo.save(demande);
            timelineService.logStatusChange(updated, enAttente, current, null, null);
        }

        return mapper.toResponse(saved);
    }

    @Override
    public Optional<DevisResponse> update(Integer id, DevisRequest request) {
        return devisRepo.findById(id)
                .map(existing -> {
                    BigDecimal previous = existing.getMontantTotal();
                    existing.setMontantTotal(request.getMontantTotal());
                    Devis updated = devisRepo.save(existing);
                    if (existing.getDemande() != null && !Objects.equals(previous, request.getMontantTotal())) {
                        timelineService.logMontantValidation(existing.getDemande(), updated.getMontantTotal(),
                                "Montant du devis mis à jour", null, null);
                    }
                    return mapper.toResponse(updated);
                });
    }

    @Override
    public boolean delete(Integer id) {
        if (!devisRepo.existsById(id)) return false;
        devisRepo.deleteById(id);
        return true;
    }
}
