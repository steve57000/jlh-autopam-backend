package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.*;
import com.jlh.jlhautopambackend.modeles.Promotion;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "idPromotion", ignore = true)
    @Mapping(target = "administrateur", ignore = true)
    Promotion toEntity(PromotionRequest dto);

    @Mapping(target = "idPromotion", source = "entity.idPromotion")
    @Mapping(target = "administrateurId", source = "entity.administrateur.idAdmin")
    @Mapping(target = "imageUrl",      source = "entity.imageUrl")
    @Mapping(target = "validFrom",     source = "entity.validFrom")
    @Mapping(target = "validTo",       source = "entity.validTo")
    PromotionResponse toResponse(Promotion entity);
}