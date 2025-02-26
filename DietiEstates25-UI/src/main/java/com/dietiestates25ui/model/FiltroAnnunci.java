package com.dietiestates25ui.model;

import java.util.List;

public class FiltroAnnunci {
    private String tipo;
    private String tipologia;
    private Double prezzoMin;
    private Double prezzoMax;
    private Double superficieMin;
    private Double superficieMax;
    private Integer locali;
    private Integer bagni;
    private Integer piano;
    private List<String> classeEnergetica;
    private Boolean ascensore;
    private Boolean portineria;
    private Boolean climatizzazione;
    private Boolean vicinoScuola;
    private Boolean vicinoParco;
    private Boolean vicinoTrasportoPubblico;

    public FiltroAnnunci() {
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTipologia() {
        return tipologia;
    }

    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    public Double getPrezzoMin() {
        return prezzoMin;
    }

    public void setPrezzoMin(Double prezzoMin) {
        this.prezzoMin = prezzoMin;
    }

    public Double getPrezzoMax() {
        return prezzoMax;
    }

    public void setPrezzoMax(Double prezzoMax) {
        this.prezzoMax = prezzoMax;
    }

    public Double getSuperficieMin() {
        return superficieMin;
    }

    public void setSuperficieMin(Double superficieMin) {
        this.superficieMin = superficieMin;
    }

    public Double getSuperficieMax() {
        return superficieMax;
    }

    public void setSuperficieMax(Double superficieMax) {
        this.superficieMax = superficieMax;
    }

    public Integer getLocali() {
        return locali;
    }

    public void setLocali(Integer locali) {
        this.locali = locali;
    }

    public Integer getBagni() {
        return bagni;
    }

    public void setBagni(Integer bagni) {
        this.bagni = bagni;
    }

    public Integer getPiano() {
        return piano;
    }

    public void setPiano(Integer piano) {
        this.piano = piano;
    }

    public List<String> getClasseEnergetica() {
        return classeEnergetica;
    }

    public void setClasseEnergetica(List<String> classeEnergetica) {
        this.classeEnergetica = classeEnergetica;
    }

    public Boolean getAscensore() {
        return ascensore;
    }

    public void setAscensore(Boolean ascensore) {
        this.ascensore = ascensore;
    }

    public Boolean getPortineria() {
        return portineria;
    }

    public void setPortineria(Boolean portineria) {
        this.portineria = portineria;
    }

    public Boolean getClimatizzazione() {
        return climatizzazione;
    }

    public void setClimatizzazione(Boolean climatizzazione) {
        this.climatizzazione = climatizzazione;
    }

    public Boolean getVicinoScuola() {
        return vicinoScuola;
    }

    public void setVicinoScuola(Boolean vicinoScuola) {
        this.vicinoScuola = vicinoScuola;
    }

    public Boolean getVicinoParco() {
        return vicinoParco;
    }

    public void setVicinoParco(Boolean vicinoParco) {
        this.vicinoParco = vicinoParco;
    }

    public Boolean getVicinoTrasportoPubblico() {
        return vicinoTrasportoPubblico;
    }

    public void setVicinoTrasportoPubblico(Boolean vicinoTrasportoPubblico) {
        this.vicinoTrasportoPubblico = vicinoTrasportoPubblico;
    }

    @Override
    public String toString() {
        return "FiltroAnnunci{" +
                "tipo='" + tipo + '\'' +
                ", tipologia='" + tipologia + '\'' +
                ", prezzoMin=" + prezzoMin +
                ", prezzoMax=" + prezzoMax +
                ", superficieMin=" + superficieMin +
                ", superficieMax=" + superficieMax +
                ", locali=" + locali +
                ", bagni=" + bagni +
                ", piano=" + piano +
                ", classeEnergetica='" + classeEnergetica + '\'' +
                ", ascensore=" + ascensore +
                ", portineria=" + portineria +
                ", climatizzazione=" + climatizzazione +
                ", vicinoScuola=" + vicinoScuola +
                ", vicinoParco=" + vicinoParco +
                ", vicinoTrasportoPubblico=" + vicinoTrasportoPubblico +
                '}';
    }
}
