package co.edu.uniquindio.proyecto;

import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.Ciudad;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoUsuario;
import co.edu.uniquindio.proyecto.modelo.enums.Rol;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;


@SpringBootApplication
public class ProyectoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProyectoApplication.class, args);
    }


    @Bean
    CommandLineRunner init(UsuarioRepo usuarioRepository) {
        return args -> {
            Usuario usuario = Usuario.builder()
                    .nombre("Salomé")
                    .ciudad(Ciudad.ARMENIA) // Usa uno válido de tu enum
                    .direccion("Calle 123")
                    .email("salome@correo.com")
                    .telefono("1234567890")
                    .password("claveSegura")
                    .rol(Rol.ADMINISTRADOR) // Usa un valor real de tu enum
                    .estado(EstadoUsuario.ACTIVO) // También uno válido
                    .fechaRegistro(LocalDateTime.now())
                    .build();

            usuarioRepository.save(usuario);
            System.out.println("✅ Usuario insertado en MongoDB");
        };
    }













}