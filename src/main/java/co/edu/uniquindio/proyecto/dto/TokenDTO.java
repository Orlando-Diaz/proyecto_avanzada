package co.edu.uniquindio.proyecto.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenDTO() {
    @NotBlank
    static String token;
}
