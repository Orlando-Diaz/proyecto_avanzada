package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.*;

import java.util.List;

public interface UsuarioServicio {

    void crear(CrearUsuarioDTO crearUsuarioDTO) throws Exception;

    void modificarEstadoCuentaUsuario(String idUsuario, EstadoUsuarioDTO estadoCuentaUsuario) throws Exception;

    void verificarCodigoActivarUsuario(String idUsuario, String codigo) throws Exception;

    void eliminar(String id) throws Exception;
    void editar(String id, EditarUsuarioDTO cuenta) throws Exception;
    UsuarioDTO obtener(String id) throws Exception;
    List<UsuarioDTO> listarTodos(String nombre, String ciudad, int pagina);

    void recuperarContrasenia(RecuperarContraseniaDTO recuperarContraseniaDTO) throws Exception;
    void cambiarContrasenia(CambiarPasswordDTO cambiarPasswordDTO) throws Exception;
}


