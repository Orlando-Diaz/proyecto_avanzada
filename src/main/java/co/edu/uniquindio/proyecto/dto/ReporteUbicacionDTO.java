package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;

import java.time.LocalDateTime;

public record ReporteUbicacionDTO(
        String id,
        String titulo,
        String categoria,
        UbicacionDTO ubicacion,
        double distanciaKm,
        LocalDateTime fecha,
        EstadoReporte estadoActual
) {
}
