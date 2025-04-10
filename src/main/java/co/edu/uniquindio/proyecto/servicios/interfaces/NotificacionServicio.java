package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.EmailDTO;
import co.edu.uniquindio.proyecto.dto.NotificacionDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.Notificacion;
import org.bson.types.ObjectId;

import java.util.List;

public interface NotificacionServicio {

    NotificacionDTO crearNotificacion(NotificacionDTO notificacionDTO);

    void enviarNotificacionPorWebSocket(NotificacionDTO notificacion);

    void enviarCorreoElectronico(EmailDTO emailDTO);

    List<NotificacionDTO> listarNotificacionesPorUsuario(String idUsuario);

    void marcarNotificacionComoLeida(String idNotificacion);

    List<NotificacionDTO> listarNotificacionesNoLeidas(String idUsuario);
}
