package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.LoginDTO;
import co.edu.uniquindio.proyecto.dto.TokenDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.seguridad.JWTUtils;
import co.edu.uniquindio.proyecto.servicios.interfaces.AuthServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServicioImpl implements AuthServicio {

    private final UsuarioRepo usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;

    @Override
    public TokenDTO login(LoginDTO loginDTO) throws Exception {
        // 1. Buscar usuario por email
        Usuario usuario = usuarioRepo.findByEmail(loginDTO.email())
                .orElseThrow(() -> new Exception("Usuario no encontrado"));

        /* 2. Verificar que la cuenta esté activa
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new Exception("Cuenta no activada. Verifica tu correo.");
        }
        */


        // 3. Generar token
        String token = jwtUtils.generateToken(
                usuario.getId().toString(),
                Map.of(
                        "email", usuario.getEmail(),
                        "rol", "ROLE_" + usuario.getRol().name()
                )
        );

        return new TokenDTO(token);
    }
}