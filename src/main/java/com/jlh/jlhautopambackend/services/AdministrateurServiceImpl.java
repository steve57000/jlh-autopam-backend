package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.AdministrateurRequest;
import com.jlh.jlhautopambackend.dto.AdministrateurResponse;
import com.jlh.jlhautopambackend.mapper.AdministrateurMapper;
import com.jlh.jlhautopambackend.modeles.Administrateur;
import com.jlh.jlhautopambackend.repositories.AdministrateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdministrateurServiceImpl implements AdministrateurService {

    private final AdministrateurRepository repository;
    private final AdministrateurMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public AdministrateurServiceImpl(AdministrateurRepository repository,
                                     AdministrateurMapper mapper,
                                     PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AdministrateurResponse create(AdministrateurRequest request) {
        Administrateur entity = mapper.toEntity(request);
        entity.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        Administrateur saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdministrateurResponse> findById(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministrateurResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AdministrateurResponse> update(Integer id, AdministrateurRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setUsername(request.getUsername());
                    existing.setNom(request.getNom());
                    existing.setPrenom(request.getPrenom());
                    if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
                        existing.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
                    }
                    Administrateur updated = repository.save(existing);
                    return mapper.toResponse(updated);
                });
    }

    @Override
    public boolean delete(Integer id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
