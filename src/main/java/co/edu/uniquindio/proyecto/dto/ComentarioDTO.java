package co.edu.uniquindio.proyecto.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ComentarioDTO(

        @NotBlank String idUsuario,
        @NotBlank String contenido,
        LocalDateTime fecha
) {
}
