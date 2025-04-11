package co.edu.uniquindio.proyecto.excepciones;

public class CorreoEnUsoException extends RuntimeException {
    public CorreoEnUsoException(String message) {
        super(message);
    }
}
