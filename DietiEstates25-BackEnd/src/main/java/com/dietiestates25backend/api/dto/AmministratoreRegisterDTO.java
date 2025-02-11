package com.dietiestates25backend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmministratoreRegisterDTO {
    @NotBlank(message = "Email non può essere vuota")
    @Email(message = "Email non valida")
    private String email;
    @NotBlank(message = "Password non può essere vuota")
    @Size(min = 8, message = "La password deve contenere almeno 8 caratteri")
    private String password;
}
