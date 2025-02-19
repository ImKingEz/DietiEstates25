package com.dietiestates25backend.business.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "annuncio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Annuncio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_agente", nullable = false)
    private Long idAgente;

    @Column(name = "id_immobile", nullable = false)
    private Long idImmobile;

    @Column(name = "titolo", nullable = false)
    private String titolo;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "prezzo", nullable = false)
    private double prezzo;

    @Column(name = "descrizione", nullable = false)
    private String descrizione;

    public Annuncio(String titolo, String tipo, double prezzo, String descrizione, Long idAgente, Long idImmobile) {
        this.titolo = titolo;
        this.tipo = tipo;
        this.prezzo = prezzo;
        this.descrizione = descrizione;
        this.idAgente = idAgente;
        this.idImmobile = idImmobile;
    }
}