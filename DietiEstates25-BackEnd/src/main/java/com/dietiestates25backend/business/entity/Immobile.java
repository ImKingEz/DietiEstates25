package com.dietiestates25backend.business.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private String tipo; // "Affitto" o "Vendita"

    @Column(name = "indirizzo", nullable = false)
    private String indirizzo;

    @Column(name = "prezzo", nullable = false)
    private double prezzo;

    @Column(name = "descrizione", nullable = false)
    private String descrizione;

    @Column(name = "dimensioni", nullable = false)
    private double dimensione;

    @Column(name = "numero_camere", nullable = false)
    private int numero_stanze;

    @Column(name = "numero_bagni", nullable = false)
    private int numero_bagni;

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

    @ElementCollection
    private List<String> immaginiUrls; // Immagazzina gli URL delle immagini

    //@ManyToOne
    @JoinColumn(name = "id_agente")
    private String agente;
}