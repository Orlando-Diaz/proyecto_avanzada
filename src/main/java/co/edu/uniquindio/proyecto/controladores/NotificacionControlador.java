package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.EmailDTO;
import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.dto.NotificacionDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.Notificacion;
import co.edu.uniquindio.proyecto.servicios.interfaces.NotificacionServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Gestión de Notificaciones",
        description = "Maneja toda la gestión de notificaciones de los usuarios"
)
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionControlador {

    private final NotificacionServicio notificacionServicio;

    @Operation(
            summary = "Registra una nueva notificación",
            description = "Crea una notificación teniendo en cuenta los registros básicos.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notificación creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
            }
    )
    @PostMapping
    public ResponseEntity<NotificacionDTO> crearNotificacion(@RequestBody NotificacionDTO notificacionDTO) {
        return ResponseEntity.ok(notificacionServicio.crearNotificacion(notificacionDTO));
    }

    @Operation(
            summary = "Obtiene todas las notificaciones de un usuario",
            description = "Lista todas las notificaciones asociadas al ID del usuario proporcionado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida exitosamente"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            }
    )
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<NotificacionDTO>> listarNotificacionesPorUsuario(@PathVariable String idUsuario) {
        return ResponseEntity.ok(notificacionServicio.listarNotificacionesPorUsuario(idUsuario));
    }

    @Operation(
            summary = "Obtiene todas las notificaciones no leídas de un usuario",
            description = "Lista únicamente las notificaciones no leídas asociadas al ID del usuario.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de notificaciones no leídas obtenida exitosamente"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            }
    )
    @GetMapping("/no-leidas/usuario/{idUsuario}")
    public ResponseEntity<List<NotificacionDTO>> listarNotificacionesNoLeidas(@PathVariable String idUsuario) {
        return ResponseEntity.ok(notificacionServicio.listarNotificacionesNoLeidas(idUsuario));
    }

    @Operation(
            summary = "Marca una notificación como leída",
            description = "Actualiza el estado de la notificación a 'leída' utilizando su ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notificación marcada como leída exitosamente"),
                    @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
            }
    )
    @PutMapping("/marcar-leida/{idNotificacion}")
    public ResponseEntity<Void> marcarNotificacionComoLeida(@PathVariable String idNotificacion) {
        notificacionServicio.marcarNotificacionComoLeida(idNotificacion);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Envía una notificación por correo electrónico",
            description = "Envía un correo electrónico con los datos suministrados en el cuerpo de la solicitud.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Correo electrónico enviado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos del correo inválidos")
            }
    )
    @PostMapping("/enviar-correo")
    public ResponseEntity<Void> enviarCorreo(@RequestBody EmailDTO emailDTO) {
        notificacionServicio.enviarCorreoElectronico(emailDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Envía una notificación en tiempo real",
            description = "Procesa y envía una notificación a través de WebSocket para ser recibida en tiempo real por los clientes suscritos.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notificación enviada vía WebSocket exitosamente")
            }
    )
    @MessageMapping("/notificacion")
    @SendTo("/topic/notificaciones")
    public NotificacionDTO procesarNotificacion(@Payload NotificacionDTO notificacion) {
        return notificacionServicio.crearNotificacion(notificacion);
    }
}