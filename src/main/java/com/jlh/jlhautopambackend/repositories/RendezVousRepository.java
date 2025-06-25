package com.jlh.jlhautopambackend.repositories;

import com.jlh.jlhautopambackend.modeles.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RendezVousRepository extends JpaRepository<Client, Integer> { }