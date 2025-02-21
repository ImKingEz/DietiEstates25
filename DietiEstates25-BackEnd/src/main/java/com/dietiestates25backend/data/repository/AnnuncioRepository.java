package com.dietiestates25backend.data.repository;

import com.dietiestates25backend.business.entity.Annuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnuncioRepository extends JpaRepository<Annuncio, Long> {
    @Query("SELECT a FROM Annuncio a " +
            "JOIN Immobile i ON a.idImmobile = i.id " +
            "WHERE LOWER(i.indirizzo) LIKE LOWER(concat('%', :citta, '%'))")
    List<Annuncio> findByCittaInIndirizzo(@Param("citta") String citta);
}