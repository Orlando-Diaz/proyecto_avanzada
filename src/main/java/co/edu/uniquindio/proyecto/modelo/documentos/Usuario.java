package co.edu.uniquindio.proyecto.modelo.documentos;

import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import co.edu.uniquindio.proyecto.modelo.enums.Rol;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document("usuarios")
public class Usuario {

    @Id
    @EqualsAndHashCode.Include
    private ObjectId id;

    private String nombre;
    private Ciudad ciudad;
    private String direccion;
    private String email;
    private String telefono;
    private String password;
    private Rol rol;
    private EstadoUsuario estado;
    private LocalDateTime fechaRegistro; //FECHA DE REGISTRO DE LA CUENTA. NO CAMBIARA EN EL TIEMPO

    private String codigoValidacion; //CODIGO QUE SE USARA PARA ACTIVACION DE CUENTA Y RECUPERACION DE CONTRASENIA. CAMBIARA EN EL TIEMPO
    private LocalDateTime fechaCodigoValidacion; //Fecha de generacion del codigo de validacion.


    @Builder
    public Usuario(String nombre, Ciudad ciudad, String direccion, String email, String telefono,
                   String password, Rol rol, EstadoUsuario estado, LocalDateTime fechaRegistro,
                   String codigoValidacion, LocalDateTime fechCodigoValidacion) {
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
        this.password = password;
        this.rol = rol;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;

        //Agregados el 04/09
        this.codigoValidacion = codigoValidacion;
        this.fechaCodigoValidacion = fechCodigoValidacion;
    }
}

