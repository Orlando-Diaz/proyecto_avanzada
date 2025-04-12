package co.edu.uniquindio.proyecto.mapper;

import co.edu.uniquindio.proyecto.dto.NotificacionDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.Notificacion;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-12T16:55:43-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.3 (Eclipse Adoptium)"
)
@Component
public class NotificacionMapperImpl implements NotificacionMapper {

    @Override
    public NotificacionDTO toDTO(Notificacion notificacion) {
        if ( notificacion == null ) {
            return null;
        }

        String id = null;
        String reporteId = null;
        String idUsuario = null;
        String mensaje = null;
        LocalDateTime fecha = null;
        String tipo = null;
        boolean leida = false;

        id = objectIdToString( notificacion.getId() );
        reporteId = objectIdToString( notificacion.getReporteId() );
        idUsuario = objectIdToString( notificacion.getIdUsuario() );
        mensaje = notificacion.getMensaje();
        fecha = notificacion.getFecha();
        tipo = notificacion.getTipo();
        leida = notificacion.isLeida();

        String titulo = null;

        NotificacionDTO notificacionDTO = new NotificacionDTO( id, mensaje, fecha, tipo, leida, reporteId, idUsuario, titulo );

        return notificacionDTO;
    }

    @Override
    public Notificacion toEntity(NotificacionDTO notificacionDTO) {
        if ( notificacionDTO == null ) {
            return null;
        }

        Notificacion.NotificacionBuilder notificacion = Notificacion.builder();

        notificacion.id( stringToObjectId( notificacionDTO.id() ) );
        notificacion.reporteId( stringToObjectId( notificacionDTO.reporteId() ) );
        notificacion.idUsuario( stringToObjectId( notificacionDTO.idUsuario() ) );
        notificacion.mensaje( notificacionDTO.mensaje() );
        notificacion.fecha( notificacionDTO.fecha() );
        notificacion.tipo( notificacionDTO.tipo() );

        return notificacion.build();
    }
}
