package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.*;
import co.edu.uniquindio.proyecto.excepciones.EstadoReporteInvalidoException;
import co.edu.uniquindio.proyecto.modelo.documentos.HistorialReporte;
import co.edu.uniquindio.proyecto.modelo.documentos.Reporte;
import org.springframework.security.core.Authentication;
import co.edu.uniquindio.proyecto.repositorios.ReporteRepo;
import co.edu.uniquindio.proyecto.servicios.interfaces.ReporteServicio;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Reportes", description = "Gestión de reportes")
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteControlador {

    private final ReporteServicio reporteServicio;
    private final ReporteRepo reporteRepo;

    // --- Operaciones CRUD de Reporte ---

    @Operation(summary = "Crear un reporte")
    @PostMapping
    public ResponseEntity<MensajeDTO<String>> crearReporte(
            @Parameter(
                    name = "crearReporteDTO",
                    description = "Datos para la creación del reporte",
                    required = true
            )
            @Valid @RequestBody(required = true) CrearReporteDTO crearReporteDTO) throws Exception {
        reporteServicio.crearReporte(crearReporteDTO);
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "Reporte creado exitosamente"));
    }

    @Operation(summary = "Editar un reporte")
    @PutMapping("/{id}")
    public ResponseEntity<MensajeDTO<String>> editarReporte(
            @Parameter(name = "id", description = "ID único del reporte a editar", required = true)
            @PathVariable(name = "id") String id,
            @Valid @RequestBody(required = true) EditarReporteDTO editarReporteDTO
    ) {
        try {
            // Obtener el email del usuario desde el contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String emailUsuario = authentication.getName(); // Esto ahora es el email del usuario

            System.out.println("Editando reporte: " + id);
            System.out.println("Email del usuario autenticado: " + emailUsuario);
            System.out.println("Roles: " + authentication.getAuthorities());

            // Verificar si el usuario es el propietario del reporte o un administrador
            boolean esPropietario = reporteServicio.verificarPropietarioReporte(id, emailUsuario);
            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

            System.out.println("¿Es propietario? " + esPropietario);
            System.out.println("¿Es admin? " + esAdmin);

            if (!esPropietario && !esAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MensajeDTO<>(true, "No tienes permisos para editar este reporte"));
            }

            reporteServicio.editarReporte(id, editarReporteDTO);
            return ResponseEntity.ok().body(new MensajeDTO<>(false, "Reporte actualizado exitosamente"));
        } catch (Exception e) {
            System.out.println("Error al editar reporte: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MensajeDTO<>(true, "Error al editar reporte: " + e.getMessage()));
        }
    }

    @Operation(summary = "Listar reportes del usuario actual")
    @GetMapping("/mis-reportes")
    public ResponseEntity<MensajeDTO<Object>> listarMisReportes() {
        try {
            // Obtener el email del usuario desde el contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String emailUsuario = authentication.getName(); // Esto ahora es el email del usuario

            System.out.println("Listando reportes para el usuario con email: " + emailUsuario);

            List<ReporteDTO> reportes = reporteServicio.listarReportesPorEmailUsuario(emailUsuario);
            System.out.println("Reportes encontrados: " + reportes.size());

            // Usando Object como tipo genérico nos permite devolver tanto List<ReporteDTO> como String
            return ResponseEntity.ok().body(new MensajeDTO<>(false, reportes));
        } catch (Exception e) {
            System.out.println("Error al listar reportes del usuario: " + e.getMessage());
            e.printStackTrace();

            // Ahora podemos devolver un mensaje de error como String
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeDTO<>(true, "Error al listar reportes: " + e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar un reporte")
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeDTO<String>> eliminarReporte(
            @Parameter(name = "id", description = "ID único del reporte a eliminar", required = true)
            @PathVariable(name = "id") String id
    ) {
        try {
            // Obtener el email del usuario desde el contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String emailUsuario = authentication.getName(); // Email del usuario actual

            System.out.println("Eliminando reporte: " + id);
            System.out.println("Email del usuario autenticado: " + emailUsuario);
            System.out.println("Roles: " + authentication.getAuthorities());

            // Verificar si el usuario es el propietario del reporte o un administrador
            boolean esPropietario = reporteServicio.verificarPropietarioReporte(id, emailUsuario);
            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

            System.out.println("¿Es propietario? " + esPropietario);
            System.out.println("¿Es admin? " + esAdmin);

            if (!esPropietario && !esAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MensajeDTO<>(true, "No tienes permisos para eliminar este reporte"));
            }

            reporteServicio.eliminarReporte(id);
            return ResponseEntity.ok().body(new MensajeDTO<>(false, "Reporte eliminado exitosamente"));
        } catch (Exception e) {
            System.out.println("Error al eliminar reporte: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MensajeDTO<>(true, "Error al eliminar reporte: " + e.getMessage()));
        }
    }

    @Operation(summary = "Obtener reporte por ID")
    @GetMapping("/{id}")
    public ResponseEntity<MensajeDTO<ReporteDTO>> obtenerReporte(
            @Parameter(
                    name = "id",
                    description = "ID único del reporte a obtener",
                    required = true,
                    example = "64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "id") String id) throws Exception {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, reporteServicio.obtenerReportes(id)));
    }

    @Operation(summary = "Listar todos los reportes")
    @GetMapping
    public ResponseEntity<MensajeDTO<List<ReporteDTO>>> listarReportes(
            @Parameter(
                    name = "nombre",
                    description = "Filtrar reportes por nombre",
                    required = false,
                    example = "Contaminación río"
            )
            @RequestParam(name = "nombre", required = false) String nombre,
            @Parameter(
                    name = "ciudad",
                    description = "Filtrar reportes por ciudad",
                    required = false,
                    example = "Armenia"
            )
            @RequestParam(name = "ciudad", required = false) String ciudad,
            @Parameter(
                    name = "categoria",
                    description = "Filtrar reportes por categoría",
                    required = false,
                    example = "Medio Ambiente"
            )
            @RequestParam(name = "categoria", required = false) String categoria) {
        return ResponseEntity.ok().body(
                new MensajeDTO<>(false, reporteServicio.listarTodos(nombre, ciudad, categoria))
        );
    }

    // --- Operaciones de Comentarios ---

    @Operation(summary = "Agregar Comentario")
    @PostMapping("/{idReporte}/comentarios")
    public ResponseEntity<MensajeDTO<String>> agregarComentario(
            @Parameter(
                    name = "idReporte",
                    description = "ID único del reporte al que se agrega el comentario",
                    required = true,
                    example = "64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "idReporte") String idReporte,
            @Parameter(
                    name = "comentarioDTO",
                    description = "Datos del comentario a agregar",
                    required = true
            )
            @Valid @RequestBody(required = true) ComentarioDTO comentarioDTO) throws Exception {

        String idComentario = reporteServicio.agregarComentario(idReporte, comentarioDTO);
        return ResponseEntity.ok().body(new MensajeDTO<>(false, idComentario));
    }

    @Operation(summary = "Listar Comentario")
    @GetMapping("/{idReporte}/comentarios")
    public ResponseEntity<MensajeDTO<List<ComentarioDTO>>> listarComentarios(
            @Parameter(
                    name = "idReporte",
                    description = "ID único del reporte del que se listarán los comentarios",
                    required = true,
                    example = "64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "idReporte") String idReporte) throws Exception {
        List<ComentarioDTO> comentarios = reporteServicio.listarComentarios(idReporte);
        return ResponseEntity.ok().body(new MensajeDTO<>(false, comentarios));
    }

    // --- Historial de Cambios de un Reporte ---

    @Operation(
            summary = "Obtener historial de cambios",
            description = "Muestra los cambios realizados en un reporte"
    )
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialReporteDTO>> obtenerHistorial(
            @Parameter(
                    name = "id",
                    description = "ID único del reporte del que se obtendrá el historial",
                    required = true,
                    example = "64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "id") String id) throws Exception {

        List<HistorialReporteDTO> historial = reporteServicio.obtenerHistorial(id);
        return ResponseEntity.ok(historial);
    }

    // --- Marcar Reporte como Importante ---


    @PostMapping("/{id}/importante")
    public ResponseEntity<RespuestaImportanciaDTO> marcarImportante(
            @Parameter(
                    name = "id",
                    description = "ID único del reporte a marcar como importante",
                    required = true,
                    example = "64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "id") String id) {

        try {
            // Servicio ahora devuelve el contador actualizado
            int nuevoContador = reporteServicio.marcarComoImportante(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new RespuestaImportanciaDTO(
                            "Reporte marcado como importante",
                            id,
                            nuevoContador
                    ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new RespuestaImportanciaDTO(
                            e.getMessage(),
                            id,
                            -1
                    ));
        }
    }

    @GetMapping("/ordenados-importancia")
    public ResponseEntity<List<ReporteDTO>> listarReportesPorImportancia() {
        try {
            List<ReporteDTO> reportes = reporteServicio.listarReportesOrdenadosPorImportancia();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(reportes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- Reporte Anónimo ---

    @Operation(summary = "Crear reporte anónimo")
    @PostMapping("/anonimos")
    public ResponseEntity<MensajeDTO<String>> crearReporteAnonimo(
            @Parameter(
                    name = "dto",
                    description = "Datos para la creación del reporte anónimo",
                    required = true
            )
            @Valid @RequestBody(required = true) CrearReporteAnonimoDTO dto) {
        try {
            reporteServicio.crearReporteAnonimo(dto);
            return ResponseEntity.ok()
                    .body(new MensajeDTO<>(false, "Reporte anónimo creado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MensajeDTO<>(true, e.getMessage()));
        }
    }


    // --- Rechazar un Reporte ---

    @Operation(summary = "Rechazar un reporte",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reporte rechazado"),
                    @ApiResponse(responseCode = "400", description = "Justificación inválida"),
                    @ApiResponse(responseCode = "404", description = "Reporte no encontrado")
            })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<MensajeDTO<String>> rechazarReporte(
            @Parameter(
                    name = "id",
                    description = "ID único del reporte a rechazar",
                    required = true,
                    example = "64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "id") String id,
            @Parameter(
                    name = "dto",
                    description = "Datos con la justificación del rechazo",
                    required = true
            )
            @Valid @RequestBody(required = true) RechazarReporteDTO dto) {
        try {
            reporteServicio.rechazarReporte(id, dto);
            return ResponseEntity.ok()
                    .body(new MensajeDTO<>(false, "Reporte rechazado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MensajeDTO<>(true, e.getMessage()));
        }
    }

    // --- Gestionar el Estado de un Reporte (Administrador) ---

    @Operation(
            summary = "Gestionar el estado de un reporte",
            description = "Permite a un administrador cambiar el estado de un reporte si no ha sido eliminado y si el nuevo estado es diferente al actual.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estado del reporte actualizado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Estado inválido o el reporte ya tiene ese estado"),
                    @ApiResponse(responseCode = "404", description = "Reporte no encontrado"),
            }
    )
    @PutMapping("/gestionar-estado-reporte-admin")
    public ResponseEntity<MensajeDTO<String>> gestionarEstadoReporte(
            @Parameter(
                    name = "dto",
                    description = "DTO con los datos necesarios para cambiar el estado de un reporte",
                    required = true
            )
            @RequestBody(required = true) GestionarEstadoReporteDTO dto
    ) throws Exception {

        reporteServicio.gestionarEstadoReporteAdministrador(dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Estado del reporte actualizado correctamente"));
    }

    /**
     * Permite a un cliente cambiar el estado de su propio reporte a RESUELTO o ELIMINADO.
     * Solo es posible si el estado actual no es RECHAZADO ni ELIMINADO.
     *
     * @param dto DTO con la información del cambio solicitado.
     * @return Respuesta con mensaje de éxito o error.
     */
    @Operation(
            summary = "Gestionar estado del reporte (Cliente)",
            description = "Permite al cliente marcar su reporte como RESUELTO o ELIMINADO, si el estado actual lo permite. "
                    + "No puede cambiar el estado si el reporte ya fue RECHAZADO o ELIMINADO. "
                    + "Tampoco puede asignar estado VERIFICADO o RECHAZADO."
    )
    @PutMapping("/cliente/gestionar-estado-reporte-usuario")
    public ResponseEntity<?> gestionarEstadoReporteCliente(
            @Parameter(
                    name = "dto",
                    description = "DTO con los datos necesarios para cambiar el estado del reporte (ID y nuevo estado)",
                    required = true
            )
            @RequestBody(required = true) GestionarEstadoReporteDTO dto) {
        try {
            reporteServicio.gestionarEstadoReporteCliente(dto);
            return ResponseEntity.ok().body(
                    Map.of("mensaje", "El estado del reporte ha sido actualizado correctamente.")
            );
        } catch (EstadoReporteInvalidoException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", true, "mensaje", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", true, "mensaje", "Error al gestionar el estado del reporte: " + e.getMessage())
            );
        }
    }
}