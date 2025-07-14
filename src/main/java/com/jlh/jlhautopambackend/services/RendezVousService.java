package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.RendezVousRequest;
import com.jlh.jlhautopambackend.dto.RendezVousResponse;

import java.util.List;
import java.util.Optional;

public interface RendezVousService {
    List<RendezVousResponse> findAll();
    Optional<RendezVousResponse> findById(Integer id);
    RendezVousResponse create(RendezVousRequest request);
    Optional<RendezVousResponse> update(Integer id, RendezVousRequest request);
    boolean delete(Integer id);
}
