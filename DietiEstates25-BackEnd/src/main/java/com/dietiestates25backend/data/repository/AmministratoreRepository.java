package com.dietiestates25backend.data.repository;

import com.dietiestates25backend.business.entity.Amministratore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AmministratoreRepository extends JpaRepository<Amministratore, Long> {
    Optional<Amministratore> findByEmail(String email);
    boolean existsByEmail(String email);
}