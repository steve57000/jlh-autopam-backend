package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DemandeRequest;
import com.jlh.jlhautopambackend.dto.DemandeResponse;
import java.util.List;
import java.util.Optional;

public interface DemandeService {
    DemandeResponse create(DemandeRequest request);
    Optional<DemandeResponse> findById(Integer id);
    List<DemandeResponse> findAll();
    Optional<DemandeResponse> update(Integer id, DemandeRequest request);
    boolean delete(Integer id);
}