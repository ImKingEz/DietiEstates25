package com.dietiestates25ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Immobile {
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

    public Immobile() {
    }

    public String getTipologia() {
        return tipologia;
    }

    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public double getDimensione() {
        return dimensione;
    }

    public void setDimensione(double dimensione) {
        this.dimensione = dimensione;
    }

    public int getNumeroLocali() {
        return numeroLocali;
    }

    public void setNumeroLocali(int numeroLocali) {
        this.numeroLocali = numeroLocali;
    }

    public int getNumeroBagni() {
        return numeroBagni;
    }

    public void setNumeroBagni(int numeroBagni) {
        this.numeroBagni = numeroBagni;
    }

    public String getClasseEnergetica() {
        return classeEnergetica;
    }

    public void setClasseEnergetica(String classeEnergetica) {
        this.classeEnergetica = classeEnergetica;
    }

    public Integer getPiano() {
        return piano;
    }

    public void setPiano(Integer piano) {
        this.piano = piano;
    }

    public boolean isAscensore() {
        return ascensore;
    }

    public void setAscensore(boolean ascensore) {
        this.ascensore = ascensore;
    }

    public boolean isPortineria() {
        return portineria;
    }

    public void setPortineria(boolean portineria) {
        this.portineria = portineria;
    }

    public boolean isClimatizzazione() {
        return climatizzazione;
    }

    public void setClimatizzazione(boolean climatizzazione) {
        this.climatizzazione = climatizzazione;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }

    public boolean isVicinoScuole() {
        return vicinoScuole;
    }

    public void setVicinoScuole(boolean vicinoScuole) {
        this.vicinoScuole = vicinoScuole;
    }

    public boolean isVicinoParchi() {
        return vicinoParchi;
    }

    public void setVicinoParchi(boolean vicinoParchi) {
        this.vicinoParchi = vicinoParchi;
    }

    public boolean isVicinoTrasportoPubblico() {
        return vicinoTrasportoPubblico;
    }

    public void setVicinoTrasportoPubblico(boolean vicinoTrasportoPubblico) {
        this.vicinoTrasportoPubblico = vicinoTrasportoPubblico;
    }
}