package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.CrearUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.EditarUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.dto.UsuarioDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.UsuarioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Gestión de Usuarios",
        description = "Operaciones CRUD para la gestión de usuarios registrados en el sistema"
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/usuarios")
public class Usuariocontrolador {

    private final UsuarioServicio usuarioServicio;

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea un nuevo usuario en el sistema con los datos básicos requeridos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                    @ApiResponse(responseCode = "409", description = "El correo electrónico ya está registrado")
            }
    )
    @PostMapping
    public ResponseEntity<MensajeDTO<String>> crear(
            @Valid @RequestBody CrearUsuarioDTO cuenta) throws Exception {
        usuarioServicio.crear(cuenta);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Su registro ha sido exitoso"));
    }

    @Operation(
            summary = "Obtener usuario por ID",
            description = "Recupera toda la información de un usuario específico usando su ID único",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "ID con formato inválido")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<MensajeDTO<UsuarioDTO>> obtener(
            @Parameter(description = "ID único del usuario")
            @PathVariable String id) throws Exception {
        UsuarioDTO info = usuarioServicio.obtener(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, info));
    }

    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina permanentemente un usuario del sistema (borrado lógico)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario eliminado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "403", description = "No autorizado para esta operación")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeDTO<String>> eliminar(
            @Parameter(description = "ID del usuario a eliminar")
            @PathVariable String id) throws Exception {
        usuarioServicio.eliminar(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Cuenta eliminada exitosamente"));
    }

    @Operation(
            summary = "Listado de usuarios",
            description = "Obtiene una lista paginada de usuarios con filtros opcionales",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida")
            }
    )
    @GetMapping
    public ResponseEntity<MensajeDTO<List<UsuarioDTO>>> listarTodos(
            @Parameter(description = "Filtro por nombre ")
            @RequestParam(required = false) String nombre,

            @Parameter(description = "Filtro por ciudad", example = "ARMENIA")
            @RequestParam(required = false) String ciudad,

            @Parameter(description = "Número de página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pagina) {

        List<UsuarioDTO> usuarios = usuarioServicio.listarTodos(nombre, ciudad, pagina);
        return ResponseEntity.ok(new MensajeDTO<>(false, usuarios));
    }

    @Operation(
            summary = "Actualizar usuario",
            description = "Modifica la información de un usuario existente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Datos actualizados"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<MensajeDTO<String>> editar(
            @Parameter(description = "ID del usuario a editar")
            @PathVariable String id,
            @Valid @RequestBody EditarUsuarioDTO cuenta
    ) throws Exception {
        usuarioServicio.editar(id, cuenta);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Cuenta editada exitosamente"));
    }
}