package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.modelo.documentos.Reporte;
import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    List<Reporte> findAllByOrderByContadorImportanteDesc();

    // Para buscar por categoría
    List<Reporte> findByCategoriaAndFechaBetween(String categoria, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Para buscar por categoría sin filtro de fecha
    List<Reporte> findByCategoria(String categoria);

    // Para buscar todos en un rango de fechas
    List<Reporte> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Reporte> findByEstadoActual(EstadoReporte estado);

    long countByEstadoActual(EstadoReporte estado);

    // Para la búsqueda geoespacial (necesita anotación especial)
    @Query("{'ubicacion': {$near: {$geometry: {type: 'Point', coordinates: [?0, ?1]}, $maxDistance: ?2}}}")
    List<Reporte> findByUbicacionNear(double longitud, double latitud, double distanciaMetros);

    // Búsqueda geoespacial con filtro de fechas
    @Query("{'ubicacion': {$near: {$geometry: {type: 'Point', coordinates: [?0, ?1]}, $maxDistance: ?2}}, 'fecha': {$gte: ?3, $lte: ?4}}")
    List<Reporte> findByUbicacionNearAndFechaBetween(
            double longitud, double latitud, double distanciaMetros,
            LocalDateTime fechaInicio, LocalDateTime fechaFin);

}
