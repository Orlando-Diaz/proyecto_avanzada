package co.edu.uniquindio.proyecto.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Record para respuesta de subida de imágenes
 */
@Schema(description = "Información detallada de una imagen subida")
public record ImagenResponseDTO(
        @Schema(description = "URL pública de la imagen", example = "https://res.cloudinary.com/demo/image/upload/v1620123456/reportes/abc123.jpg")
        String url,

        @Schema(description = "ID público de la imagen en Cloudinary (para eliminar o referenciar)", example = "reportes/abc123")
        String id,

        @Schema(description = "Nombre original del archivo", example = "mi-foto.jpg")
        String nombreOriginal,

        @Schema(description = "Tamaño del archivo en bytes", example = "256000")
        Long tamano,

        @Schema(description = "Formato de la imagen (jpg, png, etc.)", example = "jpg")
        String formato
) {}