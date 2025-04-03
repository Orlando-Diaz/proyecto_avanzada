package co.edu.uniquindio.proyecto.modelo.documentos;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document("ubicacion")
public class Ubicacion {

    private String latitud;
    private String longitud;

    @Builder
    public Ubicacion(String latitud, String longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

}
