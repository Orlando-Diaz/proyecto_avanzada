package co.edu.uniquindio.proyecto.dto;

import jakarta.validation.constraints.NotBlank;

public record RechazarReporteDTO(
        @NotBlank String justificacion

) {
}
