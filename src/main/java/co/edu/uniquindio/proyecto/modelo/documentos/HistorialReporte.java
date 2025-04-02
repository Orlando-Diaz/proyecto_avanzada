package co.edu.uniquindio.proyecto.modelo.documentos;

import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class HistorialReporte {

    @Id
    private ObjectId clienteId;
    private String observaciones;
    private EstadoReporte estado;
    private LocalDateTime fecha;
}
