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

import java.util.List;
import java.util.Optional;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promoRepo;
    private final AdministrateurRepository adminRepo;
    private final PromotionMapper mapper;

    public PromotionServiceImpl(PromotionRepository promoRepo,
                                AdministrateurRepository adminRepo,
                                PromotionMapper mapper) {
        this.promoRepo = promoRepo;
        this.adminRepo  = adminRepo;
        this.mapper     = mapper;
    }

    @Override
    public List<PromotionResponse> findAll() {
        return promoRepo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public Optional<PromotionResponse> findById(Integer id) {
        return promoRepo.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        Administrateur admin = adminRepo.findById(request.getAdministrateurId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrateur introuvable : " + request.getAdministrateurId()));
        Promotion toSave = mapper.toEntity(request);
        toSave.setAdministrateur(admin);
        Promotion saved = promoRepo.save(toSave);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public Optional<PromotionResponse> update(Integer id, PromotionRequest request) {
        return promoRepo.findById(id)
                .map(existing -> {
                    if (request.getValidFrom().isAfter(request.getValidTo())) {
                        throw new IllegalArgumentException("validFrom doit être avant validTo");
                    }
                    existing.setImageUrl(request.getImageUrl());
                    existing.setValidFrom(request.getValidFrom());
                    existing.setValidTo(request.getValidTo());

                    if (!existing.getAdministrateur().getIdAdmin()
                            .equals(request.getAdministrateurId())) {
                        Administrateur admin = adminRepo.findById(request.getAdministrateurId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Administrateur introuvable : " + request.getAdministrateurId()));
                        existing.setAdministrateur(admin);
                    }

                    Promotion updated = promoRepo.save(existing);
                    return mapper.toResponse(updated);
                });
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        if (!promoRepo.existsById(id)) {
            return false;
        }
        promoRepo.deleteById(id);
        return true;
    }
}
