package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;

import java.time.LocalDateTime;
import java.util.Map;

public record HistorialReporteDTO(
        String descripcion,
        EstadoReporte estado,
        LocalDateTime fecha,
        Map<String, String> cambios

) {
}
