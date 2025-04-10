package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.EmailDTO;
import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.dto.NotificacionDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.Notificacion;
import co.edu.uniquindio.proyecto.servicios.interfaces.NotificacionServicio;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionControlador {

    private final NotificacionServicio notificacionServicio;

    @PostMapping
    public ResponseEntity<NotificacionDTO> crearNotificacion(@RequestBody NotificacionDTO notificacionDTO) {
        return ResponseEntity.ok(notificacionServicio.crearNotificacion(notificacionDTO));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<NotificacionDTO>> listarNotificacionesPorUsuario(@PathVariable String idUsuario) {
        return ResponseEntity.ok(notificacionServicio.listarNotificacionesPorUsuario(idUsuario));
    }

    @GetMapping("/no-leidas/usuario/{idUsuario}")
    public ResponseEntity<List<NotificacionDTO>> listarNotificacionesNoLeidas(@PathVariable String idUsuario) {
        return ResponseEntity.ok(notificacionServicio.listarNotificacionesNoLeidas(idUsuario));
    }

    @PutMapping("/marcar-leida/{idNotificacion}")
    public ResponseEntity<Void> marcarNotificacionComoLeida(@PathVariable String idNotificacion) {
        notificacionServicio.marcarNotificacionComoLeida(idNotificacion);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/enviar-correo")
    public ResponseEntity<Void> enviarCorreo(@RequestBody EmailDTO emailDTO) {
        notificacionServicio.enviarCorreoElectronico(emailDTO);
        return ResponseEntity.ok().build();
    }

    // WebSocket endpoints
    @MessageMapping("/notificacion")
    @SendTo("/topic/notificaciones")
    public NotificacionDTO procesarNotificacion(@Payload NotificacionDTO notificacion) {
        return notificacionServicio.crearNotificacion(notificacion);
    }
}
