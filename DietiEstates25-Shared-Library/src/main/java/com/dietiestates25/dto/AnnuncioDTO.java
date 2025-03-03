package com.dietiestates25.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnuncioDTO {
    private long idImmobile;
    private long idAgente;
    private String titolo;
    private String tipo;
    private double prezzo;
    private String descrizione;
    private List<String> immaginiUrls;
    private int numeroVisualizzazioni;
    private int numeroOfferte;
    private int numeroVisitePrenotate;

    public AnnuncioDTO(long idImmobile, long idAgente, String titolo, String tipo, double prezzo, String descrizione) {
        this.idImmobile = idImmobile;
        this.idAgente = idAgente;
        this.titolo = titolo;
        this.tipo = tipo;
        this.prezzo = prezzo;
        this.descrizione = descrizione;
    }

    public AnnuncioDTO(long idImmobile, long idAgente, String titolo, String tipo, double prezzo, String descrizione, int numeroVisualizzazioni, int numeroOfferte, int numeroVisitePrenotate) {
        this.idImmobile = idImmobile;
        this.idAgente = idAgente;
        this.titolo = titolo;
        this.tipo = tipo;
        this.prezzo = prezzo;
        this.descrizione = descrizione;
        this.numeroVisualizzazioni = numeroVisualizzazioni;
        this.numeroOfferte = numeroOfferte;
        this.numeroVisitePrenotate = numeroVisitePrenotate;
    }
}