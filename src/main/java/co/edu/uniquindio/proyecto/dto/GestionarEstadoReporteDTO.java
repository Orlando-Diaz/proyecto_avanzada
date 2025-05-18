package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO que sirve para que el administrador gestione los estados de los reportes
 * @param idReporte
 * @param idUsuarioModifica
 * @param motivo
 * @param estado
 */
public record GestionarEstadoReporteDTO(@NotBlank @NotNull String idReporte,
                                        @NotBlank @NotNull String idUsuarioModifica,
                                        @NotBlank @NotNull String motivo,
                                        @NotBlank @NotNull EstadoReporte estado
) {
}
