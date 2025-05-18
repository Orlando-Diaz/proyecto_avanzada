package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;

import java.time.LocalDateTime;

public record ReporteResumidoDTO(
        String id,
        String titulo,
        LocalDateTime fecha,
        EstadoReporte estadoActual,
        int contadorImportante
) {
}
