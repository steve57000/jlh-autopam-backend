package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.PromotionRequest;
import com.jlh.jlhautopambackend.dto.PromotionResponse;

import java.util.List;
import java.util.Optional;

public interface PromotionService {
    List<PromotionResponse> findAll();
    Optional<PromotionResponse> findById(Integer id);
    PromotionResponse create(PromotionRequest request);
    Optional<PromotionResponse> update(Integer id, PromotionRequest request);
    boolean delete(Integer id);
}
