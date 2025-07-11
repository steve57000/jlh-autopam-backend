package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        imports = {
                com.jlh.jlhautopambackend.dto.DemandeServiceKeyDto.class,
                java.util.stream.Collectors.class
        }
)
public interface DemandeMapper {

    @Mapping(target = "idDemande", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "typeDemande", ignore = true)
    @Mapping(target = "statutDemande", ignore = true)
    @Mapping(target = "services", ignore = true)
    Demande toEntity(DemandeRequest dto);

    @Mapping(target = "clientId", source = "entity.client.idClient")
    @Mapping(target = "typeDemande", source = "entity.typeDemande")
    @Mapping(target = "statutDemande", source = "entity.statutDemande")
    @Mapping(
            target = "services",
            expression =
                    """
                            java(entity.getServices().stream()
                                .map(ds -> new DemandeServiceKeyDto(
                                    ds.getId().getIdDemande(),
                                    ds.getId().getIdService()))
                                .collect(Collectors.toList()))"""
    )
    DemandeResponse toResponse(Demande entity);

    // MapStruct saura utiliser ces méthodes pour convertir vos entités en DTO
    TypeDemandeDto toDto(TypeDemande td);
    StatutDemandeDto toDto(StatutDemande sd);
}
