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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.List;
import java.util.UUID;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PruebasAdmisionFrmTest {

    @InjectMocks private PruebasAdmisionFrm frm;

    @Mock private PruebasAdmisionDAO pruebasAdmisionDAO;
    @Mock private FacesContext facesContextMock;
    @Mock private Application application;
    @Mock private ResourceBundle resourceBundle;

    private MockedStatic<FacesContext> facesContextStatic;
    private PruebasAdmision entidad;

    // ==================== SETUP ====================
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        facesContextStatic = mockStatic(FacesContext.class);
        facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContextMock);

        entidad = new PruebasAdmision();
        entidad.setId(testId);
        entidad.setNombrePrueba("Prueba 2026");
        entidad.setAnio(2026);
        entidad.setActiva(true);
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
    void constructor_NombreBeanEsPruebasDeAdmision() {
        assertEquals("Pruebas de Admisión", frm.getNombreBean());
    }

    // ==================== getIdAsText ====================

    @Test
    void getIdAsText_EntidadConId_RetornaStringDelId() {
        assertEquals(testId.toString(), frm.getIdAsText(entidad));
    }

    @Test
    void getIdAsText_EntidadSinId_RetornaNull() {
        assertNull(frm.getIdAsText(new PruebasAdmision()));
    }

    @Test
    void getIdAsText_EntidadNula_RetornaNull() {
        assertNull(frm.getIdAsText(null));
    }

    // ==================== getIdByText ====================

    @Test
    void getIdByText_IdValido_RetornaEntidadDesdeDAO() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);
        PruebasAdmision resultado = frm.getIdByText(testId.toString());
        assertSame(entidad, resultado);
        verify(pruebasAdmisionDAO).leer(testId);
    }

    @Test
    void getIdByText_IdNulo_RetornaNull() {
        assertNull(frm.getIdByText(null));
        verify(pruebasAdmisionDAO, never()).leer(any());
    }

    @Test
    void getIdByText_FormatoInvalido_RetornaNull() {
        assertNull(frm.getIdByText("no-es-numero"));
        verify(pruebasAdmisionDAO, never()).leer(any());
    }

    @Test
    void getIdByText_DAORetornaNull_RetornaNull() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(null);
        assertNull(frm.getIdByText(testId.toString()));
    }

    // ==================== createNewEntity ====================

    @Test
    void createNewEntity_CamposInicializadosCorrectamente() {
        configurarBundle("frm.botones.formListo", "Listo");
        frm.btnNuevoHandler(null);
        PruebasAdmision nuevo = frm.getRegistro();
        assertNotNull(nuevo);
        assertEquals("", nuevo.getNombrePrueba());
        assertNotNull(nuevo.getAnio());
        assertFalse(nuevo.getActiva());
        assertNull(nuevo.getId());
    }

    // ==================== inicializar (PostConstruct) ====================

    @Test
    void inicializar_InvocaRegistros() {
        when(pruebasAdmisionDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        frm.inicializar();
        verify(pruebasAdmisionDAO).findRange(0, 5);
        assertEquals(1, frm.getRegistros().size());
    }

    // ==================== inicializarRegistros ====================

    @Test
    void inicializarRegistros_ConDatos_CargaLista() {
        when(pruebasAdmisionDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        frm.inicializarRegistros();
        assertEquals(1, frm.getRegistros().size());
        assertSame(entidad, frm.getRegistros().get(0));
    }

    @Test
    void inicializarRegistros_ConExcepcion_ListaVaciaYMensajeError() {
        when(pruebasAdmisionDAO.findRange(anyInt(), anyInt()))
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
        when(pruebasAdmisionDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnGuardarHandler(null);
        verify(pruebasAdmisionDAO).crear(entidad);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnGuardarHandler_RegistroNulo_ErrorSinLlamarDao() {
        frm.setRegistro(null);
        frm.btnGuardarHandler(null);
        verify(pruebasAdmisionDAO, never()).crear(any());
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
        assertTrue(msg.getDetail().contains("nulo"));
    }

    @Test
    void btnGuardarHandler_Excepcion_ErrorSinRefrescar() {
        frm.setRegistro(entidad);
        doThrow(new RuntimeException("Fallo BD")).when(pruebasAdmisionDAO).crear(any());
        frm.btnGuardarHandler(null);
        verify(pruebasAdmisionDAO, never()).findRange(anyInt(), anyInt());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnModificarHandler ====================

    @Test
    void btnModificarHandler_Valido_ActualizaRefrescaYLimpia() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);
        when(pruebasAdmisionDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        configurarBundle("frm.botones.opModificar", "Modificado");
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(pruebasAdmisionDAO).actualizar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<PruebasAdmision> registrosInvalidosParaModificar() {
        return Stream.of(null, new PruebasAdmision());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaModificar")
    void btnModificarHandler_RegistroInvalido_ErrorSinActualizar(PruebasAdmision registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnModificarHandler(null);
        verify(pruebasAdmisionDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void btnModificarHandler_NoEncontradoEnBD_Error() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(null);
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(pruebasAdmisionDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnEliminarHandler ====================

    @Test
    void btnEliminarHandler_Valido_EliminaRefrescaYLimpia() {
        configurarBundle("frm.botones.opEliminar", "Eliminado");
        when(pruebasAdmisionDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnEliminarHandler(null);
        verify(pruebasAdmisionDAO).eliminar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<PruebasAdmision> registrosInvalidosParaEliminar() {
        return Stream.of(null, new PruebasAdmision());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaEliminar")
    void btnEliminarHandler_RegistroInvalido_ErrorSinEliminar(PruebasAdmision registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnEliminarHandler(null);
        verify(pruebasAdmisionDAO, never()).eliminar(any());
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
        frm.setPaginaActual(2);
        frm.setPageSize(10);
        when(pruebasAdmisionDAO.findRange(20, 10)).thenReturn(List.of());
        frm.inicializarRegistros();
        verify(pruebasAdmisionDAO).findRange(20, 10);
    }
}
