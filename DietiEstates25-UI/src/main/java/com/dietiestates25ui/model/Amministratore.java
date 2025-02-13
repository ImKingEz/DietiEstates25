package com.dietiestates25ui.model;

public class Amministratore {
    private String email;
    private String password;
    private Long idAgenzia;

    public Amministratore(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Amministratore(String email, Long idAgenzia) {
        this.email = email;
        this.idAgenzia = idAgenzia;
    }

    public Amministratore(String email, String password, Long idAgenzia) {
        this.email = email;
        this.password = password;
        this.idAgenzia = idAgenzia;
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

    public Long getIdAgenzia() {
        return idAgenzia;
    }

    public void setIdAgenzia(Long idAgenzia) {
        this.idAgenzia = idAgenzia;
    }
}