package com.dietiestates25ui.model;

public class AgenziaImmobiliare {
    private String nome;
    private String partitaIva;
    private String indirizzo;
    private String email;
    private String telefono;
    private String logoUrl;
    private String password;

    public AgenziaImmobiliare(String nome, String partitaIva, String indirizzo, String email, String telefono, String logoUrl) {
        this.nome = nome;
        this.partitaIva = partitaIva;
        this.indirizzo = indirizzo;
        this.email = email;
        this.telefono = telefono;
        this.logoUrl = logoUrl;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPartitaIva() {
        return partitaIva;
    }

    public void setPartitaIva(String partitaIva) {
        this.partitaIva = partitaIva;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
