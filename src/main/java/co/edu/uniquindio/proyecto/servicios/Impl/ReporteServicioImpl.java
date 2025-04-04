package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.CrearReporteDTO;
import co.edu.uniquindio.proyecto.dto.EditarReporteDTO;
import co.edu.uniquindio.proyecto.dto.EnviarCorreoDTO;
import co.edu.uniquindio.proyecto.dto.ReporteDTO;
import co.edu.uniquindio.proyecto.mapper.ReporteMapper;
import co.edu.uniquindio.proyecto.modelo.documentos.HistorialReporte;
import co.edu.uniquindio.proyecto.modelo.documentos.Reporte;
import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import co.edu.uniquindio.proyecto.repositorios.ReporteRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.servicios.interfaces.EmailServicio;
import co.edu.uniquindio.proyecto.servicios.interfaces.ReporteServicio;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReporteServicioImpl implements ReporteServicio {

    private final ReporteMapper reporteMapper;
    private final ReporteRepo reporteRepo;
    private final MongoTemplate mongoTemplate;
    private final UsuarioRepo usuarioRepo;
    private final EmailServicio emailServicio;

    @Override
    public void crearReporte(CrearReporteDTO crearReporteDTO) throws Exception {
        // Validar usuario
        if (!ObjectId.isValid(crearReporteDTO.idUsuario())) {
            throw new Exception("El ID del usuario es inválido");
        }

        ObjectId usuarioId = new ObjectId(crearReporteDTO.idUsuario());
        Optional<Usuario> usuario = usuarioRepo.findById(usuarioId);

        if(usuario.isEmpty()) {
            throw new Exception("El usuario no existe");
        }

        // Validar campos requeridos
        if(crearReporteDTO.titulo().isBlank()) {
            throw new Exception("El título es obligatorio");
        }

        if(crearReporteDTO.descripcion().isBlank()) {
            throw new Exception("La descripción es obligatoria");
        }

        // Mapear DTO a entidad
        Reporte reporte = reporteMapper.toDocument(crearReporteDTO);

        // Configurar valores iniciales
        reporte.setFecha(LocalDateTime.now());
        reporte.setEstadoActual(EstadoReporte.PENDIENTE);
        reporte.setId(usuarioId);

        // Guardar en base de datos
        Reporte reporteGuardado = reporteRepo.save(reporte);

    }

    @Override
    public void editarReporte(String id, EditarReporteDTO editarReporteDTO) throws Exception {
        // Validar formato del ID
        if (!ObjectId.isValid(id)) {
            throw new Exception("ID de reporte inválido");
        }

        ObjectId reporteId = new ObjectId(id);
        Optional<Reporte> optionalReporte = reporteRepo.findById(reporteId);

        if (optionalReporte.isEmpty()) {
            throw new Exception("Reporte no encontrado");
        }

        Reporte reporte = optionalReporte.get();

        // Actualizar campos usando el mapper (ignorar el ID en el DTO)
        reporteMapper.updateFromDto(editarReporteDTO, reporte);

        // Asegurar que el historial no sea null
        if (reporte.getHistorial() == null) {
            reporte.setHistorial(new ArrayList<>());
        }

        // Agregar entrada al historial
        reporte.getHistorial().add(new HistorialReporte(
                "Reporte modificado",
                reporte.getEstadoActual(),
                LocalDateTime.now()
        ));

        reporteRepo.save(reporte);
    }

    @Override
    public void eliminarReporte(String id) throws Exception {
        if(!ObjectId.isValid(id)) {
            throw new Exception("ID de reporte inválido");
        }

        ObjectId reporteId = new ObjectId(id);
        Optional<Reporte> optionalReporte = reporteRepo.findById(reporteId);

        if(optionalReporte.isEmpty()) {
            throw new Exception("Reporte no encontrado");
        }

        Reporte reporte = optionalReporte.get();
        reporte.setEstadoActual(EstadoReporte.ELIMINADO);

        // Registrar en historial
        reporte.getHistorial().add(new HistorialReporte(
                "Reporte eliminado",
                EstadoReporte.ELIMINADO,
                LocalDateTime.now()
        ));

        reporteRepo.save(reporte);
    }

    @Override
    public ReporteDTO obtenerReportes(String id) throws Exception {
        if(!ObjectId.isValid(id)) {
            throw new Exception("ID de reporte inválido");
        }

        ObjectId reporteId = new ObjectId(id);
        Optional<Reporte> optionalReporte = reporteRepo.findById(reporteId);

        if(optionalReporte.isEmpty()) {
            throw new Exception("Reporte no encontrado");
        }

        return reporteMapper.toDto(optionalReporte.get());
    }

    @Override
    public List<ReporteDTO> listarTodos() {
        return reporteRepo.findAll().stream()
                .map(reporteMapper::toDto)
                .toList();
    }

    @Override
    public List<ReporteDTO> listarTodos(String nombre, String ciudad) {
        Criteria criteria = new Criteria();

        if (nombre != null && !nombre.isBlank()) {
            // Buscar por título (insensible a mayúsculas)
            criteria.and("titulo").regex(nombre, "i");
        }

        if (ciudad != null && !ciudad.isBlank()) {
            // Convertir el String ciudad a ENUM y filtrar
            criteria.and("ciudad").is(Ciudad.valueOf(ciudad.toUpperCase()));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, Reporte.class).stream()
                .map(reporteMapper::toDto)
                .toList();
    }

}