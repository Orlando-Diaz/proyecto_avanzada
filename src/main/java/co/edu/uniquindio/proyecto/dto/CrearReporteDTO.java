package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.documentos.Ubicacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record CrearReporteDTO(
        @NotBlank @Length(max = 150) String titulo,
        @NotBlank @Length(max = 400) String descripcion,
        @NotEmpty List<String> fotos,
        @NotBlank String categoriaNombre,
        @NotNull UbicacionDTO ubicacion,
        @NotBlank String idUsuario,
        @NotBlank String ciudad
) {}
