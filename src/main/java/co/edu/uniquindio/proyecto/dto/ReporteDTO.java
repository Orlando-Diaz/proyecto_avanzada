package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

public record ReporteDTO(
        String id,
        String descripcion,
        LocalDateTime fecha,
        int contadorImportante,
        String idUsuario,
        String titulo,
        UbicacionDTO ubicacion,
        List<String> fotos,
        String estadoActual,
        Ciudad ciudad,
        List<String> comentarios,
        boolean esAnonimo,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String nombreUsuario
) {}