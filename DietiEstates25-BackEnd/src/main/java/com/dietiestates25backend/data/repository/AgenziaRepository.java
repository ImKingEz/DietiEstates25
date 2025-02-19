package com.dietiestates25backend.data.repository;

import com.dietiestates25backend.business.entity.AgenziaImmobiliare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgenziaRepository extends JpaRepository<AgenziaImmobiliare, Long> {
    boolean existsByEmail(String email);
    Optional<AgenziaImmobiliare> findByEmail(String email);
}