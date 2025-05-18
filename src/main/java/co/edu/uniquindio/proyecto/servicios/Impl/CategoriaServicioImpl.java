// Implementación corregida del servicio de categoría

package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.ActualizarCategoriaDTO;
import co.edu.uniquindio.proyecto.dto.CategoriaDTO;
import co.edu.uniquindio.proyecto.dto.CrearCategoriaDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.Categoria;
import co.edu.uniquindio.proyecto.repositorios.CategoriaRepo;
import co.edu.uniquindio.proyecto.servicios.interfaces.CategoriaServicio;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServicioImpl implements CategoriaServicio {

    private final CategoriaRepo categoriaRepo;

    @Override
    public String crear(CrearCategoriaDTO crearCategoriaDTO) throws Exception {
        // Verificar nombre único
        if (categoriaRepo.existsByNombre(crearCategoriaDTO.nombre())) {
            throw new Exception("Ya existe una categoría con el nombre: " + crearCategoriaDTO.nombre());
        }

        // Crear y guardar la categoría con ID autogenerado
        Categoria categoria = Categoria.builder()
                .id(new ObjectId())
                .nombre(crearCategoriaDTO.nombre())
                .build();

        Categoria guardada = categoriaRepo.save(categoria);
        return guardada.getId().toString();
    }

    @Override
    public void editar(String id, ActualizarCategoriaDTO actualizarCategoriaDTO) throws Exception {
        try {
            // Validar ID
            if (id == null || id.isBlank()) {
                throw new Exception("El ID de la categoría no puede estar vacío");
            }

            ObjectId objectId;
            try {
                objectId = new ObjectId(id);
            } catch (IllegalArgumentException e) {
                throw new Exception("ID de categoría inválido: " + id);
            }

            // Buscar la categoría
            Optional<Categoria> optCategoria = categoriaRepo.findById(objectId);
            if (optCategoria.isEmpty()) {
                throw new Exception("No se encontró la categoría con ID: " + id);
            }

            Categoria categoria = optCategoria.get();
            String nuevoNombre = actualizarCategoriaDTO.nombre();

            // Si el nombre no ha cambiado, no hay nada que hacer
            if (categoria.getNombre().equals(nuevoNombre)) {
                return;
            }

            // Verificar que el nuevo nombre no exista en otra categoría
            List<Categoria> categorias = categoriaRepo.findAll();
            for (Categoria c : categorias) {
                if (!c.getId().equals(objectId) && c.getNombre().equalsIgnoreCase(nuevoNombre)) {
                    throw new Exception("Ya existe otra categoría con el nombre: " + nuevoNombre);
                }
            }

            // Actualizar y guardar
            categoria.setNombre(nuevoNombre);
            categoriaRepo.save(categoria);

        } catch (Exception e) {
            System.err.println("Error al editar categoría: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar para que el controlador lo maneje
        }
    }

    @Override
    public String eliminar(String id) throws Exception {
        if (!ObjectId.isValid(id)) {
            throw new Exception("ID de categoría inválido");
        }

        ObjectId objectId = new ObjectId(id);
        Optional<Categoria> optCategoria = categoriaRepo.findById(objectId);

        if (optCategoria.isEmpty()) {
            throw new Exception("No se encontró la categoría con ID: " + id);
        }

        String nombreCategoria = optCategoria.get().getNombre();
        categoriaRepo.deleteById(objectId);

        return "Categoría '" + nombreCategoria + "' eliminada exitosamente";
    }

    @Override
    public CategoriaDTO obtener(String id) throws Exception {
        if (!ObjectId.isValid(id)) {
            throw new Exception("ID de categoría inválido");
        }

        ObjectId objectId = new ObjectId(id);
        Categoria categoria = categoriaRepo.findById(objectId)
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + id));

        return convertirADTO(categoria);
    }

    @Override
    public List<CategoriaDTO> listarTodos(String nombre, int pagina) {
        PageRequest pageable = PageRequest.of(pagina, 10);
        Page<Categoria> categoriasPage;

        if (nombre != null && !nombre.isEmpty()) {
            categoriasPage = categoriaRepo.findByNombreContainingIgnoreCase(nombre, pageable);
        } else {
            categoriasPage = categoriaRepo.findAll(pageable);
        }

        return categoriasPage.getContent().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private CategoriaDTO convertirADTO(Categoria categoria) {
        return new CategoriaDTO(
                categoria.getId().toString(),
                categoria.getNombre()
        );
    }
}