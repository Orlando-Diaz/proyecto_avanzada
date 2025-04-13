@ExtendWith(MockitoExtension.class)
public class ReporteServicioTest {

    @InjectMocks
    private ReporteServicioImpl reporteServicio;

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private ReporteRepo reporteRepo;

    @Mock
    private HistorialReporteRepo historialReporteRepo;

    @Test
    void gestionarEstadoReporteAdministrador_estadoActualizadoConExito() throws Exception {

        // Arrange (preparar los datos)
        ObjectId idUsuario = new ObjectId();
        ObjectId idReporte = new ObjectId();

        Usuario administrador = new Usuario();
        administrador.setId(idUsuario);
        administrador.setRol("ADMINISTRADOR");

        Reporte reporte = new Reporte();
        reporte.setId(idReporte);
        reporte.setEstadoActual(EstadoReporte.PENDIENTE);

        GestionarEstadoReporteDTO dto = new GestionarEstadoReporteDTO(
                idReporte.toHexString(),
                EstadoReporte.RESUELTO,
                idUsuario.toHexString(),
                "Prueba de cambio de estado"
        );

        // Mockear comportamiento de repositorios
        Mockito.when(usuarioRepo.findById(idUsuario)).thenReturn(Optional.of(administrador));
        Mockito.when(reporteRepo.findById(idReporte)).thenReturn(Optional.of(reporte));
        Mockito.when(reporteRepo.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(historialReporteRepo.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act (ejecutar el método a probar)
        reporteServicio.gestionarEstadoReporteAdministrador(dto);

        // Assert (verificar resultado esperado)
        Assertions.assertEquals(EstadoReporte.RESUELTO, reporte.getEstadoActual());
        Assertions.assertFalse(reporte.getHistorial().isEmpty());
    }
}
