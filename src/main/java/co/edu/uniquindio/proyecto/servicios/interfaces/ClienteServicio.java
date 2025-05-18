package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.CambiarPasswordClienteDTO;
import co.edu.uniquindio.proyecto.dto.EditarUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.UsuarioDTO;

public interface ClienteServicio {

    /**
     * Obtiene la información del cliente por su email
     * @param email Email del cliente autenticado
     * @return DTO con la información del cliente
     * @throws Exception si el cliente no existe
     */
    UsuarioDTO obtenerPorEmail(String email) throws Exception;

    /**
     * Actualiza el perfil del cliente autenticado
     * @param email Email del cliente autenticado
     * @param editarUsuarioDTO Datos actualizados del perfil
     * @throws Exception si el cliente no existe o los datos son inválidos
     */
    void editarPerfil(String email, EditarUsuarioDTO editarUsuarioDTO) throws Exception;

    /**
     * Elimina la cuenta del cliente (baja lógica)
     * @param email Email del cliente autenticado
     * @throws Exception si el cliente no existe
     */
    void eliminarCuenta(String email) throws Exception;

    /**
     * Cambia la contraseña del cliente autenticado
     * @param email Email del cliente autenticado
     * @param cambiarPasswordClienteDTO DTO con la contraseña actual y la nueva contraseña
     * @throws Exception si el cliente no existe, la contraseña actual es incorrecta, o la nueva contraseña es inválida
     */
    void cambiarPassword(String email, CambiarPasswordClienteDTO cambiarPasswordClienteDTO) throws Exception;
}