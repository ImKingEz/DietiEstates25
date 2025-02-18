package com.dietiestates25backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterImmobileDTO {

    private String titolo;
    private String tipologia;
    private String indirizzo;
    private double prezzo;
    private String descrizione;
    private double dimensione;
    private int numeroCamere;
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
    private List<MultipartFile> immaginiUrls;
}