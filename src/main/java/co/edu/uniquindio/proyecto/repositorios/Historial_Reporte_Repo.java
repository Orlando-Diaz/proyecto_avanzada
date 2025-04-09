package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.modelo.documentos.HistorialReporte;
import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface Historial_Reporte_Repo extends MongoRepository<HistorialReporte, ObjectId> {

    List<HistorialReporte> findByReporteId(String reporteId);
}
