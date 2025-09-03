package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
import com.jlh.jlhautopambackend.mapper.ClientMapper;
import com.jlh.jlhautopambackend.modeles.Client;
import com.jlh.jlhautopambackend.repository.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repository;
    private final ClientMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public ClientServiceImpl(
            ClientRepository repository,
                ClientMapper mapper,
                PasswordEncoder passwordEncoder,
                EmailVerificationService emailVerificationService
    )
    {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * Compatibilité descendante : crée un client ET envoie un e‑mail de vérification.
     */
    @Override
    public ClientResponse create(ClientRequest request) {
        return create(request, true);
    }

    /**
     * Crée un client, avec contrôle explicite de l’envoi de l’e‑mail de vérification.
     */
    @Override
    public ClientResponse create(ClientRequest request, boolean sendVerification) {
        // Le mapper encode déjà le mot de passe et ignore l'ID (voir ClientMapper)
        Client entity = mapper.toEntity(request);

        // Par défaut : non vérifié
        entity.setEmailVerified(false);
        entity.setEmailVerifiedAt(null);

        Client saved = repository.save(entity);

        if (sendVerification) {
            emailVerificationService.sendVerificationForClient(saved.getIdClient());
        }

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClientResponse> findById(Integer id) {
        return repository.findById(id).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ClientResponse> update(Integer id, ClientRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    boolean emailChanged = request.getEmail() != null
                            && !request.getEmail().equalsIgnoreCase(existing.getEmail());

                    // Champs modifiables
                    existing.setNom(request.getNom());
                    existing.setPrenom(request.getPrenom());
                    existing.setEmail(request.getEmail());
                    existing.setTelephone(request.getTelephone());
                    existing.setAdresse(request.getAdresse());
                    existing.setImmatriculation(request.getImmatriculation());

                    // MAJ mot de passe si fourni
                    if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
                        existing.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
                    }

                    // Si l'email change : on invalide la vérification
                    if (emailChanged) {
                        existing.setEmailVerified(false);
                        existing.setEmailVerifiedAt(null);
                    }

                    Client updated = repository.save(existing);

                    // Si l'email a changé, renvoyer une vérification
                    if (emailChanged) {
                        emailVerificationService.sendVerificationForClient(updated.getIdClient());
                    }

                    return mapper.toResponse(updated);
                });
    }

    @Override
    public boolean delete(Integer id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
