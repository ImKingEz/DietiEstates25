package com.dietiestates25.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroAnnunciDTO {
    private String tipo;
    private String tipologia;
    private Double prezzoMin;
    private Double prezzoMax;
    private Double superficieMin;
    private Double superficieMax;
    private Integer locali;
    private Integer bagni;
    private Integer piano;
    private String classeEnergetica;
    private Boolean ascensore;
    private Boolean portineria;
    private Boolean climatizzazione;
    private Boolean vicinoScuola;
    private Boolean vicinoParco;
    private Boolean vicinoTrasportoPubblico;
}