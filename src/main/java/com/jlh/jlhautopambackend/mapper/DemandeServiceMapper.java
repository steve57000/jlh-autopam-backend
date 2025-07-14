package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DemandeServiceMapper {

    @Mapping(source = "request.demandeId", target = "demande.idDemande")
    @Mapping(source = "request.serviceId", target = "service.idService")
    @Mapping(source = "request.quantite", target = "quantite")
    DemandeService toEntity(DemandeServiceRequest request);

    @Mapping(source = "entity.id.idDemande", target = "id.idDemande")
    @Mapping(source = "entity.id.idService", target = "id.idService")
    DemandeServiceResponse toDto(DemandeService entity);
}
