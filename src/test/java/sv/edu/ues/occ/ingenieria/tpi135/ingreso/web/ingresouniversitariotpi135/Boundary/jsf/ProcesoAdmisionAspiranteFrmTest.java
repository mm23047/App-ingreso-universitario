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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CatalogoCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.EtapasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ProcesoAdmisionAspiranteDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ProcesoAdmisionAspirante;

import java.util.List;
import java.util.UUID;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcesoAdmisionAspiranteFrmTest {

    @InjectMocks private ProcesoAdmisionAspiranteFrm frm;

    @Mock private ProcesoAdmisionAspiranteDAO procesoAdmisionAspiranteDAO;
    @Mock private InscripcionesPruebaDAO inscripcionesPruebaDAO;
    @Mock private EtapasAdmisionDAO etapasAdmisionDAO;
    @Mock private CatalogoCarreraDAO catalogoCarreraDAO;
    @Mock private FacesContext facesContextMock;
    @Mock private Application application;
    @Mock private ResourceBundle resourceBundle;

    private MockedStatic<FacesContext> facesContextStatic;
    private ProcesoAdmisionAspirante entidad;
    private InscripcionesPrueba inscripcion;
    private EtapasAdmision etapa;
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

        etapa = new EtapasAdmision();
        etapa.setId(testId);
        etapa.setNombre("Inscripcion");

        carrera = new CatalogoCarrera();
        carrera.setIdCarrera("IC");
        carrera.setNombre("Ingenieria en Sistemas");

        entidad = new ProcesoAdmisionAspirante();
        entidad.setId(testId);
        entidad.setInscripcionesPrueba(inscripcion);
        entidad.setIdEtapaActual(etapa);
        entidad.setCarreraAsignada(carrera);
        entidad.setEstado("ACTIVO");
    }

    @AfterEach
    void tearDown() {
        facesContextStatic.close();
    }

    // ==================== METODOS AUXILIARES ====================

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
    void constructor_NombreBeanEsProcesoAdmisionAspirante() {
        assertEquals("Proceso Admision Aspirante", frm.getNombreBean());
    }

    // ==================== getIdAsText ====================

    @Test
    void getIdAsText_EntidadConId_RetornaString() {
        assertEquals(testId.toString(), frm.getIdAsText(entidad));
    }

    @Test
    void getIdAsText_EntidadConIdNulo_RetornaNull() {
        assertNull(frm.getIdAsText(new ProcesoAdmisionAspirante()));
    }

    @Test
    void getIdAsText_EntidadNula_RetornaNull() {
        assertNull(frm.getIdAsText(null));
    }

    // ==================== getIdByText ====================

    @Test
    void getIdByText_IdValido_RetornaEntidadDesdeDAO() {
        when(procesoAdmisionAspiranteDAO.leer(testId)).thenReturn(entidad);
        ProcesoAdmisionAspirante resultado = frm.getIdByText(testId.toString());
        assertSame(entidad, resultado);
        verify(procesoAdmisionAspiranteDAO).leer(testId);
    }

    @Test
    void getIdByText_IdNulo_RetornaNull() {
        assertNull(frm.getIdByText(null));
        verify(procesoAdmisionAspiranteDAO, never()).leer(any());
    }

    @Test
    void getIdByText_IdNoNumerico_RetornaNull() {
        assertNull(frm.getIdByText("abc"));
        verify(procesoAdmisionAspiranteDAO, never()).leer(any());
    }

    @Test
    void getIdByText_DAORetornaNull_RetornaNull() {
        when(procesoAdmisionAspiranteDAO.leer(testId)).thenReturn(null);
        assertNull(frm.getIdByText(testId.toString()));
    }

    // ==================== createNewEntity ====================

    @Test
    void createNewEntity_EstadoInicializadoVacio() {
        configurarBundle("frm.botones.formListo", "Listo");
        frm.btnNuevoHandler(null);
        ProcesoAdmisionAspirante nuevo = frm.getRegistro();
        assertNotNull(nuevo);
        assertEquals("", nuevo.getEstado());
        // Las relaciones las elige el usuario en la vista
        assertNull(nuevo.getInscripcionesPrueba());
        assertNull(nuevo.getIdEtapaActual());
        assertNull(nuevo.getCarreraAsignada());
    }

    // ==================== inicializarListas ====================

    @Test
    void inicializarListas_Exitoso_CargaTodasLasListas() {
        when(inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(inscripcion));
        when(etapasAdmisionDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(etapa));
        when(catalogoCarreraDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(carrera));

        frm.inicializarListas();

        assertEquals(1, frm.getInscripcionesDisponibles().size());
        assertSame(inscripcion, frm.getInscripcionesDisponibles().get(0));
        assertEquals(1, frm.getEtapasDisponibles().size());
        assertSame(etapa, frm.getEtapasDisponibles().get(0));
        assertEquals(1, frm.getCarrerasDisponibles().size());
        assertSame(carrera, frm.getCarrerasDisponibles().get(0));
    }

    @Test
    void inicializarListas_ExcepcionEnInscripciones_SigueCargandoResto() {
        when(inscripcionesPruebaDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo inscripciones"));
        when(etapasAdmisionDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(etapa));
        when(catalogoCarreraDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(carrera));

        frm.inicializarListas();

        assertTrue(frm.getInscripcionesDisponibles().isEmpty());
        assertEquals(1, frm.getEtapasDisponibles().size());
        assertEquals(1, frm.getCarrerasDisponibles().size());
    }

    @Test
    void inicializarListas_ExcepcionEnEtapas_InscripcionesYCarrerasCargadas() {
        when(inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(inscripcion));
        when(etapasAdmisionDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo etapas"));
        when(catalogoCarreraDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(carrera));

        frm.inicializarListas();

        assertEquals(1, frm.getInscripcionesDisponibles().size());
        assertTrue(frm.getEtapasDisponibles().isEmpty());
        assertEquals(1, frm.getCarrerasDisponibles().size());
    }

    @Test
    void inicializarListas_ExcepcionEnCarreras_OtrasListasYaCargadas() {
        when(inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(inscripcion));
        when(etapasAdmisionDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(etapa));
        when(catalogoCarreraDAO.findRange(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Fallo carreras"));

        frm.inicializarListas();

        assertEquals(1, frm.getInscripcionesDisponibles().size());
        assertEquals(1, frm.getEtapasDisponibles().size());
        assertTrue(frm.getCarrerasDisponibles().isEmpty());
    }

    // ==================== inicializar (PostConstruct) ====================

    @Test
    void inicializar_InvocaRegistrosYListas() {
        when(procesoAdmisionAspiranteDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        when(inscripcionesPruebaDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(inscripcion));
        when(etapasAdmisionDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(etapa));
        when(catalogoCarreraDAO.findRange(0, Integer.MAX_VALUE)).thenReturn(List.of(carrera));

        frm.inicializar();

        verify(procesoAdmisionAspiranteDAO).findRange(0, 5);
        assertEquals(1, frm.getRegistros().size());
        assertEquals(1, frm.getInscripcionesDisponibles().size());
        assertEquals(1, frm.getEtapasDisponibles().size());
        assertEquals(1, frm.getCarrerasDisponibles().size());
    }

    // ==================== inicializarRegistros ====================

    @Test
    void inicializarRegistros_ConDatos_CargaLista() {
        when(procesoAdmisionAspiranteDAO.findRange(0, 5)).thenReturn(List.of(entidad));
        frm.inicializarRegistros();
        assertEquals(1, frm.getRegistros().size());
        assertSame(entidad, frm.getRegistros().get(0));
    }

    @Test
    void inicializarRegistros_ConExcepcion_ListaVaciaYMensajeError() {
        when(procesoAdmisionAspiranteDAO.findRange(anyInt(), anyInt()))
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
        when(procesoAdmisionAspiranteDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnGuardarHandler(null);
        verify(procesoAdmisionAspiranteDAO).crear(entidad);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstado());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnGuardarHandler_RegistroNulo_ErrorSinLlamarDao() {
        frm.setRegistro(null);
        frm.btnGuardarHandler(null);
        verify(procesoAdmisionAspiranteDAO, never()).crear(any());
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
        assertTrue(msg.getDetail().contains("nulo"));
    }

    @Test
    void btnGuardarHandler_Excepcion_ErrorSinRefrescar() {
        frm.setRegistro(entidad);
        doThrow(new RuntimeException("Fallo BD")).when(procesoAdmisionAspiranteDAO).crear(any());
        frm.btnGuardarHandler(null);
        verify(procesoAdmisionAspiranteDAO, never()).findRange(anyInt(), anyInt());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnModificarHandler ====================

    @Test
    void btnModificarHandler_Valido_ActualizaRefrescaYLimpia() {
        when(procesoAdmisionAspiranteDAO.leer(testId)).thenReturn(entidad);
        when(procesoAdmisionAspiranteDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        configurarBundle("frm.botones.opModificar", "Modificado");
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(procesoAdmisionAspiranteDAO).actualizar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<ProcesoAdmisionAspirante> registrosInvalidosParaModificar() {
        return Stream.of(null, new ProcesoAdmisionAspirante());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaModificar")
    void btnModificarHandler_RegistroInvalido_ErrorSinActualizar(ProcesoAdmisionAspirante registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnModificarHandler(null);
        verify(procesoAdmisionAspiranteDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void btnModificarHandler_NoEncontradoEnBD_Error() {
        when(procesoAdmisionAspiranteDAO.leer(testId)).thenReturn(null);
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(procesoAdmisionAspiranteDAO, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnEliminarHandler ====================

    @Test
    void btnEliminarHandler_Valido_EliminaRefrescaYLimpia() {
        configurarBundle("frm.botones.opEliminar", "Eliminado");
        when(procesoAdmisionAspiranteDAO.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnEliminarHandler(null);
        verify(procesoAdmisionAspiranteDAO).eliminar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<ProcesoAdmisionAspirante> registrosInvalidosParaEliminar() {
        return Stream.of(null, new ProcesoAdmisionAspirante());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaEliminar")
    void btnEliminarHandler_RegistroInvalido_ErrorSinEliminar(ProcesoAdmisionAspirante registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnEliminarHandler(null);
        verify(procesoAdmisionAspiranteDAO, never()).eliminar(any());
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

    // ==================== Paginacion ====================

    @Test
    void paginacion_ValoresPorDefecto_SonCorrectos() {
        assertEquals(0, frm.getPaginaActual());
        assertEquals(5, frm.getPageSize());
    }

    @Test
    void paginacion_CambioValores_UsaOffsetCorrecto() {
        frm.setPaginaActual(2);
        frm.setPageSize(5);
        when(procesoAdmisionAspiranteDAO.findRange(10, 5)).thenReturn(List.of());
        frm.inicializarRegistros();
        verify(procesoAdmisionAspiranteDAO).findRange(10, 5);
    }
}
