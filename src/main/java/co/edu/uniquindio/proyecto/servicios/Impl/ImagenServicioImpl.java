package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.config.CloudinaryConfigProperties;
import co.edu.uniquindio.proyecto.dto.ImagenResponseDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.ImagenServicio;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ImagenServicioImpl implements ImagenServicio {

    private final Cloudinary cloudinary;

    public ImagenServicioImpl(CloudinaryConfigProperties cloudinaryConfig) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudinaryConfig.getCloudName());
        config.put("api_key", cloudinaryConfig.getApiKey());
        config.put("api_secret", cloudinaryConfig.getApiSecret());

        this.cloudinary = new Cloudinary(config);
    }

    @Override
    public ImagenResponseDTO subirImagen(MultipartFile imagen) throws Exception {
        try {
            // Validar el archivo
            validarImagen(imagen);

            // Convertir a File (requerido por Cloudinary)
            File file = convertir(imagen);

            // Generar un nombre único para la imagen
            String nombreUnico = UUID.randomUUID().toString();

            // Subir a Cloudinary con opciones adicionales
            Map resultado = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                    "folder", "reportes",
                    "public_id", nombreUnico,
                    "resource_type", "auto"
            ));

            // Eliminar el archivo temporal
            if (file.exists()) {
                file.delete();
            }

            log.info("Imagen subida exitosamente a Cloudinary: {}", resultado.get("public_id"));

            // Preparar y devolver la respuesta
            return buildResponseDTO(resultado, imagen);

        } catch (Exception e) {
            log.error("Error al subir imagen a Cloudinary", e);
            throw new Exception("Error al subir la imagen: " + e.getMessage());
        }
    }

    @Override
    public Map eliminarImagen(String idImagen) throws Exception {
        try {
            log.info("Eliminando imagen con ID: {}", idImagen);
            Map resultado = cloudinary.uploader().destroy(idImagen, ObjectUtils.emptyMap());
            log.info("Imagen eliminada: {}", resultado);
            return resultado;
        } catch (Exception e) {
            log.error("Error al eliminar imagen de Cloudinary: {}", idImagen, e);
            throw new Exception("Error al eliminar la imagen: " + e.getMessage());
        }
    }

    /**
     * Convierte un MultipartFile a File
     */
    private File convertir(MultipartFile imagen) throws IOException {
        File file = File.createTempFile(imagen.getOriginalFilename(), null);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(imagen.getBytes());
        fos.close();
        return file;
    }

    /**
     * Valida que el archivo sea una imagen válida y no exceda el tamaño máximo
     */
    private void validarImagen(MultipartFile archivo) throws Exception {
        // Verificar que no sea nulo
        if (archivo == null || archivo.isEmpty()) {
            throw new Exception("El archivo está vacío");
        }

        // Verificar el tamaño (máx 5MB)
        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new Exception("El archivo excede el tamaño máximo permitido (5MB)");
        }

        // Verificar el tipo
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new Exception("El archivo no es una imagen válida");
        }
    }

    /**
     * Construye el DTO de respuesta con la información de la imagen subida
     */
    private ImagenResponseDTO buildResponseDTO(Map cloudinaryResponse, MultipartFile archivo) {
        // Crear un nuevo Record con los datos
        return new ImagenResponseDTO(
                (String) cloudinaryResponse.get("url"),
                (String) cloudinaryResponse.get("public_id"),
                archivo.getOriginalFilename(),
                archivo.getSize(),
                (String) cloudinaryResponse.get("format")
        );
    }
}