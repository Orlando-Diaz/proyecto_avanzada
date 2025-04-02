package co.edu.uniquindio.proyecto.modelo.documentos;

import co.edu.uniquindio.proyecto.dto.Ubicacion;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document("reportes")
public class Reporte {

    private String descricion;
    private List<HistorialReporte> historial;
    private LocalDateTime fecha;
    private int contadorImportante;
    private ObjectId clienteId;
    private String titulo;
    private Ubicacion ubicacion;
    private ObjectId id;
    private List<String> fotos;
    private EstadoReporte estadoActual;


}
