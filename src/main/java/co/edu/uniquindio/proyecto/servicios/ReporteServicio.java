package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.CrearUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.EditarUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.ReporteDTO;
import co.edu.uniquindio.proyecto.dto.UsuarioDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
