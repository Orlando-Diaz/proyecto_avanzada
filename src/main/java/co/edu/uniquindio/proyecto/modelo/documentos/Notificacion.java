package co.edu.uniquindio.proyecto.modelo.documentos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    private ObjectId id;

    private String mensaje;

    private LocalDateTime fecha;

    private String tipo; // Puede ser "INFO", "WARNING", "ERROR", etc.

    private boolean leida;

    private ObjectId reporteId; // ID del reporte asociado a la notificación

    private ObjectId idUsuario; // ID del usuario que debe recibir la notificación

    private String titulo; // Título de la notificación
}