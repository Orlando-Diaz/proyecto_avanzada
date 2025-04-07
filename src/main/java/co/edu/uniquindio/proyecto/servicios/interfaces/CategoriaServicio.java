package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.CategoriaDTO;
import co.edu.uniquindio.proyecto.dto.CrearCategoriaDTO;

import java.util.List;

public interface CategoriaServicio {


    void crear(CrearCategoriaDTO crearCategoriaDTO) throws Exception;
    void editar(String id, CategoriaDTO categoriaDTO) throws Exception;
    void eliminar(String id) throws Exception;
    CategoriaDTO obtener(String id) throws Exception;
    List<CategoriaDTO> listarTodos(String nombre, int pagina);

}
