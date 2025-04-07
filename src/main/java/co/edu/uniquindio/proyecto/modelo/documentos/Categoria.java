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
    private String id; // Cambiado de ObjectId a String

    @NotBlank
    @Size(min = 3, max = 50)
    private String nombre;

    @Builder
    public Categoria(String nombre) {
        this.nombre = nombre;
        this.id = new ObjectId().toString(); // Genera el ID como String
    }
}
