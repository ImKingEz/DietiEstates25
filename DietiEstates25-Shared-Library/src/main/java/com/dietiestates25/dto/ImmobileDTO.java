package com.dietiestates25.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImmobileDTO {

    private String titolo;
    private String tipologia;
    private String indirizzo;
    private double prezzo;
    private String descrizione;
    private double dimensione;
    private int numero_camere;
    private int numero_bagni;
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
    private List<String> immaginiUrls;

    public ImmobileDTO(String titolo, String tipologia, String indirizzo, double prezzo, String descrizione, double dimensione, int numero_camere, int numero_bagni, String classeEnergetica, Integer piano, boolean ascensore, boolean portineria, boolean climatizzazione, double latitudine, double longitudine, boolean vicinoScuole, boolean vicinoParchi, boolean vicinoTrasportoPubblico) {
        this.titolo = titolo;
        this.tipologia = tipologia;
        this.indirizzo = indirizzo;
        this.prezzo = prezzo;
        this.descrizione = descrizione;
        this.dimensione = dimensione;
        this.numero_camere = numero_camere;
        this.numero_bagni = numero_bagni;
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
    }
}