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
    List<ReporteDTO> listarTodos(String nombre, String ciudad,String categoria);

    //requisito de agregar un comentario y listar comentarios
    String agregarComentario(String idReporte, ComentarioDTO comentarioDTO) throws Exception;
    List<ComentarioDTO> listarComentarios(String idReporte) throws Exception;

    //HISTORIAL REPORTE
    List<HistorialReporteDTO> obtenerHistorial(String idReporte) throws Exception;

    int marcarComoImportante(String idReporte) throws Exception;
    List<ReporteDTO> listarReportesOrdenadosPorImportancia();

//  Agregado 1:15 am 04-08-2025
    String editarEstadoReporte(String idReporte, String idUsuario,String motivo, EstadoReporteDTO estadoReporteDTO) throws Exception;

    InformeCategoriaDTO generarInformePorCategoria(String categoria, LocalDate fechaInicio, LocalDate fechaFin);

    InformeGeograficoDTO generarInformePorUbicacion(
            Double latitud, Double longitud, Double radioKm,
            LocalDate fechaInicio, LocalDate fechaFin);

    byte[] generarInformePorCategoriaPDF(String categoria, LocalDate fechaInicio, LocalDate fechaFin) throws Exception;

    byte[] generarInformePorUbicacionPDF(Double latitud, Double longitud, Double radioKm,
                                         LocalDate fechaInicio, LocalDate fechaFin) throws Exception;

    EstadisticasGeneralesDTO obtenerEstadisticasGenerales();

    List<ReporteDTO> listarReportesPorEstado(EstadoReporte estado);

    //PARA REPORTE ANONIMO
    void crearReporteAnonimo(CrearReporteAnonimoDTO crearReporteAnonimoDTO) throws Exception;

    //RECHAZAR UN REPORTE
    void rechazarReporte(String idReporte, RechazarReporteDTO dto) throws Exception;


}
