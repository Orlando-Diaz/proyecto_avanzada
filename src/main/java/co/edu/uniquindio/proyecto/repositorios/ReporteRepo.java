package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.modelo.documentos.Reporte;
import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteRepo extends MongoRepository<Reporte, ObjectId> {

    // Buscar reportes por ID de usuario
    List<Reporte> findByIdUsuario(ObjectId idUsuario);

    // Buscar reporte por título
    Optional<Reporte> findByTitulo(String titulo);

    // Buscar por ciudad (enum)
    List<Reporte> findByCiudad(Ciudad ciudad);

    List<Reporte> findByTituloContainingAndCiudad(String titulo, Ciudad ciudad);


}
