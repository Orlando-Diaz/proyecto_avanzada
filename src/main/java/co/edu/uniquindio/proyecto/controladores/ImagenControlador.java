package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.ImagenServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "Imágenes", description = "Gestión de imágenes")
@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor
public class ImagenControlador {

    private final ImagenServicio imagenServicio;

    @Operation(summary = "Subir una imagen")
    @PostMapping
    public ResponseEntity<MensajeDTO<String>> subirImagen(
            @RequestParam("imagen") MultipartFile imagen) throws Exception {

        Map resultado = imagenServicio.subirImagen(imagen);

        // Extraer la URL de la imagen subida
        String url = (String) resultado.get("url");

        return ResponseEntity.ok().body(new MensajeDTO<>(false, url));
    }

    @Operation(summary = "Eliminar una imagen")
    @DeleteMapping("/{idImagen}")
    public ResponseEntity<MensajeDTO<String>> eliminarImagen(
            @PathVariable String idImagen) throws Exception {

        imagenServicio.eliminarImagen(idImagen);

        return ResponseEntity.ok().body(new MensajeDTO<>(false, "Imagen eliminada correctamente"));
    }

    @PostMapping("/multiple")
    public ResponseEntity<MensajeDTO<List<String>>> subirMultiplesImagenes(
            @RequestParam("imagenes") List<MultipartFile> imagenes) throws Exception {

        List<String> urls = new ArrayList<>();

        for (MultipartFile imagen : imagenes) {
            Map resultado = imagenServicio.subirImagen(imagen);
            String url = (String) resultado.get("url");
            urls.add(url);
        }

        return ResponseEntity.ok().body(new MensajeDTO<>(false, urls));
    }
}