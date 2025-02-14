package com.dietiestates25.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgenteDTO {
    private Long idAgenzia;
    private String nome;
    private String cognome;
    private LocalDate dataDiNascita;
    private String sesso;
    private String email;
}