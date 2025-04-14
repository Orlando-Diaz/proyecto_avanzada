package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.*;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import co.edu.uniquindio.proyecto.servicios.interfaces.ReporteServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para la generación de informes y estadísticas relacionados con los reportes.
 * Permite generar informes en distintos formatos, así como consultar estadísticas generales.
 */
@Tag(name = "Informes", description = "Generación de informes y estadísticas")
@RestController
@RequestMapping("/api/informes")
@RequiredArgsConstructor
public class InformeControlador {

    private final ReporteServicio reporteServicio;

    /**
     * Genera un informe con estadísticas de reportes filtrados por categoría y rango de fechas.
     *
     * @param categoria El nombre de la categoría de los reportes (opcional).
     * @param fechaInicio Fecha de inicio del rango (opcional).
     * @param fechaFin Fecha de fin del rango (opcional).
     * @return ResponseEntity con un objeto MensajeDTO que contiene el informe.
     */
    @Operation(
            summary = "Generar informe por categoría",
            description = "Genera un informe con estadísticas de reportes filtrados por categoría y rango de fechas"
    )
    @GetMapping("/categoria")
    public ResponseEntity<MensajeDTO<InformeCategoriaDTO>> obtenerInformePorCategoria(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        InformeCategoriaDTO informe = reporteServicio.generarInformePorCategoria(categoria, fechaInicio, fechaFin);
        return ResponseEntity.ok().body(new MensajeDTO<>(false, informe));
    }

    /**
     * Genera un informe con reportes cercanos a una ubicación específica dentro de un radio dado.
     *
     * @param latitud Latitud de la ubicación.
     * @param longitud Longitud de la ubicación.
     * @param radioKm Radio de búsqueda en kilómetros.
     * @param fechaInicio Fecha de inicio del rango (opcional).
     * @param fechaFin Fecha de fin del rango (opcional).
     * @return ResponseEntity con un objeto MensajeDTO que contiene el informe geográfico.
     */
    @Operation(
            summary = "Generar informe por ubicación geográfica",
            description = "Genera un informe con reportes cercanos a una ubicación específica dentro de un radio dado"
    )
    @GetMapping("/geografico")
    public ResponseEntity<MensajeDTO<InformeGeograficoDTO>> obtenerInformePorUbicacion(
            @RequestParam Double latitud,
            @RequestParam Double longitud,
            @RequestParam Double radioKm,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        InformeGeograficoDTO informe = reporteServicio.generarInformePorUbicacion(
                latitud, longitud, radioKm, fechaInicio, fechaFin);
        return ResponseEntity.ok().body(new MensajeDTO<>(false, informe));
    }

    /**
     * Genera un documento PDF con estadísticas de reportes filtrados por categoría y rango de fechas.
     *
     * @param categoria El nombre de la categoría de los reportes (opcional).
     * @param fechaInicio Fecha de inicio del rango (opcional).
     * @param fechaFin Fecha de fin del rango (opcional).
     * @return ResponseEntity con el documento PDF de los informes.
     * @throws Exception En caso de error al generar el PDF.
     */
    @Operation(
            summary = "Generar informe por categoría en PDF",
            description = "Genera un documento PDF con estadísticas de reportes filtrados por categoría y rango de fechas"
    )
    @GetMapping("/categoria/pdf")
    public ResponseEntity<byte[]> generarInformePorCategoriaPDF(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) throws Exception {

        byte[] pdfBytes = reporteServicio.generarInformePorCategoriaPDF(categoria, fechaInicio, fechaFin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "informe-categoria.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Genera un documento PDF con reportes cercanos a una ubicación específica dentro de un radio dado.
     *
     * @param latitud Latitud de la ubicación.
     * @param longitud Longitud de la ubicación.
     * @param radioKm Radio de búsqueda en kilómetros.
     * @param fechaInicio Fecha de inicio del rango (opcional).
     * @param fechaFin Fecha de fin del rango (opcional).
     * @return ResponseEntity con el documento PDF de los informes geográficos.
     * @throws Exception En caso de error al generar el PDF.
     */
    @Operation(
            summary = "Generar informe geográfico en PDF",
            description = "Genera un documento PDF con reportes cercanos a una ubicación específica"
    )
    @GetMapping("/geografico/pdf")
    public ResponseEntity<byte[]> generarInformePorUbicacionPDF(
            @RequestParam Double latitud,
            @RequestParam Double longitud,
            @RequestParam Double radioKm,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) throws Exception {

        byte[] pdfBytes = reporteServicio.generarInformePorUbicacionPDF(
                latitud, longitud, radioKm, fechaInicio, fechaFin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "informe-geografico.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }


    /**
     * Devuelve estadísticas generales sobre todos los reportes en el sistema.
     *
     * @return ResponseEntity con un objeto MensajeDTO que contiene las estadísticas generales.
     */
    @Operation(
            summary = "Obtener estadísticas generales",
            description = "Devuelve estadísticas generales sobre todos los reportes en el sistema"
    )
    @GetMapping("/estadisticas")
    public ResponseEntity<MensajeDTO<EstadisticasGeneralesDTO>> obtenerEstadisticasGenerales() {
        EstadisticasGeneralesDTO estadisticas = reporteServicio.obtenerEstadisticasGenerales();
        return ResponseEntity.ok().body(new MensajeDTO<>(false, estadisticas));
    }


    /**
     * Lista todos los reportes que se encuentran en un estado específico.
     *
     * @param estado El estado del reporte (e.g., verificado, rechazado).
     * @return ResponseEntity con un objeto MensajeDTO que contiene la lista de reportes en el estado dado.
     */
    @Operation(
            summary = "Obtener reportes por estado",
            description = "Lista todos los reportes que se encuentran en un estado específico"
    )
    @GetMapping("/estado/{estado}")
    public ResponseEntity<MensajeDTO<List<ReporteDTO>>> obtenerReportesPorEstado(
            @PathVariable EstadoReporte estado) {

        List<ReporteDTO> reportes = reporteServicio.listarReportesPorEstado(estado);
        return ResponseEntity.ok().body(new MensajeDTO<>(false, reportes));
    }
}