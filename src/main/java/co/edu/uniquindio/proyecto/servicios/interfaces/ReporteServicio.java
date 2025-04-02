package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.ReporteDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ReporteServicio {

    public static void crearReporte(ReporteDTO reporte) throws Exception{
    }

    public static void editarReporte(ReporteDTO Reporte) throws Exception{
    }

    public static void eliminarReporte(String id) throws Exception{
    }

    public static ReporteDTO obtenerReportes(String id) throws Exception{
        return null;
    }

    public static List<ReporteDTO> listarTodos(){
        return List.of();
    }

    List<ReporteDTO> listarTodos(String nombre, String ciudad);
}
