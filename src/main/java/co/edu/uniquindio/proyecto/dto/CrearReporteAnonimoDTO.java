package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CrearReporteAnonimoDTO(
        @NotBlank String titulo,
        @NotBlank String descripcion,
        @NotBlank String categoria,
        boolean esAnonimo,
        UbicacionDTO ubicacion,
        List<String> fotos,
        Ciudad ciudad
) {}
