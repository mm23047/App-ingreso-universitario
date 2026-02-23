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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AulasExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TurnosExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AulasExamanFrmTest {

    @InjectMocks private AulasExamanFrm frm;

    @Mock private AulasExamanDAO aulasExamanDAO;
    @Mock private TurnosExamanDAO turnosExamanDAO;
    @Mock private FacesContext facesContextMock;
    @Mock private Application application;
    @Mock private ResourceBundle resourceBundle;

    private MockedStatic<FacesContext> facesContextStatic;
    private AulasExaman entidad;
    private TurnosExaman turno;

    // ==================== SETUP ====================

    @BeforeEach
    void setUp() {
        facesContextStatic = mockStatic(FacesContext.class);
        facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContextMock);

        turno = new TurnosExaman();
        turno.setId(1);
        turno.setNombreTurno("Turno Mañana");

        entidad = new AulasExaman();
        entidad.setId(3);
        entidad.setIdAulaApi("AULA-01");
        entidad.setCapacidad(30);
        entidad.setCuposOcupados(0);
        entidad.setAccesibleSillaRuedas(false);
        entidad.setIdTurno(turno);
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
    void constructor_NombreBeanEsAulasDeExamen() {
        assertEquals("Aulas de Examen", frm.getNombreBean());
    }

    // ==================== getIdAsText ====================

    @Test
    void getIdAsText_EntidadConId_RetornaStringDelId() {
        assertEquals("3", frm.getIdAsText(entidad));
    }

    @Test
    void getIdAsText_EntidadSinId_RetornaNull() {
        assertNull(frm.getIdAsText(new AulasExaman()));
    }

    @Test
    void getIdAsText_EntidadNula_RetornaNull() {
        assertNull(frm.getIdAsText(null));
    }

    // ==================== getIdByText ====================

    @Test
    void getIdByText_IdValido_RetornaEntidadDesdeDAO() {
        when(aulasExamanDAO.leer(3)).thenReturn(entidad);
        AulasExaman resultado = frm.getIdByText("3");
        assertSame(entidad, resultado);
        verify(aulasExamanDAO).leer(3);
    }

    @Test
    void getIdByText_IdNulo_RetornaNull() {
        assertNull(frm.getIdByText(null));
        verify(aulasExamanDAO, never()).leer(any());
    }

    @Test
    void getIdByText_FormatoInvalido_RetornaNull() {
        assertNull(frm.getIdByText("no-es-numero"));
        verify(aulasExamanDAO, never()).leer(any());
    }

    @Test
    void getIdByText_DAORetornaNull_RetornaNull() {
        when(aulasExamanDAO.leer(99)).thenReturn(null);
        assertNull(frm.getIdByText("99"));
    }

    // ==================== createNewEntity ====================

    @Test
    void createNewEntity_CamposInicializadosCorrectamente() {
        configurarBundle("frm.botones.formListo", "Listo");
        frm.btnNuevoHandler(null);
        AulasExaman nueva = frm.getRegistro();
        assertNotNull(nueva);
        assertEquals("", nueva.getIdAulaApi());
        assertEquals(0, nueva.getCapacidad());
        assertEquals(0, nueva.getCuposOcupados());
        assertFalse(nueva.getAccesibleSillaRuedas());
        // La relación idTurno la elige el usuario en la vista
        assertNull(nueva.getIdTurno());
        assertNull(nueva.getId());
    }

    // ==================== inicializarListas ====================

    @Test
    void inicializarListas_Exitoso_CargaTurnos() {
        when(turnosExamanDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(turno));
        frm.inicializarListas();
        assertEquals(1, frm.getTurnosDisponibles().size());
        assertSame(turno, frm.getTurnosDisponibles().get(0));
    }

    @Test
    void inicializarListas_ListaVacia_RetornaVacia() {
        when(turnosExamanDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of());
        frm.inicializarListas();
        assertTrue(frm.getTurnosDisponibles().isEmpty());
    }

    @Test
    void inicializarListas_ConExcepcion_ListaVacia() {
        when(turnosExamanDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo BD"));
        frm.inicializarListas();
        assertTrue(frm.getTurnosDisponibles().isEmpty());
    }

    @Test
    void getTurnosDisponibles_EstadoInicialEsVacio() {
        assertTrue(frm.getTurnosDisponibles().isEmpty());
    }

    // ==================== inicializar (PostConstruct) ====================

    @Test
    void inicializar_InvocaRegistrosYListas() {
        when(aulasExamanDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        when(turnosExamanDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(turno));

        frm.inicializar();

        verify(aulasExamanDAO).findRange(0, 5);
        verify(turnosExamanDAO).findRange(0, Integer.MAX_VALUE);
        assertEquals(1, frm.getRegistros().size());
        assertEquals(1, frm.getTurnosDisponibles().size());
    }

    // ==================== inicializarRegistros ====================

    @Test
    void inicializarRegistros_ConDatos_CargaLista() {
        when(aulasExamanDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        frm.inicializarRegistros();
        assertEquals(1, frm.getRegistros().size());
        assertSame(entidad, frm.getRegistros().get(0));
    }

    @Test
    void inicializarRegistros_ConExcepcion_ListaVaciaYMensajeError() {
        when(aulasExamanDAO.findRange(anyInt(), anyInt()))
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
        when(aulasExamanDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnGuardarHandler(null);
        verify(aulasExamanDAO).crear(entidad);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnGuardarHandler_RegistroNulo_ErrorSinLlamarDao() {
        frm.setRegistro(null);
        frm.btnGuardarHandler(null);
        verify(aulasExamanDAO, never()).crear(any());
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
        assertTrue(msg.getDetail().contains("nulo"));
    }

    @Test
    void btnGuardarHandler_Excepcion_ErrorSinRefrescar() {
        frm.setRegistro(entidad);
        doThrow(new RuntimeException("Fallo BD")).when(aulasExamanDAO).crear(any());
        frm.btnGuardarHandler(null);
        verify(aulasExamanDAO, never()).findRange(anyInt(), anyInt());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnModificarHandler ====================

    @Test
    void btnModificarHandler_Valido_ActualizaRefrescaYLimpia() {
        when(aulasExamanDAO.leer(3)).thenReturn(entidad);
        when(aulasExamanDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        configurarBundle("frm.botones.opModificar", "Modificado");
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(aulasExamanDAO).actualizar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<AulasExaman> registrosInvalidosParaModificar() {
        return Stream.of(null, new AulasExaman());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaModificar")
    void btnModificarHandler_RegistroInvalido_ErrorSinActualizar(AulasExaman registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnModificarHandler(null);
        verify(aulasExamanDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void btnModificarHandler_NoEncontradoEnBD_Error() {
        when(aulasExamanDAO.leer(3)).thenReturn(null);
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(aulasExamanDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnEliminarHandler ====================

    @Test
    void btnEliminarHandler_Valido_EliminaRefrescaYLimpia() {
        configurarBundle("frm.botones.opEliminar", "Eliminado");
        when(aulasExamanDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnEliminarHandler(null);
        verify(aulasExamanDAO).eliminar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<AulasExaman> registrosInvalidosParaEliminar() {
        return Stream.of(null, new AulasExaman());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaEliminar")
    void btnEliminarHandler_RegistroInvalido_ErrorSinEliminar(AulasExaman registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnEliminarHandler(null);
        verify(aulasExamanDAO, never()).eliminar(any());
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
        frm.setPaginaActual(3);
        frm.setPageSize(6);
        when(aulasExamanDAO.findRange(18, 6)).thenReturn(List.of());
        frm.inicializarRegistros();
        verify(aulasExamanDAO).findRange(18, 6);
    }
}
