package co.edu.uniquindio.proyecto.modelo.documentos;


import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("categorias")
public class Categorias {

    @Id
    private ObjectId id;
    private String nombre;
}
