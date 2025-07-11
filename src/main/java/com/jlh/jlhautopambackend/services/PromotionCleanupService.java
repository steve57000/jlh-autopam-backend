package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.repositories.PromotionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
@Service
public class PromotionCleanupService {

    private final PromotionRepository promoRepo;

    public PromotionCleanupService(PromotionRepository promoRepo) {
        this.promoRepo = promoRepo;
    }

    /** Supprime chaque jour à 23h59 toutes les promotions expirées. */
    @Scheduled(cron = "0 59 23 * * *")
    @Transactional
    public void removeExpiredPromotions() {
        Instant now = Instant.now();
        promoRepo.deleteByValidToBefore(now);
    }
}
