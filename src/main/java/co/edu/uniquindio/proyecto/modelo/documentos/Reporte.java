package co.edu.uniquindio.proyecto.modelo.documentos;

import co.edu.uniquindio.proyecto.modelo.documentos.Ubicacion;
import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document("reportes")
@Getter
@Setter
@NoArgsConstructor
public class Reporte {
    @Id
    private ObjectId id;
    private String descripcion;
    private List<HistorialReporte> historial = new ArrayList<>();
    private LocalDateTime fecha;
    private int contadorImportante;
    private ObjectId idUsuario;
    private String titulo;
    private Ubicacion ubicacion;
    private List<String> fotos;
    private EstadoReporte estadoActual;

    private Ciudad ciudad;
}
