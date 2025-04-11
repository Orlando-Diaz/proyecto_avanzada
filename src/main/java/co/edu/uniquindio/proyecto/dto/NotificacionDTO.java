package co.edu.uniquindio.proyecto.dto;


import java.time.LocalDateTime;

public record NotificacionDTO(
        String id,
        String mensaje,
        LocalDateTime fecha,
        String tipo,
        boolean leida,
        String reporteId,
        String idUsuario,
        String titulo
) {
    public NotificacionDTO withFechaActual() {
        return fecha == null ? new NotificacionDTO(id, mensaje, LocalDateTime.now(), tipo, leida, reporteId, idUsuario, titulo) : this;
    }
}
