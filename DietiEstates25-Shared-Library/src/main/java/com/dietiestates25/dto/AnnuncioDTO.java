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
    private String titolo;
    private String tipo;
    private double prezzo;
    private String descrizione;
    private List<String> immaginiUrls;

    public AnnuncioDTO(long idImmobile, String titolo, String tipo, double prezzo, String descrizione) {
        this.idImmobile = idImmobile;
        this.titolo = titolo;
        this.tipo = tipo;
        this.prezzo = prezzo;
        this.descrizione = descrizione;
    }
}
