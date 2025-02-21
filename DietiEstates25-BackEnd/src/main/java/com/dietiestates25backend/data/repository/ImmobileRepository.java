package com.dietiestates25backend.data.repository;

import com.dietiestates25backend.business.entity.Immobile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImmobileRepository extends JpaRepository<Immobile, Long> {
    List<Immobile> findByCittaIgnoreCase(String citta); // Corrispondenza esatta, case-insensitive
}