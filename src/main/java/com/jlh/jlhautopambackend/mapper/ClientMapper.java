package com.jlh.jlhautopambackend.mapper;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;
import com.jlh.jlhautopambackend.modeles.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    // On ignore idClient : il est généré par la BDD
    @Mapping(target = "idClient", ignore = true)
    Client toEntity(ClientRequest dto);

    // MapStruct voit que ClientResponse.idClient <-> Client.getIdClient(), pas besoin de @Mapping ici
    ClientResponse toResponse(Client entity);
}
