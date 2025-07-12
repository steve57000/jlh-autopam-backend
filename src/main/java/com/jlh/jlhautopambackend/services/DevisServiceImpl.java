package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.DevisRequest;
import com.jlh.jlhautopambackend.dto.DevisResponse;
import com.jlh.jlhautopambackend.mapper.DevisMapper;
import com.jlh.jlhautopambackend.modeles.Devis;
import com.jlh.jlhautopambackend.repositories.DevisRepository;
import com.jlh.jlhautopambackend.repositories.DemandeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DevisServiceImpl implements DevisService {

    private final DevisRepository devisRepo;
    private final DemandeRepository demandeRepo;
    private final DevisMapper mapper;

    public DevisServiceImpl(DevisRepository devisRepo,
                            DemandeRepository demandeRepo,
                            DevisMapper mapper) {
        this.devisRepo = devisRepo;
        this.demandeRepo = demandeRepo;
        this.mapper = mapper;
    }

    @Override
    public List<DevisResponse> findAll() {
        return devisRepo.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DevisResponse> findById(Integer id) {
        return devisRepo.findById(id)
                .map(mapper::toResponse);
    }

    @Override
    public DevisResponse create(DevisRequest request) {
        var demande = demandeRepo.findById(request.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        Devis entity = mapper.toEntity(request);
        entity.setDemande(demande);
        Devis saved = devisRepo.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public Optional<DevisResponse> update(Integer id, DevisRequest request) {
        return devisRepo.findById(id)
                .map(existing -> {
                    existing.setMontantTotal(request.getMontantTotal());
                    Devis updated = devisRepo.save(existing);
                    return mapper.toResponse(updated);
                });
    }

    @Override
    public boolean delete(Integer id) {
        if (!devisRepo.existsById(id)) {
            return false;
        }
        devisRepo.deleteById(id);
        return true;
    }
}