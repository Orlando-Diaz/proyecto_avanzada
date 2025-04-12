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
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class, ArrayList.class})
public interface ReporteMapper {

    // Mapeo para creación normal
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estadoActual", constant = "PENDIENTE")
    @Mapping(target = "fecha", expression = "java(LocalDateTime.now())")
    @Mapping(target = "contadorImportante", constant = "0")
    @Mapping(target = "historial", expression = "java(new ArrayList<>())")
    @Mapping(target = "comentarios", expression = "java(new ArrayList<>())")
    @Mapping(target = "idUsuario", source = "idUsuario", qualifiedByName = "stringToObjectId")
    @Mapping(target = "ubicacion", source = "ubicacion")
    @Mapping(target = "fotos", source = "fotos")
    @Mapping(target = "categoria", source = "idCategoria", qualifiedByName = "stringToObjectId")
    @Mapping(target = "esAnonimo", constant = "false") // Por defecto no es anónimo
    Reporte toDocument(CrearReporteDTO dto);

    //MAPEO REPORTES ANONIMOS
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estadoActual", constant = "PENDIENTE")
    @Mapping(target = "fecha", expression = "java(LocalDateTime.now())")
    @Mapping(target = "contadorImportante", constant = "0")
    @Mapping(target = "historial", expression = "java(new ArrayList<>())")
    @Mapping(target = "comentarios", expression = "java(new ArrayList<>())")
    @Mapping(target = "idUsuario", ignore = true)  // No se asocia usuario
    @Mapping(target = "esAnonimo", source = "esAnonimo")
    @Mapping(target = "ubicacion", source = "ubicacion")
    @Mapping(target = "fotos", source = "fotos")
    @Mapping(target = "ciudad", source = "ciudad")
    Reporte toDocumentFromAnonimo(CrearReporteAnonimoDTO dto);

    // Mapeo para actualización
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "estadoActual", ignore = true)
    @Mapping(target = "historial", ignore = true)
    @Mapping(target = "contadorImportante", ignore = true)
    @Mapping(target = "comentarios", ignore = true)
    @Mapping(target = "esAnonimo", ignore = true)
    void updateFromDto(EditarReporteDTO dto, @MappingTarget Reporte reporte);

    // Convertir a DTO
    @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
    @Mapping(target = "idUsuario", source = "idUsuario", qualifiedByName = "objectIdToString")
    @Mapping(target = "ubicacion", source = "ubicacion")
    @Mapping(target = "comentarios", ignore = true)
    @Mapping(target = "nombreUsuario", ignore = true)
    ReporteDTO toDto(Reporte reporte);

    /* Métodos de conversión */
    @Named("stringToObjectId")
    default ObjectId stringToObjectId(String id) {
        return id != null ? new ObjectId(id) : null;
    }

    @Named("objectIdToString")
    default String objectIdToString(ObjectId id) {
        return id != null ? id.toString() : null;
    }
}