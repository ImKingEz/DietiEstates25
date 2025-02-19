package com.dietiestates25.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AgenziaDTO {

    private String nome;
    private String partitaIva;
    private String indirizzo;
    private String email;
    private String telefono;
    private String logo;
}