package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.ReporteDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.ReporteServicio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteServicioImpl implements ReporteServicio {
    @Override
    public List<ReporteDTO> listarTodos(String nombre, String ciudad) {
        return List.of();
    }
}
