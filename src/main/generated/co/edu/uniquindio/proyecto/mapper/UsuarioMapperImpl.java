package co.edu.uniquindio.proyecto.mapper;

import co.edu.uniquindio.proyecto.dto.CrearUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.EditarUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.UsuarioDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import co.edu.uniquindio.proyecto.modelo.enums.Rol;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-12T16:55:43-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.3 (Eclipse Adoptium)"
)
@Component
public class UsuarioMapperImpl implements UsuarioMapper {

    @Override
    public Usuario toDocument(CrearUsuarioDTO usuarioDTO) {
        if ( usuarioDTO == null ) {
            return null;
        }

        Usuario.UsuarioBuilder usuario = Usuario.builder();

        usuario.nombre( usuarioDTO.nombre() );
        usuario.ciudad( usuarioDTO.ciudad() );
        usuario.direccion( usuarioDTO.direccion() );
        usuario.email( usuarioDTO.email() );
        usuario.telefono( usuarioDTO.telefono() );
        usuario.password( usuarioDTO.password() );

        usuario.rol( Rol.CLIENTE );
        usuario.estado( EstadoUsuario.INACTIVO );
        usuario.fechaRegistro( java.time.LocalDateTime.now() );

        return usuario.build();
    }

    @Override
    public void toDocument(EditarUsuarioDTO editarUsuarioDTO, Usuario usuario) {
        if ( editarUsuarioDTO == null ) {
            return;
        }

        usuario.setNombre( editarUsuarioDTO.nombre() );
        usuario.setCiudad( editarUsuarioDTO.ciudad() );
        usuario.setDireccion( editarUsuarioDTO.direccion() );
        usuario.setTelefono( editarUsuarioDTO.telefono() );
    }

    @Override
    public UsuarioDTO toDTO(Usuario usuario) {
        if ( usuario == null ) {
            return null;
        }

        String id = null;
        String nombre = null;
        Ciudad ciudad = null;
        String direccion = null;
        String telefono = null;
        String email = null;

        id = map( usuario.getId() );
        nombre = usuario.getNombre();
        ciudad = usuario.getCiudad();
        direccion = usuario.getDireccion();
        telefono = usuario.getTelefono();
        email = usuario.getEmail();

        UsuarioDTO usuarioDTO = new UsuarioDTO( id, nombre, ciudad, direccion, telefono, email );

        return usuarioDTO;
    }
}
