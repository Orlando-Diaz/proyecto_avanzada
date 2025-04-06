package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;

import java.time.LocalDateTime;

public record HistorialReporteDTO(
        String descripcion,
        EstadoReporte estado,
        LocalDateTime fecha
) {
}
