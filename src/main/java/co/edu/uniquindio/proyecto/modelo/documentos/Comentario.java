package co.edu.uniquindio.proyecto.modelo.documentos;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@Document("comentario")
public class Comentario {

    @Id
    @EqualsAndHashCode.Include
    private ObjectId id;
    private String contenido;
    private LocalDateTime fecha;
    private ObjectId idUsuario;

    @Builder
    public Comentario(String contenido, LocalDateTime fecha, ObjectId id, ObjectId idUsuario) {
        this.contenido = contenido;
        this.fecha = fecha;
        this.id = id;
        this.idUsuario = idUsuario;
        this.id = new ObjectId();
    }
}
