package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.*;
import co.edu.uniquindio.proyecto.excepciones.*;
import co.edu.uniquindio.proyecto.mapper.UsuarioMapper;
import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.servicios.interfaces.EmailServicio;
import co.edu.uniquindio.proyecto.servicios.interfaces.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.data.mongodb.core.query.Query;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UsuarioServicioImpl implements UsuarioServicio {

    private final UsuarioRepo usuarioRepo;
    private final UsuarioMapper usuarioMapper;
    private final MongoTemplate mongoTemplate;
    private final EmailServicio emailServicio;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, String> codigosVerificacion = new HashMap<>();

    @Autowired
    private JavaMailSender mailSender;


    @Override
    public void crear(CrearUsuarioDTO crearUsuarioDTO) throws Exception {
        if(existeEmail(crearUsuarioDTO.email())) {
            throw new CorreoEnUsoException("El email "+crearUsuarioDTO.email()+" ya está en uso");
        }

        Usuario usuario = usuarioMapper.toDocument(crearUsuarioDTO);
        usuario.setEstado(EstadoUsuario.INACTIVO);
        usuario.setPassword(passwordEncoder.encode(crearUsuarioDTO.password())); // Encriptar


        String codigo=generarCodigo();

        //Se asigna el codigo de validacion y se envia al email del usuario
        usuario.setCodigoValidacion(codigo);

        emailServicio.enviarCorreo(new EnviarCorreoDTO(crearUsuarioDTO.email(),"RECUPERACION",
                "CODIGO DE RECUPERACION DE CONTRASENIA "+codigo));

        usuario.setFechaCodigoValidacion(LocalDateTime.now()); //FECHA CODIGO DE VALIDACION. SERA USADO PARA VALIDAR 15 MIN DE VALIDEZ

        usuario.setFechaRegistro(LocalDateTime.now()); //FECHA DE REGISTRO DE LA CUENTA

        usuarioRepo.save(usuario);

    }

    /**
     * Metodo que modifica el estado de un usuario
     * @param email correo al que se le modificara el estado
     * @param estadoUsuarioDTO ACTIVO/INACTIVO/ELIMINADO
     * @throws Exception
     */
    public void modificarEstadoCuentaUsuario(String email, EstadoUsuarioDTO estadoUsuarioDTO) throws Exception {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new CorreoInexistenteException("Usuario no encontrado"));

        if (estadoUsuarioDTO.nuevoEstado()==null){
            throw new EstadoCuentaInvalidoException("ERROR. ESTADO NULL");
        }

        //Validar que su estado actual no sea ELIMINADO
        if (usuario.getEstado().equals(EstadoUsuario.ELIMINADO)) {
            throw new Exception("ERROR: EL USUARIO "+usuario.getEmail()+" YA HA SIDO ELIMINADO PREVIAMENTE");
        }

        //Validar que el estado actual del usuario sea distinto al que llega por parametro
        if (usuario.getEstado().equals(estadoUsuarioDTO.nuevoEstado())) {
            throw new Exception("ERROR: EL USUARIO "+usuario.getEmail()+" YA HA TIENE EL ESTADO A MODIFICAR");
        }
        usuario.setEstado(estadoUsuarioDTO.nuevoEstado());

        usuarioRepo.save(usuario);
    }

    /**
     * Verifica si el código ingresado por el usuario es correcto y aún está dentro del tiempo permitido (15 minutos).
     * Si ambas condiciones se cumplen, la cuenta queda activa, de lo conntrario lanza una excepcion
     * @param email correo del usuario a verificar.
     * @param codigo Código ingresado por el usuario.
     * @return true si el código es válido y vigente.
     * @throws UsuarioInexistente si no se encuentra el usuario.
     * @throws CodigoVerificacionNoCoincideException si el código no coincide con el registrado.
     */
    @Override
    public void verificarCodigoActivarUsuario(String email, String codigo) throws Exception {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new CorreoInexistenteException("Correo no encontrado en el sistema"));

        // Validar que el código coincida
        if (!usuario.getCodigoValidacion().equals(codigo)) {
            throw new CodigoVerificacionNoCoincideException("ERROR. El código ingresado no es correcto");
        }


        // Validar que no hayan pasado más de 15 minutos desde que se generó el código
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaGeneracion = usuario.getFechaCodigoValidacion();

        Duration duracion = Duration.between(fechaGeneracion, ahora);
        if (duracion.toMinutes() > 15) {
            throw new CodigoExpiradoException("ERROR. EL CODIGO YA HA EXPIRADO"); // código expirado
        }

        if (usuario.getEstado().equals(EstadoUsuario.ACTIVO)){
            throw new EstadoCuentaInvalidoException("ERROR. LA CUENTA YA HA SIDO ACTIVADA");
        }
        usuario.setEstado(EstadoUsuario.ACTIVO); // código válido y vigente
        usuarioRepo.save(usuario);
    }


    /**
     * Metodo que genera un codigo de 6 caracteres
     * @return codigo
     */
    private String generarCodigo() {

        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(caracteres.length());
            sb.append(caracteres.charAt(index));
        }

        return sb.toString();
    }

    @Override
    public void eliminar(String id) throws Exception {

        //Validamos el id
        if (!ObjectId.isValid(id)) {
            throw new Exception("No se encontró el usuario con el id "+id);
        }

        ObjectId objectId = new ObjectId(id);
        Optional<Usuario> usuarioOptional = usuarioRepo.findById(objectId);

        if(usuarioOptional.isEmpty()){
            throw new Exception("No se encontró el usuario con el id "+id);
        }

        Usuario usuario = usuarioOptional.get();
        usuario.setEstado(EstadoUsuario.ELIMINADO);

        usuarioRepo.save(usuario);
    }

    @Override
    public void editar(String id, EditarUsuarioDTO cuentaDTO) throws Exception {
        // Validar ID
        if (!ObjectId.isValid(id)) {
            throw new Exception("ID inválido");
        }

        // Buscar usuario
        Usuario usuario = usuarioRepo.findById(new ObjectId(id))
                .orElseThrow(() -> new Exception("Usuario no encontrado"));

        // Actualizar campos desde el DTO
        usuario.setNombre(cuentaDTO.nombre());
        usuario.setCiudad(cuentaDTO.ciudad());
        usuario.setDireccion(cuentaDTO.direccion());
        usuario.setTelefono(cuentaDTO.telefono());

        usuarioRepo.save(usuario);
    }


    @Override
    public UsuarioDTO obtener(String id) throws Exception {

        //Validamos el id
        if (!ObjectId.isValid(id)) {
            throw new Exception("No se encontró el usuario con el id "+id);
        }

        //Buscamos el usuario que se quiere obtener
        ObjectId objectId = new ObjectId(id);
        Optional<Usuario> usuarioOptional = usuarioRepo.findById(objectId);

        //Si no se encontró el usuario, lanzamos una excepción
        if(usuarioOptional.isEmpty()){
            throw new Exception("No se encontró el usuario con el id "+id);
        }

        //Retornamos el usuario encontrado convertido a DTO
        return usuarioMapper.toDTO(usuarioOptional.get());

    }

    @Override
    public List<UsuarioDTO> listarTodos(String nombre, String ciudad, int pagina) {

        if (pagina < 0) {
            throw new IllegalArgumentException("La página no puede ser menor a 0");
        }

        Criteria criteria = new Criteria();

        // Búsqueda parcial con regex (ej: "jua" encuentra "Juan")
        if (nombre != null && !nombre.isEmpty()) {
            criteria.and("nombre").regex(".*" + nombre + ".*", "i");
        }

        if (ciudad != null && !ciudad.isEmpty()) {
            criteria.and("ciudad").regex(".*" + ciudad + ".*", "i");
        }

        Query query = new Query(criteria).with(PageRequest.of(pagina, 5));

        // Corrección: Usuario.class como parámetro
        List<Usuario> usuarios = mongoTemplate.find(query, Usuario.class);

        return usuarios.stream()
                .map(usuarioMapper::toDTO)
                .toList();
    }


    /**
     * Servicio para recuperar una contrasenia de un USUARIO ACTIVO
     * @param recuperarPasswordDTO contiene el email del usuario que se le actualizara la contrasenia
     * @throws Exception
     */
    @Override
    public void recuperarContrasenia(RecuperarContraseniaDTO recuperarPasswordDTO) throws Exception {
        //Verificar que el email pertenezca a un usuario registrado

        if (!existeEmail(recuperarPasswordDTO.email())){
            throw new CorreoInexistenteException("ERROR. LA CONTRASENIA NO PUDO SER RECUPERADA, CORREO INEXISTENTE");
        }

        // Buscar usuario
        Usuario usuario = usuarioRepo.findByEmail(recuperarPasswordDTO.email())
                .orElseThrow(() -> new UsuarioInexistente("ERROR. Usuario no encontrado"));


        //Validar que el codigo sea solicitado una vez cada 2 minutos
        if (usuario.getFechaCodigoValidacion() != null &&
                Duration.between(usuario.getFechaCodigoValidacion(), LocalDateTime.now()).toMinutes() < 2) {
            throw new Exception("Debes esperar al menos 2 minutos para solicitar un nuevo código.");
        }

        //Enviar codigo
        String codigo = generarCodigo();
        //Se asigna el codigo de validacion y se envia al email del usuario
        usuario.setCodigoValidacion(codigo);
        usuario.setFechaCodigoValidacion(LocalDateTime.now()); //FECHA CODIGO DE VALIDACION. SERA USADO PARA VALIDAR 15 MIN DE VALIDEZ

        usuarioRepo.save(usuario);

        emailServicio.enviarCorreo(new EnviarCorreoDTO(recuperarPasswordDTO.email(),"RECUPERACION",
                "CODIGO DE RECUPERACION DE CONTRASENIA "+codigo));

    }

    /**
     * Servicio que valida que el codigo ingresado por el usuario sea valido y actualiza su
     * contrasenia si el codigo es correcto
     * @param cambiarPasswordDTO contiene el codigo a validar y la nueva contrasenia
     * @throws Exception
     */
    @Override
    public void cambiarContrasenia(CambiarPasswordDTO cambiarPasswordDTO) throws Exception {

        Usuario usuario = usuarioRepo.findByEmail(cambiarPasswordDTO.email())
                .orElseThrow(() -> new UsuarioInexistente("ERROR. Usuario no encontrado"));

        if (!usuario.getEstado().equals(EstadoUsuario.ACTIVO)){
            throw new EstadoCuentaInvalidoException("ERROR. LA CUENTA NO TIENE UN ESTADO VALIDO");
        }

        // Validar que el código coincida
        if (!usuario.getCodigoValidacion().equals(cambiarPasswordDTO.codigo())) {
            throw new CodigoVerificacionNoCoincideException("ERROR. El código ingresado no es correcto");
        }

        // Validar que no hayan pasado más de 15 minutos desde que se generó el código
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaGeneracion = usuario.getFechaCodigoValidacion();

        Duration duracion = Duration.between(fechaGeneracion, ahora);
        if (duracion.toMinutes() > 15) {
            throw new CodigoExpiradoException("ERROR. EL CODIGO YA HA EXPIRADO"); // código expirado
        }

        //almacenar nueva contrasenia
        usuario.setPassword(passwordEncoder.encode(cambiarPasswordDTO.nuevaPassword()));

        //Garantiza que el codigo no sea reutilizado
        usuario.setCodigoValidacion(null);
        usuario.setFechaCodigoValidacion(null);
        usuarioRepo.save(usuario);

    }


    private boolean existeEmail(String email) {
        return usuarioRepo.findByEmail(email).isPresent();
    }

}

