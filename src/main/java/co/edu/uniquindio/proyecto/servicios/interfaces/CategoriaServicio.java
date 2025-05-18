package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.ActualizarCategoriaDTO;
import co.edu.uniquindio.proyecto.dto.CategoriaDTO;
import co.edu.uniquindio.proyecto.dto.CrearCategoriaDTO;

import java.util.List;

public interface CategoriaServicio {
    /**
     * Crea una nueva categoría con ID generado automáticamente
     * @param crearCategoriaDTO Datos de la categoría a crear
     * @return ID de la categoría creada
     * @throws Exception Si ya existe una categoría con el mismo nombre
     */
    String crear(CrearCategoriaDTO crearCategoriaDTO) throws Exception;

    /**
     * Actualiza una categoría existente
     * @param id ID de la categoría a actualizar
     * @param actualizarCategoriaDTO Datos actualizados de la categoría (sin ID)
     * @throws Exception Si la categoría no existe o si ya existe otra con el mismo nombre
     */
    void editar(String id, ActualizarCategoriaDTO actualizarCategoriaDTO) throws Exception;

    /**
     * Elimina una categoría por su ID
     * @param id ID de la categoría a eliminar
     * @return Mensaje indicando el resultado de la operación
     * @throws Exception Si la categoría no existe
     */
    String eliminar(String id) throws Exception;

    /**
     * Obtiene una categoría por su ID
     * @param id ID de la categoría a obtener
     * @return DTO con los datos de la categoría
     * @throws Exception Si la categoría no existe
     */
    CategoriaDTO obtener(String id) throws Exception;

    /**
     * Lista todas las categorías, opcionalmente filtradas por nombre
     * @param nombre Filtro opcional por nombre
     * @param pagina Número de página para paginación
     * @return Lista de DTOs con los datos de las categorías
     */
    List<CategoriaDTO> listarTodos(String nombre, int pagina);
}