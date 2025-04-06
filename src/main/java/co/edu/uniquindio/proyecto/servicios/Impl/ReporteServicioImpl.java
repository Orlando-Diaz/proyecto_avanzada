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
import co.edu.uniquindio.proyecto.servicios.interfaces.EmailServicio;
import co.edu.uniquindio.proyecto.servicios.interfaces.ReporteServicio;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
        Reporte reporte = obtenerReporte(id);
        Map<String, String> cambios = new HashMap<>();

        // Actualizar solo campos no nulos
        if (editarReporteDTO.titulo() != null) {
            if (!editarReporteDTO.titulo().equals(reporte.getTitulo())) {
                cambios.put("titulo", "De '"+reporte.getTitulo()+"' a '"+editarReporteDTO.titulo()+"'");
                reporte.setTitulo(editarReporteDTO.titulo());
            }
        }

        if (editarReporteDTO.descripcion() != null) {
            if (!editarReporteDTO.descripcion().equals(reporte.getDescripcion())) {
                cambios.put("descripcion", "Descripción modificada");
                reporte.setDescripcion(editarReporteDTO.descripcion());
            }
        }

        // Repetir para otros campos...

        if (!cambios.isEmpty()) {
            HistorialReporte historialEntry = new HistorialReporte(
                    "Reporte modificado",
                    reporte.getEstadoActual(),
                    LocalDateTime.now(),
                    cambios
            );

            if (reporte.getHistorial() == null) {
                reporte.setHistorial(new ArrayList<>());
            }
            reporte.getHistorial().add(historialEntry);
        }

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

    @Override
    public void actualizarParcialReporte(String id, Map<String, Object> camposActualizados) throws Exception {
        // 1. Validar ID y obtener reporte
        if (!ObjectId.isValid(id)) {
            throw new Exception("ID de reporte inválido");
        }

        Reporte reporte = reporteRepo.findById(new ObjectId(id))
                .orElseThrow(() -> new Exception("Reporte no encontrado"));

        // 2. Preparar para registrar cambios
        Map<String, String> cambiosDetallados = new HashMap<>();

        // 3. Actualizar solo campos proporcionados
        camposActualizados.forEach((campo, valor) -> {
            try {
                switch (campo) {
                    case "titulo":
                        if (valor != null && !valor.toString().equals(reporte.getTitulo())) {
                            cambiosDetallados.put("titulo", "De '"+reporte.getTitulo()+"' a '"+valor+"'");
                            reporte.setTitulo(valor.toString());
                        }
                        break;

                    case "descripcion":
                        if (valor != null && !valor.toString().equals(reporte.getDescripcion())) {
                            cambiosDetallados.put("descripcion", "Descripción modificada");
                            reporte.setDescripcion(valor.toString());
                        }
                        break;

                    case "categoria":
                        if (valor != null && !valor.toString().equals(reporte.getCategoria())) {
                            cambiosDetallados.put("categoria", "De '"+reporte.getCategoria()+"' a '"+valor+"'");
                            reporte.setCategoria(valor.toString());
                        }
                        break;

                    // Añadir más campos según necesites
                }
            } catch (Exception e) {
                // Manejar error si el campo no existe
            }
        });

        // 4. Registrar en historial si hubo cambios
        if (!cambiosDetallados.isEmpty()) {
            HistorialReporte entradaHistorial = new HistorialReporte(
                    "Actualización parcial de reporte",
                    reporte.getEstadoActual(),
                    LocalDateTime.now(),
                    cambiosDetallados
            );

            if (reporte.getHistorial() == null) {
                reporte.setHistorial(new ArrayList<>());
            }
            reporte.getHistorial().add(entradaHistorial);

            // 5. Guardar cambios
            reporteRepo.save(reporte);
        }
    }
}