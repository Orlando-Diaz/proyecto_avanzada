package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.*;
import co.edu.uniquindio.proyecto.servicios.interfaces.ReporteServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reportes", description = "Gestión de reportes")
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteControlador {

    private final ReporteServicio reporteServicio; // Corregido el nombre

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
            @RequestParam(required = false) String ciudad) {
        return ResponseEntity.ok().body(
                new MensajeDTO<>(false, reporteServicio.listarTodos(nombre, ciudad))
        );
    }
}