package co.edu.uniquindio.proyecto.modelo.documentos;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document("categoria")
public class Categoria {


    @Id
    @EqualsAndHashCode.Include
    private ObjectId id;
    private String nombre;
    private String icono;

    @Builder
    public Categoria(String nombre, String icono) {
        this.nombre = nombre;
        this.icono = icono;
        this.id = new ObjectId();
    }

}
