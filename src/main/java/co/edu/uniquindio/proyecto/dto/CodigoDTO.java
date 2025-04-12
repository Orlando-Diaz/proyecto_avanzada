package co.edu.uniquindio.proyecto.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Codigo utilizado para activar cuenta
 */
public record CodigoDTO(@NotBlank String codigo) {
}
