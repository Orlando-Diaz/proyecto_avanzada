package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.modelo.documentos.Notificacion;
import org.bson.types.ObjectId;

import java.util.List;

public interface NotificacionServicio {

    // Métodos para gestionar notificaciones
    Notificacion crearNotificacion(Notificacion notificacion);

    void enviarNotificacionFirebase(ObjectId usuarioId, String titulo, String mensaje, ObjectId reporteId);

    void enviarNotificacionEmail(String email, String asunto, String mensaje);

    List<Notificacion> listarNotificacionesUsuario(ObjectId usuarioId);

    Notificacion marcarNotificacionComoLeida(ObjectId notificacionId);

    void eliminarNotificacion(ObjectId notificacionId);
}
