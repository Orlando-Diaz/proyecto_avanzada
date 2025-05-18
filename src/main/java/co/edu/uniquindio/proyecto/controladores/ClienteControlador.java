package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.*;
import co.edu.uniquindio.proyecto.servicios.interfaces.ClienteServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Gestión de Cliente",
        description = "Operaciones para que el cliente gestione su propia información"
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/clientes")
public class ClienteControlador {

    private final ClienteServicio clienteServicio;

    @Operation(
            summary = "Obtener perfil del cliente autenticado",
            description = "Permite al cliente obtener su propia información de perfil",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Información del perfil obtenida correctamente"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
            }
    )
    @GetMapping("/perfil")
    public ResponseEntity<MensajeDTO<UsuarioDTO>> obtenerPerfil(
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        // Se obtiene el email del token JWT del usuario autenticado
        String email = userDetails.getUsername();
        System.out.println(email);
        UsuarioDTO info = clienteServicio.obtenerPorEmail(email);
        return ResponseEntity.ok(new MensajeDTO<>(false, info));
    }

    @Operation(
            summary = "Actualizar perfil del cliente",
            description = "Permite al cliente actualizar su propia información de perfil",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
            }
    )
    @PutMapping("/perfil")
    public ResponseEntity<MensajeDTO<String>> actualizarPerfil(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EditarUsuarioDTO editarUsuarioDTO) throws Exception {
        // Se obtiene el email del token JWT del usuario autenticado
        String email = userDetails.getUsername();
        clienteServicio.editarPerfil(email, editarUsuarioDTO);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Perfil actualizado correctamente"));
    }

    @Operation(
            summary = "Eliminar cuenta de cliente",
            description = "Permite al cliente eliminar su propia cuenta (baja lógica)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cuenta eliminada correctamente"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
            }
    )
    @DeleteMapping("/cuenta")
    public ResponseEntity<MensajeDTO<String>> eliminarCuenta(
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        // Se obtiene el email del token JWT del usuario autenticado
        String email = userDetails.getUsername();
        clienteServicio.eliminarCuenta(email);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Cuenta eliminada correctamente"));
    }

    @Operation(
            summary = "Cambiar contraseña de cliente",
            description = "Permite al cliente cambiar su contraseña proporcionando la contraseña actual",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente"),
                    @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
            }
    )
    @PutMapping("/cambiar-password")
    public ResponseEntity<MensajeDTO<String>> cambiarPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CambiarPasswordClienteDTO cambiarPasswordClienteDTO) throws Exception {
        // Se obtiene el email del token JWT del usuario autenticado
        String email = userDetails.getUsername();
        clienteServicio.cambiarPassword(email, cambiarPasswordClienteDTO);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Contraseña actualizada correctamente"));
    }
}