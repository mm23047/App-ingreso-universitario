package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TemaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PruebasAdmisionResourceTest {

    @Mock
    private PruebasAdmisionDAO pruebasAdmisionDAO;

    @Mock
    private TemaDAO temaDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private PruebasAdmisionResource resource;

    private PruebasAdmision entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidad = new PruebasAdmision();
        entidad.setIdPruebaAdmision(testId);
        entidad.setNombrePrueba("Prueba 2026");
        entidad.setAnio(2026);
        entidad.setActiva(true);
    }

    // ==================== listPruebas (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(pruebasAdmisionDAO.count()).thenReturn(1);
        when(pruebasAdmisionDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        // CORREGIDO: Se añade null como tercer parámetro de filtro
        Response response = resource.listPruebas(0, 10, null);

        assertEquals(200, response.getStatus());

        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
        verify(pruebasAdmisionDAO).count();
        verify(pruebasAdmisionDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(pruebasAdmisionDAO.count()).thenReturn(0);
        when(pruebasAdmisionDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        // CORREGIDO: Se añade null como tercer parámetro de filtro
        Response response = resource.listPruebas(0, 10, null);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(pruebasAdmisionDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        // CORREGIDO: Se añade null como tercer parámetro de filtro
        Response response = resource.listPruebas(0, 10, null);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void listPruebas_ConBusquedaPorTermino_DebeRetornarResultadosFiltrados() {
        when(pruebasAdmisionDAO.count()).thenReturn(5);
        when(pruebasAdmisionDAO.buscarPorTermino("2026", 0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listPruebas(0, 10, "2026");

        assertEquals(200, response.getStatus());
        verify(pruebasAdmisionDAO).buscarPorTermino("2026", 0, 10);
        verify(pruebasAdmisionDAO, never()).findRange(anyInt(), anyInt());
    }

    @Test
    void listPruebas_ConBusquedaEnBlanco_DebeUsarFindRange() {
        when(pruebasAdmisionDAO.count()).thenReturn(1);
        when(pruebasAdmisionDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listPruebas(0, 10, "   ");

        assertEquals(200, response.getStatus());
        verify(pruebasAdmisionDAO).findRange(0, 10);
        verify(pruebasAdmisionDAO, never()).buscarPorTermino(anyString(), anyInt(), anyInt());
    }

    @Test
    void listPruebas_ConExcepcionEnBusqueda_DebeRetornar500() {
        when(pruebasAdmisionDAO.count()).thenReturn(5);
        when(pruebasAdmisionDAO.buscarPorTermino(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Error en búsqueda"));

        Response response = resource.listPruebas(0, 10, "test");

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getPrueba (GET /{idPrueba}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getPrueba(testId.toString());

        assertEquals(200, response.getStatus());
        PruebasAdmision resultado = (PruebasAdmision) response.getEntity();
        assertEquals(testId, resultado.getIdPruebaAdmision());
        assertEquals("Prueba 2026", resultado.getNombrePrueba());
        verify(pruebasAdmisionDAO).leer(testId);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(null);

        Response response = resource.getPrueba(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void findById_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.getPrueba("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(pruebasAdmisionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getPrueba(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== createPrueba (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        PruebasAdmision nueva = new PruebasAdmision();
        nueva.setNombrePrueba("Prueba 2027");
        nueva.setAnio(2027);
        nueva.setActiva(false);

        doAnswer(inv -> {
            PruebasAdmision p = inv.getArgument(0);
            p.setIdPruebaAdmision(UUID.randomUUID());
            return null;
        }).when(pruebasAdmisionDAO).crear(any(PruebasAdmision.class));

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/pruebas_admision/1"));

        Response response = resource.createPrueba(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(pruebasAdmisionDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar400() {
        Response response = resource.createPrueba(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void create_SinNombrePrueba_DebeRetornar400() {
        PruebasAdmision sinNombre = new PruebasAdmision();
        sinNombre.setAnio(2027);

        Response response = resource.createPrueba(sinNombre, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void create_ConNombreEnBlanco_DebeRetornar400() {
        PruebasAdmision conBlanco = new PruebasAdmision();
        conBlanco.setNombrePrueba("   ");

        Response response = resource.createPrueba(conBlanco, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void create_ConNombreDuplicado_DebeRetornar409() {
        PruebasAdmision nueva = new PruebasAdmision();
        nueva.setNombrePrueba("Prueba 2026");
        doThrow(new IllegalArgumentException("Ya existe una prueba con ese nombre"))
                .when(pruebasAdmisionDAO).crear(any());

        Response response = resource.createPrueba(nueva, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        PruebasAdmision nueva = new PruebasAdmision();
        nueva.setNombrePrueba("Prueba 2027");
        nueva.setAnio(2027);
        doThrow(new RuntimeException("Error de BD")).when(pruebasAdmisionDAO).crear(any());

        Response response = resource.createPrueba(nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== updatePrueba (PUT /{idPrueba}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);
        PruebasAdmision actualizada = new PruebasAdmision();
        actualizada.setNombrePrueba("Prueba 2026 Actualizada");

        Response response = resource.updatePrueba(testId.toString(), actualizada);

        assertEquals(200, response.getStatus());
        verify(pruebasAdmisionDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdFormatoInvalido_DebeRetornar409() {
        // updatePrueba captura IAE (UUID inválido) → CONFLICT 409
        Response response = resource.updatePrueba("no-es-uuid", entidad);

        assertEquals(409, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(null);

        Response response = resource.updatePrueba(testId.toString(), entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void update_ConIllegalArgumentEnActualizar_DebeRetornar409() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);
        PruebasAdmision datos = new PruebasAdmision();
        datos.setNombrePrueba("Duplicada");
        when(pruebasAdmisionDAO.actualizar(datos))
                .thenThrow(new IllegalArgumentException("Nombre duplicado"));

        Response response = resource.updatePrueba(testId.toString(), datos);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void update_ConExcepcionEnLeer_DebeRetornar500() {
        when(pruebasAdmisionDAO.leer(testId)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updatePrueba(testId.toString(), entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void update_ConExcepcionEnActualizar_DebeRetornar500() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);
        PruebasAdmision datos = new PruebasAdmision();
        datos.setNombrePrueba("Nuevo nombre");
        when(pruebasAdmisionDAO.actualizar(datos)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updatePrueba(testId.toString(), datos);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== deletePrueba (DELETE /{idPrueba}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.deletePrueba(testId.toString());

        assertEquals(204, response.getStatus());
        verify(pruebasAdmisionDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.deletePrueba("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(null);

        Response response = resource.deletePrueba(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(pruebasAdmisionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deletePrueba(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getPruebasActivas (GET /activas) ====================

    @Test
    void getPruebasActivas_ConResultados_DebeRetornar200() {
        when(pruebasAdmisionDAO.findActivas()).thenReturn(List.of(entidad));

        Response response = resource.getPruebasActivas();

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        verify(pruebasAdmisionDAO).findActivas();
    }

    @Test
    void getPruebasActivas_SinResultados_DebeRetornar200ConListaVacia() {
        when(pruebasAdmisionDAO.findActivas()).thenReturn(Collections.emptyList());

        Response response = resource.getPruebasActivas();

        assertEquals(200, response.getStatus());
    }

    @Test
    void getPruebasActivas_ConExcepcionEnDAO_DebeRetornar500() {
        when(pruebasAdmisionDAO.findActivas()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getPruebasActivas();

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getAreasByPrueba (GET /{idPrueba}/areas) ====================

    @Test
    void getAreasByPrueba_ConTemasExistentes_DebeRetornar200() {
        AreasConocimiento area = new AreasConocimiento();
        area.setIdAreaConocimiento(UUID.randomUUID());
        area.setNombreArea("Matemáticas");

        Tema tema = new Tema();
        tema.setIdTema(UUID.randomUUID());
        tema.setNombreTema("Álgebra");
        tema.setAreaConocimiento(area);

        when(temaDAO.findByPrueba(testId)).thenReturn(List.of(tema));

        Response response = resource.getAreasByPrueba(testId.toString());

        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getAreasByPrueba_SinTemas_DebeRetornar200ConListaVacia() {
        when(temaDAO.findByPrueba(testId)).thenReturn(Collections.emptyList());

        Response response = resource.getAreasByPrueba(testId.toString());

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getAreasByPrueba_ConJerarquiaPadreHijo_DebeCargarAncestrosYRetornar200() {
        AreasConocimiento area = new AreasConocimiento();
        area.setIdAreaConocimiento(UUID.randomUUID());
        area.setNombreArea("Matemáticas");

        Tema padre = new Tema();
        padre.setIdTema(UUID.randomUUID());
        padre.setNombreTema("Álgebra");
        padre.setAreaConocimiento(area);

        Tema hijo = new Tema();
        hijo.setIdTema(UUID.randomUUID());
        hijo.setNombreTema("Álgebra Lineal");
        hijo.setAreaConocimiento(area);
        hijo.setIdTemaPadre(padre);

        when(temaDAO.findByPrueba(testId)).thenReturn(List.of(hijo));
        when(temaDAO.leer(padre.getIdTema())).thenReturn(padre);

        Response response = resource.getAreasByPrueba(testId.toString());

        assertEquals(200, response.getStatus());
        verify(temaDAO).leer(padre.getIdTema());
    }

    @Test
    void getAreasByPrueba_ConMultiplesAreas_DebeAgruparCorrectamente() {
        AreasConocimiento areaMat = new AreasConocimiento();
        areaMat.setIdAreaConocimiento(UUID.randomUUID());
        areaMat.setNombreArea("Matemáticas");

        AreasConocimiento areaCiencias = new AreasConocimiento();
        areaCiencias.setIdAreaConocimiento(UUID.randomUUID());
        areaCiencias.setNombreArea("Ciencias");

        Tema temaMat = new Tema();
        temaMat.setIdTema(UUID.randomUUID());
        temaMat.setNombreTema("Álgebra");
        temaMat.setAreaConocimiento(areaMat);

        Tema temaCiencias = new Tema();
        temaCiencias.setIdTema(UUID.randomUUID());
        temaCiencias.setNombreTema("Física");
        temaCiencias.setAreaConocimiento(areaCiencias);

        when(temaDAO.findByPrueba(testId)).thenReturn(List.of(temaMat, temaCiencias));

        Response response = resource.getAreasByPrueba(testId.toString());

        assertEquals(200, response.getStatus());
        assertEquals("2", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getAreasByPrueba_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getAreasByPrueba("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(temaDAO);
    }

    @Test
    void getAreasByPrueba_ConExcepcionEnDAO_DebeRetornar500() {
        when(temaDAO.findByPrueba(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getAreasByPrueba(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== activarPruebaExclusiva (PUT /{idPrueba}/activar) ====================

    @Test
    void activarPruebaExclusiva_ConIdValido_DebeRetornar200() {
        Response response = resource.activarPruebaExclusiva(testId.toString());

        assertEquals(200, response.getStatus());
        verify(pruebasAdmisionDAO).setPruebaActivaExclusiva(testId);
    }

    @Test
    void activarPruebaExclusiva_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.activarPruebaExclusiva("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void activarPruebaExclusiva_ConIllegalArgumentEnDAO_DebeRetornar400() {
        doThrow(new IllegalArgumentException("La prueba no existe"))
                .when(pruebasAdmisionDAO).setPruebaActivaExclusiva(testId);

        Response response = resource.activarPruebaExclusiva(testId.toString());

        assertEquals(400, response.getStatus());
    }

    @Test
    void activarPruebaExclusiva_ConExcepcionEnDAO_DebeRetornar500() {
        doThrow(new RuntimeException("Error de BD"))
                .when(pruebasAdmisionDAO).setPruebaActivaExclusiva(any());

        Response response = resource.activarPruebaExclusiva(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
