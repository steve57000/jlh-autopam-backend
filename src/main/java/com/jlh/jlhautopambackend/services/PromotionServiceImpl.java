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
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promoRepo;
    private final AdministrateurRepository adminRepo;
    private final PromotionMapper mapper;
    private final FileStorageService storage;

    public PromotionServiceImpl(PromotionRepository promoRepo,
                                AdministrateurRepository adminRepo,
                                PromotionMapper mapper,
                                FileStorageService storage) {
        this.promoRepo = promoRepo;
        this.adminRepo = adminRepo;
        this.mapper    = mapper;
        this.storage   = storage;
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
    public PromotionResponse create(PromotionRequest req, MultipartFile file) throws IOException {
        Administrateur admin = adminRepo.findById(req.getAdministrateurId())
                .orElseThrow(() -> new IllegalArgumentException("Administrateur introuvable"));

        Promotion promo = mapper.toEntity(req);
        promo.setAdministrateur(admin);

        // Gestion du fichier (image/PDF) : optionnel
        if (file != null && !file.isEmpty()) {
            String stored = storage.store(file);
            promo.setImageUrl("/promotions/images/" + stored);
        } else {
            promo.setImageUrl(null);
        }

        Promotion saved = promoRepo.save(promo);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public Optional<PromotionResponse> update(Integer id, PromotionRequest req, MultipartFile file) throws IOException {
        return promoRepo.findById(id).map(existing -> {
            if (req.getValidFrom().isAfter(req.getValidTo())) {
                throw new IllegalArgumentException("La date de début doit être avant la date de fin");
            }

            // Si un nouveau fichier est fourni, supprimer l'ancien puis stocker le nouveau
            if (file != null && !file.isEmpty()) {
                try {
                    String oldUrl = existing.getImageUrl();
                    if (oldUrl != null) {
                        String oldName = oldUrl.substring(oldUrl.lastIndexOf('/') + 1);
                        storage.delete(oldName);
                    }
                    String newName = storage.store(file);
                    existing.setImageUrl("/promotions/images/" + newName);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            existing.setValidFrom(req.getValidFrom());
            existing.setValidTo(req.getValidTo());

            // Mise à jour de l'administrateur si changé
            if (!existing.getAdministrateur().getIdAdmin().equals(req.getAdministrateurId())) {
                Administrateur newAdmin = adminRepo.findById(req.getAdministrateurId())
                        .orElseThrow(() -> new IllegalArgumentException("Administrateur introuvable"));
                existing.setAdministrateur(newAdmin);
            }

            Promotion updated = promoRepo.save(existing);
            return mapper.toResponse(updated);
        });
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        return promoRepo.findById(id).map(p -> {
            try {
                String url = p.getImageUrl();
                if (url != null) {
                    String name = url.substring(url.lastIndexOf('/') + 1);
                    storage.delete(name);
                }
            } catch (IOException ignored) { }
            promoRepo.deleteById(id);
            return true;
        }).orElse(false);
    }
}
