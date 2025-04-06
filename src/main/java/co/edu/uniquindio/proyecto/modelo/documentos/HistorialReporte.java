package co.edu.uniquindio.proyecto.modelo.documentos;

import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Document("historialReporte")
public class HistorialReporte {

    private ObjectId clienteId;
    private String observaciones;
    private EstadoReporte estado;
    private LocalDateTime fecha;
    private Map<String, String> cambios;

    @Builder
    public HistorialReporte(String observaciones, EstadoReporte estado, LocalDateTime fecha, Map<String, String> cambios) {
        this.observaciones = observaciones;
        this.estado = estado;
        this.fecha = fecha;
        this.cambios = cambios;
    }
}
