package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AsignacionesAulaPupitreDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AulasExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.List;
import java.util.UUID;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionesAulaPupitreFrmTest {

    // Mockito inyecta los tres @Mock por coincidencia de tipo en los campos del FRM
    @InjectMocks private AsignacionesAulaPupitreFrm frm;

    @Mock private AsignacionesAulaPupitreDAO asignacionesAulaPupitreDAO;
    @Mock private InscripcionesPruebaDAO inscripcionesPruebaDAO;
    @Mock private AulasExamanDAO aulasExamanDAO;
    @Mock private FacesContext facesContextMock;
    @Mock private Application application;
    @Mock private ResourceBundle resourceBundle;

    private MockedStatic<FacesContext> facesContextStatic;
    private AsignacionesAulaPupitre entidad;
    private InscripcionesPrueba inscripcion;
    private AulasExaman aula;

    // ==================== SETUP ====================
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        facesContextStatic = mockStatic(FacesContext.class);
        facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContextMock);

        inscripcion = new InscripcionesPrueba();
        inscripcion.setId(testId);

        aula = new AulasExaman();
        aula.setId(testId);

        entidad = new AsignacionesAulaPupitre();
        entidad.setId(testId);
        entidad.setPupitre("A-12");
        entidad.setIdInscripcion(inscripcion);
        entidad.setIdAula(aula);
    }

    @AfterEach
    void tearDown() {
        facesContextStatic.close();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void configurarBundle(String clave, String valor) {
        when(facesContextMock.getApplication()).thenReturn(application);
        when(application.getResourceBundle(any(FacesContext.class), anyString())).thenReturn(resourceBundle);
        when(resourceBundle.getString(clave)).thenReturn(valor);
    }

    private FacesMessage capturarMensaje() {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContextMock).addMessage(isNull(), captor.capture());
        return captor.getValue();
    }

    private void verificarSeveridad(FacesMessage.Severity severidadEsperada) {
        assertEquals(severidadEsperada, capturarMensaje().getSeverity());
    }

    // ==================== Constructor y configuración del bean ====================

    @Test
    void constructor_NombreBeanEsAsignacionesDeAulaYPupitre() {
        assertEquals("Asignaciones de Aula y Pupitre", frm.getNombreBean());
    }

    // ==================== getIdAsText ====================

    @Test
    void getIdAsText_EntidadConId_RetornaStringDelId() {
        assertEquals(testId.toString(), frm.getIdAsText(entidad));
    }

    @Test
    void getIdAsText_EntidadSinId_RetornaNull() {
        assertNull(frm.getIdAsText(new AsignacionesAulaPupitre()));
    }

    @Test
    void getIdAsText_EntidadNula_RetornaNull() {
        assertNull(frm.getIdAsText(null));
    }

    // ==================== getIdByText ====================

    @Test
    void getIdByText_IdValido_RetornaEntidadDesdeDAO() {
        when(asignacionesAulaPupitreDAO.leer(testId)).thenReturn(entidad);
        AsignacionesAulaPupitre resultado = frm.getIdByText(testId.toString());
        assertSame(entidad, resultado);
        verify(asignacionesAulaPupitreDAO).leer(testId);
    }

    @Test
    void getIdByText_IdNulo_RetornaNull() {
        AsignacionesAulaPupitre resultado = frm.getIdByText(null);
        assertNull(resultado);
        verify(asignacionesAulaPupitreDAO, never()).leer(any());
    }

    @Test
    void getIdByText_FormatoInvalido_RetornaNull() {
        // parseInt lanza NumberFormatException → catch la captura y retorna null
        AsignacionesAulaPupitre resultado = frm.getIdByText("no-es-un-numero");
        assertNull(resultado);
        verify(asignacionesAulaPupitreDAO, never()).leer(any());
    }

    @Test
    void getIdByText_DAORetornaNull_RetornaNull() {
        when(asignacionesAulaPupitreDAO.leer(testId)).thenReturn(null);
        assertNull(frm.getIdByText(testId.toString()));
    }

    // ==================== createNewEntity ====================

    @Test
    void createNewEntity_PupitreEsVacioYRelacionesNulas() {
        configurarBundle("frm.botones.formListo", "Formulario listo");
        frm.btnNuevoHandler(null);
        assertNotNull(frm.getRegistro());
        assertEquals("", frm.getRegistro().getPupitre());
        // Las relaciones deben estar vacías — el usuario las elige en la vista
        assertNull(frm.getRegistro().getIdInscripcion());
        assertNull(frm.getRegistro().getIdAula());
    }

    @Test
    void createNewEntity_IdEsNuloParaEntidadNueva() {
        configurarBundle("frm.botones.formListo", "Formulario listo");
        frm.btnNuevoHandler(null);
        assertNull(frm.getRegistro().getId());
    }

    // ==================== getEntityId ====================

    @Test
    void getEntityId_EntidadConId_PermiteModificar() {
        when(asignacionesAulaPupitreDAO.leer(testId)).thenReturn(entidad);
        when(asignacionesAulaPupitreDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        configurarBundle("frm.botones.opModificar", "Modificado");
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(asignacionesAulaPupitreDAO).actualizar(entidad);
    }

    @Test
    void getEntityId_EntidadNula_HandlerEnviaError() {
        frm.setRegistro(null);
        frm.btnModificarHandler(null);
        verify(asignacionesAulaPupitreDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== inicializarListas ====================

    @Test
    void inicializarListas_Exitoso_CargaAmbas() {
        when(inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(inscripcion));
        when(aulasExamanDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(aula));

        frm.inicializarListas();

        assertEquals(1, frm.getInscripcionesDisponibles().size());
        assertSame(inscripcion, frm.getInscripcionesDisponibles().get(0));
        assertEquals(1, frm.getAulasDisponibles().size());
        assertSame(aula, frm.getAulasDisponibles().get(0));
    }

    @Test
    void inicializarListas_ExcepcionEnInscripciones_ListaVaciaYSigueCargandoAulas() {
        when(inscripcionesPruebaDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo inscripciones"));
        when(aulasExamanDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(aula));

        frm.inicializarListas();

        assertTrue(frm.getInscripcionesDisponibles().isEmpty());
        assertEquals(1, frm.getAulasDisponibles().size());
    }

    @Test
    void inicializarListas_ExcepcionEnAulas_InscripcionesCargadasYAulasVacia() {
        when(inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(inscripcion));
        when(aulasExamanDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo aulas"));

        frm.inicializarListas();

        assertEquals(1, frm.getInscripcionesDisponibles().size());
        assertTrue(frm.getAulasDisponibles().isEmpty());
    }

    @Test
    void inicializarListas_AmbosVacios_RetornaListasVacias() {
        when(inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of());
        when(aulasExamanDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of());

        frm.inicializarListas();

        assertTrue(frm.getInscripcionesDisponibles().isEmpty());
        assertTrue(frm.getAulasDisponibles().isEmpty());
    }

    // ==================== Getters de listas auxiliares (estado inicial) ====================

    @Test
    void getInscripcionesDisponibles_EstadoInicialEsVacio() {
        // Antes de inicializarListas, la lista es Collections.emptyList()
        assertTrue(frm.getInscripcionesDisponibles().isEmpty());
    }

    @Test
    void getAulasDisponibles_EstadoInicialEsVacio() {
        assertTrue(frm.getAulasDisponibles().isEmpty());
    }

    // ==================== inicializar (PostConstruct) ====================

    @Test
    void inicializar_InvocaInicializarRegistrosEInicializarListas() {
        when(asignacionesAulaPupitreDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        when(inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(inscripcion));
        when(aulasExamanDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(aula));

        frm.inicializar();

        // Verifica que ambas vías fueron ejecutadas
        verify(asignacionesAulaPupitreDAO).findRange(0, 5);
        verify(inscripcionesPruebaDAO).findRange(0, Integer.MAX_VALUE);
        verify(aulasExamanDAO).findRange(0, Integer.MAX_VALUE);
        assertEquals(1, frm.getRegistros().size());
        assertEquals(1, frm.getInscripcionesDisponibles().size());
        assertEquals(1, frm.getAulasDisponibles().size());
    }

    // ==================== inicializarRegistros ====================

    @Test
    void inicializarRegistros_ConDatos_CargaLista() {
        when(asignacionesAulaPupitreDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        frm.inicializarRegistros();
        assertEquals(1, frm.getRegistros().size());
        assertSame(entidad, frm.getRegistros().get(0));
    }

    @Test
    void inicializarRegistros_ConExcepcion_ListaVaciaYMensajeError() {
        when(asignacionesAulaPupitreDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo BD"));
        frm.inicializarRegistros();
        assertTrue(frm.getRegistros().isEmpty());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== seleccionarRegistro ====================

    @Test
    void seleccionarRegistro_Valido_EstableceMODIFICAR() {
        frm.seleccionarRegistro(entidad);
        assertSame(entidad, frm.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, frm.getEstado());
    }

    @Test
    void seleccionarRegistro_Null_NoModificaEstado() {
        frm.seleccionarRegistro(null);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
    }

    // ==================== btnGuardarHandler ====================

    @Test
    void btnGuardarHandler_Valido_CreaRefrescaYLimpia() {
        configurarBundle("frm.botones.creado", "Creado");
        when(asignacionesAulaPupitreDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnGuardarHandler(null);
        verify(asignacionesAulaPupitreDAO).crear(entidad);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnGuardarHandler_RegistroNulo_ErrorSinLlamarDao() {
        frm.setRegistro(null);
        frm.btnGuardarHandler(null);
        verify(asignacionesAulaPupitreDAO, never()).crear(any());
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
        assertTrue(msg.getDetail().contains("nulo"));
    }

    @Test
    void btnGuardarHandler_Excepcion_ErrorSinRefrescar() {
        frm.setRegistro(entidad);
        doThrow(new RuntimeException("Fallo BD")).when(asignacionesAulaPupitreDAO).crear(any());
        frm.btnGuardarHandler(null);
        verify(asignacionesAulaPupitreDAO, never()).findRange(anyInt(), anyInt());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnModificarHandler ====================

    @Test
    void btnModificarHandler_Valido_ActualizaRefrescaYLimpia() {
        when(asignacionesAulaPupitreDAO.leer(testId)).thenReturn(entidad);
        when(asignacionesAulaPupitreDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        configurarBundle("frm.botones.opModificar", "Modificado");
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(asignacionesAulaPupitreDAO).actualizar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<AsignacionesAulaPupitre> registrosInvalidosParaModificar() {
        return Stream.of(null, new AsignacionesAulaPupitre());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaModificar")
    void btnModificarHandler_RegistroInvalido_ErrorSinActualizar(AsignacionesAulaPupitre registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnModificarHandler(null);
        verify(asignacionesAulaPupitreDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void btnModificarHandler_NoEncontradoEnBD_Error() {
        when(asignacionesAulaPupitreDAO.leer(testId)).thenReturn(null);
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(asignacionesAulaPupitreDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnEliminarHandler ====================

    @Test
    void btnEliminarHandler_Valido_EliminaRefrescaYLimpia() {
        configurarBundle("frm.botones.opEliminar", "Eliminado");
        when(asignacionesAulaPupitreDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnEliminarHandler(null);
        verify(asignacionesAulaPupitreDAO).eliminar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<AsignacionesAulaPupitre> registrosInvalidosParaEliminar() {
        return Stream.of(null, new AsignacionesAulaPupitre());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaEliminar")
    void btnEliminarHandler_RegistroInvalido_ErrorSinEliminar(AsignacionesAulaPupitre registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnEliminarHandler(null);
        verify(asignacionesAulaPupitreDAO, never()).eliminar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnCancelarHandler ====================

    @Test
    void btnCancelarHandler_Exitoso_LimpiaYMensajeInfo() {
        configurarBundle("frm.botones.opCancelar", "Cancelado");
        frm.setRegistro(entidad);
        frm.btnCancelarHandler(null);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnCancelarHandler_Excepcion_LimpiaDeTodasFormas() {
        when(facesContextMock.getApplication()).thenReturn(application);
        when(application.getResourceBundle(any(), anyString()))
                .thenThrow(new RuntimeException("Bundle no encontrado"));
        frm.setRegistro(entidad);
        frm.btnCancelarHandler(null);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== Paginación ====================

    @Test
    void paginacion_ValoresPorDefecto_SonCorrectos() {
        assertEquals(0, frm.getPaginaActual());
        assertEquals(5, frm.getPageSize());
    }

    @Test
    void paginacion_CambioValores_UsaOffsetCorrecto() {
        frm.setPaginaActual(1);
        frm.setPageSize(8);
        when(asignacionesAulaPupitreDAO.findRange(8, 8)).thenReturn(List.of());
        frm.inicializarRegistros();
        verify(asignacionesAulaPupitreDAO).findRange(8, 8);
    }
}
