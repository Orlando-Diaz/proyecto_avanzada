package co.edu.uniquindio.proyecto.dto;

import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CrearUsuarioDTO(
        @NotBlank @Length(max = 100) String nombre,
        @Length(max = 10) String telefono,
        Ciudad ciudad,
        @NotBlank @Length(max = 100) String direccion,
        @NotBlank @Length(max = 50) @Email String email,
        @NotBlank @Length(min = 7, max = 20) String password
) {
}


