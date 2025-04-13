package com.qmd.backend.service;

import co.edu.uniquindio.proyecto.dto.GestionarEstadoReporteDTO;
import co.edu.uniquindio.proyecto.modelo.documentos.HistorialReporte;
import co.edu.uniquindio.proyecto.modelo.documentos.Reporte;
import co.edu.uniquindio.proyecto.modelo.documentos.Usuario;
import co.edu.uniquindio.proyecto.modelo.enums.EstadoReporte;
import co.edu.uniquindio.proyecto.repositorios.HistorialReporteRepo;
import co.edu.uniquindio.proyecto.repositorios.ReporteRepo;
import co.edu.uniquindio.proyecto.repositorios.UsuarioRepo;
import co.edu.uniquindio.proyecto.servicios.impl.ReporteServicioImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class ReporteServiceTest {

    private ReporteServicioImpl reporteServicio;
    private ReporteRepo reporteRepo;
    private UsuarioRepo usuarioRepo;
    private HistorialReporteRepo historialReporteRepo;

    @BeforeEach
    public void setup() {
        reporteRepo = mock(ReporteRepo.class);
        usuarioRepo = mock(UsuarioRepo.class);
        historialReporteRepo = mock(HistorialReporteRepo.class);
        reporteServicio = new ReporteServicioImpl(reporteRepo, usuarioRepo, historialReporteRepo);
    }

    @Test
    public void testGestionarEstadoReporteAdministrador() throws Exception {
        // Datos de prueba
        ObjectId idUsuario = new ObjectId();
        ObjectId idReporte = new ObjectId();

        GestionarEstadoReporteDTO dto = new GestionarEstadoReporteDTO(
                idReporte.toHexString(),
                EstadoReporte.VERIFICADO,
                "Verificado por pruebas",
                idUsuario.toHexString()
        );

        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);
        usuario.setRol("ADMINISTRADOR");

        Reporte reporte = new Reporte();
        reporte.setId(idReporte);
        reporte.setEstadoActual(EstadoReporte.PENDIENTE);

        // Mockeo de repositorios
        when(usuarioRepo.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(reporteRepo.findById(idReporte)).thenReturn(Optional.of(reporte));

        // Ejecutar método
        reporteServicio.gestionarEstadoReporteAdministrador(dto);

        // Verificaciones
        verify(usuarioRepo).findById(idUsuario);
        verify(reporteRepo).findById(idReporte);
        verify(reporteRepo).save(any(Reporte.class));
        verify(historialReporteRepo).save(any(HistorialReporte.class));
    }
}
