package com.dietiestates25backend.data.repository;

import com.dietiestates25backend.business.entity.Immobile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImmobileRepository extends JpaRepository<Immobile, Long> {
    Optional<Immobile> findByTitolo(String titolo); //recupera l'entità intera e non solo l'id
}