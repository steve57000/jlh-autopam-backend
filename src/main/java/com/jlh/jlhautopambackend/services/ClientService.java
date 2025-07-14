package com.jlh.jlhautopambackend.services;

import com.jlh.jlhautopambackend.dto.ClientRequest;
import com.jlh.jlhautopambackend.dto.ClientResponse;

import java.util.List;
import java.util.Optional;

public interface ClientService {
    ClientResponse create(ClientRequest request);
    Optional<ClientResponse> findById(Integer id);
    List<ClientResponse> findAll();
    Optional<ClientResponse> update(Integer id, ClientRequest request);
    boolean delete(Integer id);
}
