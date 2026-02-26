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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenesRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.OpcionesRespuestaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.RespuestasExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestasExaman;

import java.util.List;
import java.util.UUID;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RespuestasExamanFrmTest {

    @InjectMocks private RespuestasExamanFrm frm;

    @Mock private RespuestasExamanDAO respuestasExamanDAO;
    @Mock private ExamenesRealizadoDAO examenesRealizadoDAO;
    @Mock private BancoPreguntaDAO bancoPreguntaDAO;
    @Mock private OpcionesRespuestaDAO opcionesRespuestaDAO;
    @Mock private FacesContext facesContextMock;
    @Mock private Application application;
    @Mock private ResourceBundle resourceBundle;

    private MockedStatic<FacesContext> facesContextStatic;
    private RespuestasExaman entidad;
    private ExamenesRealizado examen;
    private BancoPregunta pregunta;
    private OpcionesRespuesta opcion;

    // ==================== SETUP ====================
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        facesContextStatic = mockStatic(FacesContext.class);
        facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContextMock);

        AreasConocimiento area = new AreasConocimiento();
        area.setId(testId);
        area.setNombreArea("Matemáticas");

        pregunta = new BancoPregunta();
        pregunta.setId(testId);
        pregunta.setEnunciado("¿Cuánto es 2+2?");
        pregunta.setIdArea(area);

        opcion = new OpcionesRespuesta();
        opcion.setId(testId);
        opcion.setTextoOpcion("4");
        opcion.setEsCorrecta(true);
        opcion.setIdPregunta(pregunta);

        examen = new ExamenesRealizado();
        examen.setId(testId);

        entidad = new RespuestasExaman();
        entidad.setId(testId);
        entidad.setIdExamen(examen);
        entidad.setIdPregunta(pregunta);
        entidad.setIdOpcionSeleccionada(opcion);
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
    void constructor_NombreBeanEsRespuestasDeExamen() {
        assertEquals("Respuestas de Examen", frm.getNombreBean());
    }

    // ==================== getIdAsText ====================

    @Test
    void getIdAsText_EntidadConId_RetornaStringDelId() {
        assertEquals(testId.toString(), frm.getIdAsText(entidad));
    }

    @Test
    void getIdAsText_EntidadSinId_RetornaNull() {
        assertNull(frm.getIdAsText(new RespuestasExaman()));
    }

    @Test
    void getIdAsText_EntidadNula_RetornaNull() {
        assertNull(frm.getIdAsText(null));
    }

    // ==================== getIdByText ====================

    @Test
    void getIdByText_IdValido_RetornaEntidadDesdeDAO() {
        when(respuestasExamanDAO.leer(testId)).thenReturn(entidad);
        RespuestasExaman resultado = frm.getIdByText(testId.toString());
        assertSame(entidad, resultado);
        verify(respuestasExamanDAO).leer(testId);
    }

    @Test
    void getIdByText_IdNulo_RetornaNull() {
        assertNull(frm.getIdByText(null));
        verify(respuestasExamanDAO, never()).leer(any());
    }

    @Test
    void getIdByText_FormatoInvalido_RetornaNull() {
        assertNull(frm.getIdByText("no-es-numero"));
        verify(respuestasExamanDAO, never()).leer(any());
    }

    @Test
    void getIdByText_DAORetornaNull_RetornaNull() {
        when(respuestasExamanDAO.leer(testId)).thenReturn(null);
        assertNull(frm.getIdByText(testId.toString()));
    }

    // ==================== createNewEntity ====================

    @Test
    void createNewEntity_EntidadCreadaSinRelaciones() {
        configurarBundle("frm.botones.formListo", "Listo");
        frm.btnNuevoHandler(null);
        RespuestasExaman nuevo = frm.getRegistro();
        assertNotNull(nuevo);
        // Las relaciones se asignan desde dropdowns
        assertNull(nuevo.getId());
        assertNull(nuevo.getIdExamen());
        assertNull(nuevo.getIdPregunta());
        assertNull(nuevo.getIdOpcionSeleccionada());
    }

    // ==================== inicializarListas ====================

    @Test
    void inicializarListas_Exitoso_CargaTresListas() {
        when(examenesRealizadoDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(examen));
        when(bancoPreguntaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(pregunta));
        when(opcionesRespuestaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(opcion));

        frm.inicializarListas();

        assertEquals(1, frm.getExamenesDisponibles().size());
        assertEquals(1, frm.getPreguntasDisponibles().size());
        assertEquals(1, frm.getOpcionesDisponibles().size());
    }

    @Test
    void inicializarListas_ExcepcionEnExamenes_SigueCargandoRestantes() {
        when(examenesRealizadoDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo exámenes"));
        when(bancoPreguntaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(pregunta));
        when(opcionesRespuestaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(opcion));

        frm.inicializarListas();

        assertTrue(frm.getExamenesDisponibles().isEmpty());
        assertEquals(1, frm.getPreguntasDisponibles().size());
        assertEquals(1, frm.getOpcionesDisponibles().size());
    }

    @Test
    void inicializarListas_ExcepcionEnPreguntas_SigueCargandoRestantes() {
        when(examenesRealizadoDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(examen));
        when(bancoPreguntaDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo preguntas"));
        when(opcionesRespuestaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(opcion));

        frm.inicializarListas();

        assertEquals(1, frm.getExamenesDisponibles().size());
        assertTrue(frm.getPreguntasDisponibles().isEmpty());
        assertEquals(1, frm.getOpcionesDisponibles().size());
    }

    @Test
    void inicializarListas_ExcepcionEnOpciones_SigueCargandoRestantes() {
        when(examenesRealizadoDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(examen));
        when(bancoPreguntaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(pregunta));
        when(opcionesRespuestaDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo opciones"));

        frm.inicializarListas();

        assertEquals(1, frm.getExamenesDisponibles().size());
        assertEquals(1, frm.getPreguntasDisponibles().size());
        assertTrue(frm.getOpcionesDisponibles().isEmpty());
    }

    @Test
    void getListasAuxiliares_EstadoInicialEsVacio() {
        assertTrue(frm.getExamenesDisponibles().isEmpty());
        assertTrue(frm.getPreguntasDisponibles().isEmpty());
        assertTrue(frm.getOpcionesDisponibles().isEmpty());
    }

    // ==================== inicializar (PostConstruct) ====================

    @Test
    void inicializar_InvocaRegistrosYListas() {
        when(respuestasExamanDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        when(examenesRealizadoDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(examen));
        when(bancoPreguntaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(pregunta));
        when(opcionesRespuestaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(opcion));

        frm.inicializar();

        verify(respuestasExamanDAO).findRange(0, 5);
        assertEquals(1, frm.getRegistros().size());
        assertEquals(1, frm.getExamenesDisponibles().size());
        assertEquals(1, frm.getPreguntasDisponibles().size());
        assertEquals(1, frm.getOpcionesDisponibles().size());
    }

    // ==================== inicializarRegistros ====================

    @Test
    void inicializarRegistros_ConDatos_CargaLista() {
        when(respuestasExamanDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        frm.inicializarRegistros();
        assertEquals(1, frm.getRegistros().size());
        assertSame(entidad, frm.getRegistros().get(0));
    }

    @Test
    void inicializarRegistros_ConExcepcion_ListaVaciaYMensajeError() {
        when(respuestasExamanDAO.findRange(anyInt(), anyInt()))
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
        when(respuestasExamanDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnGuardarHandler(null);
        verify(respuestasExamanDAO).crear(entidad);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnGuardarHandler_RegistroNulo_ErrorSinLlamarDao() {
        frm.setRegistro(null);
        frm.btnGuardarHandler(null);
        verify(respuestasExamanDAO, never()).crear(any());
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
        assertTrue(msg.getDetail().contains("nulo"));
    }

    @Test
    void btnGuardarHandler_Excepcion_ErrorSinRefrescar() {
        frm.setRegistro(entidad);
        doThrow(new RuntimeException("Fallo BD")).when(respuestasExamanDAO).crear(any());
        frm.btnGuardarHandler(null);
        verify(respuestasExamanDAO, never()).findRange(anyInt(), anyInt());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnModificarHandler ====================

    @Test
    void btnModificarHandler_Valido_ActualizaRefrescaYLimpia() {
        when(respuestasExamanDAO.leer(testId)).thenReturn(entidad);
        when(respuestasExamanDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        configurarBundle("frm.botones.opModificar", "Modificado");
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(respuestasExamanDAO).actualizar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<RespuestasExaman> registrosInvalidosParaModificar() {
        return Stream.of(null, new RespuestasExaman());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaModificar")
    void btnModificarHandler_RegistroInvalido_ErrorSinActualizar(RespuestasExaman registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnModificarHandler(null);
        verify(respuestasExamanDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void btnModificarHandler_NoEncontradoEnBD_Error() {
        when(respuestasExamanDAO.leer(testId)).thenReturn(null);
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(respuestasExamanDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnEliminarHandler ====================

    @Test
    void btnEliminarHandler_Valido_EliminaRefrescaYLimpia() {
        configurarBundle("frm.botones.opEliminar", "Eliminado");
        when(respuestasExamanDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnEliminarHandler(null);
        verify(respuestasExamanDAO).eliminar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<RespuestasExaman> registrosInvalidosParaEliminar() {
        return Stream.of(null, new RespuestasExaman());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaEliminar")
    void btnEliminarHandler_RegistroInvalido_ErrorSinEliminar(RespuestasExaman registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnEliminarHandler(null);
        verify(respuestasExamanDAO, never()).eliminar(any());
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
        when(respuestasExamanDAO.findRange(20, 10)).thenReturn(List.of());
        frm.inicializarRegistros();
        verify(respuestasExamanDAO).findRange(20, 10);
    }
}
