package co.edu.uniquindio.proyecto.excepciones;

public class UsuarioInexistente extends RuntimeException {
    public UsuarioInexistente(String message) {
        super(message);
    }
}
