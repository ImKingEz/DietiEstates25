package com.dietiestates25backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtenteDTO {
    private String nome;
    private String cognome;
    private String citta;
    private String email;
}