package co.edu.uniquindio.proyecto.servicios;

import co.edu.uniquindio.proyecto.dto.CrearUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.EditarUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.dto.UsuarioDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Service
public interface usuarioServicio {

    public static void crear(CrearUsuarioDTO cuenta) throws Exception{
    }

    public static void editar(EditarUsuarioDTO cuenta) throws Exception{
    }

    public static void eliminar(String id) throws Exception{
    }

    public static UsuarioDTO obtener(String id) throws Exception{
        return null;
    }

    public default List<UsuarioDTO> listarTodos(){
        return List.of();
    }

    List<UsuarioDTO> listarTodos(String nombre, String ciudad);
}


