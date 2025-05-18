package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.documentos.Ubicacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record EditarReporteDTO(
        String titulo,
        String descripcion,
        List<String> fotos,
        String idCategoria,
        UbicacionDTO ubicacion
) {
}