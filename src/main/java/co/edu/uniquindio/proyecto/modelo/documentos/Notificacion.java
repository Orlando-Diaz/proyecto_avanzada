package co.edu.uniquindio.proyecto.modelo.documentos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document("notificaciones")
public class Notificacion {

    private String mensaje;
    private LocalDateTime fecha;
    private String tipo;
    private ObjectId id;
    private boolean leida;
    private ObjectId reporteId;
    private ObjectId idUsuario;
}
