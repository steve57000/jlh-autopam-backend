package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.PromotionRequest;
import com.jlh.jlhautopambackend.dto.PromotionResponse;
import com.jlh.jlhautopambackend.mapper.PromotionMapper;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Promotion;
import com.jlh.jlhautopambackend.repositories.AdministrateurRepository;
import com.jlh.jlhautopambackend.repositories.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promoRepo;
    private final AdministrateurRepository adminRepo;
    private final PromotionMapper mapper;

    public PromotionServiceImpl(PromotionRepository promoRepo,
                                AdministrateurRepository adminRepo,
                                PromotionMapper mapper) {
        this.promoRepo = promoRepo;
        this.adminRepo = adminRepo;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionResponse> findAll() {
        return promoRepo.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PromotionResponse> findById(Integer id) {
        return promoRepo.findById(id)
                .map(mapper::toResponse);
    }

    // --- surcharge pour vos tests sans MultipartFile ---

    /**
     * Crée une promotion (tests uniquement).
     */
    public PromotionResponse create(PromotionRequest req) {
        // vérifie les dates
        if (req.getValidFrom().isAfter(req.getValidTo())) {
            throw new IllegalArgumentException("validFrom doit être avant validTo");
        }

        // charge l'admin
        Administrateur admin = adminRepo.findById(req.getAdministrateurId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Administrateur introuvable : " + req.getAdministrateurId())
                );

        // mappe en entité
        Promotion entity = mapper.toEntity(req);
        entity.setAdministrateur(admin);

        // persiste
        Promotion saved = promoRepo.save(entity);
        return mapper.toResponse(saved);
    }

    /**
     * Met à jour une promotion (tests uniquement).
     */
    public Optional<PromotionResponse> update(Integer id, PromotionRequest req) {
        Optional<Promotion> opt = promoRepo.findById(id);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Promotion existing = opt.get();

        // vérifie les dates
        if (req.getValidFrom().isAfter(req.getValidTo())) {
            throw new IllegalArgumentException("validFrom doit être avant validTo");
        }

        // si l'admin change, on le recharge
        Integer newAdminId = req.getAdministrateurId();
        if (!existing.getAdministrateur().getIdAdmin().equals(newAdminId)) {
            Administrateur newAdmin = adminRepo.findById(newAdminId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Administrateur introuvable : " + newAdminId)
                    );
            existing.setAdministrateur(newAdmin);
        }

        // met à jour les autres champs
        existing.setImageUrl(req.getImageUrl());
        existing.setValidFrom(req.getValidFrom());
        existing.setValidTo(req.getValidTo());

        Promotion saved = promoRepo.save(existing);
        return Optional.of(mapper.toResponse(saved));
    }

    @Override
    public boolean delete(Integer id) {
        if (!promoRepo.existsById(id)) {
            return false;
        }
        promoRepo.deleteById(id);
        return true;
    }

    // --- implémentation de l’interface pour le controller (avec MultipartFile) ---

    @Override
    public PromotionResponse create(PromotionRequest req, MultipartFile file) throws IOException {
        // si vous stockez le fichier, faites-le ici, par ex. :
        // String url = fileStorageService.store(file);
        // req.setImageUrl(url);
        return create(req);
    }

    @Override
    public Optional<PromotionResponse> update(Integer id, PromotionRequest req, MultipartFile file) throws IOException {
        // même remarque pour l’image :
        // if (file != null) { … }
        return update(id, req);
    }
}
