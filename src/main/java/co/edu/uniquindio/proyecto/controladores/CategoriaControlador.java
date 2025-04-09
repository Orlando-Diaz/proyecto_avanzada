package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.CategoriaDTO;
import co.edu.uniquindio.proyecto.dto.CrearCategoriaDTO;
import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.CategoriaServicio;
import io.swagger.v3.oas.annotations.Operation;
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
public class CategoriaControlador {

    private final CategoriaServicio categoriaServicio;

    @Operation(summary = "Crear una categoria")
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeDTO<String>> crearCategoria(@Valid @RequestBody CrearCategoriaDTO crearCategoriaDTO) throws Exception {
        categoriaServicio.crear(crearCategoriaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new MensajeDTO<>(false, "Categoría creada exitosamente")
        );
    }

    @Operation(summary = "Editar una categoria")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeDTO<String>> actualizarCategoria(
            @PathVariable String id,
            @Valid @RequestBody CategoriaDTO categoriaDTO) throws Exception {
        categoriaServicio.editar(id, categoriaDTO);
        return ResponseEntity.ok().body(
                new MensajeDTO<>(false, "Categoría actualizada exitosamente")
        );
    }

    @Operation(summary = "ELiminar una categoria")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MensajeDTO<String>> eliminarCategoria(@PathVariable String id) throws Exception {
        categoriaServicio.eliminar(id);
        return ResponseEntity.ok().body(
                new MensajeDTO<>(false, "Categoría eliminada exitosamente")
        );
    }

    @Operation(summary = "Obtener categoria")
    @GetMapping("/{id}")
    public ResponseEntity<MensajeDTO<CategoriaDTO>> obtenerCategoria(@PathVariable String id) throws Exception {
        return ResponseEntity.ok().body(
                new MensajeDTO<>(false, categoriaServicio.obtener(id))
        );
    }

    @Operation(summary = "Listar todas las categorias")
    @GetMapping
    public ResponseEntity<MensajeDTO<List<CategoriaDTO>>> listarCategorias(
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "0") int pagina) {
        return ResponseEntity.ok().body(
                new MensajeDTO<>(false, categoriaServicio.listarTodos(nombre, pagina))
        );
    }
}