package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDateTime;


public record HistorialReporteDTO(
        @NotBlank String comentario,
        EstadoReporte estado,
        LocalDateTime fecha
) {}
