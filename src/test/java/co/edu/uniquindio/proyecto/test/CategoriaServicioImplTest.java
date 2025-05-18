package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.dto.CategoriaDTO;
import co.edu.uniquindio.proyecto.dto.CrearCategoriaDTO;
import co.edu.uniquindio.proyecto.excepciones.ResourceAlreadyExistsException;
import co.edu.uniquindio.proyecto.excepciones.ResourceNotFoundException;
import co.edu.uniquindio.proyecto.modelo.documentos.Categoria;
import co.edu.uniquindio.proyecto.repositorios.CategoriaRepo;
import co.edu.uniquindio.proyecto.servicios.Impl.CategoriaServicioImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CategoriaServicioImplTest {

    @Autowired
    private CategoriaServicioImpl categoriaServicio;

    @Autowired
    private CategoriaRepo categoriaRepo;

    @BeforeEach
    public void setUp() {
        categoriaRepo.deleteAll();
    }

    // Test para verificar la creación exitosa de una categoría
    @Test
    public void crearCategoriaExitoso() throws Exception {
        CrearCategoriaDTO dto = new CrearCategoriaDTO("Seguridad");

        categoriaServicio.crear(dto);

        assertTrue(categoriaRepo.existsByNombre("Seguridad"));
    }

    // Test para verificar que no se pueden crear categorías duplicadas
    @Test
    public void crearCategoriaNombreRepetido() {
        categoriaRepo.save(new Categoria("Emergencias Médicas"));

        CrearCategoriaDTO dto = new CrearCategoriaDTO("Emergencias Médicas");

        assertThrows(ResourceAlreadyExistsException.class, () -> categoriaServicio.crear(dto));
    }

    // Test para eliminar una categoría existente
    @Test
    public void eliminarCategoriaExitoso() throws Exception {
        Categoria categoria = categoriaRepo.save(new Categoria("Infraestructura"));
        String id = categoria.getId().toString();

        categoriaServicio.eliminar(id);

        assertFalse(categoriaRepo.existsById(categoria.getId()));
    }

    // Test para verificar que no se puede eliminar una categoría inexistente
    @Test
    public void eliminarCategoriaNoExistente() {
        String idInexistente = new ObjectId().toString();

        assertThrows(ResourceNotFoundException.class, () -> categoriaServicio.eliminar(idInexistente));
    }

    // Test para actualizar una categoría existente
    @Test
    public void editarCategoriaExitoso() throws Exception {
        Categoria categoria = categoriaRepo.save(new Categoria("Mascotas"));
        String id = categoria.getId().toString();

        CategoriaDTO dto = new CategoriaDTO(id, "Mascotas Perdidas");
        categoriaServicio.editar(id, dto);

        assertEquals("Mascotas Perdidas", categoriaRepo.findById(categoria.getId()).get().getNombre());
    }

    // Test para verificar que no se puede actualizar a un nombre ya existente
    @Test
    public void editarCategoriaNombreRepetido() {
        categoriaRepo.save(new Categoria("Contaminación"));
        Categoria existente2 = categoriaRepo.save(new Categoria("Basuras"));

        CategoriaDTO dto = new CategoriaDTO(existente2.getId().toString(), "Contaminación");

        assertThrows(ResourceAlreadyExistsException.class, () -> categoriaServicio.editar(existente2.getId().toString(), dto));
    }

    // Test para obtener una categoría por ID
    @Test
    public void obtenerCategoriaExitoso() throws Exception {
        Categoria categoria = categoriaRepo.save(new Categoria("Comunidad"));
        String id = categoria.getId().toString();

        CategoriaDTO resultado = categoriaServicio.obtener(id);

        assertEquals(id, resultado.id());
        assertEquals("Comunidad", resultado.nombre());
    }

    // Test para listar todas las categorías sin filtro
    @Test
    public void listarTodosSinFiltro() {
        categoriaRepo.saveAll(List.of(
                new Categoria("Robos"),
                new Categoria("Accidentes"),
                new Categoria("Alumbrado Público")
        ));

        List<CategoriaDTO> resultado = categoriaServicio.listarTodos(null, 0);

        assertEquals(3, resultado.size());
    }

    // Test para listar categorías con filtro por nombre
    @Test
    public void listarTodosConFiltro() {
        categoriaRepo.saveAll(List.of(
                new Categoria("Robo de Bicicletas"),
                new Categoria("Robo a Mano Armada"),
                new Categoria("Accidente de Tránsito")
        ));

        List<CategoriaDTO> resultado = categoriaServicio.listarTodos("Robo", 0);

        assertEquals(2, resultado.size());
    }

    // Test para verificar la paginación
    @Test
    public void listarTodosPaginacion() {
        // Insertar 15 categorías de prueba
        for (int i = 0; i < 15; i++) {
            categoriaRepo.save(new Categoria("Categoría " + i));
        }

        // Primera página (10 elementos)
        List<CategoriaDTO> pagina0 = categoriaServicio.listarTodos(null, 0);
        // Segunda página (5 elementos)
        List<CategoriaDTO> pagina1 = categoriaServicio.listarTodos(null, 1);

        assertEquals(10, pagina0.size());
        assertEquals(5, pagina1.size());
    }
}