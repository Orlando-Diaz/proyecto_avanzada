package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.LoginDTO;
import co.edu.uniquindio.proyecto.dto.TokenDTO;
import co.edu.uniquindio.proyecto.excepciones.CredencialesInvalidasException;
import co.edu.uniquindio.proyecto.excepciones.EstadoCuentaInvalidoException;
import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.seguridad.JWTUtils;
import co.edu.uniquindio.proyecto.servicios.interfaces.AuthServicio;
import co.edu.uniquindio.proyecto.servicios.interfaces.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServicioImpl implements AuthServicio {

    private final UsuarioRepo usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;

    @Autowired
    private final UsuarioServicioImpl usuarioServicio;

    @Override
    public TokenDTO login(LoginDTO loginDTO) throws Exception {
        // 1. Buscar usuario por email
        Usuario usuario = usuarioRepo.findByEmail(loginDTO.correo())
                .orElseThrow(() -> new Exception("ERROR: CORREO NO ENCONTRADO EN EL SISTEMA"));

        // 2. VERIFICAR SI EL CORREO O LA CONTRASENIA SEAN INCORRECTOS PARA LANZAR UNA EXCEPCION
        if (!passwordEncoder.matches(loginDTO.password(), usuario.getPassword())) {
            throw new CredencialesInvalidasException("ERROR: CREDENCIALES INCORRECTAS");
        }

        // 3.VERIFICAR CREDENCIALES CORRECTA Y EL ESTADO DE LA CUENTA
        if (usuario.getEmail().equals(loginDTO.correo())&&passwordEncoder.matches(loginDTO.password(), usuario.getPassword())){

            //SINO ESTADOCUENTA!=INACTIVO NO SE PIDE CODIGO PORQUE LA CUENTA YA FUE ACTIVADA
            if (usuario.getEstado().equals(EstadoUsuario.ACTIVO)){

                // 3. Generar token
                String token = jwtUtils.generateToken(
                        usuario.getId().toString(),
                        Map.of(
                                "email", usuario.getEmail(),
                                "rol", "ROLE_" + usuario.getRol().name()
                        )
                );
                return new TokenDTO(token);
            }{
                throw new EstadoCuentaInvalidoException("ERROR. ESTADO DE LA CUENTA INVALIDO");
            }
        }{
            throw new CredencialesInvalidasException("ERROR: CREDENCIALES INCORRECTAS PARA INICIAR SESION");
        }
    }
}