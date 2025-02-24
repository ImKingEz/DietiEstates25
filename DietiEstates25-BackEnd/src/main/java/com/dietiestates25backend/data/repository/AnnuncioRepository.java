package com.dietiestates25backend.data.repository;

import com.dietiestates25backend.business.entity.Annuncio;
import com.dietiestates25.dto.FiltroAnnunciDTO; // Importa il DTO
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnuncioRepository extends JpaRepository<Annuncio, Long> {
    @Query("SELECT a FROM Annuncio a " +
            "JOIN Immobile i ON a.idImmobile = i.id " +
            "WHERE LOWER(i.citta) = LOWER(:#{#citta}) " +
            "AND LOWER(a.tipo) = LOWER(:#{#filtro.tipo}) " +
            "AND LOWER(i.tipologia) = LOWER(:#{#filtro.tipologia}) " +
            "AND (:#{#filtro.prezzoMin} IS NULL OR a.prezzo >= :#{#filtro.prezzoMin}) " +
            "AND (:#{#filtro.prezzoMax} IS NULL OR a.prezzo <= :#{#filtro.prezzoMax}) " +
            "AND (:#{#filtro.superficieMin} IS NULL OR i.dimensione >= :#{#filtro.superficieMin}) " +
            "AND (:#{#filtro.superficieMax} IS NULL OR i.dimensione <= :#{#filtro.superficieMax}) " +
            "AND (:#{#filtro.locali} IS NULL OR i.numeroLocali >= :#{#filtro.locali}) " +
            "AND (:#{#filtro.bagni} IS NULL OR i.numeroBagni >= :#{#filtro.bagni}) " +
            "AND (:#{#filtro.piano} IS NULL OR i.piano = :#{#filtro.piano}) " +
            "AND (:#{#filtro.classeEnergetica} IS NULL OR i.classeEnergetica IN :#{#filtro.classeEnergetica}) " +
            "AND (:#{#filtro.ascensore} IS NULL OR i.ascensore = :#{#filtro.ascensore}) " +
            "AND (:#{#filtro.portineria} IS NULL OR i.portineria = :#{#filtro.portineria}) " +
            "AND (:#{#filtro.climatizzazione} IS NULL OR i.climatizzazione = :#{#filtro.climatizzazione}) " +
            "AND (:#{#filtro.vicinoScuola} IS NULL OR i.vicinoScuole = :#{#filtro.vicinoScuola}) " +
            "AND (:#{#filtro.vicinoParco} IS NULL OR i.vicinoParchi = :#{#filtro.vicinoParco}) " +
            "AND (:#{#filtro.vicinoTrasportoPubblico} IS NULL OR i.vicinoTrasportoPubblico = :#{#filtro.vicinoTrasportoPubblico})")
    List<Annuncio> findByFiltro(@Param("filtro") FiltroAnnunciDTO filtro, @Param("citta") String citta);
}