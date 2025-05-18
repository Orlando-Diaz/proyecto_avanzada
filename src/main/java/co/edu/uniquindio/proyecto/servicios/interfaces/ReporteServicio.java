// Cambios en la interfaz ReporteServicio.java

package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.*;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;

import java.time.LocalDate;
import java.util.List;

public interface ReporteServicio {

    void crearReporte(CrearReporteDTO crearReporteDTO) throws Exception;

    void editarReporte(String id, EditarReporteDTO editarReporteDTO) throws Exception;

    void eliminarReporte(String id) throws Exception;

    ReporteDTO obtenerReportes(String id) throws Exception;

    List<ReporteDTO> listarTodos();

    List<ReporteDTO> listarTodos(String nombre, String ciudad, String categoria);

    String agregarComentario(String idReporte, ComentarioDTO comentarioDTO) throws Exception;

    List<ComentarioDTO> listarComentarios(String idReporte) throws Exception;

    List<HistorialReporteDTO> obtenerHistorial(String idReporte) throws Exception;

    int marcarComoImportante(String idReporte) throws Exception;

    List<ReporteDTO> listarReportesOrdenadosPorImportancia();

    void gestionarEstadoReporteAdministrador(GestionarEstadoReporteDTO gestionEstadoReporteDTO) throws Exception;

    void gestionarEstadoReporteCliente(GestionarEstadoReporteDTO gestionEstadoReporteDTO) throws Exception;

    InformeCategoriaDTO generarInformePorCategoria(String categoria, LocalDate fechaInicio, LocalDate fechaFin);

    InformeGeograficoDTO generarInformePorUbicacion(Double latitud, Double longitud, Double radioKm, LocalDate fechaInicio, LocalDate fechaFin);

    List<ReporteDTO> listarReportesPorEstado(EstadoReporte estado);

    EstadisticasGeneralesDTO obtenerEstadisticasGenerales();

    byte[] generarInformePorCategoriaPDF(String categoria, LocalDate fechaInicio, LocalDate fechaFin) throws Exception;

    byte[] generarInformePorUbicacionPDF(Double latitud, Double longitud, Double radioKm, LocalDate fechaInicio, LocalDate fechaFin) throws Exception;

    void crearReporteAnonimo(CrearReporteAnonimoDTO dto) throws Exception;

    void rechazarReporte(String idReporte, RechazarReporteDTO dto) throws Exception;

    List<ReporteDTO> listarReportesPorUsuario(String idUsuario) throws Exception;

    // Nuevos métodos
    boolean verificarPropietarioReporte(String idReporte, String emailUsuario) throws Exception;

    List<ReporteDTO> listarReportesPorEmailUsuario(String emailUsuario) throws Exception;
}