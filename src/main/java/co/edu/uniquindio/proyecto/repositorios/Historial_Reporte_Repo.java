package co.edu.uniquindio.proyecto.repositorios;

import co.edu.uniquindio.proyecto.modelo.documentos.HistorialReporte;

import java.util.List;

public interface Historial_Reporte_Repo {

    List<HistorialReporte> findByReporteId(String reporteId);
}
