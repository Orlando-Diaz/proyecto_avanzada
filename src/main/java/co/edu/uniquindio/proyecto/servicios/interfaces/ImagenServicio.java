package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.ImagenResponseDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * Interfaz para el servicio de gestión de imágenes en Cloudinary
 */
public interface ImagenServicio {

    /**
     * Sube una imagen a Cloudinary y devuelve información detallada
     * @param imagen Archivo de imagen a subir
     * @return DTO con información detallada de la imagen subida
     * @throws Exception Si ocurre algún error en la subida
     */
    ImagenResponseDTO subirImagen(MultipartFile imagen) throws Exception;

    /**
     * Elimina una imagen de Cloudinary por su ID público
     * @param idImagen ID público de la imagen en Cloudinary
     * @return Mapa con el resultado de la operación
     * @throws Exception Si ocurre algún error en la eliminación
     */
    Map eliminarImagen(String idImagen) throws Exception;
}