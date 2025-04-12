package co.edu.uniquindio.proyecto.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record EstadisticasGeneralesDTO(
        long totalReportes,
        long reportesPendientes,
        long reportesResueltos,
        long reportesRechazados,
        Map<String, Long> reportesPorCategoria,
        Map<String, Long> reportesPorCiudad,
        LocalDateTime ultimaActualizacion
) {
}
