package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.CambiarPasswordDTO;
import co.edu.uniquindio.proyecto.dto.CrearUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.EditarUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.UsuarioDTO;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UsuarioServicio {

    void crear(CrearUsuarioDTO crearUsuarioDTO) throws Exception;

    void modificarEstadoCuentaUsuario(String idUsuario, EstadoUsuario estadoCuentaUsuario) throws Exception;

    void verificarCodigoUsuario(String idUsuario, String codigo) throws Exception;

    void eliminar(String id) throws Exception;
    void editar(String id, EditarUsuarioDTO cuenta) throws Exception;
    UsuarioDTO obtener(String id) throws Exception;
    List<UsuarioDTO> listarTodos(String nombre, String ciudad, int pagina);


}


