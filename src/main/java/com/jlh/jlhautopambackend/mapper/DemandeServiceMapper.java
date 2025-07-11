package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DemandeServiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "demande", ignore = true)
    @Mapping(target = "service", ignore = true)
    DemandeService toEntity(DemandeServiceRequest dto);

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "quantite", source = "entity.quantite")
    DemandeServiceResponse toResponse(DemandeService entity);
}
