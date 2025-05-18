package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.modelo.documentos.HistorialReporte;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HistorialReporteRepo extends MongoRepository<HistorialReporte, ObjectId> {

    //List<HistorialReporte> findByReporteId(String reporteId);
}
