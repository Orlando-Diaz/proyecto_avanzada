// Controlador WebSocket para notificaciones en tiempo real

package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.NotificacionDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebSocketControlador {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envía un mensaje a todos los clientes suscritos a /topic/notifications
     * @param notificacion La notificación a enviar
     * @return La misma notificación (se enviará a todos los suscriptores)
     */
    @MessageMapping("/send-notification")
    @SendTo("/topic/notifications")
    public NotificacionDTO enviarNotificacionGeneral(NotificacionDTO notificacion) {
        return notificacion; // Spring automáticamente envía esto a /topic/notifications
    }

    /**
     * Envía un mensaje a un usuario específico
     * @param notificacion La notificación con el idUsuario del destinatario
     */
    @MessageMapping("/send-private-notification")
    public void enviarNotificacionPrivada(NotificacionDTO notificacion) {
        if (notificacion.idUsuario() != null && !notificacion.idUsuario().isBlank()) {
            // Enviar al usuario específico
            messagingTemplate.convertAndSendToUser(
                    notificacion.idUsuario(),
                    "/queue/private-notifications",
                    notificacion
            );
        }
    }

    /**
     * Permite que un cliente solicite sus notificaciones más recientes
     * @return Un mensaje de confirmación
     */
    @MessageMapping("/request-notifications")
    public String solicitarNotificaciones() {
        // Obtener el usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        // Aquí podrías cargar las notificaciones y enviarlas
        // (En este ejemplo simplemente enviamos un mensaje de confirmación)
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/private-notifications",
                new NotificacionDTO(
                        null,
                        "Notificaciones actualizadas",
                        null,
                        "SYSTEM",
                        false,
                        null,
                        userId,
                        "Actualización de notificaciones"
                )
        );

        return "Solicitud de notificaciones recibida";
    }
}