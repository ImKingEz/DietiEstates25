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
            "WHERE LOWER(i.citta) = LOWER(:citta) " +
            "AND LOWER(a.tipo) = LOWER(:tipoAnnuncio) " +
            "AND LOWER(i.tipologia) = LOWER(:tipologiaImmobile)")
    List<Annuncio> findByCittaAndTipoAnnuncioAndTipologiaImmobile(
            @Param("citta") String citta,
            @Param("tipoAnnuncio") String tipoAnnuncio,
            @Param("tipologiaImmobile") String tipologiaImmobile);

    @Query("SELECT a FROM Annuncio a " +
            "JOIN Immobile i ON a.idImmobile = i.id " +
            "WHERE a.tipo = :tipoAnnuncio " +
            "AND i.tipologia = :tipologiaImmobile " +
            "AND haversine(:latitude, :longitude, i.latitudine, i.longitudine) <= :radius")
    List<Annuncio> findAnnunciInRadius(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radius") double radius,
            @Param("tipoAnnuncio") String tipoAnnuncio,
            @Param("tipologiaImmobile") String tipologiaImmobile
    );
}