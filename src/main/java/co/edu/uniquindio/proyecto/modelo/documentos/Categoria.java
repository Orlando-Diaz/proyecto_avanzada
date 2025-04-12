package co.edu.uniquindio.proyecto.modelo.documentos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("categoria")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {
    @Id
    private ObjectId id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String nombre;

    @Builder
    public Categoria(String nombre, ObjectId id) {
        this.nombre = nombre;
        this.id = id; // Genera el ID como String
    }
}
