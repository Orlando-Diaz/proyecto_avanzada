package co.edu.uniquindio.proyecto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO Para cambiar la contrasenia y validar el codigo
 * @param codigo
 * @param nuevaPassword
 */
public record CambiarPasswordDTO(
        @Email String email,
        @Schema(description = "Código de recuperación", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String codigo,
        @Schema(description = "Nueva contraseña (mínimo 8 caracteres)", example = "NuevaContraseña123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String nuevaPassword
) {}

