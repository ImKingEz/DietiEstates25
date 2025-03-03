package com.dietiestates25ui.controller;

public class TokenManager {

    private static TokenManager instance;
    private String token;
    private Object loggedInUser;

    private TokenManager() {}

    public static TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void clearToken() {
        this.token = null;
    }

    public Object getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(Object loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public void clearLoggedInUser(){
        this.loggedInUser = null;
    }
}