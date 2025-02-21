package com.dietiestates25backend.data.repository;

import com.dietiestates25backend.business.entity.Immobile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImmobileRepository extends JpaRepository<Immobile, Long> {
    @Query("SELECT i FROM Immobile i WHERE LOWER(i.indirizzo) LIKE LOWER(concat('%, ', :citta, ',%'))")
    List<Immobile> findByCittaInIndirizzo(@Param("citta") String citta);
}