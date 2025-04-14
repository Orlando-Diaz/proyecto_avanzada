package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.*;
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
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea un nuevo usuario en el sistema con los datos básicos requeridos",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                    @ApiResponse(responseCode = "409", description = "El email electrónico ya está registrado")
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

            @Parameter(description = "Filtro por ciudad")
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

    @Operation(
            summary = "Cambiar estado de un usuario",
            description = "Permite actualizar el estado de un usuario (ACTIVO, INACTIVO, ELIMINADO)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estado cambiado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Estado inválido")
            }
    )
    @PutMapping("/{email}/cambiarEstadoUsuario")
    public ResponseEntity<MensajeDTO<String>> cambiarEstadoUsuario(
            @Parameter(description = "Correo del usuario")
            @PathVariable ("email") String email,
            @RequestBody EstadoUsuarioDTO estadoDTO) throws Exception {

        usuarioServicio.modificarEstadoCuentaUsuario(email, estadoDTO);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Estado actualizado correctamente"));
    }


    @Operation(
            summary = "Verificar el código de un usuario",
            description = "Verifica que el código ingresado por el usuario sea válido y no haya expirado. Si es correcto, activa la cuenta.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cuenta activada correctamente"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                    @ApiResponse(responseCode = "400", description = "Código inválido o expirado")
            }
    )
    @PutMapping("/{email}/verificarCodigoActivacionUsuario")
    public ResponseEntity<MensajeDTO<String>> verificarCodigoUsuario(
            @Parameter(description = "Correo del usuario")
            @PathVariable ("email") String email,
            @RequestBody CodigoDTO codigoDTO) throws Exception {

        usuarioServicio.verificarCodigoActivarUsuario(email, codigoDTO.codigo());
        return ResponseEntity.ok(new MensajeDTO<>(false, "Cuenta activada correctamente"));
    }

    @Operation(
            summary = "Solicitar código de recuperación de contraseña",
            description = "Envía un código al correo del usuario activo registrado para permitir la recuperación de contraseña",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Código enviado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Correo no registrado"),
                    @ApiResponse(responseCode = "400", description = "Usuario con estado inválido")
            }
    )
    @PostMapping("/recuperarContrasenia")
    public ResponseEntity<MensajeDTO<String>> recuperarContrasenia(
            @RequestBody RecuperarContraseniaDTO recuperarPasswordDTO) throws Exception {

        usuarioServicio.recuperarContrasenia(recuperarPasswordDTO);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Código enviado correctamente al correo"));
    }


    @Operation(
            summary = "Cambiar contraseña del usuario",
            description = "Valida el código enviado por correo y actualiza la contraseña del usuario si el código es válido",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Código incorrecto o expirado"),
                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
            }
    )
    @PutMapping("/cambiarContrasenia")
    public ResponseEntity<MensajeDTO<String>> cambiarContrasenia(
            @RequestBody CambiarPasswordDTO cambiarPasswordDTO) throws Exception {

        usuarioServicio.cambiarContrasenia(cambiarPasswordDTO);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Contraseña actualizada correctamente"));
    }



}