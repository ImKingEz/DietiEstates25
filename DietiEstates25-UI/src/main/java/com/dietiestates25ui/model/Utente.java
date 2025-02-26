package com.dietiestates25ui.model;


public class Utente {
    private String nome;
    private String cognome;
    private String citta;
    private String email;
    private String password;

    public Utente() {
        // Costruttore di default
    }

    public Utente(String nome, String cognome, String citta, String email, String password) {
        this.nome = nome;
        this.cognome = cognome;
        this.citta = citta;
        this.email = email;
        this.password = password;
    }

    public Utente(String nome, String cognome, String email, String password) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
    }

    public Utente(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}