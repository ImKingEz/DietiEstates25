package com.dietiestates25backend.data.repository;

import com.dietiestates25backend.business.entity.FotoImmobile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FotoImmobileRepository extends JpaRepository<FotoImmobile, Long> {
}