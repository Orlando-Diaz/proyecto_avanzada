package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.dto.LoginDTO;
import co.edu.uniquindio.proyecto.dto.TokenDTO;
import co.edu.uniquindio.proyecto.excepciones.CredencialesInvalidasException;
import co.edu.uniquindio.proyecto.excepciones.EstadoCuentaInvalidoException;
import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.servicios.Impl.AuthServicioImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthServicioImplTest {

    @Autowired
    private AuthServicioImpl authServicio;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        usuarioRepo.deleteAll();
    }

    // Test 1: Login exitoso con credenciales correctas y cuenta activa
    @Test
    public void loginExitoso() throws Exception {
        // Arrange
        Usuario usuario = crearUsuarioActivo("test@uniquindio.edu.co", "password123");

        LoginDTO loginDTO = new LoginDTO("test@uniquindio.edu.co", "password123");

        // Act
        TokenDTO resultado = authServicio.login(loginDTO);

        // Assert
        assertNotNull(resultado.token());
        assertTrue(resultado.token().length() > 20); // Verifica que el token no esté vacío
    }

    // Test 2: Fallo por correo incorrecto
    @Test
    public void loginCorreoIncorrecto() {
        LoginDTO loginDTO = new LoginDTO("noexiste@uniquindio.edu.co", "password123");

        assertThrows(Exception.class, () -> authServicio.login(loginDTO));
    }

    // Test 3: Fallo por contraseña incorrecta
    @Test
    public void loginPasswordIncorrecta() {
        Usuario usuario = crearUsuarioActivo("test@uniquindio.edu.co", "password123");
        LoginDTO loginDTO = new LoginDTO("test@uniquindio.edu.co", "passwordErronea");

        assertThrows(CredencialesInvalidasException.class, () -> authServicio.login(loginDTO));
    }

    // Test 4: Fallo por cuenta inactiva
    @Test
    public void loginCuentaInactiva() {
        Usuario usuario = new Usuario();
        usuario.setEmail("inactivo@uniquindio.edu.co");
        usuario.setPassword(passwordEncoder.encode("password123"));
        usuario.setEstado(EstadoUsuario.INACTIVO);
        usuarioRepo.save(usuario);

        LoginDTO loginDTO = new LoginDTO("inactivo@uniquindio.edu.co", "password123");

        assertThrows(EstadoCuentaInvalidoException.class, () -> authServicio.login(loginDTO));
    }

    // Método auxiliar para crear usuarios de prueba
    private Usuario crearUsuarioActivo(String email, String password) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setEstado(EstadoUsuario.ACTIVO);
        return usuarioRepo.save(usuario);
    }
}