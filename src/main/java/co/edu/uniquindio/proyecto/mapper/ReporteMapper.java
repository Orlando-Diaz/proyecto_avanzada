package co.edu.uniquindio.proyecto.mapper;

import co.edu.uniquindio.proyecto.dto.*;
import co.edu.uniquindio.proyecto.modelo.documentos.Reporte;
import co.edu.uniquindio.proyecto.modelo.documentos.Ubicacion;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = LocalDateTime.class)
public interface ReporteMapper {

    // Mapeo para creación
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estadoActual", constant = "PENDIENTE")
    @Mapping(target = "fecha", expression = "java(LocalDateTime.now())")
    @Mapping(target = "contadorImportante", constant = "0")
    @Mapping(target = "historial", ignore = true)
    @Mapping(target = "idUsuario", source = "idUsuario", qualifiedByName = "stringToObjectId")
    @Mapping(target = "ubicacion", source = "ubicacion")
    @Mapping(target = "fotos", source = "fotos")
    Reporte toDocument(CrearReporteDTO dto);

    // Mapeo para actualización
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "estadoActual", ignore = true)
    @Mapping(target = "historial", ignore = true)
    @Mapping(target = "contadorImportante", ignore = true)
    @Mapping(target = "ubicacion", source = "ubicacion")
    @Mapping(target = "fotos", source = "fotos")
    void updateFromDto(EditarReporteDTO dto, @MappingTarget Reporte reporte);

    // Convertir a DTO
    @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
    @Mapping(target = "idUsuario", source = "idUsuario", qualifiedByName = "objectIdToString")
    @Mapping(target = "ubicacion", source = "ubicacion")
    ReporteDTO toDto(Reporte reporte);

    /* Métodos de conversión */
    @Named("stringToObjectId")
    default ObjectId stringToObjectId(String id) {
        return new ObjectId(id);
    }

    @Named("objectIdToString")
    default String objectIdToString(ObjectId id) {
        return id != null ? id.toString() : null;
    }
}