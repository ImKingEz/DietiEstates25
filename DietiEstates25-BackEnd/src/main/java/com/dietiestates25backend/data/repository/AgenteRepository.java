package com.dietiestates25backend.data.repository;

import com.dietiestates25backend.business.entity.AgenteImmobiliare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgenteRepository extends JpaRepository<AgenteImmobiliare, Long> {
    boolean existsByEmail(String email);
}