package com.dietiestates25backend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterAgenteDTO {
    private Long idAgenzia;

    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    private String cognome;

    @NotNull(message = "La data di nascita è obbligatoria")
    private LocalDate dataDiNascita;

    @NotBlank(message = "Il sesso è obbligatorio")
    private String sesso;

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "L'email non è valida")
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 6, message = "La password deve essere lunga almeno 6 caratteri")
    private String password;
}