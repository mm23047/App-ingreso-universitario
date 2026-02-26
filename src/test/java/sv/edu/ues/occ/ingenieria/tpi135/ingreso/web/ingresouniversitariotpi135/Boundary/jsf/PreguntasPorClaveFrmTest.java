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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntasPorClaveDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClaveId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreguntasPorClaveFrmTest {

    @InjectMocks private PreguntasPorClaveFrm frm;

    @Mock private PreguntasPorClaveDAO preguntasPorClaveDAO;
    @Mock private ClavesExamanDAO clavesExamanDAO;
    @Mock private BancoPreguntaDAO bancoPreguntaDAO;
    @Mock private FacesContext facesContextMock;
    @Mock private Application application;
    @Mock private ResourceBundle resourceBundle;

    private MockedStatic<FacesContext> facesContextStatic;
    private PreguntasPorClave entidad;
    private PreguntasPorClaveId claveId;
    private ClavesExaman clave;
    private BancoPregunta pregunta;

    // ==================== SETUP ====================
    private UUID testId;
    private UUID claveUUID;
    private UUID preguntaUUID;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        claveUUID = UUID.randomUUID();
        preguntaUUID = UUID.randomUUID();
        facesContextStatic = mockStatic(FacesContext.class);
        facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContextMock);

        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setId(testId);
        prueba.setNombrePrueba("Prueba 2026");
        prueba.setAnio(2026);

        clave = new ClavesExaman();
        clave.setId(testId);
        clave.setNombreClave("Clave A");
        clave.setIdPrueba(prueba);

        AreasConocimiento area = new AreasConocimiento();
        area.setId(testId);
        area.setNombreArea("Matemáticas");

        pregunta = new BancoPregunta();
        pregunta.setId(testId);
        pregunta.setEnunciado("¿Cuánto es 2+2?");
        pregunta.setIdArea(area);

        claveId = new PreguntasPorClaveId();
        claveId.setIdClave(claveUUID);
        claveId.setIdPregunta(preguntaUUID);

        entidad = new PreguntasPorClave();
        entidad.setId(claveId);
        entidad.setIdClave(clave);
        entidad.setIdPregunta(pregunta);
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
    void constructor_NombreBeanEsPreguntasPorClave() {
        assertEquals("Preguntas por Clave", frm.getNombreBean());
    }

    // ==================== getIdAsText ====================

    @Test
    void getIdAsText_EntidadConIdCompleto_RetornaTextoCompuesto() {
        // La clave compuesta se serializa como "idClave|idPregunta"
        assertEquals(claveUUID + "|" + preguntaUUID, frm.getIdAsText(entidad));
    }

    @Test
    void getIdAsText_EntidadConIdNulo_RetornaNull() {
        assertNull(frm.getIdAsText(new PreguntasPorClave()));
    }

    @Test
    void getIdAsText_EntidadNula_RetornaNull() {
        assertNull(frm.getIdAsText(null));
    }

    @Test
    void getIdAsText_EntidadConClaveParcialNula_RetornaNull() {
        PreguntasPorClave conIdVacio = new PreguntasPorClave();
        conIdVacio.setId(new PreguntasPorClaveId());
        assertNull(frm.getIdAsText(conIdVacio));
    }

    // ==================== getIdByText ====================

    @Test
    void getIdByText_FormatoValido_RetornaEntidadDesdeDAO() {
        when(preguntasPorClaveDAO.leer(claveId)).thenReturn(entidad);
        PreguntasPorClave resultado = frm.getIdByText(claveUUID + "|" + preguntaUUID);
        assertSame(entidad, resultado);
        verify(preguntasPorClaveDAO).leer(claveId);
    }

    @Test
    void getIdByText_IdNulo_RetornaNull() {
        assertNull(frm.getIdByText(null));
        verify(preguntasPorClaveDAO, never()).leer(any());
    }

    @Test
    void getIdByText_SinSeparador_RetornaNull() {
        assertNull(frm.getIdByText(claveUUID.toString().replace("-", "") + preguntaUUID.toString().replace("-", "")));
        verify(preguntasPorClaveDAO, never()).leer(any());
    }

    @Test
    void getIdByText_ClaveNoNumerica_RetornaNull() {
        assertNull(frm.getIdByText("no-es-un-uuid-valido|" + preguntaUUID));
        verify(preguntasPorClaveDAO, never()).leer(any());
    }

    @Test
    void getIdByText_PreguntaNoNumerica_RetornaNull() {
        assertNull(frm.getIdByText(claveUUID + "|no-es-un-uuid-valido"));
        verify(preguntasPorClaveDAO, never()).leer(any());
    }

    @Test
    void getIdByText_DAORetornaNull_RetornaNull() {
        UUID otroClaveUUID = UUID.randomUUID();
        UUID otroPreguntaUUID = UUID.randomUUID();
        PreguntasPorClaveId otraClave = new PreguntasPorClaveId();
        otraClave.setIdClave(otroClaveUUID);
        otraClave.setIdPregunta(otroPreguntaUUID);
        when(preguntasPorClaveDAO.leer(otraClave)).thenReturn(null);
        assertNull(frm.getIdByText(otroClaveUUID + "|" + otroPreguntaUUID));
    }

    // ==================== createNewEntity ====================

    @Test
    void createNewEntity_CamposInicializadosCorrectamente() {
        configurarBundle("frm.botones.formListo", "Listo");
        frm.btnNuevoHandler(null);
        PreguntasPorClave nueva = frm.getRegistro();
        assertNotNull(nueva);
        // La clave compuesta se inicializa vacía (no null) para JPA
        assertNotNull(nueva.getId());
        assertInstanceOf(PreguntasPorClaveId.class, nueva.getId());
        assertNull(nueva.getId().getIdClave());
        assertNull(nueva.getId().getIdPregunta());
        // Las relaciones las elige el usuario en la vista
        assertNull(nueva.getIdClave());
        assertNull(nueva.getIdPregunta());
    }

    // ==================== inicializarListas ====================

    @Test
    void inicializarListas_Exitoso_CargaAmbas() {
        when(clavesExamanDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(clave));
        when(bancoPreguntaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(pregunta));

        frm.inicializarListas();

        assertEquals(1, frm.getClavesDisponibles().size());
        assertSame(clave, frm.getClavesDisponibles().get(0));
        assertEquals(1, frm.getPreguntasDisponibles().size());
        assertSame(pregunta, frm.getPreguntasDisponibles().get(0));
    }

    @Test
    void inicializarListas_ExcepcionEnClaves_SigueCargandoPreguntas() {
        when(clavesExamanDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo claves"));
        when(bancoPreguntaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(pregunta));

        frm.inicializarListas();

        assertTrue(frm.getClavesDisponibles().isEmpty());
        assertEquals(1, frm.getPreguntasDisponibles().size());
    }

    @Test
    void inicializarListas_ExcepcionEnPreguntas_ClavesYaCargadasPermanecen() {
        when(clavesExamanDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(clave));
        when(bancoPreguntaDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo preguntas"));

        frm.inicializarListas();

        assertEquals(1, frm.getClavesDisponibles().size());
        assertTrue(frm.getPreguntasDisponibles().isEmpty());
    }

    @Test
    void getListasAuxiliares_EstadoInicialEsVacio() {
        assertTrue(frm.getClavesDisponibles().isEmpty());
        assertTrue(frm.getPreguntasDisponibles().isEmpty());
    }

    // ==================== inicializar (PostConstruct) ====================

    @Test
    void inicializar_InvocaRegistrosYListas() {
        when(preguntasPorClaveDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        when(clavesExamanDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(clave));
        when(bancoPreguntaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(pregunta));

        frm.inicializar();

        verify(preguntasPorClaveDAO).findRange(0, 5);
        assertEquals(1, frm.getRegistros().size());
        assertEquals(1, frm.getClavesDisponibles().size());
        assertEquals(1, frm.getPreguntasDisponibles().size());
    }

    // ==================== inicializarRegistros ====================

    @Test
    void inicializarRegistros_ConDatos_CargaLista() {
        when(preguntasPorClaveDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        frm.inicializarRegistros();
        assertEquals(1, frm.getRegistros().size());
        assertSame(entidad, frm.getRegistros().get(0));
    }

    @Test
    void inicializarRegistros_ConExcepcion_ListaVaciaYMensajeError() {
        when(preguntasPorClaveDAO.findRange(anyInt(), anyInt()))
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
        when(preguntasPorClaveDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnGuardarHandler(null);
        verify(preguntasPorClaveDAO).crear(entidad);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnGuardarHandler_RegistroNulo_ErrorSinLlamarDao() {
        frm.setRegistro(null);
        frm.btnGuardarHandler(null);
        verify(preguntasPorClaveDAO, never()).crear(any());
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
        assertTrue(msg.getDetail().contains("nulo"));
    }

    @Test
    void btnGuardarHandler_Excepcion_ErrorSinRefrescar() {
        frm.setRegistro(entidad);
        doThrow(new RuntimeException("Fallo BD")).when(preguntasPorClaveDAO).crear(any());
        frm.btnGuardarHandler(null);
        verify(preguntasPorClaveDAO, never()).findRange(anyInt(), anyInt());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnModificarHandler ====================

    @Test
    void btnModificarHandler_Valido_ActualizaRefrescaYLimpia() {
        when(preguntasPorClaveDAO.leer(claveId)).thenReturn(entidad);
        when(preguntasPorClaveDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        configurarBundle("frm.botones.opModificar", "Modificado");
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(preguntasPorClaveDAO).actualizar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<PreguntasPorClave> registrosInvalidosParaModificar() {
        return Stream.of(null, new PreguntasPorClave());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaModificar")
    void btnModificarHandler_RegistroInvalido_ErrorSinActualizar(PreguntasPorClave registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnModificarHandler(null);
        verify(preguntasPorClaveDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void btnModificarHandler_NoEncontradoEnBD_Error() {
        when(preguntasPorClaveDAO.leer(claveId)).thenReturn(null);
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(preguntasPorClaveDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnEliminarHandler ====================

    @Test
    void btnEliminarHandler_Valido_EliminaRefrescaYLimpia() {
        configurarBundle("frm.botones.opEliminar", "Eliminado");
        when(preguntasPorClaveDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnEliminarHandler(null);
        verify(preguntasPorClaveDAO).eliminar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<PreguntasPorClave> registrosInvalidosParaEliminar() {
        return Stream.of(null, new PreguntasPorClave());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaEliminar")
    void btnEliminarHandler_RegistroInvalido_ErrorSinEliminar(PreguntasPorClave registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnEliminarHandler(null);
        verify(preguntasPorClaveDAO, never()).eliminar(any());
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
        when(preguntasPorClaveDAO.findRange(8, 8)).thenReturn(List.of());
        frm.inicializarRegistros();
        verify(preguntasPorClaveDAO).findRange(8, 8);
    }
}
