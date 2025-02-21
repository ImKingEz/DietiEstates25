package com.dietiestates25.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImmobileDTO {
    private Long id;
    private String tipologia;
    private String indirizzo;
    private double dimensione;
    private int numeroLocali;
    private int numeroBagni;
    private String classeEnergetica;
    private Integer piano;
    private boolean ascensore;
    private boolean portineria;
    private boolean climatizzazione;
    private double latitudine;
    private double longitudine;
    private boolean vicinoScuole;
    private boolean vicinoParchi;
    private boolean vicinoTrasportoPubblico;
    private String citta;

    public ImmobileDTO(String tipologia, String indirizzo, double dimensione, int numeroLocali, int numeroBagni, String classeEnergetica, Integer piano, boolean ascensore, boolean portineria, boolean climatizzazione, double latitudine, double longitudine, boolean vicinoScuole, boolean vicinoParchi, boolean vicinoTrasportoPubblico, String citta) {
        this.tipologia = tipologia;
        this.indirizzo = indirizzo;
        this.dimensione = dimensione;
        this.numeroLocali = numeroLocali;
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
        this.citta = citta;
    }
}