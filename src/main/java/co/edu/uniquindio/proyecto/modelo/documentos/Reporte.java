package co.edu.uniquindio.proyecto.modelo.documentos;

import co.edu.uniquindio.proyecto.dto.Ubicacion;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document("reportes")
public class Reporte {


    @Id
    @EqualsAndHashCode.Include
    private ObjectId id;
    private String descricion;
    private List<HistorialReporte> historial;
    private LocalDateTime fecha;
    private int contadorImportante;
    private ObjectId clienteId;
    private String titulo;
    private Ubicacion ubicacion;
    private List<String> fotos;
    private EstadoReporte estadoActual;

    @Builder
    public Reporte(String descricion, List<HistorialReporte> historial, LocalDateTime fecha
            , int contadorImportante, ObjectId clienteId, String titulo){
        this.descricion = descricion;
        this.historial = historial;
        this.fecha = fecha;
        this.contadorImportante = contadorImportante;
        this.clienteId = clienteId;
        this.titulo = titulo;

    }


}
