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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CarrerasElegidaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CatalogoCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarrerasElegidaFrmTest {

    // Mockito inyecta los tres @Mock por coincidencia de tipo en los campos del FRM
    @InjectMocks private CarrerasElegidaFrm frm;

    @Mock private CarrerasElegidaDAO carrerasElegidaDAO;
    @Mock private InscripcionesPruebaDAO inscripcionesPruebaDAO;
    @Mock private CatalogoCarreraDAO catalogoCarreraDAO;
    @Mock private FacesContext facesContextMock;
    @Mock private Application application;
    @Mock private ResourceBundle resourceBundle;

    private MockedStatic<FacesContext> facesContextStatic;
    private CarrerasElegida entidad;
    private CarrerasElegidaId carrerasId;
    private InscripcionesPrueba inscripcion;
    private CatalogoCarrera carrera;

    // ==================== SETUP ====================
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        facesContextStatic = mockStatic(FacesContext.class);
        facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContextMock);

        inscripcion = new InscripcionesPrueba();
        inscripcion.setId(testId);

        carrera = new CatalogoCarrera();
        carrera.setIdCarrera("MEC");
        carrera.setNombre("Ingeniería Mecánica");

        carrerasId = new CarrerasElegidaId();
        carrerasId.setIdInscripcion(testId);
        carrerasId.setIdCarrera("MEC");

        entidad = new CarrerasElegida();
        entidad.setId(carrerasId);
        entidad.setIdInscripcion(inscripcion);
        entidad.setIdCarrera(carrera);
        entidad.setPrioridad((short) 1);
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
    void constructor_NombreBeanEsCarrerasElegidas() {
        assertEquals("Carreras Elegidas", frm.getNombreBean());
    }

    // ==================== getIdAsText ====================

    @Test
    void getIdAsText_EntidadConIdCompleto_RetornaTextoCompuesto() {
        // La clave compuesta se serializa como "idInscripcion|idCarrera"
        assertEquals(testId + "|MEC", frm.getIdAsText(entidad));
    }

    @Test
    void getIdAsText_EntidadConIdNulo_RetornaNull() {
        // Entidad sin id configurado
        assertNull(frm.getIdAsText(new CarrerasElegida()));
    }

    @Test
    void getIdAsText_EntidadNula_RetornaNull() {
        assertNull(frm.getIdAsText(null));
    }

    @Test
    void getIdAsText_EntidadConClaveParcialNula_RetornaNull() {
        // CarrerasElegidaId con campos vacíos (idInscripcion y idCarrera son null)
        CarrerasElegida conIdVacio = new CarrerasElegida();
        conIdVacio.setId(new CarrerasElegidaId());
        assertNull(frm.getIdAsText(conIdVacio));
    }

    // ==================== getIdByText ====================

    @Test
    void getIdByText_FormatoValido_RetornaEntidadDesdeDAO() {
        // La clave reconstruida debe igualar carrerasId por equals/hashCode
        when(carrerasElegidaDAO.leer(carrerasId)).thenReturn(entidad);
        CarrerasElegida resultado = frm.getIdByText(testId + "|MEC");
        assertSame(entidad, resultado);
        verify(carrerasElegidaDAO).leer(carrerasId);
    }

    @Test
    void getIdByText_IdNulo_RetornaNull() {
        assertNull(frm.getIdByText(null));
        verify(carrerasElegidaDAO, never()).leer(any());
    }

    @Test
    void getIdByText_SinSeparador_RetornaNull() {
        // split produce solo 1 parte → no se reconstruye la clave
        assertNull(frm.getIdByText(testId.toString().replace("-", "") + "MEC"));
        verify(carrerasElegidaDAO, never()).leer(any());
    }

    @Test
    void getIdByText_InscripcionNoNumerica_RetornaNull() {
        // parseInt("abc") lanza NumberFormatException → catch retorna null
        assertNull(frm.getIdByText("abc|MEC"));
        verify(carrerasElegidaDAO, never()).leer(any());
    }

    @Test
    void getIdByText_DAORetornaNull_RetornaNull() {
        UUID otherId = UUID.randomUUID();
        CarrerasElegidaId clave = new CarrerasElegidaId();
        clave.setIdInscripcion(otherId);
        clave.setIdCarrera("XXX");
        when(carrerasElegidaDAO.leer(clave)).thenReturn(null);
        assertNull(frm.getIdByText(otherId + "|XXX"));
    }

    // ==================== createNewEntity ====================

    @Test
    void createNewEntity_CamposInicializadosCorrectamente() {
        configurarBundle("frm.botones.formListo", "Listo");
        frm.btnNuevoHandler(null);
        CarrerasElegida nueva = frm.getRegistro();
        assertNotNull(nueva);
        // La clave compuesta se inicializa vacía (no null) para JPA
        assertNotNull(nueva.getId());
        assertNull(nueva.getId().getIdInscripcion());
        assertNull(nueva.getId().getIdCarrera());
        assertEquals((short) 0, nueva.getPrioridad());
        // Las relaciones las elige el usuario en la vista
        assertNull(nueva.getIdInscripcion());
        assertNull(nueva.getIdCarrera());
    }

    @Test
    void createNewEntity_IdContieneCarrerasElegidaIdVacio() {
        configurarBundle("frm.botones.formListo", "Listo");
        frm.btnNuevoHandler(null);
        assertInstanceOf(CarrerasElegidaId.class, frm.getRegistro().getId());
    }

    // ==================== inicializarListas ====================

    @Test
    void inicializarListas_Exitoso_CargaAmbas() {
        when(inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(inscripcion));
        when(catalogoCarreraDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(carrera));

        frm.inicializarListas();

        assertEquals(1, frm.getInscripcionesDisponibles().size());
        assertSame(inscripcion, frm.getInscripcionesDisponibles().get(0));
        assertEquals(1, frm.getCarrerasDisponibles().size());
        assertSame(carrera, frm.getCarrerasDisponibles().get(0));
    }

    @Test
    void inicializarListas_ExcepcionEnInscripciones_ListaVaciaYSigueCargandoCarreras() {
        when(inscripcionesPruebaDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo inscripciones"));
        when(catalogoCarreraDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(carrera));

        frm.inicializarListas();

        assertTrue(frm.getInscripcionesDisponibles().isEmpty());
        assertEquals(1, frm.getCarrerasDisponibles().size());
    }

    @Test
    void inicializarListas_ExcepcionEnCarreras_InscripcionesCargadasYCarrerasVacia() {
        when(inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(inscripcion));
        when(catalogoCarreraDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo carreras"));

        frm.inicializarListas();

        assertEquals(1, frm.getInscripcionesDisponibles().size());
        assertTrue(frm.getCarrerasDisponibles().isEmpty());
    }

    @Test
    void inicializarListas_AmbosVacios_RetornaListasVacias() {
        when(inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of());
        when(catalogoCarreraDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of());

        frm.inicializarListas();

        assertTrue(frm.getInscripcionesDisponibles().isEmpty());
        assertTrue(frm.getCarrerasDisponibles().isEmpty());
    }

    // ==================== Getters de listas auxiliares (estado inicial) ====================

    @Test
    void getInscripcionesDisponibles_EstadoInicialEsVacio() {
        assertTrue(frm.getInscripcionesDisponibles().isEmpty());
    }

    @Test
    void getCarrerasDisponibles_EstadoInicialEsVacio() {
        assertTrue(frm.getCarrerasDisponibles().isEmpty());
    }

    // ==================== inicializar (PostConstruct) ====================

    @Test
    void inicializar_InvocaRegistrosEInicializarListas() {
        when(carrerasElegidaDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        when(inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(inscripcion));
        when(catalogoCarreraDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(carrera));

        frm.inicializar();

        verify(carrerasElegidaDAO).findRange(0, 5);
        verify(inscripcionesPruebaDAO).findRange(0, Integer.MAX_VALUE);
        verify(catalogoCarreraDAO).findRange(0, Integer.MAX_VALUE);
        assertEquals(1, frm.getRegistros().size());
        assertEquals(1, frm.getInscripcionesDisponibles().size());
        assertEquals(1, frm.getCarrerasDisponibles().size());
    }

    // ==================== inicializarRegistros ====================

    @Test
    void inicializarRegistros_ConDatos_CargaLista() {
        when(carrerasElegidaDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        frm.inicializarRegistros();
        assertEquals(1, frm.getRegistros().size());
        assertSame(entidad, frm.getRegistros().get(0));
    }

    @Test
    void inicializarRegistros_ConExcepcion_ListaVaciaYMensajeError() {
        when(carrerasElegidaDAO.findRange(anyInt(), anyInt()))
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
        when(carrerasElegidaDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnGuardarHandler(null);
        verify(carrerasElegidaDAO).crear(entidad);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnGuardarHandler_RegistroNulo_ErrorSinLlamarDao() {
        frm.setRegistro(null);
        frm.btnGuardarHandler(null);
        verify(carrerasElegidaDAO, never()).crear(any());
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
        assertTrue(msg.getDetail().contains("nulo"));
    }

    @Test
    void btnGuardarHandler_Excepcion_ErrorSinRefrescar() {
        frm.setRegistro(entidad);
        doThrow(new RuntimeException("Fallo BD")).when(carrerasElegidaDAO).crear(any());
        frm.btnGuardarHandler(null);
        verify(carrerasElegidaDAO, never()).findRange(anyInt(), anyInt());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnModificarHandler ====================

    @Test
    void btnModificarHandler_Valido_ActualizaRefrescaYLimpia() {
        // carrerasId tiene equals/hashCode, Mockito lo usa para hacer matching
        when(carrerasElegidaDAO.leer(carrerasId)).thenReturn(entidad);
        when(carrerasElegidaDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        configurarBundle("frm.botones.opModificar", "Modificado");
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(carrerasElegidaDAO).actualizar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<CarrerasElegida> registrosInvalidosParaModificar() {
        // null: getEntityId(null) → null → error
        // new CarrerasElegida(): id es null → getEntityId → null → error
        return Stream.of(null, new CarrerasElegida());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaModificar")
    void btnModificarHandler_RegistroInvalido_ErrorSinActualizar(CarrerasElegida registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnModificarHandler(null);
        verify(carrerasElegidaDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void btnModificarHandler_NoEncontradoEnBD_Error() {
        when(carrerasElegidaDAO.leer(carrerasId)).thenReturn(null);
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(carrerasElegidaDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnEliminarHandler ====================

    @Test
    void btnEliminarHandler_Valido_EliminaRefrescaYLimpia() {
        configurarBundle("frm.botones.opEliminar", "Eliminado");
        when(carrerasElegidaDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnEliminarHandler(null);
        verify(carrerasElegidaDAO).eliminar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<CarrerasElegida> registrosInvalidosParaEliminar() {
        return Stream.of(null, new CarrerasElegida());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaEliminar")
    void btnEliminarHandler_RegistroInvalido_ErrorSinEliminar(CarrerasElegida registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnEliminarHandler(null);
        verify(carrerasElegidaDAO, never()).eliminar(any());
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
        when(carrerasElegidaDAO.findRange(8, 8)).thenReturn(List.of());
        frm.inicializarRegistros();
        verify(carrerasElegidaDAO).findRange(8, 8);
    }
}
