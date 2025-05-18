package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.Rol;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepo extends MongoRepository<Usuario, ObjectId> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findById(ObjectId id);

    /**
     * Busca usuarios por su rol
     * @param rol Rol de los usuarios a buscar
     * @return Lista de usuarios con el rol especificado
     */
    List<Usuario> findByRol(Rol rol);

}
