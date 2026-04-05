package com.bytebooks.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50)
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 50)
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email invalido")
        @Size(max = 150)
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,100}$",
                message = "La contraseña debe contener al menos una mayuscula, una minuscula y un numero"
        )
        String password) { }
