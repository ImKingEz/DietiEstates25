package com.dietiestates25ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Annuncio {
    private String titolo;
    private String tipo;
    private double prezzo;
    private String descrizione;
    private List<String> immaginiUrls;
    private Long idImmobile;
    private Long idAgente;

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(double prezzo) {
        this.prezzo = prezzo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public List<String> getImmaginiUrls() {
        return immaginiUrls;
    }

    public void setImmaginiUrls(List<String> immaginiUrls) {
        this.immaginiUrls = immaginiUrls;
    }

    public Long getIdImmobile() {
        return idImmobile;
    }

    public void setIdImmobile(Long idImmobile) {
        this.idImmobile = idImmobile;
    }

    public Long getIdAgente() {
        return idAgente;
    }

    public void setIdAgente(Long idAgente) {
        this.idAgente = idAgente;
    }

    public Annuncio() {
    }

    public Annuncio(String titolo, String tipo, double prezzo, String descrizione, Long idImmobile, Long idAgente) {
        this.titolo = titolo;
        this.tipo = tipo;
        this.prezzo = prezzo;
        this.descrizione = descrizione;
        this.idImmobile = idImmobile;
        this.idAgente = idAgente;
    }

}
