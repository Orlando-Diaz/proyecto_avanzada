package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.dto.NotificacionDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.NotificacionServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestión de notificaciones")
public class NotificacionControlador {

    private final NotificacionServicio notificacionServicio;

    @Operation(summary = "Listar notificaciones del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas exitosamente")
    @GetMapping
    public ResponseEntity<MensajeDTO<List<NotificacionDTO>>> listarNotificaciones() {
        try {
            // Obtener el ID del usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String idUsuario = authentication.getName();

            List<NotificacionDTO> notificaciones = notificacionServicio.listarNotificacionesPorUsuario(idUsuario);
            return ResponseEntity.ok().body(new MensajeDTO<>(false, notificaciones));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDTO<>(true, null));
        }
    }

    @Operation(summary = "Listar notificaciones no leídas del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Notificaciones no leídas obtenidas exitosamente")
    @GetMapping("/no-leidas")
    public ResponseEntity<MensajeDTO<List<NotificacionDTO>>> listarNotificacionesNoLeidas() {
        try {
            // Obtener el ID del usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String idUsuario = authentication.getName();

            List<NotificacionDTO> notificaciones = notificacionServicio.listarNotificacionesNoLeidas(idUsuario);
            return ResponseEntity.ok().body(new MensajeDTO<>(false, notificaciones));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDTO<>(true, null));
        }
    }

    @Operation(summary = "Marcar notificación como leída")
    @ApiResponse(responseCode = "200", description = "Notificación marcada como leída exitosamente")
    @PutMapping("/{id}/leer")
    public ResponseEntity<MensajeDTO<String>> marcarComoLeida(
            @Parameter(name = "id", description = "ID de la notificación a marcar como leída", required = true)
            @PathVariable String id) {
        try {
            notificacionServicio.marcarNotificacionComoLeida(id);
            return ResponseEntity.ok().body(new MensajeDTO<>(false, "Notificación marcada como leída"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDTO<>(true, "Error al marcar como leída: " + e.getMessage()));
        }
    }

    @Operation(summary = "Crear notificación (solo para administradores)")
    @ApiResponse(responseCode = "200", description = "Notificación creada exitosamente")
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeDTO<NotificacionDTO>> crearNotificacion(
            @RequestBody NotificacionDTO notificacionDTO) {
        try {
            NotificacionDTO notificacionCreada = notificacionServicio.crearNotificacion(notificacionDTO);
            return ResponseEntity.ok().body(new MensajeDTO<>(false, notificacionCreada));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeDTO<>(true, null));
        }
    }
}