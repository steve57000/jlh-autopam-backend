package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.PromotionRequest;
import com.jlh.jlhautopambackend.dto.PromotionResponse;
import com.jlh.jlhautopambackend.mapper.PromotionMapper;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.modeles.Promotion;
import com.jlh.jlhautopambackend.repository.AdministrateurRepository;
import com.jlh.jlhautopambackend.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private static final Logger log = LoggerFactory.getLogger(PromotionServiceImpl.class);

    private final PromotionRepository promoRepo;
    private final AdministrateurRepository adminRepo;
    private final PromotionMapper mapper;
    private final FileStorageService fileStorage;

    public PromotionServiceImpl(PromotionRepository promoRepo,
                                AdministrateurRepository adminRepo,
                                PromotionMapper mapper,
                                FileStorageService fileStorage) {
        this.promoRepo = promoRepo;
        this.adminRepo = adminRepo;
        this.mapper = mapper;
        this.fileStorage = fileStorage;
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

    /** Création sans fichier, utilisée pour les tests */
    public PromotionResponse create(PromotionRequest req) {
        validateDates(req.getValidFrom(), req.getValidTo());
        Administrateur admin = loadAdmin(req.getAdministrateurId());
        Promotion entity = mapper.toEntity(req);
        entity.setAdministrateur(admin);
        Promotion saved = promoRepo.save(entity);
        return mapper.toResponse(saved);
    }

    /** Mise à jour sans fichier, utilisée pour les tests */
    public Optional<PromotionResponse> update(Integer id, PromotionRequest req) {
        Optional<Promotion> opt = promoRepo.findById(id);
        if (opt.isEmpty()) return Optional.empty();

        validateDates(req.getValidFrom(), req.getValidTo());
        Promotion existing = opt.get();
        handleAdminChange(existing, req.getAdministrateurId());

        // NE METTRE à jour que si req.imageUrl non-null ET non-blank
        if (StringUtils.hasText(req.getImageUrl())) {
            existing.setImageUrl(req.getImageUrl());
        }

        existing.setValidFrom(req.getValidFrom());
        existing.setValidTo(req.getValidTo());
        existing.setDescription(req.getDescription());

        Promotion saved = promoRepo.save(existing);
        return Optional.of(mapper.toResponse(saved));
    }

    @Override
    public boolean delete(Integer id) {
        Optional<Promotion> opt = promoRepo.findById(id);
        if (opt.isEmpty()) {
            return false;
        }

        Promotion promo = opt.get();
        resolveRelativePath(promo.getImageUrl()).ifPresent(path -> {
            try {
                fileStorage.delete(path);
            } catch (IOException e) {
                log.warn("Impossible de supprimer l'image associée à la promotion {} : {}", id, e.getMessage());
            }
        });

        promoRepo.delete(promo);
        return true;
    }

    // --- implémentation avec MultipartFile ---

    @Override
    public PromotionResponse create(PromotionRequest req, MultipartFile file) throws IOException {
        // si un fichier est fourni, on le stocke
        if (file != null && !file.isEmpty()) {
            String filename = fileStorage.store(file);
            req.setImageUrl("/promotions/images/" + filename);
        }
        return create(req);
    }

    @Override
    public Optional<PromotionResponse> update(Integer id, PromotionRequest req, MultipartFile file) throws IOException {
        // on supprime l'ancienne image si on remplace
        if (file != null && !file.isEmpty()) {
            // charger l'ancienne entité pour récupérer l'URL
            promoRepo.findById(id).ifPresent(old -> {
                String oldUrl = old.getImageUrl();
                resolveRelativePath(oldUrl).ifPresent(path -> {
                    try {
                        fileStorage.delete(path);
                    } catch (IOException e) {
                        log.warn("Impossible de supprimer l'ancienne image de la promotion {} : {}", id, e.getMessage());
                    }
                });
            });
            String filename = fileStorage.store(file);
            req.setImageUrl("/promotions/images/" + filename);
        }
        return update(id, req);
    }

    // --- méthodes utilitaires privées ---

    private void validateDates(Instant from, Instant to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("validFrom doit être avant validTo");
        }
    }

    private Administrateur loadAdmin(Integer adminId) {
        return adminRepo.findById(adminId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Administrateur introuvable : " + adminId)
                );
    }

    private void handleAdminChange(Promotion existing, Integer newAdminId) {
        if (!existing.getAdministrateur().getIdAdmin().equals(newAdminId)) {
            Administrateur newAdmin = loadAdmin(newAdminId);
            existing.setAdministrateur(newAdmin);
        }
    }

    private Optional<String> resolveRelativePath(String imageUrl) {
        if (imageUrl == null) {
            return Optional.empty();
        }
        String prefix = "/promotions/images/";
        if (!imageUrl.startsWith(prefix)) {
            return Optional.empty();
        }
        return Optional.of(imageUrl.substring(prefix.length()));
    }
}
