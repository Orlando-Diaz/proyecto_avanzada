package co.edu.uniquindio.proyecto.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO utilizado para enviar un correo electrónico
 * Incluye validaciones para asegurar que los campos no estén vacíos y el correo sea válido.
 */
public record EnviarCorreoDTO(
        @NotBlank @Email String destinatario,
        @NotBlank String asunto,
        @NotBlank String cuerpo
) {
}

