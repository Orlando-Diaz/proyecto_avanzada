package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.CambiarPasswordClienteDTO;
import co.edu.uniquindio.proyecto.dto.EditarUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.UsuarioDTO;
import co.edu.uniquindio.proyecto.excepciones.CorreoInexistenteException;
import co.edu.uniquindio.proyecto.excepciones.EstadoCuentaInvalidoException;
import co.edu.uniquindio.proyecto.mapper.UsuarioMapper;
import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.servicios.interfaces.ClienteServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClienteServicioImpl implements ClienteServicio {

    private final UsuarioRepo usuarioRepo;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UsuarioDTO obtenerPorEmail(String email) throws Exception {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new CorreoInexistenteException("Cliente no encontrado"));

        // Verificar que el usuario sea un cliente activo
        if (!usuario.getEstado().equals(EstadoUsuario.ACTIVO)) {
            throw new EstadoCuentaInvalidoException("La cuenta no está activa");
        }

        return usuarioMapper.toDTO(usuario);
    }

    @Override
    public void editarPerfil(String email, EditarUsuarioDTO editarUsuarioDTO) throws Exception {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new CorreoInexistenteException("Cliente no encontrado"));

        // Verificar que el usuario sea un cliente activo
        if (!usuario.getEstado().equals(EstadoUsuario.ACTIVO)) {
            throw new EstadoCuentaInvalidoException("La cuenta no está activa");
        }

        // Actualizar campos
        usuario.setNombre(editarUsuarioDTO.nombre());
        usuario.setCiudad(editarUsuarioDTO.ciudad());
        usuario.setDireccion(editarUsuarioDTO.direccion());
        usuario.setTelefono(editarUsuarioDTO.telefono());

        usuarioRepo.save(usuario);
    }

    @Override
    public void eliminarCuenta(String email) throws Exception {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new CorreoInexistenteException("Cliente no encontrado"));

        // Verificar que el usuario sea un cliente activo
        if (usuario.getEstado().equals(EstadoUsuario.ELIMINADO)) {
            throw new EstadoCuentaInvalidoException("La cuenta ya ha sido eliminada");
        }

        // Realizar una baja lógica
        usuario.setEstado(EstadoUsuario.ELIMINADO);
        usuarioRepo.save(usuario);
    }

    @Override
    public void cambiarPassword(String email, CambiarPasswordClienteDTO cambiarPasswordClienteDTO) throws Exception {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new CorreoInexistenteException("Cliente no encontrado"));

        // Verificar que el usuario sea un cliente activo
        if (!usuario.getEstado().equals(EstadoUsuario.ACTIVO)) {
            throw new EstadoCuentaInvalidoException("La cuenta no está activa");
        }

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(cambiarPasswordClienteDTO.passwordActual(), usuario.getPassword())) {
            throw new Exception("La contraseña actual es incorrecta");
        }

        // Verificar que la nueva contraseña cumpla con requisitos mínimos
        if (cambiarPasswordClienteDTO.nuevaPassword().length() < 8) {
            throw new Exception("La nueva contraseña debe tener al menos 8 caracteres");
        }

        // Actualizar la contraseña
        usuario.setPassword(passwordEncoder.encode(cambiarPasswordClienteDTO.nuevaPassword()));
        usuarioRepo.save(usuario);
    }
}