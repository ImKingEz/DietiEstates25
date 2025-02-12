package com.dietiestates25backend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;


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

    private MultipartFile logo;

    @NotBlank(message = "La password è obbligatoria")
    @Length(min = 8, message = "La password deve essere di almeno 8 caratteri")
    private String password;
}