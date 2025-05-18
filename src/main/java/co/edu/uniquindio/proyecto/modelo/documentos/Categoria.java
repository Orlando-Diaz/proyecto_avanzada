// Modelo Categoria corregido

package co.edu.uniquindio.proyecto.modelo.documentos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("categoria") // Mantiene el nombre de la colección tal como está
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {
    @Id
    private ObjectId id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String nombre;

    // Método de ayuda para crear una nueva categoría con ID generado
    public static Categoria crearNueva(String nombre) {
        return Categoria.builder()
                .id(new ObjectId())
                .nombre(nombre)
                .build();
    }
}