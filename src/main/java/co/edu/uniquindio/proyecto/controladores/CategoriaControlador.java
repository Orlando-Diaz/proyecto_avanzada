// Controlador de Categoría con mejor manejo de errores

package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.ActualizarCategoriaDTO;
import co.edu.uniquindio.proyecto.dto.CategoriaDTO;
import co.edu.uniquindio.proyecto.dto.CrearCategoriaDTO;
import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.CategoriaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categoría", description = "Gestión de categorías")
public class CategoriaControlador {

    private final CategoriaServicio categoriaServicio;

    @Operation(summary = "Crear una nueva categoría")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado, solo para administradores")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeDTO<String>> crearCategoria(
            @Parameter(
                    name = "dto",
                    description = "Datos de la categoría a crear",
                    required = true
            )
            @Valid @RequestBody(required = true) CrearCategoriaDTO dto) {
        try {
            String idCategoria = categoriaServicio.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new MensajeDTO<>(false, "Categoría creada exitosamente con ID: " + idCategoria)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new MensajeDTO<>(true, "Error al crear categoría: " + e.getMessage())
            );
        }
    }

    @Operation(summary = "Actualizar una categoría existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado, solo para administradores")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeDTO<String>> actualizarCategoria(
            @Parameter(
                    name = "id",
                    description = "ID único de la categoría a actualizar",
                    required = true,
                    example = "64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "id") String id,
            @Parameter(
                    name = "actualizarCategoriaDTO",
                    description = "Datos actualizados de la categoría (solo nombre)",
                    required = true
            )
            @Valid @RequestBody(required = true) ActualizarCategoriaDTO actualizarCategoriaDTO) {
        try {
            System.out.println("Recibida solicitud para actualizar categoría con ID: " + id);
            System.out.println("Nuevo nombre: " + actualizarCategoriaDTO.nombre());

            categoriaServicio.editar(id, actualizarCategoriaDTO);

            System.out.println("Categoría actualizada exitosamente");
            return ResponseEntity.ok().body(
                    new MensajeDTO<>(false, "Categoría actualizada exitosamente")
            );
        } catch (Exception e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());

            // Capturar la traza completa del error
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();

            System.err.println("Traza completa del error:");
            System.err.println(stackTrace);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new MensajeDTO<>(true, "Error al actualizar categoría: " + e.getMessage())
            );
        }
    }

    @Operation(summary = "Eliminar una categoría por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado, solo para administradores")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeDTO<String>> eliminarCategoria(
            @Parameter(
                    name = "id",
                    description = "ID único de la categoría a eliminar",
                    required = true,
                    example = "64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "id") String id) {
        try {
            String mensaje = categoriaServicio.eliminar(id);
            return ResponseEntity.ok().body(new MensajeDTO<>(false, mensaje));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new MensajeDTO<>(true, "Error al eliminar categoría: " + e.getMessage())
            );
        }
    }

    @Operation(summary = "Obtener una categoría por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MensajeDTO<CategoriaDTO>> obtenerCategoria(
            @Parameter(
                    name = "id",
                    description = "ID único de la categoría a obtener",
                    required = true,
                    example = "64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "id") String id) {
        try {
            return ResponseEntity.ok().body(
                    new MensajeDTO<>(false, categoriaServicio.obtener(id))
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new MensajeDTO<>(true, null)
            );
        }
    }

    @Operation(summary = "Listar todas las categorías")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<MensajeDTO<List<CategoriaDTO>>> listarCategorias(
            @Parameter(
                    name = "nombre",
                    description = "Filtrar categorías por nombre (opcional)",
                    required = false,
                    example = "Medio Ambiente"
            )
            @RequestParam(name = "nombre", required = false) String nombre,
            @Parameter(
                    name = "pagina",
                    description = "Número de página para paginación (inicia en 0)",
                    required = false,
                    example = "0"
            )
            @RequestParam(name = "pagina", defaultValue = "0") int pagina) {
        try {
            return ResponseEntity.ok().body(
                    new MensajeDTO<>(false, categoriaServicio.listarTodos(nombre, pagina))
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new MensajeDTO<>(true, null)
            );
        }
    }
}