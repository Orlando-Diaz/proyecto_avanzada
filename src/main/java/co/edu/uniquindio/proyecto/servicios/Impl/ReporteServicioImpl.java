package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.*;
import co.edu.uniquindio.proyecto.excepciones.EstadoReporteInvalidoException;
import co.edu.uniquindio.proyecto.excepciones.RoleInvalidoException;
import co.edu.uniquindio.proyecto.mapper.ReporteMapper;
import co.edu.uniquindio.proyecto.modelo.documentos.*;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import co.edu.uniquindio.proyecto.modelo.enums.Rol;
import co.edu.uniquindio.proyecto.repositorios.CategoriaRepo;
import co.edu.uniquindio.proyecto.repositorios.HistorialReporteRepo;
import co.edu.uniquindio.proyecto.repositorios.ReporteRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.seguridad.JWTUtils;
import co.edu.uniquindio.proyecto.servicios.interfaces.EmailServicio;
import co.edu.uniquindio.proyecto.servicios.interfaces.NotificacionServicio;
import co.edu.uniquindio.proyecto.servicios.interfaces.ReporteServicio;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

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
    private final NotificacionServicio notificacionServicio;

    @Override
    public String crearReporte(CrearReporteDTO crearReporteDTO) throws Exception {
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

        // Guardar el reporte
        Reporte reporteGuardado = reporteRepo.save(reporte);

        // Devolver el ID del reporte creado
        return reporteGuardado.getId().toString();
    }

    @Override
    public void editarReporte(String id, EditarReporteDTO editarReporteDTO) throws Exception {
        // 1. Validar ID y obtener reporte actual
        if (!ObjectId.isValid(id)) {
            throw new Exception("ID de reporte inválido");
        }

        ObjectId reporteId = new ObjectId(id);
        Reporte reporte = reporteRepo.findById(reporteId)
                .orElseThrow(() -> new Exception("Reporte no encontrado"));

        // 2. Obtener email del cliente desde el token JWT
        String clienteEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName(); // Ahora es el email

        // Buscar el cliente por su email para obtener su ID
        Usuario cliente = usuarioRepo.findByEmail(clienteEmail)
                .orElseThrow(() -> new Exception("Usuario no encontrado con email: " + clienteEmail));
        ObjectId clienteId = cliente.getId();

        // 3. Preparar cambios
        Map<String, String> cambios = new HashMap<>();

        // Actualizar título si no es nulo y es diferente
        if (editarReporteDTO.titulo() != null && !editarReporteDTO.titulo().isBlank() &&
                !editarReporteDTO.titulo().equals(reporte.getTitulo())) {
            cambios.put("titulo", reporte.getTitulo() + " → " + editarReporteDTO.titulo());
            reporte.setTitulo(editarReporteDTO.titulo());
        }

        // Actualizar descripción si no es nulo y es diferente
        if (editarReporteDTO.descripcion() != null && !editarReporteDTO.descripcion().isBlank() &&
                !editarReporteDTO.descripcion().equals(reporte.getDescripcion())) {
            cambios.put("descripcion", "Descripción modificada");
            reporte.setDescripcion(editarReporteDTO.descripcion());
        }

        // Actualizar fotos si no es nulo
        if (editarReporteDTO.fotos() != null) {
            cambios.put("fotos", "Fotos actualizadas");
            reporte.setFotos(editarReporteDTO.fotos());
        }

        // Actualizar categoría si no es nulo y es válido
        if (editarReporteDTO.idCategoria() != null && !editarReporteDTO.idCategoria().isBlank() &&
                ObjectId.isValid(editarReporteDTO.idCategoria())) {
            ObjectId categoriaId = new ObjectId(editarReporteDTO.idCategoria());
            Optional<Categoria> categoria = categoriaRepo.findById(categoriaId);

            if (categoria.isPresent()) {
                cambios.put("categoria", "Categoría actualizada");
                reporte.setCategoria(categoriaId);
            }
        }

        // Actualizar ubicación si no es nulo
        if (editarReporteDTO.ubicacion() != null) {
            if (reporte.getUbicacion() == null) {
                reporte.setUbicacion(new Ubicacion());
            }

            UbicacionDTO ubicacionDTO = editarReporteDTO.ubicacion();
            reporte.getUbicacion().setLatitud(ubicacionDTO.latitud());
            reporte.getUbicacion().setLongitud(ubicacionDTO.longitud());
            cambios.put("ubicacion", "Ubicación actualizada");
        }

        // 4. Registrar en historial si hay cambios
        if (!cambios.isEmpty()) {
            HistorialReporte historial = new HistorialReporte();
            historial.setClienteId(clienteId); // Usar el ID obtenido del email
            historial.setObservaciones("Edición manual por " + clienteEmail);
            historial.setEstado(reporte.getEstadoActual());
            historial.setFecha(LocalDateTime.now());
            historial.setCambios(cambios);

            if (reporte.getHistorial() == null) {
                reporte.setHistorial(new ArrayList<>());
            }
            reporte.getHistorial().add(historial);

            // 5. Guardar
            reporteRepo.save(reporte);
        }
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

        // Obtener información del usuario que está eliminando
        String emailUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean esAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        // Preparar detalles de cambios para el historial
        Map<String, String> cambios = new HashMap<>();
        cambios.put("estado", "De " + estadoAnterior + " a ELIMINADO");
        cambios.put("eliminadoPor", emailUsuario);
        cambios.put("tipoUsuario", esAdmin ? "ADMINISTRADOR" : "CLIENTE");

        // Buscar el usuario por su email para obtener su ID
        Usuario usuario = usuarioRepo.findByEmail(emailUsuario)
                .orElse(null);
        ObjectId usuarioId = usuario != null ? usuario.getId() : null;

        // Registrar en historial con más detalles
        HistorialReporte entradaHistorial = new HistorialReporte();
        entradaHistorial.setObservaciones("Reporte eliminado " + (esAdmin ? "por un administrador" : "por su creador"));
        entradaHistorial.setEstado(EstadoReporte.ELIMINADO);
        entradaHistorial.setFecha(LocalDateTime.now());
        entradaHistorial.setCambios(cambios);

        if (usuarioId != null) {
            entradaHistorial.setClienteId(usuarioId);
        }

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

    /**
     * Lista todos los reportes creados por un usuario específico
     * @param idUsuario ID del usuario del que se listarán los reportes
     * @return Lista de ReporteDTO con los reportes del usuario
     * @throws Exception si ocurre algún error
     */
    @Override
    public List<ReporteDTO> listarReportesPorUsuario(String idUsuario) throws Exception {
        if (!ObjectId.isValid(idUsuario)) {
            throw new Exception("ID de usuario inválido");
        }

        ObjectId usuarioObjectId = new ObjectId(idUsuario);
        List<Reporte> reportes = reporteRepo.findByIdUsuario(usuarioObjectId);

        return reportes.stream()
                .map(reporteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReporteDTO> listarTodos() {
        return reporteRepo.findAll().stream()
                .map(reporteMapper::toDto)
                .toList();
    }

    @Override
    public List<ReporteDTO> listarTodos(String nombre, String ciudad, String categoria) {
        System.out.println("Buscando reportes con filtros - nombre: " + nombre + ", ciudad: " + ciudad + ", categoria: " + categoria);

        // En lugar de usar una consulta geoespacial, vamos a obtener todos los reportes
        // que coincidan con los otros filtros y luego filtrar por distancia en memoria

        // Construir la consulta para nombre y categoría
        org.bson.Document queryDoc = new org.bson.Document();

        if (nombre != null && !nombre.isBlank()) {
            queryDoc.append("titulo", new org.bson.Document("$regex", nombre).append("$options", "i"));
        }

        if (categoria != null && !categoria.isBlank()) {
            if (ObjectId.isValid(categoria)) {
                // Si es un ObjectId válido, buscar por el ObjectId
                System.out.println("Buscando por categoría con ObjectId: " + categoria);
                queryDoc.append("categoria", new ObjectId(categoria));
            } else {
                // Si no es un ObjectId, intentar buscar por el nombre de categoría
                System.out.println("Buscando por categoría como String: " + categoria);
                queryDoc.append("categoria", categoria);
            }
        }

        System.out.println("MongoDB Query inicial: " + queryDoc.toJson());

        // Obtener los reportes que coinciden con nombre y categoría
        List<org.bson.Document> resultadosDB = mongoTemplate.getCollection("reportes")
                .find(queryDoc)
                .into(new ArrayList<>());

        System.out.println("Reportes encontrados antes de filtrar por ciudad: " + resultadosDB.size());

        // Si se especificó una ciudad, filtrar por distancia en memoria
        List<org.bson.Document> resultadosFiltrados = resultadosDB;

        if (ciudad != null && !ciudad.isBlank()) {
            System.out.println("Filtrando por ciudad: " + ciudad);

            // Obtener coordenadas para la ciudad
            CoordenadasCiudad coordenadas = obtenerCoordenadasCiudad(ciudad);
            if (coordenadas != null) {
                System.out.println("Coordenadas encontradas para " + ciudad + ": " +
                        coordenadas.latitud + ", " + coordenadas.longitud +
                        " con radio de " + coordenadas.radioKm + " km");

                // Filtrar los reportes por distancia
                final double radioKm = coordenadas.radioKm;
                final double latitudCiudad = coordenadas.latitud;
                final double longitudCiudad = coordenadas.longitud;

                resultadosFiltrados = resultadosDB.stream()
                        .filter(doc -> {
                            try {
                                // Extraer las coordenadas del reporte
                                org.bson.Document ubicacionDoc = (org.bson.Document) doc.get("ubicacion");
                                if (ubicacionDoc != null) {
                                    Double latitud = ubicacionDoc.getDouble("latitud");
                                    Double longitud = ubicacionDoc.getDouble("longitud");

                                    if (latitud != null && longitud != null) {
                                        // Calcular la distancia entre las coordenadas
                                        double distancia = calcularDistanciaHaversine(
                                                latitudCiudad, longitudCiudad,
                                                latitud, longitud
                                        );

                                        System.out.println("Reporte " + doc.getObjectId("_id") +
                                                " distancia: " + distancia + " km");

                                        // Conservar reportes dentro del radio de la ciudad
                                        return distancia <= radioKm;
                                    }
                                }
                                return false;
                            } catch (Exception e) {
                                System.out.println("Error al calcular distancia para reporte: " + e.getMessage());
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
            } else {
                System.out.println("Ciudad no reconocida: " + ciudad);
                // Si no reconocemos la ciudad, devolver lista vacía
                resultadosFiltrados = new ArrayList<>();
            }
        }

        System.out.println("Reportes encontrados después de filtrar por ciudad: " + resultadosFiltrados.size());

        // Convertir los documentos a objetos Reporte y luego a DTOs
        List<Reporte> reportes = resultadosFiltrados.stream()
                .map(doc -> mongoTemplate.getConverter().read(Reporte.class, doc))
                .collect(Collectors.toList());

        return reportes.stream()
                .map(reporteMapper::toDto)
                .toList();
    }

    /**
     * Clase auxiliar para almacenar coordenadas de ciudades
     */
    private static class CoordenadasCiudad {
        double latitud;
        double longitud;
        double radioKm; // Radio aproximado de la ciudad en kilómetros

        public CoordenadasCiudad(double latitud, double longitud, double radioKm) {
            this.latitud = latitud;
            this.longitud = longitud;
            this.radioKm = radioKm;
        }
    }

    /**
     * Método para obtener las coordenadas de una ciudad por su nombre
     * @param nombreCiudad Nombre de la ciudad
     * @return Objeto con las coordenadas y radio, o null si no se reconoce la ciudad
     */
    private CoordenadasCiudad obtenerCoordenadasCiudad(String nombreCiudad) {
        // Normalizar el nombre de la ciudad (quitar acentos, convertir a minúsculas)
        String ciudadNormalizada = nombreCiudad.toLowerCase()
                .replaceAll("[áàäâã]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöôõ]", "o")
                .replaceAll("[úùüû]", "u");

        // Mapa de ciudades con sus coordenadas (latitud, longitud, radio en km)
        Map<String, CoordenadasCiudad> coordenadasCiudades = new HashMap<>();

        // Ciudades principales de Colombia
        coordenadasCiudades.put("armenia", new CoordenadasCiudad(4.5387911, -75.6699968, 5));
        coordenadasCiudades.put("bogota", new CoordenadasCiudad(4.7110, -74.0721, 15));
        coordenadasCiudades.put("medellin", new CoordenadasCiudad(6.2442, -75.5812, 10));
        coordenadasCiudades.put("cali", new CoordenadasCiudad(3.4516, -76.5320, 10));
        coordenadasCiudades.put("barranquilla", new CoordenadasCiudad(10.9685, -74.7813, 8));
        coordenadasCiudades.put("cartagena", new CoordenadasCiudad(10.3910, -75.4794, 8));
        coordenadasCiudades.put("pereira", new CoordenadasCiudad(4.8143, -75.6946, 5));
        coordenadasCiudades.put("manizales", new CoordenadasCiudad(5.0687, -75.5173, 5));
        coordenadasCiudades.put("bucaramanga", new CoordenadasCiudad(7.1254, -73.1198, 7));
        coordenadasCiudades.put("cucuta", new CoordenadasCiudad(7.8939, -72.5078, 6));
        coordenadasCiudades.put("ibague", new CoordenadasCiudad(4.4389, -75.2322, 5));
        coordenadasCiudades.put("pasto", new CoordenadasCiudad(1.2136, -77.2811, 4));
        coordenadasCiudades.put("santa marta", new CoordenadasCiudad(11.2404, -74.1996, 6));
        coordenadasCiudades.put("villavicencio", new CoordenadasCiudad(4.1533, -73.6351, 5));

        // Intentar encontrar la ciudad en el mapa
        for (Map.Entry<String, CoordenadasCiudad> entry : coordenadasCiudades.entrySet()) {
            if (ciudadNormalizada.contains(entry.getKey()) ||
                    entry.getKey().contains(ciudadNormalizada)) {
                return entry.getValue();
            }
        }

        // Si no se encuentra la ciudad, retornar null
        return null;
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
        // Obtener el reporte
        Reporte reporte = obtenerReporte(idReporte);

        // Crear y configurar el comentario
        Comentario comentario = new Comentario();
        comentario.setId(new ObjectId());
        comentario.setIdUsuario(new ObjectId(comentarioDTO.idUsuario()));
        comentario.setContenido(comentarioDTO.contenido());
        comentario.setFecha(LocalDateTime.now());

        // Añadir el comentario al reporte
        reporte.getComentarios().add(comentario);

        // Obtener el usuario que creó el reporte (dueño del reporte)
        Optional<Usuario> optionalDuenoReporte = usuarioRepo.findById(reporte.getIdUsuario());
        if (optionalDuenoReporte.isEmpty()) {
            throw new Exception("No se encontró el dueño del reporte");
        }

        // Obtener el usuario que hizo el comentario
        Optional<Usuario> optionalComentarista = usuarioRepo.findById(new ObjectId(comentarioDTO.idUsuario()));
        String nombreComentarista = optionalComentarista.isPresent() ?
                optionalComentarista.get().getNombre() :
                "Un usuario";

        // Enviar correo al dueño del reporte
        Usuario duenoReporte = optionalDuenoReporte.get();
        emailServicio.enviarCorreo(new EnviarCorreoDTO(
                duenoReporte.getEmail(),
                "Nuevo comentario en tu reporte: " + reporte.getTitulo(),
                nombreComentarista + " ha comentado en tu reporte:\n\n" + comentarioDTO.contenido()
        ));

        // Guardar el reporte actualizado
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
        // Validar role usuario
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

        // NUEVO CÓDIGO: Enviar notificación por correo al dueño del reporte
        try {
            // Obtener el dueño del reporte
            Usuario duenoReporte = usuarioRepo.findById(reporte.getIdUsuario())
                    .orElseThrow(() -> new Exception("No se encontró el dueño del reporte"));

            // Construir el asunto y cuerpo del correo
            String asunto = "Estado de tu reporte actualizado: " + reporte.getTitulo();
            String cuerpo = "Hola " + duenoReporte.getNombre() + ",\n\n" +
                    "Tu reporte \"" + reporte.getTitulo() + "\" ha sido actualizado.\n\n" +
                    "Estado anterior: " + cambios.get("estadoAnterior") + "\n" +
                    "Nuevo estado: " + cambios.get("estadoNuevo") + "\n\n" +
                    "Motivo: " + gestionEstadoReporteDTO.motivo() + "\n\n" +
                    "Gracias por usar nuestro sistema de reportes.";

            // Enviar correo
            emailServicio.enviarCorreo(new EnviarCorreoDTO(
                    duenoReporte.getEmail(),
                    asunto,
                    cuerpo
            ));

            // NUEVO CÓDIGO: Crear notificación en la plataforma
            NotificacionDTO notificacionDTO = new NotificacionDTO(
                    null,
                    "Tu reporte \"" + reporte.getTitulo() + "\" ha sido actualizado a estado " + nuevoEstado,
                    LocalDateTime.now(),
                    "ESTADO_REPORTE",
                    false,
                    reporte.getId().toString(),
                    duenoReporte.getId().toString(),
                    "Estado de reporte actualizado"
            );

            // Crear la notificación en base de datos y enviar por WebSocket
            notificacionServicio.crearNotificacion(notificacionDTO);

        } catch (Exception e) {
            // Registramos el error pero no interrumpimos el flujo principal
            System.err.println("Error al enviar notificación: " + e.getMessage());
            e.printStackTrace();
        }
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

        // NUEVO CÓDIGO: Enviar notificaciones a administradores
        try {
            // Encontrar todos los administradores
            List<Usuario> administradores = usuarioRepo.findByRol(Rol.ADMINISTRADOR);

            // Enviar correo a cada administrador
            for (Usuario admin : administradores) {
                // Construir el asunto y cuerpo del correo
                String asunto = "Reporte actualizado por el cliente: " + reporte.getTitulo();
                String cuerpo = "Hola " + admin.getNombre() + ",\n\n" +
                        "El usuario " + usuario.getNombre() + " ha actualizado el estado de su reporte \"" + reporte.getTitulo() + "\".\n\n" +
                        "Estado anterior: " + cambios.get("estadoAnterior") + "\n" +
                        "Nuevo estado: " + cambios.get("estadoNuevo") + "\n\n" +
                        "Motivo: " + gestionEstadoReporteDTO.motivo() + "\n\n" +
                        "Por favor revisa el sistema para más detalles.";

                // Enviar correo
                emailServicio.enviarCorreo(new EnviarCorreoDTO(
                        admin.getEmail(),
                        asunto,
                        cuerpo
                ));

                NotificacionDTO notificacionDTO = new NotificacionDTO(
                        null,
                        "El usuario " + usuario.getNombre() + " ha actualizado su reporte \"" + reporte.getTitulo() + "\" a estado " + nuevoEstado,
                        LocalDateTime.now(),
                        "ESTADO_REPORTE_CLIENTE",
                        false,
                        reporte.getId().toString(),
                        admin.getId().toString(),  // Usar el ID del administrador
                        "Reporte actualizado por cliente"
                );

                // Crear la notificación en base de datos y enviar por WebSocket
                notificacionServicio.crearNotificacion(notificacionDTO);
            }
        } catch (Exception e) {
            // Registramos el error pero no interrumpimos el flujo principal
            System.err.println("Error al enviar notificaciones a administradores: " + e.getMessage());
            e.printStackTrace();
        }
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

        // Crear el documento PDF - Usar el nombre completo para evitar ambigüedad
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
        com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);

        document.open();

        // Añadir título
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Informe de Reportes por Categoría", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(title);
        document.add(new com.itextpdf.text.Paragraph(" ")); // Espacio

        // Añadir información del informe
        if (categoria != null && !categoria.isBlank()) {
            document.add(new com.itextpdf.text.Paragraph("Categoría: " + categoria));
        } else {
            document.add(new com.itextpdf.text.Paragraph("Todas las categorías"));
        }

        if (fechaInicio != null) {
            document.add(new com.itextpdf.text.Paragraph("Fecha Inicio: " + fechaInicio));
        }
        if (fechaFin != null) {
            document.add(new com.itextpdf.text.Paragraph("Fecha Fin: " + fechaFin));
        }
        document.add(new com.itextpdf.text.Paragraph("Total de Reportes: " + informe.totalReportes()));
        document.add(new com.itextpdf.text.Paragraph(" ")); // Espacio

        // Añadir distribución por estados
        document.add(new com.itextpdf.text.Paragraph("Distribución por Estado:",
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD)));

        for (Map.Entry<String, Long> entry : informe.distribucionEstados().entrySet()) {
            document.add(new com.itextpdf.text.Paragraph(entry.getKey() + ": " + entry.getValue() + " reportes"));
        }
        document.add(new com.itextpdf.text.Paragraph(" ")); // Espacio

        // Crear tabla para los reportes
        com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(4); // 4 columnas
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

        // Crear el documento PDF - Usar el nombre completo para evitar ambigüedad
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
        com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);

        document.open();

        // Añadir título
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Informe de Reportes por Ubicación", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(title);
        document.add(new com.itextpdf.text.Paragraph(" ")); // Espacio

        // Añadir información del informe
        document.add(new com.itextpdf.text.Paragraph("Ubicación: Latitud: " + latitud + ", Longitud: " + longitud));
        document.add(new com.itextpdf.text.Paragraph("Radio: " + radioKm + " km"));
        if (fechaInicio != null) {
            document.add(new com.itextpdf.text.Paragraph("Fecha Inicio: " + fechaInicio));
        }
        if (fechaFin != null) {
            document.add(new com.itextpdf.text.Paragraph("Fecha Fin: " + fechaFin));
        }
        document.add(new com.itextpdf.text.Paragraph("Total de Reportes: " + informe.totalReportes()));
        document.add(new com.itextpdf.text.Paragraph(" ")); // Espacio

        // Añadir estadísticas por categoría
        document.add(new com.itextpdf.text.Paragraph("Distribución por Categoría:",
                new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD)));

        for (Map.Entry<String, Long> entry : informe.reportesPorCategoria().entrySet()) {
            document.add(new com.itextpdf.text.Paragraph(entry.getKey() + ": " + entry.getValue() + " reportes"));
        }
        document.add(new com.itextpdf.text.Paragraph(" ")); // Espacio

        // Crear tabla para los reportes
        com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(5); // 5 columnas
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
    private void addTableHeader(com.itextpdf.text.pdf.PdfPTable table, String[] headers) {
        for (String header : headers) {
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell();
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            cell.setPadding(5);
            cell.setPhrase(new com.itextpdf.text.Phrase(header,
                    new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD)));
            table.addCell(cell);
        }
    }



    /*
    IMPLEMENTACIÓN PARA LA CREACIÓN DE UN REPORTE ANONIMO
     */

    @Override
    public String crearReporteAnonimo(CrearReporteAnonimoDTO dto) throws Exception {
        // Validaciones básicas
        if(dto.titulo().isBlank()) {
            throw new Exception("El título es obligatorio");
        }

        if(dto.descripcion().isBlank()) {
            throw new Exception("La descripción es obligatoria");
        }

        // Convertir DTO a documento
        Reporte reporte = reporteMapper.toDocumentFromAnonimo(dto);

        // Guardar el reporte
        Reporte reporteGuardado = reporteRepo.save(reporte);

        // Devolver el ID del reporte creado
        return reporteGuardado.getId().toString();
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

        // Obtener email del usuario que rechaza
        String emailAdmin = SecurityContextHolder.getContext().getAuthentication().getName();

        // Obtener el administrador que rechaza (para su nombre)
        Usuario admin = usuarioRepo.findByEmail(emailAdmin)
                .orElse(null); // Podría ser null si no se encuentra, pero debería existir

        String nombreAdmin = (admin != null) ? admin.getNombre() : "Un administrador";

        // Crear entrada en el historial
        Map<String, String> cambios = new HashMap<>();
        cambios.put("estado", "De PENDIENTE a RECHAZADO");
        cambios.put("justificacion", dto.justificacion());
        cambios.put("administrador", emailAdmin);

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

        // NUEVO CÓDIGO: Enviar notificación al creador del reporte
        try {
            // Solo enviar notificación si el reporte tiene un usuario asociado (no es anónimo)
            if (reporte.getIdUsuario() != null) {
                // Obtener el usuario creador del reporte
                Usuario usuarioCreador = usuarioRepo.findById(reporte.getIdUsuario())
                        .orElse(null);

                if (usuarioCreador != null) {
                    // Construir el asunto y cuerpo del correo
                    String asunto = "Tu reporte ha sido rechazado: " + reporte.getTitulo();
                    String cuerpo = "Hola " + usuarioCreador.getNombre() + ",\n\n" +
                            "Tu reporte \"" + reporte.getTitulo() + "\" ha sido rechazado.\n\n" +
                            "Justificación: " + dto.justificacion() + "\n\n" +
                            "Si tienes alguna pregunta, por favor contacta con el administrador.\n\n" +
                            "Gracias por usar nuestro sistema de reportes.";

                    // Enviar correo
                    emailServicio.enviarCorreo(new EnviarCorreoDTO(
                            usuarioCreador.getEmail(),
                            asunto,
                            cuerpo
                    ));

                    // Crear notificación en la plataforma
                    NotificacionDTO notificacionDTO = new NotificacionDTO(
                            null,
                            "Tu reporte \"" + reporte.getTitulo() + "\" ha sido rechazado. Motivo: " + dto.justificacion(),
                            LocalDateTime.now(),
                            "REPORTE_RECHAZADO",
                            false,
                            reporte.getId().toString(),
                            usuarioCreador.getId().toString(),
                            "Reporte rechazado"
                    );

                    // Crear la notificación en base de datos y enviar por WebSocket
                    notificacionServicio.crearNotificacion(notificacionDTO);
                }
            }
        } catch (Exception e) {
            // Registramos el error pero no interrumpimos el flujo principal
            System.err.println("Error al enviar notificación de rechazo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<ReporteDTO> listarReportesPorEmailUsuario(String emailUsuario) throws Exception {
        System.out.println("Buscando reportes para el usuario con email: " + emailUsuario);

        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new Exception("Email de usuario inválido");
        }

        try {
            // Primero, buscar el usuario por su email para obtener su ID
            Optional<Usuario> usuario = usuarioRepo.findByEmail(emailUsuario);

            if (usuario.isEmpty()) {
                System.out.println("No se encontró ningún usuario con el email: " + emailUsuario);
                throw new Exception("Usuario no encontrado con email: " + emailUsuario);
            }

            ObjectId idUsuario = usuario.get().getId();
            System.out.println("ID del usuario encontrado: " + idUsuario);

            // Usando el ID, buscar sus reportes
            List<Reporte> reportes = reporteRepo.findByIdUsuario(idUsuario);
            System.out.println("Reportes encontrados: " + reportes.size());

            // Convertir a DTOs
            return reportes.stream()
                    .map(reporteMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Error al buscar reportes por email: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error al buscar reportes: " + e.getMessage());
        }
    }

    /**
     * Verifica si un usuario es propietario de un reporte
     * @param idReporte ID del reporte a verificar
     * @param emailUsuario Email del usuario a verificar
     * @return true si el usuario es propietario del reporte, false en caso contrario
     * @throws Exception si ocurre algún error
     */
    @Override
    public boolean verificarPropietarioReporte(String idReporte, String emailUsuario) throws Exception {
        System.out.println("Verificando propiedad del reporte: " + idReporte);
        System.out.println("Email del usuario actual: " + emailUsuario);

        if (!ObjectId.isValid(idReporte)) {
            throw new Exception("ID de reporte inválido");
        }

        // Obtener el reporte
        ObjectId reporteObjectId = new ObjectId(idReporte);
        Reporte reporte = reporteRepo.findById(reporteObjectId)
                .orElseThrow(() -> new Exception("Reporte no encontrado"));

        // Obtener el ID del usuario del reporte
        ObjectId idUsuarioReporte = reporte.getIdUsuario();
        System.out.println("ID del usuario del reporte: " + idUsuarioReporte);

        if (idUsuarioReporte == null) {
            System.out.println("Reporte sin usuario asociado (anónimo)");
            return false; // Reporte anónimo
        }

        // Buscar el usuario por su ID para obtener su email
        Optional<Usuario> usuarioReporte = usuarioRepo.findById(idUsuarioReporte);
        if (usuarioReporte.isEmpty()) {
            System.out.println("No se encontró el usuario propietario del reporte con ID: " + idUsuarioReporte);
            return false;
        }

        // Comparar el email del usuario del reporte con el email actual
        String emailPropietario = usuarioReporte.get().getEmail();
        System.out.println("Email del propietario del reporte: " + emailPropietario);
        System.out.println("Email del usuario actual: " + emailUsuario);

        boolean esPropietario = emailPropietario.equals(emailUsuario);
        System.out.println("¿Es propietario? " + esPropietario);

        return esPropietario;
    }


}