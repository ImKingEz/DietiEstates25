package com.dietiestates25ui.model;

public class FiltroAnnunci {
    private String tipo;
    private String tipologia;
    private double prezzoMin;
    private double prezzoMax;
    private double superficieMin;
    private double superficieMax;
    private int locali;
    private int bagni;
    private int piano;
    private String classeEnergetica;
    private boolean ascensore;
    private boolean portineria;
    private boolean climatizzazione;
    private boolean vicinoScuola;
    private boolean vicinoParco;
    private boolean vicinoTrasportoPubblico;

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

    public double getPrezzoMin() {
        return prezzoMin;
    }

    public void setPrezzoMin(double prezzoMin) {
        this.prezzoMin = prezzoMin;
    }

    public double getPrezzoMax() {
        return prezzoMax;
    }

    public void setPrezzoMax(double prezzoMax) {
        this.prezzoMax = prezzoMax;
    }

    public double getSuperficieMin() {
        return superficieMin;
    }

    public void setSuperficieMin(double superficieMin) {
        this.superficieMin = superficieMin;
    }

    public double getSuperficieMax() {
        return superficieMax;
    }

    public void setSuperficieMax(double superficieMax) {
        this.superficieMax = superficieMax;
    }

    public int getLocali() {
        return locali;
    }

    public void setLocali(int locali) {
        this.locali = locali;
    }

    public int getBagni() {
        return bagni;
    }

    public void setBagni(int bagni) {
        this.bagni = bagni;
    }

    public int getPiano() {
        return piano;
    }

    public void setPiano(int piano) {
        this.piano = piano;
    }

    public String getClasseEnergetica() {
        return classeEnergetica;
    }

    public void setClasseEnergetica(String classeEnergetica) {
        this.classeEnergetica = classeEnergetica;
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

    public boolean isVicinoScuola() {
        return vicinoScuola;
    }

    public void setVicinoScuola(boolean vicinoScuola) {
        this.vicinoScuola = vicinoScuola;
    }

    public boolean isVicinoParco() {
        return vicinoParco;
    }

    public void setVicinoParco(boolean vicinoParco) {
        this.vicinoParco = vicinoParco;
    }

    public boolean isVicinoTrasportoPubblico() {
        return vicinoTrasportoPubblico;
    }

    public void setVicinoTrasportoPubblico(boolean vicinoTrasportoPubblico) {
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
