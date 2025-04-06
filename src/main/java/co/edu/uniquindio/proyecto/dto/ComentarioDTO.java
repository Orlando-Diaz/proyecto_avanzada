package co.edu.uniquindio.proyecto.dto;

import java.time.LocalDateTime;

public record ComentarioDTO(

        String idUsuario,
        String contenido,
        LocalDateTime fecha
) {
}
