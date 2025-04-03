package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.CrearUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.EditarUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.UsuarioDTO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UsuarioServicio {

    void crear(CrearUsuarioDTO crearUsuarioDTO) throws Exception;
    void eliminar(String id) throws Exception;
    void editar(EditarUsuarioDTO editarUsuarioDTO) throws Exception;
    UsuarioDTO obtener(String id) throws Exception;
    List<UsuarioDTO> listarTodos(String nombre, String ciudad, int pagina);
}


