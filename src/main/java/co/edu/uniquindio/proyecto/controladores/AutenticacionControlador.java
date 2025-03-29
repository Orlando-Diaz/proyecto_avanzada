package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.AutenticacionDTO;
import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.dto.TokenDTO;
import co.edu.uniquindio.proyecto.servicios.AutenticacionServicio;
import co.edu.uniquindio.proyecto.servicios.usuarioServicio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AutenticacionControlador {

    public AutenticacionServicio autenticacion;
    public usuarioServicio usuario;

}
