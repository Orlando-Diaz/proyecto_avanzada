package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.UsuarioDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServicioImpl implements UsuarioServicio {

    @Override
    public List<UsuarioDTO> listarTodos() {
        return UsuarioServicio.super.listarTodos();
    }

    @Override
    public List<UsuarioDTO> listarTodos(String nombre, String ciudad, int pagina) {
        return List.of();
    }
}

