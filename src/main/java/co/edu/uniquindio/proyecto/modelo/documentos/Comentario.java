package co.edu.uniquindio.proyecto.modelo.documentos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    private ObjectId reporteId;
    private String mensaje;
    private LocalDateTime fecha;

    @Id
    private ObjectId id;
    private ObjectId clienteId;
}
