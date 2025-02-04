package com.dietiestates25backend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    @NotBlank(message = "Email non può essere vuota")
    @Email(message = "Email non valida")
    private String email;
    @NotBlank(message = "Password non può essere vuota")
    private String password;
}