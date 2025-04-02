package co.edu.uniquindio.proyecto.controladores;

import co.edu.uniquindio.proyecto.dto.MensajeDTO;
import co.edu.uniquindio.proyecto.dto.ReporteDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.ReporteServicio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/reportes")
public class ReporteControlador {

    public final ReporteServicio reporteServivio;

    public ReporteControlador(ReporteServicio reporteServivio) {
        this.reporteServivio = reporteServivio;
    }

    @PostMapping
    public ResponseEntity<MensajeDTO> crearReporte(@Valid @RequestBody ReporteDTO reporteDTO) {
        return ResponseEntity.ok(new MensajeDTO(false, "SU reporte ha sido exitoso"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeDTO> editarReporte(@Valid @RequestBody ReporteDTO reporteDTO) {
        return ResponseEntity.ok(new MensajeDTO(false, "Cuenta editada con exito"));
    }
    @PostMapping("/{id}")
    public ResponseEntity<MensajeDTO> eliminarReporte(String id) throws Exception{
        return ResponseEntity.ok(new MensajeDTO(false, "Cuenta eliminada"));
    }

    @GetMapping
    public ResponseEntity<MensajeDTO<List<ReporteDTO>>> listarReportes() {
        List<ReporteDTO> reportes = ReporteServicio.listarTodos();
        return ResponseEntity.ok(new MensajeDTO<>(false, reportes));
    }
}
