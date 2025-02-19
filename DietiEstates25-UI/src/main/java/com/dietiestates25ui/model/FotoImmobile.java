package com.dietiestates25ui.model;

public class FotoImmobile {
    private String url;
    private Long idAnnuncio;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getIdAnnuncio() {
        return idAnnuncio;
    }

    public void setIdAnnuncio(Long idAnnuncio) {
        this.idAnnuncio = idAnnuncio;
    }

    public FotoImmobile() {
    }

    public FotoImmobile(String url, Long idAnnuncio) {
        this.url = url;
        this.idAnnuncio = idAnnuncio;
    }
}
