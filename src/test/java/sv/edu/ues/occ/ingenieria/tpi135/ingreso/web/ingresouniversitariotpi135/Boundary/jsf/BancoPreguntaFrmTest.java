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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AreasConocimientoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BancoPreguntaFrmTest {

    @InjectMocks private BancoPreguntaFrm frm;

    @Mock private BancoPreguntaDAO bancoPreguntaDAO;
    @Mock private AreasConocimientoDAO areasConocimientoDAO;
    @Mock private FacesContext facesContextMock;
    @Mock private Application application;
    @Mock private ResourceBundle resourceBundle;

    private MockedStatic<FacesContext> facesContextStatic;
    private BancoPregunta entidad;
    private AreasConocimiento area;

    // ==================== SETUP ====================

    @BeforeEach
    void setUp() {
        facesContextStatic = mockStatic(FacesContext.class);
        facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContextMock);

        area = new AreasConocimiento();
        area.setId(3);
        area.setNombreArea("Matemáticas");

        entidad = new BancoPregunta();
        entidad.setId(1);
        entidad.setEnunciado("¿Cuánto es 2+2?");
        entidad.setIdArea(area);
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
    void constructor_NombreBeanEsBancoDePreguntas() {
        assertEquals("Banco de Preguntas", frm.getNombreBean());
    }

    // ==================== getIdAsText ====================

    @Test
    void getIdAsText_EntidadConId_RetornaStringDelId() {
        assertEquals("1", frm.getIdAsText(entidad));
    }

    @Test
    void getIdAsText_EntidadSinId_RetornaNull() {
        assertNull(frm.getIdAsText(new BancoPregunta()));
    }

    @Test
    void getIdAsText_EntidadNula_RetornaNull() {
        assertNull(frm.getIdAsText(null));
    }

    // ==================== getIdByText ====================

    @Test
    void getIdByText_IdValido_RetornaEntidadDesdeDAO() {
        when(bancoPreguntaDAO.leer(1)).thenReturn(entidad);
        BancoPregunta resultado = frm.getIdByText("1");
        assertSame(entidad, resultado);
        verify(bancoPreguntaDAO).leer(1);
    }

    @Test
    void getIdByText_IdNulo_RetornaNull() {
        assertNull(frm.getIdByText(null));
        verify(bancoPreguntaDAO, never()).leer(any());
    }

    @Test
    void getIdByText_FormatoInvalido_RetornaNull() {
        assertNull(frm.getIdByText("no-es-numero"));
        verify(bancoPreguntaDAO, never()).leer(any());
    }

    @Test
    void getIdByText_DAORetornaNull_RetornaNull() {
        when(bancoPreguntaDAO.leer(99)).thenReturn(null);
        assertNull(frm.getIdByText("99"));
    }

    // ==================== createNewEntity ====================

    @Test
    void createNewEntity_CamposInicializadosCorrectamente() {
        configurarBundle("frm.botones.formListo", "Listo");
        frm.btnNuevoHandler(null);
        BancoPregunta nueva = frm.getRegistro();
        assertNotNull(nueva);
        assertEquals("", nueva.getEnunciado());
        // La relación idArea la elige el usuario en la vista
        assertNull(nueva.getIdArea());
        assertNull(nueva.getId());
    }

    // ==================== inicializarListas ====================

    @Test
    void inicializarListas_Exitoso_CargaAreas() {
        when(areasConocimientoDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(area));
        frm.inicializarListas();
        assertEquals(1, frm.getAreasDisponibles().size());
        assertSame(area, frm.getAreasDisponibles().get(0));
    }

    @Test
    void inicializarListas_ListaVacia_RetornaVacia() {
        when(areasConocimientoDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of());
        frm.inicializarListas();
        assertTrue(frm.getAreasDisponibles().isEmpty());
    }

    @Test
    void inicializarListas_ConExcepcion_ListaVacia() {
        when(areasConocimientoDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo BD"));
        frm.inicializarListas();
        assertTrue(frm.getAreasDisponibles().isEmpty());
    }

    @Test
    void getAreasDisponibles_EstadoInicialEsVacio() {
        assertTrue(frm.getAreasDisponibles().isEmpty());
    }

    // ==================== inicializar (PostConstruct) ====================

    @Test
    void inicializar_InvocaRegistrosYListas() {
        when(bancoPreguntaDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        when(areasConocimientoDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(area));

        frm.inicializar();

        verify(bancoPreguntaDAO).findRange(0, 5);
        verify(areasConocimientoDAO).findRange(0, Integer.MAX_VALUE);
        assertEquals(1, frm.getRegistros().size());
        assertEquals(1, frm.getAreasDisponibles().size());
    }

    // ==================== inicializarRegistros ====================

    @Test
    void inicializarRegistros_ConDatos_CargaLista() {
        when(bancoPreguntaDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        frm.inicializarRegistros();
        assertEquals(1, frm.getRegistros().size());
        assertSame(entidad, frm.getRegistros().get(0));
    }

    @Test
    void inicializarRegistros_ConExcepcion_ListaVaciaYMensajeError() {
        when(bancoPreguntaDAO.findRange(anyInt(), anyInt()))
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
        when(bancoPreguntaDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnGuardarHandler(null);
        verify(bancoPreguntaDAO).crear(entidad);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnGuardarHandler_RegistroNulo_ErrorSinLlamarDao() {
        frm.setRegistro(null);
        frm.btnGuardarHandler(null);
        verify(bancoPreguntaDAO, never()).crear(any());
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
        assertTrue(msg.getDetail().contains("nulo"));
    }

    @Test
    void btnGuardarHandler_Excepcion_ErrorSinRefrescar() {
        frm.setRegistro(entidad);
        doThrow(new RuntimeException("Fallo BD")).when(bancoPreguntaDAO).crear(any());
        frm.btnGuardarHandler(null);
        verify(bancoPreguntaDAO, never()).findRange(anyInt(), anyInt());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnModificarHandler ====================

    @Test
    void btnModificarHandler_Valido_ActualizaRefrescaYLimpia() {
        when(bancoPreguntaDAO.leer(1)).thenReturn(entidad);
        when(bancoPreguntaDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        configurarBundle("frm.botones.opModificar", "Modificado");
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(bancoPreguntaDAO).actualizar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<BancoPregunta> registrosInvalidosParaModificar() {
        return Stream.of(null, new BancoPregunta());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaModificar")
    void btnModificarHandler_RegistroInvalido_ErrorSinActualizar(BancoPregunta registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnModificarHandler(null);
        verify(bancoPreguntaDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void btnModificarHandler_NoEncontradoEnBD_Error() {
        when(bancoPreguntaDAO.leer(1)).thenReturn(null);
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(bancoPreguntaDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnEliminarHandler ====================

    @Test
    void btnEliminarHandler_Valido_EliminaRefrescaYLimpia() {
        configurarBundle("frm.botones.opEliminar", "Eliminado");
        when(bancoPreguntaDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnEliminarHandler(null);
        verify(bancoPreguntaDAO).eliminar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<BancoPregunta> registrosInvalidosParaEliminar() {
        return Stream.of(null, new BancoPregunta());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaEliminar")
    void btnEliminarHandler_RegistroInvalido_ErrorSinEliminar(BancoPregunta registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnEliminarHandler(null);
        verify(bancoPreguntaDAO, never()).eliminar(any());
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
        when(bancoPreguntaDAO.findRange(20, 10)).thenReturn(List.of());
        frm.inicializarRegistros();
        verify(bancoPreguntaDAO).findRange(20, 10);
    }
}
