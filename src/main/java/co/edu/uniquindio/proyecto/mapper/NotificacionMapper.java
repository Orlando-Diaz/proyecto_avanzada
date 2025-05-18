package co.edu.uniquindio.proyecto.mapper;

import co.edu.uniquindio.proyecto.dto.NotificacionDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.Notificacion;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface NotificacionMapper {

    @Mapping(source = "id", target = "id", qualifiedByName = "objectIdToString")
    @Mapping(source = "reporteId", target = "reporteId", qualifiedByName = "objectIdToString")
    @Mapping(source = "idUsuario", target = "idUsuario", qualifiedByName = "objectIdToString")
    NotificacionDTO toDTO(Notificacion notificacion);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToObjectId")
    @Mapping(source = "reporteId", target = "reporteId", qualifiedByName = "stringToObjectId")
    @Mapping(source = "idUsuario", target = "idUsuario", qualifiedByName = "stringToObjectId")
    Notificacion toEntity(NotificacionDTO notificacionDTO);

    @Named("objectIdToString")
    default String objectIdToString(ObjectId objectId) {
        return objectId != null ? objectId.toString() : null;
    }

    @Named("stringToObjectId")
    default ObjectId stringToObjectId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException e) {
            // Log error if needed
            return null;
        }
    }
}