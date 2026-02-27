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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TurnosExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnosExamanFrmTest {

    @InjectMocks private TurnosExamanFrm frm;

    @Mock private TurnosExamanDAO turnosExamanDAO;
    @Mock private PruebasAdmisionDAO pruebasAdmisionDAO;
    @Mock private FacesContext facesContextMock;
    @Mock private Application application;
    @Mock private ResourceBundle resourceBundle;

    private MockedStatic<FacesContext> facesContextStatic;
    private TurnosExaman entidad;
    private PruebasAdmision prueba;

    // ==================== SETUP ====================
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        facesContextStatic = mockStatic(FacesContext.class);
        facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContextMock);

        prueba = new PruebasAdmision();
        prueba.setId(testId);
        prueba.setNombrePrueba("Prueba 2026");
        prueba.setAnio(2026);

        entidad = new TurnosExaman();
        entidad.setId(testId);
        entidad.setNombreTurno("Turno Mañana");
        entidad.setFecha(LocalDate.of(2026, 3, 15));
        entidad.setHoraInicio(LocalTime.of(7, 0));
        entidad.setHoraFin(LocalTime.of(9, 0));
        entidad.setIdPrueba(prueba);
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

    // ==================== Constructor ====================

    @Test
    void constructor_NombreBeanEsTurnosDeExamen() {
        assertEquals("Turnos de Examen", frm.getNombreBean());
    }

    // ==================== getIdAsText ====================

    @Test
    void getIdAsText_EntidadConId_RetornaStringDelId() {
        assertEquals(testId.toString(), frm.getIdAsText(entidad));
    }

    @Test
    void getIdAsText_EntidadSinId_RetornaNull() {
        assertNull(frm.getIdAsText(new TurnosExaman()));
    }

    @Test
    void getIdAsText_EntidadNula_RetornaNull() {
        assertNull(frm.getIdAsText(null));
    }

    // ==================== getIdByText ====================

    @Test
    void getIdByText_IdValido_RetornaEntidadDesdeDAO() {
        when(turnosExamanDAO.leer(testId)).thenReturn(entidad);
        TurnosExaman resultado = frm.getIdByText(testId.toString());
        assertSame(entidad, resultado);
        verify(turnosExamanDAO).leer(testId);
    }

    @Test
    void getIdByText_IdNulo_RetornaNull() {
        assertNull(frm.getIdByText(null));
        verify(turnosExamanDAO, never()).leer(any());
    }

    @Test
    void getIdByText_FormatoInvalido_RetornaNull() {
        assertNull(frm.getIdByText("no-es-numero"));
        verify(turnosExamanDAO, never()).leer(any());
    }

    @Test
    void getIdByText_DAORetornaNull_RetornaNull() {
        when(turnosExamanDAO.leer(testId)).thenReturn(null);
        assertNull(frm.getIdByText(testId.toString()));
    }

    // ==================== createNewEntity ====================

    @Test
    void createNewEntity_CamposInicializadosCorrectamente() {
        configurarBundle("frm.botones.formListo", "Listo");
        frm.btnNuevoHandler(null);
        TurnosExaman nuevo = frm.getRegistro();
        assertNotNull(nuevo);
        assertEquals("", nuevo.getNombreTurno());
        assertNotNull(nuevo.getFecha());
        assertNotNull(nuevo.getHoraInicio());
        assertNotNull(nuevo.getHoraFin());
        // La relación idPrueba la elige el usuario desde el dropdown
        assertNull(nuevo.getIdPrueba());
        assertNull(nuevo.getId());
    }

    // ==================== inicializarListas ====================

    @Test
    void inicializarListas_Exitoso_CargaPruebas() {
        when(pruebasAdmisionDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(prueba));
        frm.inicializarListas();
        assertEquals(1, frm.getPruebasDisponibles().size());
        assertSame(prueba, frm.getPruebasDisponibles().get(0));
    }

    @Test
    void inicializarListas_ListaVacia_RetornaVacia() {
        when(pruebasAdmisionDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of());
        frm.inicializarListas();
        assertTrue(frm.getPruebasDisponibles().isEmpty());
    }

    @Test
    void inicializarListas_ConExcepcion_ListaVacia() {
        when(pruebasAdmisionDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo BD"));
        frm.inicializarListas();
        assertTrue(frm.getPruebasDisponibles().isEmpty());
    }

    @Test
    void getPruebasDisponibles_EstadoInicialEsVacio() {
        assertTrue(frm.getPruebasDisponibles().isEmpty());
    }

    // ==================== inicializar (PostConstruct) ====================

    @Test
    void inicializar_InvocaRegistrosYListas() {
        when(turnosExamanDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        when(pruebasAdmisionDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(prueba));
        frm.inicializar();
        verify(turnosExamanDAO).findRange(0, 5);
        verify(pruebasAdmisionDAO).findRange(0, Integer.MAX_VALUE);
        assertEquals(1, frm.getRegistros().size());
        assertEquals(1, frm.getPruebasDisponibles().size());
    }

    // ==================== inicializarRegistros ====================

    @Test
    void inicializarRegistros_ConDatos_CargaLista() {
        when(turnosExamanDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        frm.inicializarRegistros();
        assertEquals(1, frm.getRegistros().size());
        assertSame(entidad, frm.getRegistros().get(0));
    }

    @Test
    void inicializarRegistros_ConExcepcion_ListaVaciaYMensajeError() {
        when(turnosExamanDAO.findRange(anyInt(), anyInt()))
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

    // ==================== btnNuevoHandler ====================

    @Test
    void btnNuevoHandler_Exitoso_EstadoCREAR() {
        configurarBundle("frm.botones.formListo", "Formulario listo");
        frm.btnNuevoHandler(null);
        assertEquals(ESTADO_CRUD.CREAR, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnNuevoHandler_Excepcion_LimpiaYMensajeError() {
        when(facesContextMock.getApplication()).thenThrow(new RuntimeException("JSF error"));
        frm.btnNuevoHandler(null);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnGuardarHandler ====================

    @Test
    void btnGuardarHandler_Valido_CreaRefrescaYLimpia() {
        configurarBundle("frm.botones.creado", "Creado");
        when(turnosExamanDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnGuardarHandler(null);
        verify(turnosExamanDAO).crear(entidad);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnGuardarHandler_RegistroNulo_ErrorSinLlamarDao() {
        frm.setRegistro(null);
        frm.btnGuardarHandler(null);
        verify(turnosExamanDAO, never()).crear(any());
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
        assertTrue(msg.getDetail().contains("nulo"));
    }

    @Test
    void btnGuardarHandler_Excepcion_ErrorSinRefrescar() {
        frm.setRegistro(entidad);
        doThrow(new RuntimeException("Fallo BD")).when(turnosExamanDAO).crear(any());
        frm.btnGuardarHandler(null);
        verify(turnosExamanDAO, never()).findRange(anyInt(), anyInt());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnModificarHandler ====================

    @Test
    void btnModificarHandler_Valido_ActualizaRefrescaYLimpia() {
        when(turnosExamanDAO.leer(testId)).thenReturn(entidad);
        when(turnosExamanDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        configurarBundle("frm.botones.opModificar", "Modificado");
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(turnosExamanDAO).actualizar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<TurnosExaman> registrosInvalidosParaModificar() {
        return Stream.of(null, new TurnosExaman());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaModificar")
    void btnModificarHandler_RegistroInvalido_ErrorSinActualizar(TurnosExaman registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnModificarHandler(null);
        verify(turnosExamanDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void btnModificarHandler_NoEncontradoEnBD_Error() {
        when(turnosExamanDAO.leer(testId)).thenReturn(null);
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(turnosExamanDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnEliminarHandler ====================

    @Test
    void btnEliminarHandler_Valido_EliminaRefrescaYLimpia() {
        configurarBundle("frm.botones.opEliminar", "Eliminado");
        when(turnosExamanDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnEliminarHandler(null);
        verify(turnosExamanDAO).eliminar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<TurnosExaman> registrosInvalidosParaEliminar() {
        return Stream.of(null, new TurnosExaman());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaEliminar")
    void btnEliminarHandler_RegistroInvalido_ErrorSinEliminar(TurnosExaman registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnEliminarHandler(null);
        verify(turnosExamanDAO, never()).eliminar(any());
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
        when(turnosExamanDAO.findRange(8, 8)).thenReturn(List.of());
        frm.inicializarRegistros();
        verify(turnosExamanDAO).findRange(8, 8);
    }
}
