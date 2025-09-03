package com.jlh.jlhautopambackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jlh.jlhautopambackend.modeles.Client;

public interface ClientRepository extends JpaRepository<Client, Integer> { }