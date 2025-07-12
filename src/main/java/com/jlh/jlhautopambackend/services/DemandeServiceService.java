package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DemandeServiceRequest;
import com.jlh.jlhautopambackend.dto.DemandeServiceResponse;

import java.util.List;
import java.util.Optional;

public interface DemandeServiceService {
    List<DemandeServiceResponse> findAll();
    Optional<DemandeServiceResponse> findByKey(Integer demandeId, Integer serviceId);
    DemandeServiceResponse create(DemandeServiceRequest request);
    Optional<DemandeServiceResponse> update(Integer demandeId, Integer serviceId, DemandeServiceRequest request);
    boolean delete(Integer demandeId, Integer serviceId);
}