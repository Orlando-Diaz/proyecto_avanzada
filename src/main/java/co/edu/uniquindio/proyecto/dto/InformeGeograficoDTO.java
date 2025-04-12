package co.edu.uniquindio.proyecto.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record InformeGeograficoDTO(
        Double latitud,
        Double longitud,
        Double radioKm,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        long totalReportes,
        Map<String, Long> reportesPorCategoria,
        List<ReporteUbicacionDTO> reportes
) {
}
