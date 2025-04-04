package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.CrearReporteDTO;
import co.edu.uniquindio.proyecto.dto.EditarReporteDTO;
import co.edu.uniquindio.proyecto.dto.ReporteDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ReporteServicio {

    void crearReporte(CrearReporteDTO crearReporteDTO) throws Exception;
    void editarReporte(String id, EditarReporteDTO editarReporteDTO) throws Exception;
    void eliminarReporte(String id) throws Exception;
    ReporteDTO obtenerReportes(String id) throws Exception;
    List<ReporteDTO> listarTodos();
    List<ReporteDTO> listarTodos(String nombre, String ciudad);
}
