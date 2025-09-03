package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.RendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Integer> {
    boolean existsByDemandeIdDemande(Integer idDemande);
    boolean existsByCreneauIdCreneau(Integer idCreneau);
}