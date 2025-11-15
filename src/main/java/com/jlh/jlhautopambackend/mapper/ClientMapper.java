// src/main/java/com/jlh/jlhautopambackend/mapper/ClientMapper.java
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

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Mapping(target = "idClient", ignore = true)
    @Mapping(target = "motDePasse", expression = "java(passwordEncoder.encode(dto.getMotDePasse()))")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "emailVerifiedAt", ignore = true)
    // champs adresse
    @Mapping(target = "adresseLigne1", source = "adresseLigne1")
    @Mapping(target = "adresseLigne2", source = "adresseLigne2")
    @Mapping(target = "adresseCodePostal", source = "codePostal")
    @Mapping(target = "adresseVille", source = "ville")
    @Mapping(target = "vehiculeMarque", source = "vehiculeMarque")
    @Mapping(target = "vehiculeModele", source = "vehiculeModele")
    public abstract Client toEntity(ClientRequest dto);

    @Mapping(target = "codePostal", source = "adresseCodePostal")
    @Mapping(target = "ville", source = "adresseVille")
    @Mapping(target = "vehiculeMarque", source = "vehiculeMarque")
    @Mapping(target = "vehiculeModele", source = "vehiculeModele")
    public abstract ClientResponse toResponse(Client entity);
}
