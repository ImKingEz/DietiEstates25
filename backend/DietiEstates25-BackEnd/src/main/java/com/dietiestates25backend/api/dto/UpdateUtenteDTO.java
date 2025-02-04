package com.dietiestates25backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUtenteDTO {
    @NotBlank(message = "Nome non può essere vuoto")
    private String nome;
    @NotBlank(message = "Cognome non può essere vuoto")
    private String cognome;
    private String citta;
}