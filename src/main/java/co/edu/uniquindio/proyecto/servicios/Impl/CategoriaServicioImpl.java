package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.CategoriaDTO;
import co.edu.uniquindio.proyecto.dto.CrearCategoriaDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.Categoria;
import co.edu.uniquindio.proyecto.repositorios.CategoriaRepo;
import co.edu.uniquindio.proyecto.servicios.interfaces.CategoriaServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServicioImpl implements CategoriaServicio {

    private final CategoriaRepo categoriaRepo;

    @Override
    public void crear(CrearCategoriaDTO crearCategoriaDTO) throws Exception {
        if(categoriaRepo.existsByNombre(crearCategoriaDTO.nombre())) { // Cambiado de getNombre() a nombre()
            throw new Exception("Ya existe una categoría con el nombre: " + crearCategoriaDTO.nombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(crearCategoriaDTO.nombre()); // Cambiado aquí también

        categoriaRepo.save(categoria);
    }

    @Override
    public void eliminar(String id) throws Exception {
        if(!categoriaRepo.existsById(id)) {
            throw new Exception("No se encontró la categoría con ID: " + id);
        }
        categoriaRepo.deleteById(id);
    }

    @Override
    public void editar(String id, CategoriaDTO categoriaDTO) throws Exception {
        Categoria categoria = categoriaRepo.findById(id)
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + id));

        if(!categoria.getNombre().equals(categoriaDTO.nombre()) &&
                categoriaRepo.existsByNombre(categoriaDTO.nombre())) {
            throw new Exception("Ya existe una categoría con el nombre: " + categoriaDTO.nombre());
        }

        categoria.setNombre(categoriaDTO.nombre());
        categoriaRepo.save(categoria);
    }

    @Override
    public CategoriaDTO obtener(String id) throws Exception {
        Categoria categoria = categoriaRepo.findById(id)
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + id));

        return convertirADTO(categoria);
    }

    @Override
    public List<CategoriaDTO> listarTodos(String nombre, int pagina) {
        PageRequest pageable = PageRequest.of(pagina, 10);
        Page<Categoria> categoriasPage;

        if(nombre != null && !nombre.isEmpty()) {
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
                categoria.getId(),
                categoria.getNombre()
        );
    }
}