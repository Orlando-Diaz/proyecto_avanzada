package co.edu.uniquindio.proyecto.mapper;

import co.edu.uniquindio.proyecto.dto.CrearReporteDTO;
import co.edu.uniquindio.proyecto.dto.EditarReporteDTO;
import co.edu.uniquindio.proyecto.dto.ReporteDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.Reporte;
import co.edu.uniquindio.proyecto.modelo.documentos.Categoria;
import org.bson.types.ObjectId;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ReporteMapper {

    @Mapping(source = "categoriaNombre", target = "categoria", qualifiedByName = "nombreToCategoria")
    Reporte toDocument(CrearReporteDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "categoriaNombre", target = "categoria", qualifiedByName = "nombreToCategoria")
    void updateFromDto(EditarReporteDTO dto, @MappingTarget Reporte reporte);

    @Mapping(source = "categoria.id", target = "categoriaId")
    ReporteDTO toDto(Reporte reporte);

    // Método para convertir nombre de categoría a objeto Categoria
    @Named("nombreToCategoria")
    default Categoria nombreToCategoria(String nombre) {
        if (nombre == null) {
            return null;
        }
        return Categoria.builder().nombre(nombre).build();
    }

    // Método para convertir ObjectId a String
    default String map(ObjectId value) {
        return value != null ? value.toString() : null;
    }

    // Método para convertir String a ObjectId
    default ObjectId map(String value) {
        return value != null ? new ObjectId(value) : null;
    }
}