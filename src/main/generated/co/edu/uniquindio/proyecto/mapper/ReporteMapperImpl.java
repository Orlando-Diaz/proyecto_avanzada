package co.edu.uniquindio.proyecto.mapper;

import co.edu.uniquindio.proyecto.dto.CrearReporteAnonimoDTO;
import co.edu.uniquindio.proyecto.dto.CrearReporteDTO;
import co.edu.uniquindio.proyecto.dto.EditarReporteDTO;
import co.edu.uniquindio.proyecto.dto.ReporteDTO;
import co.edu.uniquindio.proyecto.dto.UbicacionDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.Reporte;
import co.edu.uniquindio.proyecto.modelo.documentos.Ubicacion;
import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-12T16:55:43-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.3 (Eclipse Adoptium)"
)
@Component
public class ReporteMapperImpl implements ReporteMapper {

    @Override
    public Reporte toDocument(CrearReporteDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Reporte reporte = new Reporte();

        reporte.setIdUsuario( stringToObjectId( dto.idUsuario() ) );
        reporte.setUbicacion( ubicacionDTOToUbicacion( dto.ubicacion() ) );
        List<String> list = dto.fotos();
        if ( list != null ) {
            reporte.setFotos( new ArrayList<String>( list ) );
        }
        reporte.setCategoria( stringToObjectId( dto.idCategoria() ) );
        reporte.setDescripcion( dto.descripcion() );
        reporte.setTitulo( dto.titulo() );

        reporte.setEstadoActual( EstadoReporte.PENDIENTE );
        reporte.setFecha( LocalDateTime.now() );
        reporte.setContadorImportante( 0 );
        reporte.setHistorial( new ArrayList<>() );
        reporte.setComentarios( new ArrayList<>() );
        reporte.setEsAnonimo( false );

        return reporte;
    }

    @Override
    public Reporte toDocumentFromAnonimo(CrearReporteAnonimoDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Reporte reporte = new Reporte();

        reporte.setEsAnonimo( dto.esAnonimo() );
        reporte.setUbicacion( ubicacionDTOToUbicacion( dto.ubicacion() ) );
        List<String> list = dto.fotos();
        if ( list != null ) {
            reporte.setFotos( new ArrayList<String>( list ) );
        }
        reporte.setCiudad( dto.ciudad() );
        reporte.setCategoria( stringToObjectId( dto.idCategoria() ) );
        reporte.setDescripcion( dto.descripcion() );
        reporte.setTitulo( dto.titulo() );

        reporte.setEstadoActual( EstadoReporte.PENDIENTE );
        reporte.setFecha( LocalDateTime.now() );
        reporte.setContadorImportante( 0 );
        reporte.setHistorial( new ArrayList<>() );
        reporte.setComentarios( new ArrayList<>() );

        return reporte;
    }

    @Override
    public void updateFromDto(EditarReporteDTO dto, Reporte reporte) {
        if ( dto == null ) {
            return;
        }

        reporte.setDescripcion( dto.descripcion() );
        reporte.setTitulo( dto.titulo() );
        if ( dto.ubicacion() != null ) {
            if ( reporte.getUbicacion() == null ) {
                reporte.setUbicacion( Ubicacion.builder().build() );
            }
            ubicacionDTOToUbicacion1( dto.ubicacion(), reporte.getUbicacion() );
        }
        else {
            reporte.setUbicacion( null );
        }
        if ( reporte.getFotos() != null ) {
            List<String> list = dto.fotos();
            if ( list != null ) {
                reporte.getFotos().clear();
                reporte.getFotos().addAll( list );
            }
            else {
                reporte.setFotos( null );
            }
        }
        else {
            List<String> list = dto.fotos();
            if ( list != null ) {
                reporte.setFotos( new ArrayList<String>( list ) );
            }
        }
        reporte.setCategoria( dto.categoria() );
    }

    @Override
    public ReporteDTO toDto(Reporte reporte) {
        if ( reporte == null ) {
            return null;
        }

        String id = null;
        String idUsuario = null;
        UbicacionDTO ubicacion = null;
        String descripcion = null;
        LocalDateTime fecha = null;
        int contadorImportante = 0;
        String titulo = null;
        List<String> fotos = null;
        String estadoActual = null;
        Ciudad ciudad = null;
        boolean esAnonimo = false;

        id = objectIdToString( reporte.getId() );
        idUsuario = objectIdToString( reporte.getIdUsuario() );
        ubicacion = ubicacionToUbicacionDTO( reporte.getUbicacion() );
        descripcion = reporte.getDescripcion();
        fecha = reporte.getFecha();
        contadorImportante = reporte.getContadorImportante();
        titulo = reporte.getTitulo();
        List<String> list = reporte.getFotos();
        if ( list != null ) {
            fotos = new ArrayList<String>( list );
        }
        if ( reporte.getEstadoActual() != null ) {
            estadoActual = reporte.getEstadoActual().name();
        }
        ciudad = reporte.getCiudad();
        esAnonimo = reporte.isEsAnonimo();

        List<String> comentarios = null;
        String nombreUsuario = null;

        ReporteDTO reporteDTO = new ReporteDTO( id, descripcion, fecha, contadorImportante, idUsuario, titulo, ubicacion, fotos, estadoActual, ciudad, comentarios, esAnonimo, nombreUsuario );

        return reporteDTO;
    }

    protected Ubicacion ubicacionDTOToUbicacion(UbicacionDTO ubicacionDTO) {
        if ( ubicacionDTO == null ) {
            return null;
        }

        Ubicacion.UbicacionBuilder ubicacion = Ubicacion.builder();

        ubicacion.latitud( ubicacionDTO.latitud() );
        ubicacion.longitud( ubicacionDTO.longitud() );

        return ubicacion.build();
    }

    protected void ubicacionDTOToUbicacion1(UbicacionDTO ubicacionDTO, Ubicacion mappingTarget) {
        if ( ubicacionDTO == null ) {
            return;
        }

        mappingTarget.setLatitud( ubicacionDTO.latitud() );
        mappingTarget.setLongitud( ubicacionDTO.longitud() );
    }

    protected UbicacionDTO ubicacionToUbicacionDTO(Ubicacion ubicacion) {
        if ( ubicacion == null ) {
            return null;
        }

        double latitud = 0.0d;
        double longitud = 0.0d;

        latitud = ubicacion.getLatitud();
        longitud = ubicacion.getLongitud();

        UbicacionDTO ubicacionDTO = new UbicacionDTO( latitud, longitud );

        return ubicacionDTO;
    }
}
