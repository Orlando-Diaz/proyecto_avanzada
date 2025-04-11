package co.edu.uniquindio.proyecto.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EnviarCorreoDTO(
        @NotBlank @Email String destinatario,
        @NotBlank String asunto,
        @NotBlank String cuerpo

) {


}
