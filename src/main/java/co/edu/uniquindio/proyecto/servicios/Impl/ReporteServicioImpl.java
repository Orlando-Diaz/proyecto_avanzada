package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.*;
import co.edu.uniquindio.proyecto.excepciones.EstadoReporteInvalidoException;
import co.edu.uniquindio.proyecto.excepciones.RoleInvalidoException;
import co.edu.uniquindio.proyecto.mapper.ReporteMapper;
import co.edu.uniquindio.proyecto.modelo.documentos.*;
import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import co.edu.uniquindio.proyecto.modelo.enums.Rol;
import co.edu.uniquindio.proyecto.repositorios.CategoriaRepo;
import co.edu.uniquindio.proyecto.repositorios.HistorialReporteRepo;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteServicioImpl implements ReporteServicio {

    private final ReporteMapper reporteMapper;
    private final HistorialReporteRepo historialReporteRepo;
    private final ReporteRepo reporteRepo;
    private final MongoTemplate mongoTemplate;
    private final UsuarioRepo usuarioRepo;
    private final EmailServicio emailServicio;
    private final JWTUtils jwtUtils;
    private final CategoriaRepo categoriaRepo;

    @Override
    public void crearReporte(CrearReporteDTO crearReporteDTO) throws Exception {
        // 1. Validar usuario
        if (!ObjectId.isValid(crearReporteDTO.idUsuario())) {
            throw new Exception("El ID del usuario es inválido");
        }

        ObjectId usuarioId = new ObjectId(crearReporteDTO.idUsuario());
        Optional<Usuario> usuario = usuarioRepo.findById(usuarioId);

        ObjectId categoriaId = new ObjectId(crearReporteDTO.idCategoria());
        Optional<Categoria> categoria = categoriaRepo.findById(categoriaId);

        if(usuario.isEmpty()) {
            throw new Exception("El usuario no existe");
        }

        if (categoria.isEmpty()){
            throw new Exception("La categoria no existe");
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

    /**
     * Servicio utilizado por un administrador para gestionar el estado de un reporte
     * @param gestionEstadoReporteDTO
     * @throws EstadoReporteInvalidoException Excepcion para validar que un reporte no este eliminado
     * ni que tenga el estado que se desea actualizar
     */
    @Override
    public void gestionarEstadoReporteAdministrador(GestionarEstadoReporteDTO gestionEstadoReporteDTO) throws Exception {

        //Validar role usuario
        // obtener usuario
        Usuario usuario = usuarioRepo.findById(new ObjectId(gestionEstadoReporteDTO.idUsuarioModifica()))
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
        System.out.println("ROL DEL USUARIO: '" + usuario.getRol() + "'");

        if (!usuario.getRol().equals(Rol.ADMINISTRADOR)) {
            throw new RoleInvalidoException("ERROR. ROL INVALIDO PARA REALIZAR CAMBIOS");
        }

        // Buscar el reporte
        Reporte reporte = reporteRepo.findById(new ObjectId(gestionEstadoReporteDTO.idReporte()))
                .orElseThrow(() -> new Exception("Reporte no encontrado"));

        // Obtener el nuevo estado
        EstadoReporte nuevoEstado = EstadoReporte.valueOf(gestionEstadoReporteDTO.estado().toString());

        // Validar que el nuevo estado sea diferente al actual
        if (reporte.getEstadoActual() == nuevoEstado) {
            throw new EstadoReporteInvalidoException("ERRORS. El reporte ya tiene el estado: " + nuevoEstado);
        }

        // Si el reporte está eliminado, no se permite cambiar el estado
        if (reporte.getEstadoActual() == EstadoReporte.ELIMINADO) {
            throw new EstadoReporteInvalidoException("ERROR. No se puede cambiar el estado de un reporte eliminado");
        }

        // Registrar motivo y responsable
        String observacion = "Actualizacion de estado: Estado cambiado de " + reporte.getEstadoActual() + " a " + nuevoEstado
                + " por usuario: " + gestionEstadoReporteDTO.idUsuarioModifica() + ". Motivo: " + gestionEstadoReporteDTO.motivo();

        // Construir historial del cambio
        Map<String, String> cambios = new HashMap<>();
        cambios.put("estadoAnterior", reporte.getEstadoActual().name());
        cambios.put("estadoNuevo", nuevoEstado.name());

        HistorialReporte historial = HistorialReporte.builder()
                .observaciones(observacion)
                .estado(nuevoEstado)
                .fecha(LocalDateTime.now())
                .cambios(cambios)
                .build();

        // Actualizar el estado actual del reporte
        reporte.setEstadoActual(nuevoEstado);

        // Agregar al historial del reporte
        reporte.getHistorial().add(historial);

        // Guardar el reporte actualizado y el historial
        reporteRepo.save(reporte);
        historialReporteRepo.save(historial);

    }

    /**
     * Requerimiento utilizado para que un cliente gestione sus propios reportes
     * Solamente puede eliminar o resolver un reporte
     * @param gestionEstadoReporteDTO
     * @throws Exception
     */
    @Override
    public void gestionarEstadoReporteCliente(GestionarEstadoReporteDTO gestionEstadoReporteDTO) throws Exception {

        Usuario usuario = usuarioRepo.findById(new ObjectId(gestionEstadoReporteDTO.idUsuarioModifica()))
                .orElseThrow(() -> new Exception("Usuario no encontrado"));

        // Buscar el reporte
        Reporte reporte = reporteRepo.findById(new ObjectId(gestionEstadoReporteDTO.idReporte()))
                .orElseThrow(() -> new Exception("Reporte no encontrado"));

        EstadoReporte estadoActual = reporte.getEstadoActual();
        EstadoReporte nuevoEstado = gestionEstadoReporteDTO.estado();

        // Validar si el estado actual es RECHAZADO o ELIMINADO
        if (estadoActual == EstadoReporte.RECHAZADO || estadoActual == EstadoReporte.ELIMINADO) {
            throw new EstadoReporteInvalidoException("No se puede modificar un reporte con estado " + estadoActual);
        }

        // Validar que el nuevo estado sea RESUELTO o ELIMINADO
        if (nuevoEstado != EstadoReporte.RESUELTO && nuevoEstado != EstadoReporte.ELIMINADO) {
            throw new EstadoReporteInvalidoException("Solo puede marcar un reporte como RESUELTO o ELIMINADO");
        }

        // Si está en estado RESUELTO o VERIFICADO, solo puede eliminarse
        if ((estadoActual == EstadoReporte.RESUELTO || estadoActual == EstadoReporte.VERIFICADO)
                && nuevoEstado != EstadoReporte.ELIMINADO) {
            throw new EstadoReporteInvalidoException("Un reporte en estado " + estadoActual + " solo puede ser eliminado");
        }

        // Si el estado es PENDIENTE, no puede marcarse como VERIFICADO o RECHAZADO (esto ya se controla arriba, pero por claridad)
        if (estadoActual == EstadoReporte.PENDIENTE &&
                (nuevoEstado == EstadoReporte.VERIFICADO || nuevoEstado == EstadoReporte.RECHAZADO)) {
            throw new EstadoReporteInvalidoException("No se puede marcar como " + nuevoEstado + " un reporte pendiente");
        }

        // Guardar cambio de estado
        Map<String, String> cambios = new HashMap<>();
        cambios.put("estadoAnterior", estadoActual.name());
        cambios.put("estadoNuevo", nuevoEstado.name());

        HistorialReporte historial = HistorialReporte.builder()
                .observaciones("Cambio de estado por el usuario dueño del reporte. Motivo: " + gestionEstadoReporteDTO.motivo())
                .estado(nuevoEstado)
                .fecha(LocalDateTime.now())
                .cambios(cambios)
                .build();

        historialReporteRepo.save(historial);
        reporte.setEstadoActual(nuevoEstado);
        reporte.getHistorial().add(historial);

        reporteRepo.save(reporte);

    }

    @Override
    public InformeCategoriaDTO generarInformePorCategoria(String categoria, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Reporte> reportes;

        // Determinar qué método del repositorio usar según los parámetros
        if (categoria != null && !categoria.isBlank()) {
            if (fechaInicio != null && fechaFin != null) {
                LocalDateTime inicio = fechaInicio.atStartOfDay();
                LocalDateTime fin = fechaFin.atTime(23, 59, 59);
                reportes = reporteRepo.findByCategoriaAndFechaBetween(categoria, inicio, fin);
            } else {
                reportes = reporteRepo.findByCategoria(categoria);
            }
        } else if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(23, 59, 59);
            reportes = reporteRepo.findByFechaBetween(inicio, fin);
        } else {
            // Sin filtros, traer todos (podría ser limitado o paginado para evitar problemas de memoria)
            reportes = reporteRepo.findAll();
        }

        // Resto del procesamiento igual que antes
        Map<String, Long> distribucionEstados = reportes.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getEstadoActual().name(),
                        Collectors.counting()
                ));

        List<ReporteResumidoDTO> reportesResumidos = reportes.stream()
                .map(r -> new ReporteResumidoDTO(
                        r.getId().toString(),
                        r.getTitulo(),
                        r.getFecha(),
                        r.getEstadoActual(),
                        r.getContadorImportante()
                ))
                .collect(Collectors.toList());

        return new InformeCategoriaDTO(
                categoria,
                fechaInicio,
                fechaFin,
                reportes.size(),
                reportesResumidos,
                distribucionEstados
        );
    }

    @Override
    public InformeGeograficoDTO generarInformePorUbicacion(
            Double latitud, Double longitud, Double radioKm,
            LocalDate fechaInicio, LocalDate fechaFin) {

        // Convertir radio a metros
        double distanciaMetros = radioKm * 1000;
        List<Reporte> reportes;

        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(23, 59, 59);
            reportes = reporteRepo.findByUbicacionNearAndFechaBetween(
                    longitud, latitud, distanciaMetros, inicio, fin);
        } else {
            reportes = reporteRepo.findByUbicacionNear(longitud, latitud, distanciaMetros);
        }

        // Calcular la distribución por categoría - Necesitamos adaptarlo para ObjectId
        Map<String, Long> reportesPorCategoria = reportes.stream()
                .collect(Collectors.groupingBy(
                        reporte -> reporte.getCategoria().toString(),
                        Collectors.counting()
                ));

        // Convertir a DTOs con distancia calculada
        List<ReporteUbicacionDTO> reportesDTO = new ArrayList<>();
        for (Reporte reporte : reportes) {
            // Calcular distancia usando la fórmula de Haversine
            double distancia = calcularDistanciaHaversine(
                    latitud, longitud,
                    reporte.getUbicacion().getLatitud(),
                    reporte.getUbicacion().getLongitud());

            // También necesitamos adaptar la categoría como String
            reportesDTO.add(new ReporteUbicacionDTO(
                    reporte.getId().toString(),
                    reporte.getTitulo(),
                    reporte.getCategoria().toString(), // Convertir ObjectId a String
                    new UbicacionDTO(reporte.getUbicacion().getLatitud(), reporte.getUbicacion().getLongitud()),
                    distancia,
                    reporte.getFecha(),
                    reporte.getEstadoActual()
            ));
        }

        // Ordenar por distancia (más cercanos primero)
        reportesDTO.sort(Comparator.comparing(ReporteUbicacionDTO::distanciaKm));

        // Crear y retornar el DTO del informe
        return new InformeGeograficoDTO(
                latitud,
                longitud,
                radioKm,
                fechaInicio,
                fechaFin,
                reportes.size(),
                reportesPorCategoria,
                reportesDTO
        );
    }

    // Método auxiliar para calcular la distancia entre dos puntos usando la fórmula de Haversine
    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        // Radio de la Tierra en kilómetros
        final double R = 6371.0;

        // Convertir coordenadas a radianes
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        // Fórmula de Haversine
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distancia en kilómetros
        return R * c;
    }

    @Override
    public List<ReporteDTO> listarReportesPorEstado(EstadoReporte estado) {
        List<Reporte> reportes = reporteRepo.findByEstadoActual(estado);
        return reportes.stream()
                .map(reporteMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public EstadisticasGeneralesDTO obtenerEstadisticasGenerales() {
        // Obtener conteo total de reportes
        long totalReportes = reporteRepo.count();

        // Obtener conteo por cada estado
        long reportesPendientes = reporteRepo.countByEstadoActual(EstadoReporte.PENDIENTE);
        long reportesResueltos = reporteRepo.countByEstadoActual(EstadoReporte.RESUELTO);
        long reportesRechazados = reporteRepo.countByEstadoActual(EstadoReporte.RECHAZADO);

        // Agrupar reportes por categoría
        List<Reporte> todosLosReportes = reporteRepo.findAll();
        Map<String, Long> reportesPorCategoria = todosLosReportes.stream()
                .collect(Collectors.groupingBy(
                        reporte -> reporte.getCategoria().toString(),
                        Collectors.counting()
                ));

        // Agrupar reportes por ciudad
        Map<String, Long> reportesPorCiudad = todosLosReportes.stream()
                .filter(reporte -> reporte.getCiudad() != null)
                .collect(Collectors.groupingBy(
                        reporte -> reporte.getCiudad().name(),
                        Collectors.counting()
                ));

        // Crear y retornar el objeto DTO
        return new EstadisticasGeneralesDTO(
                totalReportes,
                reportesPendientes,
                reportesResueltos,
                reportesRechazados,
                reportesPorCategoria,
                reportesPorCiudad,
                LocalDateTime.now()
        );
    }

    @Override
    public byte[] generarInformePorCategoriaPDF(String categoria, LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
        // Obtener primero el informe de datos
        InformeCategoriaDTO informe = generarInformePorCategoria(categoria, fechaInicio, fechaFin);

        // Crear un ByteArrayOutputStream para almacenar el PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Crear el documento PDF
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();

        // Añadir título
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Informe de Reportes por Categoría", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // Espacio

        // Añadir información del informe
        if (categoria != null && !categoria.isBlank()) {
            document.add(new Paragraph("Categoría: " + categoria));
        } else {
            document.add(new Paragraph("Todas las categorías"));
        }

        if (fechaInicio != null) {
            document.add(new Paragraph("Fecha Inicio: " + fechaInicio));
        }
        if (fechaFin != null) {
            document.add(new Paragraph("Fecha Fin: " + fechaFin));
        }
        document.add(new Paragraph("Total de Reportes: " + informe.totalReportes()));
        document.add(new Paragraph(" ")); // Espacio

        // Añadir distribución por estados
        document.add(new Paragraph("Distribución por Estado:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
        for (Map.Entry<String, Long> entry : informe.distribucionEstados().entrySet()) {
            document.add(new Paragraph(entry.getKey() + ": " + entry.getValue() + " reportes"));
        }
        document.add(new Paragraph(" ")); // Espacio

        // Crear tabla para los reportes
        PdfPTable table = new PdfPTable(4); // 4 columnas
        table.setWidthPercentage(100);

        // Encabezados de la tabla
        addTableHeader(table, new String[]{"ID", "Título", "Fecha", "Estado"});

        // Contenido de la tabla
        for (ReporteResumidoDTO reporte : informe.reportes()) {
            table.addCell(reporte.id());
            table.addCell(reporte.titulo());

            // Formatear la fecha
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaFormateada = reporte.fecha().format(formatter);
            table.addCell(fechaFormateada);

            table.addCell(reporte.estadoActual().toString());
        }

        document.add(table);

        // Agregar gráfico de distribución por estados (opcional, requiere library adicional)
        // Esta parte necesitaría JFreeChart si quieres implementar gráficos

        // Cerrar el documento
        document.close();

        return baos.toByteArray();
    }



    @Override
    public byte[] generarInformePorUbicacionPDF(Double latitud, Double longitud, Double radioKm,
                                                LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
        // Obtener primero el informe de datos
        InformeGeograficoDTO informe = generarInformePorUbicacion(latitud, longitud, radioKm, fechaInicio, fechaFin);

        // Crear un ByteArrayOutputStream para almacenar el PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Crear el documento PDF
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();

        // Añadir título
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Informe de Reportes por Ubicación", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); // Espacio

        // Añadir información del informe
        document.add(new Paragraph("Ubicación: Latitud: " + latitud + ", Longitud: " + longitud));
        document.add(new Paragraph("Radio: " + radioKm + " km"));
        if (fechaInicio != null) {
            document.add(new Paragraph("Fecha Inicio: " + fechaInicio));
        }
        if (fechaFin != null) {
            document.add(new Paragraph("Fecha Fin: " + fechaFin));
        }
        document.add(new Paragraph("Total de Reportes: " + informe.totalReportes()));
        document.add(new Paragraph(" ")); // Espacio

        // Añadir estadísticas por categoría
        document.add(new Paragraph("Distribución por Categoría:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
        for (Map.Entry<String, Long> entry : informe.reportesPorCategoria().entrySet()) {
            document.add(new Paragraph(entry.getKey() + ": " + entry.getValue() + " reportes"));
        }
        document.add(new Paragraph(" ")); // Espacio

        // Crear tabla para los reportes
        PdfPTable table = new PdfPTable(5); // 5 columnas
        table.setWidthPercentage(100);

        // Encabezados de la tabla
        addTableHeader(table, new String[]{"ID", "Título", "Categoría", "Distancia (km)", "Estado"});

        // Contenido de la tabla
        for (ReporteUbicacionDTO reporte : informe.reportes()) {
            table.addCell(reporte.id());
            table.addCell(reporte.titulo());
            table.addCell(reporte.categoria());
            table.addCell(String.format("%.2f", reporte.distanciaKm()));
            table.addCell(reporte.estadoActual().toString());
        }

        document.add(table);

        // Cerrar el documento
        document.close();

        return baos.toByteArray();
    }

    // Método auxiliar para añadir encabezados a la tabla
    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(5);
            cell.setPhrase(new Phrase(header, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
            table.addCell(cell);
        }
    }



    /*
    IMPLEMENTACIÓN PARA LA CREACIÓN DE UN REPORTE ANONIMO
     */

    @Override
    public void crearReporteAnonimo(CrearReporteAnonimoDTO dto) throws Exception {
        // Validaciones básicas
        if(dto.titulo().isBlank()) {
            throw new Exception("El título es obligatorio");
        }

        if(dto.descripcion().isBlank()) {
            throw new Exception("La descripción es obligatoria");
        }

        // Convertir DTO a documento
        Reporte reporte = reporteMapper.toDocumentFromAnonimo(dto);
        reporteRepo.save(reporte);
    }


    //RECHAZAR UN REPORTE
    @Override
    public void rechazarReporte(String idReporte, RechazarReporteDTO dto) throws Exception {

        // Obtener el usuario actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Validar que sea ADMIN
        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
            throw new AccessDeniedException("No tienes permisos para rechazar reportes");
        }

        // Validar justificación
        if(dto.justificacion() == null || dto.justificacion().isBlank()) {
            throw new Exception("La justificación es obligatoria");
        }

        // Obtener el reporte
        Reporte reporte = reporteRepo.findById(new ObjectId(idReporte))
                .orElseThrow(() -> new Exception("Reporte no encontrado"));

        // Validar que esté en estado PENDIENTE
        if(reporte.getEstadoActual() != EstadoReporte.PENDIENTE) {
            throw new Exception("Solo se pueden rechazar reportes en estado PENDIENTE");
        }

        // Obtener ID del usuario que rechaza
        String idUsuario = SecurityContextHolder.getContext().getAuthentication().getName();

        // Crear entrada en el historial
        Map<String, String> cambios = new HashMap<>();
        cambios.put("estado", "De PENDIENTE a RECHAZADO");
        cambios.put("justificacion", dto.justificacion());

        HistorialReporte historial = new HistorialReporte(
                "Reporte rechazado: " + dto.justificacion(),
                EstadoReporte.RECHAZADO,
                LocalDateTime.now(),
                cambios
        );

        // Actualizar el reporte
        reporte.setEstadoActual(EstadoReporte.RECHAZADO);
        if(reporte.getHistorial() == null) {
            reporte.setHistorial(new ArrayList<>());
        }
        reporte.getHistorial().add(historial);

        reporteRepo.save(reporte);
    }


}