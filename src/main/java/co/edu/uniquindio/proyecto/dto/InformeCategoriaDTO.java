package co.edu.uniquindio.proyecto.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record InformeCategoriaDTO(

        String categoria,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        long totalReportes,
        List<ReporteResumidoDTO> reportes,
        Map<String, Long> distribucionEstados
) {
}
