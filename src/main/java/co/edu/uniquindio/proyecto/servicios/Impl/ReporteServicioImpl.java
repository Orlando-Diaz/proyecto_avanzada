package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.*;
import co.edu.uniquindio.proyecto.mapper.ReporteMapper;
import co.edu.uniquindio.proyecto.modelo.documentos.Comentario;
import co.edu.uniquindio.proyecto.modelo.documentos.HistorialReporte;
import co.edu.uniquindio.proyecto.modelo.documentos.Reporte;
import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import co.edu.uniquindio.proyecto.repositorios.ReporteRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.seguridad.JWTUtils;
import co.edu.uniquindio.proyecto.servicios.interfaces.EmailServicio;
import co.edu.uniquindio.proyecto.servicios.interfaces.ReporteServicio;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteServicioImpl implements ReporteServicio {

    private final ReporteMapper reporteMapper;
    private final ReporteRepo reporteRepo;
    private final MongoTemplate mongoTemplate;
    private final UsuarioRepo usuarioRepo;
    private final EmailServicio emailServicio;
    private final JWTUtils jwtUtils;

    @Override
    public void crearReporte(CrearReporteDTO crearReporteDTO) throws Exception {
        // 1. Validar usuario
        if (!ObjectId.isValid(crearReporteDTO.idUsuario())) {
            throw new Exception("El ID del usuario es inválido");
        }

        ObjectId usuarioId = new ObjectId(crearReporteDTO.idUsuario());
        Optional<Usuario> usuario = usuarioRepo.findById(usuarioId);

        if(usuario.isEmpty()) {
            throw new Exception("El usuario no existe");
        }

        // 2. Validar campos requeridos
        if(crearReporteDTO.titulo().isBlank()) {
            throw new Exception("El título es obligatorio");
        }

        if(crearReporteDTO.descripcion().isBlank()) {
            throw new Exception("La descripción es obligatoria");
        }

        Reporte reporte = reporteMapper.toDocument(crearReporteDTO);

        // Configurar valores iniciales
        reporte.setId(new ObjectId()); // Generar nuevo ID
        reporte.setFecha(LocalDateTime.now());
        reporte.setEstadoActual(EstadoReporte.PENDIENTE);
        reporte.setComentarios(new ArrayList<>());
        reporte.setHistorial(new ArrayList<>());

        reporteRepo.save(reporte);
    }

    @Override
    public void editarReporte(String id, EditarReporteDTO editarReporteDTO) throws Exception {
        // 1. Obtener reporte actual
        Reporte reporte = obtenerReporte(id);

        // 2. Obtener ID del cliente desde el token JWT (¡Aquí va!)
        String clienteId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName(); // Asume que el username es el ID

        // 3. Preparar cambios (tu lógica actual)
        Map<String, String> cambios = new HashMap<>();
        if (editarReporteDTO.titulo() != null && !editarReporteDTO.titulo().equals(reporte.getTitulo())) {
            cambios.put("titulo", reporte.getTitulo() + " → " + editarReporteDTO.titulo());
            reporte.setTitulo(editarReporteDTO.titulo());
        }
        // ... otros campos ...

        // 4. Registrar en historial
        if (!cambios.isEmpty()) {
            HistorialReporte historial = new HistorialReporte();
            historial.setClienteId(new ObjectId(clienteId)); // Usar el ID del token
            historial.setObservaciones("Edición manual");
            historial.setEstado(reporte.getEstadoActual());
            historial.setFecha(LocalDateTime.now());
            historial.setCambios(cambios);

            if (reporte.getHistorial() == null) {
                reporte.setHistorial(new ArrayList<>());
            }
            reporte.getHistorial().add(historial);
        }

        // 5. Guardar
        reporteRepo.save(reporte);
    }

    private Reporte cloneReporte(Reporte original) {
        // Implementa una copia profunda del reporte
        Reporte copia = new Reporte();
        copia.setTitulo(original.getTitulo());
        copia.setDescripcion(original.getDescripcion());
        copia.setEstadoActual(original.getEstadoActual());
        // Copiar otros campos relevantes...
        return copia;
    }

    private Map<String, String> detectarCambios(Reporte original, Reporte actualizado) {
        Map<String, String> cambios = new HashMap<>();

        if (!original.getTitulo().equals(actualizado.getTitulo())) {
            cambios.put("titulo", "De '" + original.getTitulo() + "' a '" + actualizado.getTitulo() + "'");
        }

        if (!original.getDescripcion().equals(actualizado.getDescripcion())) {
            cambios.put("descripcion", "Descripción modificada");
        }

        if (original.getEstadoActual() != actualizado.getEstadoActual()) {
            cambios.put("estado", "De " + original.getEstadoActual() + " a " + actualizado.getEstadoActual());
        }

        // Agregar más comparaciones según sea necesario (ubicación, categoría, etc.)

        return cambios;
    }

    @Override
    public void eliminarReporte(String id) throws Exception {
        // Validación del ID
        if (!ObjectId.isValid(id)) {
            throw new Exception("ID de reporte inválido");
        }

        ObjectId reporteId = new ObjectId(id);
        Reporte reporte = reporteRepo.findById(reporteId)
                .orElseThrow(() -> new Exception("Reporte no encontrado"));

        // Guardar el estado anterior para el historial
        EstadoReporte estadoAnterior = reporte.getEstadoActual();

        // Cambiar el estado
        reporte.setEstadoActual(EstadoReporte.ELIMINADO);

        // Preparar detalles de cambios para el historial
        Map<String, String> cambios = new HashMap<>();
        cambios.put("estado", "De " + estadoAnterior + " a ELIMINADO");

        // Registrar en historial con más detalles
        HistorialReporte entradaHistorial = new HistorialReporte(
                "Reporte eliminado del sistema",
                EstadoReporte.ELIMINADO,
                LocalDateTime.now(),
                cambios
        );

        // Asegurarse que la lista de historial existe
        if (reporte.getHistorial() == null) {
            reporte.setHistorial(new ArrayList<>());
        }

        reporte.getHistorial().add(entradaHistorial);
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
    public List<ReporteDTO> listarTodos(String nombre, String ciudad, String categoria) {
        Criteria criteria = new Criteria();

        if (nombre != null && !nombre.isBlank()) {
            criteria.and("titulo").regex(nombre, "i");
        }

        if (ciudad != null && !ciudad.isBlank()) {
            criteria.and("ciudad").is(Ciudad.valueOf(ciudad.toUpperCase()));
        }

        if (categoria != null && !categoria.isBlank()) {
            criteria.and("categoria").is(categoria);
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, Reporte.class).stream()
                .map(reporteMapper::toDto)
                .toList();
    }

    private Reporte obtenerReporte(String idReporte) throws Exception {
        return reporteRepo.findById(new ObjectId(idReporte))
                .orElseThrow(() -> new Exception("No se encontró el reporte"));
    }

    private ComentarioDTO convertirComentarioADTO(Comentario comentario) {
        return new ComentarioDTO(
                comentario.getIdUsuario().toString(),
                comentario.getContenido(),
                comentario.getFecha()

        );
    }

    /*
    AGREGAR COMENTARIO A UN REPORTE
     */

    public String agregarComentario(String idReporte, ComentarioDTO comentarioDTO) throws Exception {
        Reporte reporte = obtenerReporte(idReporte);

        Comentario comentario = new Comentario();
        comentario.setId(new ObjectId());
        comentario.setIdUsuario(new ObjectId(comentarioDTO.idUsuario()));
        comentario.setContenido(comentarioDTO.contenido());
        comentario.setFecha(LocalDateTime.now());

        reporte.getComentarios().add(comentario);
        reporteRepo.save(reporte);

        return comentario.getId().toString();
    }

    /*
    LISTAR COMENTARIOS EN UN REPORTE
     */

    @Override
    public List<ComentarioDTO> listarComentarios(String idReporte) throws Exception {
        Reporte reporte = obtenerReporte(idReporte);
        return reporte.getComentarios().stream()
                .map(this::convertirComentarioADTO)
                .collect(Collectors.toList());
    }

    /*
    OBTENER EL HISTORIAL DE UN REPORTE MEDIANTE SU ID
     */

    @Override
    public List<HistorialReporteDTO> obtenerHistorial(String idReporte) throws Exception {
        if (!ObjectId.isValid(idReporte)) {
            throw new IllegalArgumentException("ID de reporte inválido");
        }

        Reporte reporte = reporteRepo.findById(new ObjectId(idReporte))
                .orElseThrow(() -> new Exception("Reporte no encontrado"));

        return reporte.getHistorial().stream()
                .map(h -> new HistorialReporteDTO(
                        h.getObservaciones(),
                        h.getEstado(),
                        h.getFecha(),
                        h.getCambios() != null ? h.getCambios() : Map.of()
                ))
                .collect(Collectors.toList());
    }

    /*
    MARCAR UN REPORTE COMO IMPORTANTE
     */
    @Override
    public int marcarComoImportante(String idReporte) throws Exception {
        Reporte reporte = reporteRepo.findById(new ObjectId(idReporte))
                .orElseThrow(() -> new Exception("Reporte no encontrado"));

        reporte.setContadorImportante(reporte.getContadorImportante() + 1);
        reporteRepo.save(reporte);

        return reporte.getContadorImportante(); // Devuelve el nuevo valor
    }

    @Override
    public List<ReporteDTO> listarReportesOrdenadosPorImportancia() {
        // 1. Obtener reportes ordenados
        List<Reporte> reportes = reporteRepo.findAllByOrderByContadorImportanteDesc();

        // 2. Convertir a DTO
        return reportes.stream()
                .map(reporteMapper::toDto)
                .collect(Collectors.toList());
    }


}