package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteDTO {
    private String id;
    private String descripcion;
    private LocalDateTime fecha;
    private int contadorImportante;
    private String idUsuario;
    private String titulo;
    private UbicacionDTO ubicacion;
    private List<String> fotos;
    private String estadoActual;

    private Ciudad ciudad;
    private String categoriaId;
}
