package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.dto.*;
import co.edu.uniquindio.proyecto.excepciones.*;
import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import co.edu.uniquindio.proyecto.modelo.enums.Rol;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.servicios.Impl.UsuarioServicioImpl;
import co.edu.uniquindio.proyecto.servicios.interfaces.EmailServicio;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServicioImplTest {

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private EmailServicio emailServicio;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServicioImpl usuarioServicio;

    private Usuario usuario;
    private CrearUsuarioDTO crearUsuarioDTO;
    private EditarUsuarioDTO editarUsuarioDTO;

    @BeforeEach
    void setUp() {
        crearUsuarioDTO = new CrearUsuarioDTO(
                "Juan Perez",
                "1234567890",
                Ciudad.ARMENIA,
                "Calle 123",
                "juan@example.com",
                "password123"
        );

        editarUsuarioDTO = new EditarUsuarioDTO(
                "Juan Perez Modificado",
                Ciudad.PEREIRA,
                "Calle 456",
                "0987654321"
        );

        usuario = Usuario.builder()
                .nombre("Juan Perez")
                .ciudad(Ciudad.ARMENIA)
                .direccion("Calle 123")
                .email("juan@example.com")
                .telefono("1234567890")
                .password("encodedPassword")
                .rol(Rol.CLIENTE)
                .estado(EstadoUsuario.INACTIVO)
                .fechaRegistro(LocalDateTime.now())
                .codigoValidacion("ABC123")
                .fechCodigoValidacion(LocalDateTime.now())
                .build();

        // Asignar ID después de construir
        usuario.setId(new ObjectId());
    }


    @Test
    void crear_CuandoEmailExiste_DeberiaLanzarExcepcion() {
        // Arrange
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(CorreoEnUsoException.class, () -> {
            usuarioServicio.crear(crearUsuarioDTO);
        });
    }
    @Test
    void modificarEstadoCuentaUsuario_CuandoUsuarioExisteYEstadoValido_DeberiaCambiarEstado() throws Exception {
        // Arrange
        EstadoUsuarioDTO estadoDTO = new EstadoUsuarioDTO(EstadoUsuario.ACTIVO);
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        // Act
        usuarioServicio.modificarEstadoCuentaUsuario("juan@example.com", estadoDTO);

        // Assert
        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
        verify(usuarioRepo, times(1)).save(usuario);
    }

    @Test
    void modificarEstadoCuentaUsuario_CuandoUsuarioNoExiste_DeberiaLanzarExcepcion() {
        // Arrange
        EstadoUsuarioDTO estadoDTO = new EstadoUsuarioDTO(EstadoUsuario.ACTIVO);
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CorreoInexistenteException.class, () -> {
            usuarioServicio.modificarEstadoCuentaUsuario("inexistente@example.com", estadoDTO);
        });
    }

    @Test
    void modificarEstadoCuentaUsuario_CuandoUsuarioEliminado_DeberiaLanzarExcepcion() {
        // Arrange
        usuario.setEstado(EstadoUsuario.ELIMINADO);
        EstadoUsuarioDTO estadoDTO = new EstadoUsuarioDTO(EstadoUsuario.ACTIVO);
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(Exception.class, () -> {
            usuarioServicio.modificarEstadoCuentaUsuario("juan@example.com", estadoDTO);
        });
    }@Test
    void verificarCodigoActivarUsuario_CuandoCodigoValido_DeberiaActivarCuenta() throws Exception {
        // Arrange
        usuario.setFechaCodigoValidacion(LocalDateTime.now().minusMinutes(5)); // Código generado hace 5 minutos
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        // Act
        usuarioServicio.verificarCodigoActivarUsuario("juan@example.com", "ABC123");

        // Assert
        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
        verify(usuarioRepo, times(1)).save(usuario);
    }

    @Test
    void verificarCodigoActivarUsuario_CuandoCodigoIncorrecto_DeberiaLanzarExcepcion() {
        // Arrange
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(CodigoVerificacionNoCoincideException.class, () -> {
            usuarioServicio.verificarCodigoActivarUsuario("juan@example.com", "CODIGO_INCORRECTO");
        });
    }

    @Test
    void verificarCodigoActivarUsuario_CuandoCodigoExpirado_DeberiaLanzarExcepcion() {
        // Arrange
        usuario.setFechaCodigoValidacion(LocalDateTime.now().minusMinutes(20)); // Código generado hace 20 minutos
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(CodigoExpiradoException.class, () -> {
            usuarioServicio.verificarCodigoActivarUsuario("juan@example.com", "ABC123");
        });
    }@Test
    void editar_CuandoUsuarioExiste_DeberiaActualizarDatos() throws Exception {
        // Arrange
        when(usuarioRepo.findById(any(ObjectId.class))).thenReturn(Optional.of(usuario));

        // Act
        usuarioServicio.editar(usuario.getId().toString(), editarUsuarioDTO);

        // Assert
        assertEquals("Juan Perez Modificado", usuario.getNombre());
        assertEquals(Ciudad.PEREIRA, usuario.getCiudad());
        assertEquals("Calle 456", usuario.getDireccion());
        assertEquals("0987654321", usuario.getTelefono());
        verify(usuarioRepo, times(1)).save(usuario);
    }

    @Test
    void editar_CuandoUsuarioNoExiste_DeberiaLanzarExcepcion() {
        // Arrange
        when(usuarioRepo.findById(any(ObjectId.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> {
            usuarioServicio.editar(new ObjectId().toString(), editarUsuarioDTO);
        });
    }

    @Test
    void recuperarContrasenia_CuandoUsuarioNoExiste_DeberiaLanzarExcepcion() {
        // Arrange
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CorreoInexistenteException.class, () -> {
            usuarioServicio.recuperarContrasenia(new RecuperarContraseniaDTO("inexistente@example.com"));
        });
    }

    @Test
    void recuperarContrasenia_CuandoUsuarioInactivo_DeberiaLanzarExcepcion() {
        // Arrange
        usuario.setEstado(EstadoUsuario.INACTIVO);
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(EstadoCuentaInvalidoException.class, () -> {
            usuarioServicio.recuperarContrasenia(new RecuperarContraseniaDTO("juan@example.com"));
        });
    }@Test
    void cambiarContrasenia_CuandoDatosValidos_DeberiaCambiarPassword() throws Exception {
        // Arrange
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setFechaCodigoValidacion(LocalDateTime.now().minusMinutes(5));
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(anyString())).thenReturn("nuevaPasswordEncoded");

        // Act
        usuarioServicio.cambiarContrasenia(new CambiarPasswordDTO(
                "juan@example.com",
                "ABC123",
                "nuevaPassword"
        ));

        // Assert
        assertEquals("nuevaPasswordEncoded", usuario.getPassword());
        assertNull(usuario.getCodigoValidacion());
        assertNull(usuario.getFechaCodigoValidacion());
        verify(usuarioRepo, times(1)).save(usuario);
    }

    @Test
    void cambiarContrasenia_CuandoCodigoIncorrecto_DeberiaLanzarExcepcion() {
        // Arrange
        usuario.setEstado(EstadoUsuario.ACTIVO);
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(CodigoVerificacionNoCoincideException.class, () -> {
            usuarioServicio.cambiarContrasenia(new CambiarPasswordDTO(
                    "juan@example.com",
                    "CODIGO_INCORRECTO",
                    "nuevaPassword"
            ));
        });
    }

    @Test
    void cambiarContrasenia_CuandoCodigoExpirado_DeberiaLanzarExcepcion() {
        // Arrange
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setFechaCodigoValidacion(LocalDateTime.now().minusMinutes(20));
        when(usuarioRepo.findByEmail(anyString())).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(CodigoExpiradoException.class, () -> {
            usuarioServicio.cambiarContrasenia(new CambiarPasswordDTO(
                    "juan@example.com",
                    "ABC123",
                    "nuevaPassword"
            ));
        });
    }@Test
    void generarCodigo_DeberiaGenerarCodigoDe6Caracteres() {
        // Act
        String codigo = usuarioServicio.generarCodigo();

        // Assert
        assertNotNull(codigo);
        assertEquals(6, codigo.length());
        assertTrue(codigo.matches("[A-Z0-9]+"));
    }
}