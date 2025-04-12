package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.*;
import co.edu.uniquindio.proyecto.modelo.documentos.HistorialReporte;
import co.edu.uniquindio.proyecto.modelo.documentos.Reporte;
import co.edu.uniquindio.proyecto.repositorios.ReporteRepo;
import co.edu.uniquindio.proyecto.servicios.interfaces.ReporteServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
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

    @Operation(summary = "Crear un reporte")
    @PostMapping
    public ResponseEntity<MensajeDTO<String>> crearReporte(@Valid @RequestBody CrearReporteDTO crearReporteDTO) throws Exception {
        reporteServicio.crearReporte(crearReporteDTO);
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "Reporte creado exitosamente"));
    }

    @Operation(summary = "Editar un reporte")
    @PutMapping("/{id}")
    public ResponseEntity<MensajeDTO<String>> editarReporte(
            @PathVariable String id,
            @Valid @RequestBody EditarReporteDTO editarReporteDTO
    ) throws Exception {
        reporteServicio.editarReporte(id, editarReporteDTO); // Enviar ID y DTO
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "Reporte actualizado exitosamente"));
    }

    @Operation(summary = "Eliminar un reporte")
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeDTO<String>> eliminarReporte(@PathVariable String id) throws Exception {
        reporteServicio.eliminarReporte(id);
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "Reporte eliminado exitosamente"));
    }

    @Operation(summary = "Obtener reporte por ID")
    @GetMapping("/{id}")
    public ResponseEntity<MensajeDTO<ReporteDTO>> obtenerReporte(@PathVariable String id) throws Exception {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, reporteServicio.obtenerReportes(id)));
    }

    @Operation(summary = "Listar todos los reportes")
    @GetMapping
    public ResponseEntity<MensajeDTO<List<ReporteDTO>>> listarReportes(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String categoria) {
        return ResponseEntity.ok().body(
                new MensajeDTO<>(false, reporteServicio.listarTodos(nombre, ciudad, categoria))
        );
    }

    @Operation(summary = "Agregar Comentario")
    @PostMapping("/{idReporte}/comentarios")
    public ResponseEntity<MensajeDTO<String>> agregarComentario(
            @PathVariable String idReporte,
            @Valid @RequestBody ComentarioDTO comentarioDTO) throws Exception {

        String idComentario = reporteServicio.agregarComentario(idReporte, comentarioDTO);
        return ResponseEntity.ok().body(new MensajeDTO<>(false, idComentario));
    }

    @Operation(summary = "Listar Comentario")
    @GetMapping("/{idReporte}/comentarios")
    public ResponseEntity<MensajeDTO<List<ComentarioDTO>>> listarComentarios(
            @PathVariable String idReporte) throws Exception {
        List<ComentarioDTO> comentarios = reporteServicio.listarComentarios(idReporte);
        return ResponseEntity.ok().body(new MensajeDTO<>(false, comentarios));
    }

    /*
    HISTORIAL DE CAMBIOS DE UN REPORTE
     */

    @Operation(
            summary = "Obtener historial de cambios",
            description = "Muestra los cambios realizados en un reporte"
    )
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialReporteDTO>> obtenerHistorial(
            @PathVariable String id) throws Exception {

        List<HistorialReporteDTO> historial = reporteServicio.obtenerHistorial(id);
        return ResponseEntity.ok(historial);
    }


    @PostMapping("/{id}/importante")
    public ResponseEntity<RespuestaImportanciaDTO> marcarImportante(
            @PathVariable String id) {

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


    // CONTROLADOR DE CREAR UN REPORTE ANONIMO
    @Operation(summary = "Crear reporte anónimo")
    @PostMapping("/anonimos")
    public ResponseEntity<MensajeDTO<String>> crearReporteAnonimo(
            @Valid @RequestBody CrearReporteAnonimoDTO dto) {
        try {
            reporteServicio.crearReporteAnonimo(dto);
            return ResponseEntity.ok()
                    .body(new MensajeDTO<>(false, "Reporte anónimo creado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MensajeDTO<>(true, e.getMessage()));
        }
    }


    /*
    RECHAZAR UN REPORTE CON JUSTIFICAIÓN
     */
    @Operation(summary = "Rechazar un reporte",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reporte rechazado"),
                    @ApiResponse(responseCode = "400", description = "Justificación inválida"),
                    @ApiResponse(responseCode = "404", description = "Reporte no encontrado")
            })
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<MensajeDTO<String>> rechazarReporte(
            @PathVariable String id,
            @Valid @RequestBody RechazarReporteDTO dto) {
        try {
            reporteServicio.rechazarReporte(id, dto);
            return ResponseEntity.ok()
                    .body(new MensajeDTO<>(false, "Reporte rechazado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MensajeDTO<>(true, e.getMessage()));
        }
    }



}