package com.dietiestates25backend.data.repository;

import com.dietiestates25backend.business.entity.AgenteImmobiliare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgenteRepository extends JpaRepository<AgenteImmobiliare, Long> {
    boolean existsByEmail(String email);
    Optional<AgenteImmobiliare> findByEmail(String email);
}