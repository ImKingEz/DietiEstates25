package com.dietiestates25ui.model;

import java.time.LocalDate;

public class AgenteImmobiliare {
    private String nome;
    private String cognome;
    private LocalDate dataDiNascita;
    private String sesso;

    private String email;
    private String password;

    private Long idAgenzia;

    public AgenteImmobiliare(String nome, String cognome, LocalDate dataDiNascita, String sesso, String email, String password, Long idAgenzia) {
        this.nome = nome;
        this.cognome = cognome;
        this.dataDiNascita = dataDiNascita;
        this.sesso = sesso;
        this.email = email;
        this.password = password;
        this.idAgenzia = idAgenzia;
    }

    public AgenteImmobiliare(String email, String password) {
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

    public LocalDate getDataDiNascita() {
        return dataDiNascita;
    }

    public void setDataDiNascita(LocalDate dataDiNascita) {
        this.dataDiNascita = dataDiNascita;
    }

    public String getSesso() {
        return sesso;
    }

    public void setSesso(String sesso) {
        this.sesso = sesso;
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

    public Long getIdAgenzia() {
        return idAgenzia;
    }

    public void setIdAgenzia(Long idAgenzia) {
        this.idAgenzia = idAgenzia;
    }
}

