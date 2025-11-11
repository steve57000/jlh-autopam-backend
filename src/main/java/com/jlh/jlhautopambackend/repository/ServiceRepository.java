package com.jlh.jlhautopambackend.repository;

import com.jlh.jlhautopambackend.modeles.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Integer> {
    List<Service> findAllByArchivedFalseOrderByLibelleAsc();

    Optional<Service> findByIdAndArchivedFalse(Integer id);

    boolean existsByIdAndArchivedFalse(Integer id);
}
