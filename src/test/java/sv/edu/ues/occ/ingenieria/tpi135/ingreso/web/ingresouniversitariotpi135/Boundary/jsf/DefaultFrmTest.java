package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.jsf;

import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDAOInterface;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.math.BigDecimal;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultFrmTest {

    @Mock private IngresoDAOInterface<EtapasAdmision> dao;
    @Mock private FacesContext facesContextMock;
    @Mock private Application application;
    @Mock private ResourceBundle resourceBundle;
    @Mock private UIComponent uiComponent;

    private MockedStatic<FacesContext> facesContextStatic;
    //  FIX 1: clase estática para que @MethodSource pueda instanciarla
    private TestableDefaultFrm frm;
    private EtapasAdmision entidad;

    // ==================== IMPLEMENTACIÓN CONCRETA (ESTÁTICA) ====================
    //  FIX 1: debe ser static para compatibilidad con @ParameterizedTest/@MethodSource
    static class TestableDefaultFrm extends DefaultFrm<EtapasAdmision> {

        // dependencias inyectadas desde el test
        IngresoDAOInterface<EtapasAdmision> dao;
        FacesContext facesContext;
        Supplier<EtapasAdmision> nuevoRegistroFn = EtapasAdmision::new;
        Function<Object, EtapasAdmision> buscarFn;

        TestableDefaultFrm(IngresoDAOInterface<EtapasAdmision> dao, FacesContext fc) {
            this.dao = dao;
            this.facesContext = fc;
            this.buscarFn = id -> dao.leer(id);
        }

        @Override protected FacesContext getFacesContext()          { return facesContext; }
        @Override protected IngresoDAOInterface<EtapasAdmision> getDao() { return dao; }
        @Override protected EtapasAdmision nuevoRegistro()          { return nuevoRegistroFn.get(); }
        @Override protected EtapasAdmision buscarRegistroPorId(Object id) { return buscarFn.apply(id); }
        @Override protected String getIdAsText(EtapasAdmision r) {
            return (r != null && r.getId() != null) ? r.getId().toString() : null;
        }
        @Override protected EtapasAdmision getIdByText(String id)  { return dao.leer(Short.parseShort(id)); }
        @Override protected EtapasAdmision createNewEntity()        { return new EtapasAdmision(); }
        @Override protected Object getEntityId(EtapasAdmision e)    { return e != null ? e.getId() : null; }
        @Override protected String getEntityName()                  { return "EtapasAdmision"; }

        //  FIX 3: expone campos protected para los tests
        public void setEstadoPublico(ESTADO_CRUD e)  { this.estado = e; }
        public ESTADO_CRUD getEstadoPublico()         { return this.estado; }
        public void setPaginaActualPublica(int p)     { this.paginaActual = p; }
        public void setPageSizePublico(int s)         { this.pageSize = s; }
    }

    // ==================== SETUP ====================

    @BeforeEach
    void setUp() {
        facesContextStatic = mockStatic(FacesContext.class);
        facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContextMock);
        frm = new TestableDefaultFrm(dao, facesContextMock);
        entidad = new EtapasAdmision();
        entidad.setId((short) 1);
        entidad.setNombre("Etapa Preuniversitaria");
        entidad.setPuntajeMinimo(new BigDecimal("60.00"));
        entidad.setPuntajeMaximo(new BigDecimal("100.00"));
    }

    @AfterEach
    void tearDown() { facesContextStatic.close(); }

    // ==================== MÉTODOS AUXILIARES ====================

    /** Configura ResourceBundle para pruebas que llegan a mostrar mensaje de éxito */
    private void configurarBundle(String clave, String valor) {
        when(facesContextMock.getApplication()).thenReturn(application);
        //  FIX 4: usamos any() en lugar de eq("crud") por si cambia el nombre del bundle
        when(application.getResourceBundle(any(FacesContext.class), anyString())).thenReturn(resourceBundle);
        when(resourceBundle.getString(clave)).thenReturn(valor);
    }

    /** Captura el FacesMessage enviado y lo retorna para verificación */
    private FacesMessage capturarMensaje() {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContextMock).addMessage(isNull(), captor.capture());
        return captor.getValue();
    }

    /** Verifica severidad del mensaje capturado */
    private void verificarSeveridad(FacesMessage.Severity severidadEsperada) {
        assertEquals(severidadEsperada, capturarMensaje().getSeverity());
    }

    // ==================== inicializarRegistros ====================

    @Test
    void inicializarRegistros_ConDatos_CargaLista() {
        when(dao.findRange(0, 5)).thenReturn(List.of(entidad));
        frm.inicializarRegistros();
        assertEquals(1, frm.getRegistros().size());
        assertSame(entidad, frm.getRegistros().get(0));
    }

    @Test
    void inicializarRegistros_ListaVacia_RetornaVacia() {
        when(dao.findRange(0, 5)).thenReturn(List.of());
        frm.inicializarRegistros();
        assertTrue(frm.getRegistros().isEmpty());
    }

    @Test
    void inicializarRegistros_ConExcepcion_ListaVaciaYMensajeError() {
        when(dao.findRange(anyInt(), anyInt())).thenThrow(new RuntimeException("Error BD"));
        frm.inicializarRegistros();
        assertTrue(frm.getRegistros().isEmpty());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void inicializarRegistros_RespetaPaginacion() {
        frm.setPaginaActualPublica(2);
        frm.setPageSizePublico(10);
        when(dao.findRange(20, 10)).thenReturn(List.of());
        frm.inicializarRegistros();
        verify(dao).findRange(20, 10);
    }

    // ==================== seleccionarRegistro ====================

    @Test
    void seleccionarRegistro_Valido_EstableceMODIFICAR() {
        frm.seleccionarRegistro(entidad);
        assertSame(entidad, frm.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, frm.getEstadoPublico());
    }

    @Test
    void seleccionarRegistro_Null_NoModificaEstado() {
        frm.setEstadoPublico(ESTADO_CRUD.NADA);
        frm.seleccionarRegistro(null);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstadoPublico());
    }

    // ==================== btnNuevoHandler ====================

    @Test
    void btnNuevoHandler_Exitoso_EstadoCREARYMensajeInfo() {
        configurarBundle("frm.botones.formListo", "Formulario listo");
        frm.btnNuevoHandler(null);
        assertNotNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, frm.getEstadoPublico());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnNuevoHandler_Excepcion_LimpiaYMensajeError() {
        frm.nuevoRegistroFn = () -> { throw new RuntimeException("Error al crear entidad"); };
        frm.btnNuevoHandler(null);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstadoPublico());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnGuardarHandler ====================

    @Test
    void btnGuardarHandler_Valido_CreaRefrescaYLimpia() {
        configurarBundle("frm.botones.creado", "Creado exitosamente");
        when(dao.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnGuardarHandler(null);
        verify(dao).crear(entidad);
        verify(dao).findRange(0, 5);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstadoPublico());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnGuardarHandler_RegistroNulo_ErrorSinLlamarDao() {
        frm.setRegistro(null);
        frm.btnGuardarHandler(null);
        verify(dao, never()).crear(any());
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
        assertTrue(msg.getDetail().contains("nulo"));
    }

    @Test
    void btnGuardarHandler_Excepcion_ErrorSinRefrescar() {
        frm.setRegistro(entidad);
        doThrow(new RuntimeException("Fallo BD")).when(dao).crear(any());
        frm.btnGuardarHandler(null);
        verify(dao, never()).findRange(anyInt(), anyInt());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnModificarHandler ====================

    @Test
    void btnModificarHandler_Valido_ActualizaRefrescaYLimpia() {
        configurarBundle("frm.botones.opModificar", "Modificado exitosamente");
        when(dao.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.buscarFn = id -> entidad;
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(dao).actualizar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    //  FIX 1: métodos static para @MethodSource en clase no-interna
    static Stream<EtapasAdmision> registrosInvalidosParaModificar() {
        return Stream.of(null, new EtapasAdmision());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaModificar")
    void btnModificarHandler_RegistroInvalido_ErrorSinActualizar(EtapasAdmision registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnModificarHandler(null);
        verify(dao, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void btnModificarHandler_NoEncontradoEnBD_Error() {
        frm.buscarFn = id -> null;
        frm.setRegistro(entidad);
        frm.btnModificarHandler(null);
        verify(dao, never()).actualizar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void btnModificarHandler_Excepcion_Error() {
        frm.buscarFn = id -> entidad;
        frm.setRegistro(entidad);
        doThrow(new RuntimeException("Error BD")).when(dao).actualizar(any());
        frm.btnModificarHandler(null);
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnEliminarHandler ====================

    @Test
    void btnEliminarHandler_Valido_EliminaRefrescaYLimpia() {
        configurarBundle("frm.botones.opEliminar", "Eliminado exitosamente");
        when(dao.findRange(anyInt(), anyInt())).thenReturn(List.of());
        frm.setRegistro(entidad);
        frm.btnEliminarHandler(null);
        verify(dao).eliminar(entidad);
        assertNull(frm.getRegistro());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    static Stream<EtapasAdmision> registrosInvalidosParaEliminar() {
        return Stream.of(null, new EtapasAdmision());
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidosParaEliminar")
    void btnEliminarHandler_RegistroInvalido_ErrorSinEliminar(EtapasAdmision registroInvalido) {
        frm.setRegistro(registroInvalido);
        frm.btnEliminarHandler(null);
        verify(dao, never()).eliminar(any());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    @Test
    void btnEliminarHandler_Excepcion_Error() {
        frm.setRegistro(entidad);
        doThrow(new RuntimeException("Error")).when(dao).eliminar(any());
        frm.btnEliminarHandler(null);
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== btnCancelarHandler ====================

    @Test
    void btnCancelarHandler_Exitoso_LimpiaYMensajeInfo() {
        configurarBundle("frm.botones.opCancelar", "Operación cancelada");
        frm.setRegistro(entidad);
        frm.setEstadoPublico(ESTADO_CRUD.MODIFICAR);
        frm.btnCancelarHandler(null);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstadoPublico());
        verificarSeveridad(FacesMessage.SEVERITY_INFO);
    }

    @Test
    void btnCancelarHandler_Excepcion_LimpiaDeTodasFormas() {
        //  FIX 4: simulamos excepción en el ResourceBundle, no en getApplication
        when(facesContextMock.getApplication()).thenReturn(application);
        when(application.getResourceBundle(any(), anyString())).thenThrow(new RuntimeException("Error JSF"));
        frm.setRegistro(entidad);
        frm.setEstadoPublico(ESTADO_CRUD.MODIFICAR);
        frm.btnCancelarHandler(null);
        assertNull(frm.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, frm.getEstadoPublico());
        verificarSeveridad(FacesMessage.SEVERITY_ERROR);
    }

    // ==================== validarNombre ====================

    @Test
    void validarNombre_Nulo_LanzaExcepcion() {
        assertThrows(ValidatorException.class,
                () -> frm.validarNombre(facesContextMock, uiComponent, null));
    }

    @Test
    void validarNombre_Vacio_LanzaExcepcion() {
        assertThrows(ValidatorException.class,
                () -> frm.validarNombre(facesContextMock, uiComponent, ""));
    }

    @Test
    void validarNombre_MenosDe3_MensajeContieneMinimo() {
        ValidatorException ex = assertThrows(ValidatorException.class,
                () -> frm.validarNombre(facesContextMock, uiComponent, "ab"));
        assertTrue(ex.getFacesMessage().getSummary().contains("3"));
    }

    @Test
    void validarNombre_MasDe155_MensajeContieneMaximo() {
        ValidatorException ex = assertThrows(ValidatorException.class,
                () -> frm.validarNombre(facesContextMock, uiComponent, "A".repeat(156)));
        assertTrue(ex.getFacesMessage().getSummary().contains("155"));
    }

    @Test
    void validarNombre_Exactamente3_NoLanzaExcepcion() {
        assertDoesNotThrow(() -> frm.validarNombre(facesContextMock, uiComponent, "abc"));
    }

    @Test
    void validarNombre_Exactamente155_NoLanzaExcepcion() {
        assertDoesNotThrow(() -> frm.validarNombre(facesContextMock, uiComponent, "A".repeat(155)));
    }

    @Test
    void validarNombre_Valido_NoLanzaExcepcion() {
        assertDoesNotThrow(() -> frm.validarNombre(facesContextMock, uiComponent, "Etapa válida"));
    }

    @Test
    void validarNombre_EspaciosInsuficientes_LanzaExcepcion() {
        assertThrows(ValidatorException.class,
                () -> frm.validarNombre(facesContextMock, uiComponent, "  a  "));
    }

    // ==================== enviarMensaje ====================

    @Test
    void enviarMensajeExito_SummaryYSeveridadCorrectos() {
        frm.enviarMensajeExito("Todo bien");
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_INFO, msg.getSeverity());
        assertEquals("Éxito", msg.getSummary());
        assertEquals("Todo bien", msg.getDetail());
    }

    @Test
    void enviarMensajeError_SummaryYSeveridadCorrectos() {
        //  FIX 2: verifica que el método existe y es accesible
        frm.enviarMensajeError("Algo falló");
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
        assertEquals("Error", msg.getSummary());
        assertEquals("Algo falló", msg.getDetail());
    }

    @Test
    void enviarMensajeAdvertencia_SummaryYSeveridadCorrectos() {
        //  FIX 2: verifica que el método existe y es accesible
        frm.enviarMensajeAdvertencia("Cuidado");
        FacesMessage msg = capturarMensaje();
        assertEquals(FacesMessage.SEVERITY_WARN, msg.getSeverity());
        assertEquals("Advertencia", msg.getSummary());
        assertEquals("Cuidado", msg.getDetail());
    }

    // ==================== Paginación ====================

    @Test
    void paginacion_ValoresPorDefecto_SonCorrectos() {
        assertEquals(0, frm.getPaginaActual());
        assertEquals(5, frm.getPageSize());
    }

    @Test
    void paginacion_CambioValores_UsaOffsetCorrecto() {
        frm.setPaginaActualPublica(3);
        frm.setPageSizePublico(20);
        when(dao.findRange(60, 20)).thenReturn(List.of());
        frm.inicializarRegistros();
        verify(dao).findRange(60, 20);
    }
}
