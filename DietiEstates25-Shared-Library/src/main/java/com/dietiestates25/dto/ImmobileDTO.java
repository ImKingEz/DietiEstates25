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
}