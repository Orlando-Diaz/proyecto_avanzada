package co.edu.uniquindio.proyecto.dto;

public record EnvioNotificacionDTO(
        String usuarioId,
        String reporteId,
        String titulo,
        String mensaje,
        String tipoNotificacion
) {
}
