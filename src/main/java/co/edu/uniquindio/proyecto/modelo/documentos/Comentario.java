package co.edu.uniquindio.proyecto.modelo.documentos;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document("comentarios")
public class Comentario {

    @Id
    @EqualsAndHashCode.Include
    private ObjectId id;
    private String mensaje;
    private LocalDateTime fecha;
    private ObjectId clienteId;

    @Builder
    public Comentario(String mensaje, LocalDateTime fecha, ObjectId id, ObjectId clienteId) {
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.id = id;
        this.clienteId = clienteId;
        this.id = new ObjectId();
    }
}
