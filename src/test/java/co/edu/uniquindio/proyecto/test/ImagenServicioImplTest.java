package co.edu.uniquindio.proyecto.test;

import co.edu.uniquindio.proyecto.servicios.interfaces.ImagenServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ImagenServicioImplTest {

    @Autowired
    private ImagenServicio imagenServicio;

    @Test
    public void subirImagenTest() throws Exception {
        // Arrange
        ClassPathResource resource = new ClassPathResource("imagenes/imagen.png");
        byte[] contenido = Files.readAllBytes(resource.getFile().toPath());

        MockMultipartFile archivo = new MockMultipartFile(
                "imagen",
                "test-image.png",
                "image/png",
                contenido
        );

        // Act
        Map resultado = imagenServicio.subirImagen(archivo);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.containsKey("url"));
        assertTrue(resultado.get("url").toString().startsWith("http"));
    }

    @Test
    public void eliminarImagenTest() throws Exception {
        // Similar modificación para este test
        ClassPathResource resource = new ClassPathResource("imagenes/imagen.png");
        byte[] contenido = Files.readAllBytes(resource.getFile().toPath());

        MockMultipartFile archivo = new MockMultipartFile(
                "imagen",
                "test-image.png",
                "image/png",
                contenido
        );

        Map resultado = imagenServicio.subirImagen(archivo);
        String publicId = resultado.get("public_id").toString();

        // Act
        Map respuestaEliminacion = imagenServicio.eliminarImagen(publicId);

        // Assert
        assertNotNull(respuestaEliminacion);
        assertEquals("ok", respuestaEliminacion.get("result"));
    }
}