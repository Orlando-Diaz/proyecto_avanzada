package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.EnviarCorreoDTO;
import co.edu.uniquindio.proyecto.dto.NotificacionDTO;

import java.util.List;

public interface NotificacionServicio {

    NotificacionDTO crearNotificacion(NotificacionDTO notificacionDTO);

    void enviarNotificacionPorWebSocket(NotificacionDTO notificacion);

    void enviarCorreoElectronico(EnviarCorreoDTO emailDTO);

    List<NotificacionDTO> listarNotificacionesPorUsuario(String idUsuario);

    void marcarNotificacionComoLeida(String idNotificacion);

    List<NotificacionDTO> listarNotificacionesNoLeidas(String idUsuario);
}
