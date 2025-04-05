package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.modelo.documentos.Categoria;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CategoriaRepo extends MongoRepository<Categoria, ObjectId>{

    Optional<Categoria> findByNombre(String nombre);
}
