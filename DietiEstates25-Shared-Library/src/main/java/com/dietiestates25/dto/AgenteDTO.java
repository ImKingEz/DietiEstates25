package com.dietiestates25.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AgenteDTO {
    private Long idAgenzia;
    private String nome;
    private String cognome;
    private LocalDate dataDiNascita;
    private String sesso;
    private String email;
}