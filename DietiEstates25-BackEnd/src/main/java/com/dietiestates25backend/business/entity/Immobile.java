package com.dietiestates25backend.business.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "immobile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Immobile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titolo", nullable = false)
    private String titolo;

    @Column(name = "tipo", nullable = false)
    private String tipologia;

    @Column(name = "indirizzo", nullable = false)
    private String indirizzo;

    @Column(name = "prezzo", nullable = false)
    private double prezzo;

    @Column(name = "descrizione", nullable = false)
    private String descrizione;

    @Column(name = "dimensioni", nullable = false)
    private double dimensione;

    @Column(name = "numero_stanze", nullable = false)
    private int numeroCamere;

    @Column(name = "numero_bagni", nullable = false)
    private int numeroBagni;

    @Column(name = "classe_energetica")
    private String classeEnergetica;

    @Column(name = "piano")
    private Integer piano;

    @Column(name = "ascensore")
    private boolean ascensore;

    @Column(name = "portineria")
    private boolean portineria;

    @Column(name = "climatizzazione")
    private boolean climatizzazione;

    @Column(name = "latitudine")
    private double latitudine;

    @Column(name = "longitudine")
    private double longitudine;

    @Column(name = "vicino_scuole")
    private boolean vicinoScuole;

    @Column(name = "vicino_parchi")
    private boolean vicinoParchi;

    @Column(name = "vicino_trasporto_pubblico")
    private boolean vicinoTrasportoPubblico;

    @Column(name = "id_agente", nullable = false)
    private Long idAgente;

    public Immobile(String titolo, String tipologia, String indirizzo, double prezzo, String descrizione, double dimensione, int numeroCamere, int numeroBagni, String classeEnergetica, Integer piano, boolean ascensore, boolean portineria, boolean climatizzazione, double latitudine, double longitudine, boolean vicinoScuole, boolean vicinoParchi, boolean vicinoTrasportoPubblico, Long idAgente) {
        this.titolo = titolo;
        this.tipologia = tipologia;
        this.indirizzo = indirizzo;
        this.prezzo = prezzo;
        this.descrizione = descrizione;
        this.dimensione = dimensione;
        this.numeroCamere = numeroCamere;
        this.numeroBagni = numeroBagni;
        this.classeEnergetica = classeEnergetica;
        this.piano = piano;
        this.ascensore = ascensore;
        this.portineria = portineria;
        this.climatizzazione = climatizzazione;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.vicinoScuole = vicinoScuole;
        this.vicinoParchi = vicinoParchi;
        this.vicinoTrasportoPubblico = vicinoTrasportoPubblico;
        this.idAgente = idAgente;
    }
}