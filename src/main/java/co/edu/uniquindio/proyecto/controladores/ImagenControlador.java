package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.ImagenResponseDTO;
import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.ImagenServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "Imágenes",
        description = "API para la gestión de imágenes (subida y eliminación)")
@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor
@Slf4j
public class ImagenControlador {

    private final ImagenServicio imagenServicio;

    @Operation(
            summary = "Subir una imagen",
            description = "Permite subir una imagen al servidor y devuelve información detallada incluyendo la URL pública y el ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Imagen subida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ImagenResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Archivo inválido o error en la subida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MensajeDTO.class)
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MensajeDTO<ImagenResponseDTO>> subirImagen(
            @Parameter(
                    description = "Archivo de imagen a subir (JPEG, PNG, GIF, etc.)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam(name = "imagen") MultipartFile imagen) throws Exception {

        log.info("Solicitud para subir imagen: {}", imagen.getOriginalFilename());
        ImagenResponseDTO resultado = imagenServicio.subirImagen(imagen);
        log.info("Imagen subida exitosamente: {}", resultado.id()); // Cambiado de getId() a id()

        return ResponseEntity.ok().body(new MensajeDTO<>(false, resultado));
    }

    @Operation(
            summary = "Eliminar una imagen por su ID",
            description = "Elimina permanentemente una imagen del servidor usando su ID público"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Imagen eliminada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "ID de imagen no encontrado"
            )
    })
    @DeleteMapping("/{idImagen}")
    public ResponseEntity<MensajeDTO<String>> eliminarImagen(
            @Parameter(
                    description = "ID público de la imagen a eliminar",
                    required = true,
                    example = "reportes/bc3a7f8e0b27c1234567890ab"
            )
            @PathVariable(name = "idImagen") String idImagen) throws Exception {

        log.info("Solicitud para eliminar imagen con ID: {}", idImagen);
        imagenServicio.eliminarImagen(idImagen);
        log.info("Imagen eliminada correctamente: {}", idImagen);

        return ResponseEntity.ok().body(new MensajeDTO<>(false, "Imagen eliminada correctamente"));
    }

    @Operation(
            summary = "Subir múltiples imágenes",
            description = "Permite subir varias imágenes simultáneamente y devuelve información detallada de cada una"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Imágenes subidas exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Una o más imágenes son inválidas"
            )
    })
    @PostMapping(value = "/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MensajeDTO<List<ImagenResponseDTO>>> subirMultiplesImagenes(
            @Parameter(
                    description = "Lista de archivos de imágenes a subir (JPEG, PNG, GIF, etc.)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam(name = "imagenes") List<MultipartFile> imagenes) throws Exception {

        log.info("Solicitud para subir {} imágenes", imagenes.size());
        List<ImagenResponseDTO> resultados = new ArrayList<>();

        for (MultipartFile imagen : imagenes) {
            ImagenResponseDTO resultado = imagenServicio.subirImagen(imagen);
            resultados.add(resultado);
        }

        log.info("Subidas {} imágenes exitosamente", resultados.size());
        return ResponseEntity.ok().body(new MensajeDTO<>(false, resultados));
    }
}