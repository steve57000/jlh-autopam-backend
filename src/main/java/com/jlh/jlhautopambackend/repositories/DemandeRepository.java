package com.jlh.jlhautopambackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jlh.jlhautopambackend.modeles.Demande;

public interface DemandeRepository extends JpaRepository<Demande, Integer> { }