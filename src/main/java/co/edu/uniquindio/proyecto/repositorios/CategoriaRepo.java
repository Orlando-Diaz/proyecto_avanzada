package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.modelo.documentos.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoriaRepo extends MongoRepository<Categoria, String> {


    boolean existsByNombre(String nombre);
    Page<Categoria> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

}
