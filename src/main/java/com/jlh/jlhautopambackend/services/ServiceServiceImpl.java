package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ServiceRequest;
import com.jlh.jlhautopambackend.dto.ServiceResponse;
import com.jlh.jlhautopambackend.mapper.ServiceMapper;
import com.jlh.jlhautopambackend.modeles.Service;
import com.jlh.jlhautopambackend.repositories.ServiceRepository;

import java.util.List;
import java.util.Optional;

// On utilise la forme fully-qualified pour l’annotation afin d’éviter le conflit de nom
@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository repo;
    private final ServiceMapper mapper;

    public ServiceServiceImpl(ServiceRepository repo, ServiceMapper mapper) {
        this.repo   = repo;
        this.mapper = mapper;
    }

    @Override
    public List<ServiceResponse> findAll() {
        return repo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public Optional<ServiceResponse> findById(Integer id) {
        return repo.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    public ServiceResponse create(ServiceRequest request) {
        Service toSave = mapper.toEntity(request);
        Service saved  = repo.save(toSave);
        return mapper.toResponse(saved);
    }

    @Override
    public Optional<ServiceResponse> update(Integer id, ServiceRequest request) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setLibelle(request.getLibelle());
                    existing.setDescription(request.getDescription());
                    existing.setPrixUnitaire(request.getPrixUnitaire());
                    Service saved = repo.save(existing);
                    return mapper.toResponse(saved);
                });
    }

    @Override
    public boolean delete(Integer id) {
        if (!repo.existsById(id)) {
            return false;
        }
        repo.deleteById(id);
        return true;
    }
}
