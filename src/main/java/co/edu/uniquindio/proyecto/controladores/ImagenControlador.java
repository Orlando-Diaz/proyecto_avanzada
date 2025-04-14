package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.ImagenServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagen subida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Archivo inválido o error en la subida")
    })
    @PostMapping
    public ResponseEntity<MensajeDTO<String>> subirImagen(
            @RequestParam("imagen") MultipartFile imagen) throws Exception {

        Map resultado = imagenServicio.subirImagen(imagen);
        String url = (String) resultado.get("url");

        return ResponseEntity.ok().body(new MensajeDTO<>(false, url));
    }

    @Operation(summary = "Eliminar una imagen por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagen eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "ID de imagen no encontrado")
    })
    @DeleteMapping("/{idImagen}")
    public ResponseEntity<MensajeDTO<String>> eliminarImagen(
            @PathVariable String idImagen) throws Exception {

        imagenServicio.eliminarImagen(idImagen);

        return ResponseEntity.ok().body(new MensajeDTO<>(false, "Imagen eliminada correctamente"));
    }

    @Operation(summary = "Subir múltiples imágenes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imágenes subidas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Una o más imágenes son inválidas")
    })
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
