package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.Notificacion;
import co.edu.uniquindio.proyecto.servicios.interfaces.NotificacionServicio;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionControlador {

    @Autowired
    private NotificacionServicio notificacionServicio;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Notificacion>> listarNotificacionesUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(notificacionServicio.listarNotificacionesUsuario(new ObjectId(usuarioId)));
    }

    @PutMapping("/marcar-leida/{notificacionId}")
    public ResponseEntity<Notificacion> marcarComoLeida(@PathVariable String notificacionId) {
        Notificacion notificacion = notificacionServicio.marcarNotificacionComoLeida(new ObjectId(notificacionId));
        if (notificacion != null) {
            return ResponseEntity.ok(notificacion);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{notificacionId}")
    public ResponseEntity<MensajeDTO> eliminarNotificacion(@PathVariable String notificacionId) {
        notificacionServicio.eliminarNotificacion(new ObjectId(notificacionId));
        return ResponseEntity.ok(new MensajeDTO(true, "Notificación eliminada correctamente"));
    }
}
