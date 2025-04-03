package co.edu.uniquindio.proyecto.modelo.documentos;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document("codigoValidacion")
public class CodigoValidacion {

    private LocalDateTime fecha;
    private String codigo;

    @Builder
    public CodigoValidacion(String codigo, LocalDateTime fecha) {
        this.codigo = codigo;
        this.fecha = fecha;
    }
}
