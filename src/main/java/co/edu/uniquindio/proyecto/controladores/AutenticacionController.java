package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.LoginDTO;
import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.dto.TokenDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.AuthServicio;
import co.edu.uniquindio.proyecto.servicios.interfaces.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacionController {

    private final AuthServicio authServicio;
    private final UsuarioServicio usuarioServicio;

    @PostMapping("/login")
    public ResponseEntity<MensajeDTO<Object>> login(@RequestBody LoginDTO loginDTO) {
        try {
            return ResponseEntity.ok(
                    new MensajeDTO<>(false, authServicio.login(loginDTO))
            );
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(new MensajeDTO<>(true, e.getMessage()));
        }
    }

}
