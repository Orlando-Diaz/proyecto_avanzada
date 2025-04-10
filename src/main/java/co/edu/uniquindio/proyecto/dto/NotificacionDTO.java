package co.edu.uniquindio.proyecto.dto;


import java.time.LocalDateTime;

public record NotificacionDTO(
        String id,
        String mensaje,
        LocalDateTime fecha,
        String tipo,
        boolean leida,
        String reporteId,
        String usuarioId,
        String titulo
) {
}
