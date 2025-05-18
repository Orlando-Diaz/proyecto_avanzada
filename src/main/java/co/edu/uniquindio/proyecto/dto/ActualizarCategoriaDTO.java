// DTO simplificado para actualización de categoría

package co.edu.uniquindio.proyecto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para actualizar una categoría
 * Solo contiene el nombre, ya que el ID se proporciona en la URL
 */
public record ActualizarCategoriaDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
        String nombre
) {}