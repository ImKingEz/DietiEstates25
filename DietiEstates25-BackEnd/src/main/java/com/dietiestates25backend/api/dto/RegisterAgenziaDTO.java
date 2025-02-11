package com.dietiestates25backend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAgenziaDTO {

    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    @NotBlank(message = "La partita IVA è obbligatoria")
    private String partitaIva;

    @NotBlank(message = "L'indirizzo è obbligatorio")
    private String indirizzo;

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "L'email deve essere valida")
    private String email;

    @NotBlank(message = "Il telefono è obbligatorio")
    private String telefono;

    @NotBlank(message = "Il logo è obbligatorio")
    private String logo;
}