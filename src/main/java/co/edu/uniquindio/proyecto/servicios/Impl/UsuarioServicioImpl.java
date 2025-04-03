package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.CrearUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.EditarUsuarioDTO;
import co.edu.uniquindio.proyecto.dto.UsuarioDTO;
import co.edu.uniquindio.proyecto.mapper.UsuarioMapper;
import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import co.edu.uniquindio.proyecto.modelo.enums.Rol;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.servicios.interfaces.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import org.springframework.data.mongodb.core.query.Query;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServicioImpl implements UsuarioServicio {

    private final UsuarioRepo usuarioRepo;
    private final UsuarioMapper usuarioMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public void crear(CrearUsuarioDTO crearUsuarioDTO) throws Exception {

        if(existeEmail(crearUsuarioDTO.email()) ){
            throw new Exception("El correo "+crearUsuarioDTO.email()+" ya está en uso");
        }
        Usuario usuario = usuarioMapper.toDocument(crearUsuarioDTO);
        usuarioRepo.save(usuario);
    }

    @Override
    public void eliminar(String id) throws Exception {

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

        //Obtenemos el usuario que se quiere eliminar y le asignamos el estado eliminado
        Usuario usuario = usuarioOptional.get();
        usuario.setEstado(EstadoUsuario.ELIMINADO);

        //Como el objeto usuario ya tiene un id, el save() no crea un nuevo registro sino que actualiza el que ya existe
        usuarioRepo.save(usuario);
    }

    @Override
    public void editar(EditarUsuarioDTO editarUsuarioDTO) throws Exception {

        //Validamos el id
        if (!ObjectId.isValid(editarUsuarioDTO.id())) {
            throw new Exception("No se encontró el usuario con el id "+editarUsuarioDTO.id());
        }

        //Buscamos el usuario que se quiere actualizar
        ObjectId objectId = new ObjectId(editarUsuarioDTO.id());
        Optional<Usuario> usuarioOptional = usuarioRepo.findById(objectId);

        //Si no se encontró el usuario, lanzamos una excepción
        if(usuarioOptional.isEmpty()){
            throw new Exception("No se encontró el usuario con el id "+editarUsuarioDTO.id());
        }

        // Mapear los datos actualizados al usuario existente
        Usuario usuario = usuarioOptional.get();
        usuarioMapper.toDocument(editarUsuarioDTO, usuario);

        //Como el objeto usuario ya tiene un id, el save() no crea un nuevo registro sino que actualiza el que ya existe
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


    private boolean existeEmail(String email) {
        return usuarioRepo.findByEmail(email).isPresent();
    }
}

