package com.dietiestates25backend.api.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImmobileDTO {

    @NotBlank(message = "Titolo non può essere vuoto")
    private String titolo;

    @NotBlank(message = "Tipologia non può essere vuota")
    private String tipologia;

    @NotBlank(message = "indirizzo non può essere vuoto")
    private String indirizzo;

    @NotBlank(message = "Prezzo non può essere vuoto")
    private double prezzo;

    @NotBlank(message = "Descrizione non può essere vuota")
    private String descrizione;

    @NotBlank(message = "Dimensione non può essere vuota")
    private double dimensione;

    @NotBlank(message = "Numero camere non può essere vuoto")
    private int numero_camere;

    @NotBlank(message = "Numero bagni non può essere vuoto")
    private int numero_bagni;

    @NotBlank(message = "Classe energetica non può essere vuota")
    private String classeEnergetica;

    private Integer piano;

    private boolean ascensore;

    private boolean portineria;

    private boolean climatizzazione;

    @NotBlank(message = "Latitudine non può essere vuota")
    private double latitudine;

    @NotBlank(message = "Longitudine non può essere vuota")
    private double longitudine;

    private boolean vicinoScuole;

    private boolean vicinoParchi;

    private boolean vicinoTrasportoPubblico;

    @Size(min = 1, max= 5, message = "Devi selezionare almeno un'immagine e non più di 5")
    private List<String> immaginiUrls;
}