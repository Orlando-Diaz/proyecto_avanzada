package co.edu.uniquindio.proyecto.controladores;

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

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categoría", description = "Generación de categorías")
public class CategoriaControlador {

    private final CategoriaServicio categoriaServicio;

    @Operation(summary = "Crear una nueva categoría")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado, solo para administradores")
    })
    @PostMapping("/crear/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeDTO<String>> crearCategoria(
            @Parameter(
                    name = "id",
                    description = "ID único para la nueva categoría",
                    required = true,
                    example = "cat_64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "id") String id,
            @Parameter(
                    name = "dto",
                    description = "Datos de la categoría a crear",
                    required = true
            )
            @Valid @RequestBody(required = true) CrearCategoriaDTO dto) throws Exception {
        categoriaServicio.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new MensajeDTO<>(false, "Categoría creada exitosamente")
        );
    }

    @Operation(summary = "Actualizar una categoría existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado, solo para administradores")
    })
    @PutMapping("/actualizar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeDTO<String>> actualizarCategoria(
            @Parameter(
                    name = "id",
                    description = "ID único de la categoría a actualizar",
                    required = true,
                    example = "cat_64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "id") String id,
            @Parameter(
                    name = "categoriaDTO",
                    description = "Datos actualizados de la categoría",
                    required = true
            )
            @Valid @RequestBody(required = true) CategoriaDTO categoriaDTO) throws Exception {
        categoriaServicio.editar(id, categoriaDTO);
        return ResponseEntity.ok().body(
                new MensajeDTO<>(false, "Categoría actualizada exitosamente")
        );
    }

    @Operation(summary = "Eliminar una categoría por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoría eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado, solo para administradores")
    })
    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarCategoria(
            @Parameter(
                    name = "id",
                    description = "ID único de la categoría a eliminar",
                    required = true,
                    example = "cat_64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "id") String id) throws Exception {
        categoriaServicio.eliminar(id);
        return ResponseEntity.noContent().build(); // 204 NO CONTENT
    }


    @Operation(summary = "Obtener una categoría por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/obtener/{id}")
    public ResponseEntity<MensajeDTO<CategoriaDTO>> obtenerCategoria(
            @Parameter(
                    name = "id",
                    description = "ID único de la categoría a obtener",
                    required = true,
                    example = "cat_64a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "id") String id) throws Exception {
        return ResponseEntity.ok().body(
                new MensajeDTO<>(false, categoriaServicio.obtener(id))
        );
    }

    @Operation(summary = "Listar todas las categorías")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente")
    })
    @GetMapping("/listar")
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
        return ResponseEntity.ok().body(
                new MensajeDTO<>(false, categoriaServicio.listarTodos(nombre, pagina))
        );
    }
}