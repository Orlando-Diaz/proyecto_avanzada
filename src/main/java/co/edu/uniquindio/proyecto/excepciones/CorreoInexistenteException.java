package co.edu.uniquindio.proyecto.excepciones;

public class CorreoInexistenteException extends RuntimeException {
    public CorreoInexistenteException(String message) {
        super(message);
    }
}
