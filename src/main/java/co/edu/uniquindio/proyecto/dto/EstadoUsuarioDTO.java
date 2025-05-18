package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EstadoUsuarioDTO(@NotNull EstadoUsuario nuevoEstado) {
}

