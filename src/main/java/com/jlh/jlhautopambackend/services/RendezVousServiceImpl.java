package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.RendezVousRequest;
import com.jlh.jlhautopambackend.dto.RendezVousResponse;
import com.jlh.jlhautopambackend.mapper.RendezVousMapper;
import com.jlh.jlhautopambackend.modeles.*;
import com.jlh.jlhautopambackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RendezVousServiceImpl implements RendezVousService {

    private final RendezVousRepository repo;
    private final DemandeRepository demandeRepo;
    private final CreneauRepository creneauRepo;
    private final AdministrateurRepository adminRepo;
    private final StatutRendezVousRepository statutRepo;
    private final RendezVousMapper mapper;

    public RendezVousServiceImpl(RendezVousRepository repo,
                                 DemandeRepository demandeRepo,
                                 CreneauRepository creneauRepo,
                                 AdministrateurRepository adminRepo,
                                 StatutRendezVousRepository statutRepo,
                                 RendezVousMapper mapper) {
        this.repo = repo;
        this.demandeRepo = demandeRepo;
        this.creneauRepo = creneauRepo;
        this.adminRepo = adminRepo;
        this.statutRepo = statutRepo;
        this.mapper = mapper;
    }

    @Override
    public List<RendezVousResponse> findAll() {
        return repo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public Optional<RendezVousResponse> findById(Integer id) {
        return repo.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    public RendezVousResponse create(RendezVousRequest req) {
        Demande demande = demandeRepo.findById(req.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + req.getDemandeId()));
        Creneau creneau = creneauRepo.findById(req.getCreneauId())
                .orElseThrow(() -> new IllegalArgumentException("Creneau introuvable: " + req.getCreneauId()));
        Administrateur admin = adminRepo.findById(req.getAdministrateurId())
                .orElseThrow(() -> new IllegalArgumentException("Administrateur introuvable: " + req.getAdministrateurId()));
        StatutRendezVous statut = statutRepo.findById(req.getCodeStatut())
                .orElseThrow(() -> new IllegalArgumentException("Statut introuvable: " + req.getCodeStatut()));

        RendezVous ent = mapper.toEntity(req);
        ent.setDemande(demande);
        ent.setCreneau(creneau);
        ent.setAdministrateur(admin);
        ent.setStatut(statut);

        RendezVous saved = repo.save(ent);
        return mapper.toResponse(saved);
    }

    @Override
    public Optional<RendezVousResponse> update(Integer id, RendezVousRequest req) {
        return repo.findById(id).map(existing -> {
            existing.setDemande(demandeRepo.findById(req.getDemandeId())
                    .orElseThrow(() -> new IllegalArgumentException("Demande introuvable: " + req.getDemandeId())));
            existing.setCreneau(creneauRepo.findById(req.getCreneauId())
                    .orElseThrow(() -> new IllegalArgumentException("Creneau introuvable: " + req.getCreneauId())));
            existing.setAdministrateur(adminRepo.findById(req.getAdministrateurId())
                    .orElseThrow(() -> new IllegalArgumentException("Administrateur introuvable: " + req.getAdministrateurId())));
            existing.setStatut(statutRepo.findById(req.getCodeStatut())
                    .orElseThrow(() -> new IllegalArgumentException("Statut introuvable: " + req.getCodeStatut())));
            RendezVous updated = repo.save(existing);
            return mapper.toResponse(updated);
        });
    }

    @Override
    public boolean delete(Integer id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }
}
