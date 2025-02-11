package com.dietiestates25ui.model;

public class Amministratore {
    private String email;
    private String password;
    private int id;

    public Amministratore(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Amministratore(String email, String password, int id) {
        this.email = email;
        this.password = password;
        this.id = id;
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
}
