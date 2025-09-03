package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
import com.jlh.jlhautopambackend.modeles.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public abstract class ClientMapper {

    protected PasswordEncoder passwordEncoder;

    /** Injection par setter (propre en abstract MapStruct) */
    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Création d’une entité à partir du DTO :
     * - ignore l’ID
     * - hash le mot de passe
     * - force l’état de vérification initial
     */
    @Mapping(target = "idClient", ignore = true)
    @Mapping(target = "motDePasse", expression = "java(passwordEncoder.encode(dto.getMotDePasse()))")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "emailVerifiedAt", ignore = true)
    public abstract Client toEntity(ClientRequest dto);

    /** Entité -> DTO de sortie (mot de passe jamais exposé) */
    public abstract ClientResponse toResponse(Client entity);
}
