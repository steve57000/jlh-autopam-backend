package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DemandeServiceMapper {

    @Mapping(source = "request.demandeId", target = "id.idDemande")
    @Mapping(source = "request.serviceId",  target = "id.idService")
    @Mapping(source = "request.quantite",   target = "quantite")
    @Mapping(target = "demande", ignore = true) // on les pose en service
    @Mapping(target = "service", ignore = true)
    DemandeService toEntity(DemandeServiceRequest request);

    @Mapping(source = "entity.id.idDemande", target = "id.idDemande")
    @Mapping(source = "entity.id.idService", target = "id.idService")
    DemandeServiceResponse toDto(DemandeService entity);
}

