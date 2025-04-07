package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


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

}
