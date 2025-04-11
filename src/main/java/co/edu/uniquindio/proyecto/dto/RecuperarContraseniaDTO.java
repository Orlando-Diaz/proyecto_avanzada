package co.edu.uniquindio.proyecto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO Que captura el email del usuario
 * @param email
 */
public record RecuperarContraseniaDTO(
        @Schema(description = "Email del usuario", example = "usuario@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String email) {
}
