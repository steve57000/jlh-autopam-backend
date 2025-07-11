package com.jlh.jlhautopambackend.repositories;

import com.jlh.jlhautopambackend.modeles.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministrateurRepository extends JpaRepository<Administrateur, Integer> {
    Optional<Administrateur> findByUsername(String username);
}