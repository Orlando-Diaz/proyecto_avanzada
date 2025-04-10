package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.modelo.documentos.Notificacion;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepo extends MongoRepository<Notificacion, ObjectId> {

    List<Notificacion> findByIdUsuarioOrderByFechaDesc(ObjectId idUsuario);

    List<Notificacion> findByIdUsuarioAndLeidaOrderByFechaDesc(ObjectId idUsuario, boolean leida);



}
