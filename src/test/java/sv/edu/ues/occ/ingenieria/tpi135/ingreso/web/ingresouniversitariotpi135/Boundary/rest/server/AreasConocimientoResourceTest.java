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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AreasConocimientoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TemaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AreasConocimientoResourceTest {

    // Mockito inyecta por tipo: areasDAO ← AreasConocimientoDAO, temaDAO ← TemaDAO
    @Mock
    private AreasConocimientoDAO areasDAO;

    @Mock
    private TemaDAO temaDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private AreasConocimientoResource resource;

    private AreasConocimiento entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidad = new AreasConocimiento();
        entidad.setIdAreaConocimiento(testId);
        entidad.setNombreArea("Matemáticas");
    }

    // ==================== listAreas (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(areasDAO.count()).thenReturn(1);
        when(areasDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listAreas(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
        verify(areasDAO).count();
        verify(areasDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(areasDAO.count()).thenReturn(0);
        when(areasDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listAreas(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(areasDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listAreas(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getArea (GET /{idArea}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(areasDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getArea(testId.toString());

        assertEquals(200, response.getStatus());
        AreasConocimiento resultado = (AreasConocimiento) response.getEntity();
        assertEquals(testId, resultado.getIdAreaConocimiento());
        assertEquals("Matemáticas", resultado.getNombreArea());
        verify(areasDAO).leer(testId);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(areasDAO.leer(testId)).thenReturn(null);

        Response response = resource.getArea(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void findById_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.getArea("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(areasDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(areasDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getArea(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== createArea (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        AreasConocimiento nueva = new AreasConocimiento();
        nueva.setNombreArea("Ciencias");

        doAnswer(inv -> {
            AreasConocimiento a = inv.getArgument(0);
            a.setIdAreaConocimiento(UUID.randomUUID());
            return null;
        }).when(areasDAO).crear(any(AreasConocimiento.class));

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/areas/1"));

        Response response = resource.createArea(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(areasDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar400() {
        Response response = resource.createArea(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(areasDAO);
    }

    @Test
    void create_SinNombreArea_DebeRetornar400() {
        AreasConocimiento sinNombre = new AreasConocimiento();

        Response response = resource.createArea(sinNombre, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(areasDAO);
    }

    @Test
    void create_ConNombreAreaEnBlanco_DebeRetornar400() {
        AreasConocimiento conBlancos = new AreasConocimiento();
        conBlancos.setNombreArea("   ");

        Response response = resource.createArea(conBlancos, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(areasDAO);
    }

    @Test
    void create_ConNombreDuplicadoEnDAO_DebeRetornar409() {
        AreasConocimiento nueva = new AreasConocimiento();
        nueva.setNombreArea("Matemáticas");
        doThrow(new IllegalArgumentException("Ya existe un área con ese nombre"))
                .when(areasDAO).crear(any());

        Response response = resource.createArea(nueva, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        AreasConocimiento nueva = new AreasConocimiento();
        nueva.setNombreArea("Ciencias");
        doThrow(new RuntimeException("Error de BD")).when(areasDAO).crear(any());

        Response response = resource.createArea(nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== updateArea (PUT /{idArea}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(areasDAO.leer(testId)).thenReturn(entidad);
        AreasConocimiento actualizada = new AreasConocimiento();
        actualizada.setNombreArea("Matemáticas Avanzadas");

        Response response = resource.updateArea(testId.toString(), actualizada);

        assertEquals(200, response.getStatus());
        verify(areasDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdFormatoInvalido_DebeRetornar409() {
        // updateArea captura IllegalArgumentException (UUID inválido) → CONFLICT 409
        Response response = resource.updateArea("no-es-uuid", entidad);

        assertEquals(409, response.getStatus());
        verifyNoInteractions(areasDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(areasDAO.leer(testId)).thenReturn(null);

        Response response = resource.updateArea(testId.toString(), entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void update_ConIllegalArgumentEnActualizar_DebeRetornar409() {
        when(areasDAO.leer(testId)).thenReturn(entidad);
        AreasConocimiento actualizada = new AreasConocimiento();
        actualizada.setNombreArea("Nombre duplicado");
        when(areasDAO.actualizar(actualizada))
                .thenThrow(new IllegalArgumentException("Ya existe un área con ese nombre"));

        Response response = resource.updateArea(testId.toString(), actualizada);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void update_ConExcepcionEnLeer_DebeRetornar500() {
        when(areasDAO.leer(testId)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateArea(testId.toString(), entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void update_ConExcepcionEnActualizar_DebeRetornar500() {
        when(areasDAO.leer(testId)).thenReturn(entidad);
        AreasConocimiento actualizada = new AreasConocimiento();
        actualizada.setNombreArea("Ciencias");
        when(areasDAO.actualizar(actualizada))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateArea(testId.toString(), actualizada);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== deleteArea (DELETE /{idArea}) ====================

    @Test
    void delete_ConIdExistente_SinTemas_DebeRetornar204() {
        when(areasDAO.leer(testId)).thenReturn(entidad);
        when(temaDAO.findByArea(testId)).thenReturn(Collections.emptyList());

        Response response = resource.deleteArea(testId.toString());

        assertEquals(204, response.getStatus());
        verify(areasDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdExistente_ConTemas_DebeRetornar409() {
        when(areasDAO.leer(testId)).thenReturn(entidad);
        when(temaDAO.findByArea(testId)).thenReturn(List.of(
            new sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema()
        ));

        Response response = resource.deleteArea(testId.toString());

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verify(areasDAO, never()).eliminar(any());
    }

    @Test
    void delete_ConIdFormatoInvalido_DebeRetornar500() {
        // deleteArea no tiene catch(IAE), solo catch(Exception) → 500
        Response response = resource.deleteArea("no-es-uuid");

        assertEquals(500, response.getStatus());
        verifyNoInteractions(areasDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(areasDAO.leer(testId)).thenReturn(null);

        Response response = resource.deleteArea(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(areasDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteArea(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getTemasByArea (GET /{idArea}/temas) ====================

    @Test
    void getTemasByArea_ConAreaExistenteConTemas_DebeRetornar200ConLista() {
        when(areasDAO.leer(testId)).thenReturn(entidad);
        Tema tema1 = new Tema();
        tema1.setIdTema(UUID.randomUUID());
        tema1.setNombreTema("Álgebra");
        Tema tema2 = new Tema();
        tema2.setIdTema(UUID.randomUUID());
        tema2.setNombreTema("Geometría");
        when(temaDAO.findByArea(testId)).thenReturn(List.of(tema1, tema2));

        Response response = resource.getTemasByArea(testId.toString(), 0, 50);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("2", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
        verify(temaDAO).findByArea(testId);
    }

    @Test
    void getTemasByArea_ConAreaExistenteSinTemas_DebeRetornar200ConListaVacia() {
        when(areasDAO.leer(testId)).thenReturn(entidad);
        when(temaDAO.findByArea(testId)).thenReturn(Collections.emptyList());

        Response response = resource.getTemasByArea(testId.toString(), 0, 50);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getTemasByArea_ConPaginacion_DebeRetornarSubLista() {
        when(areasDAO.leer(testId)).thenReturn(entidad);
        Tema tema1 = new Tema();
        tema1.setNombreTema("Álgebra");
        Tema tema2 = new Tema();
        tema2.setNombreTema("Geometría");
        Tema tema3 = new Tema();
        tema3.setNombreTema("Cálculo");
        when(temaDAO.findByArea(testId)).thenReturn(List.of(tema1, tema2, tema3));

        Response response = resource.getTemasByArea(testId.toString(), 1, 1);

        assertEquals(200, response.getStatus());
        List<?> resultado = (List<?>) response.getEntity();
        assertEquals(1, resultado.size());
        assertEquals("3", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getTemasByArea_ConAreaInexistente_DebeRetornar404() {
        when(areasDAO.leer(testId)).thenReturn(null);

        Response response = resource.getTemasByArea(testId.toString(), 0, 50);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verifyNoInteractions(temaDAO);
    }

    @Test
    void getTemasByArea_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getTemasByArea("no-es-uuid", 0, 50);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(areasDAO, temaDAO);
    }

    @Test
    void getTemasByArea_ConExcepcionEnDAO_DebeRetornar500() {
        when(areasDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getTemasByArea(testId.toString(), 0, 50);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== createTemaInArea (POST /{idArea}/temas) ====================

    @Test
    void createTemaInArea_ConDatosValidos_DebeRetornar201() {
        Tema tema = new Tema();
        tema.setNombreTema("Álgebra Lineal");

        when(areasDAO.leer(testId)).thenReturn(entidad);
        doAnswer(inv -> {
            Tema t = inv.getArgument(0);
            t.setIdTema(UUID.randomUUID());
            return null;
        }).when(temaDAO).crear(any(Tema.class));

        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build(any())).thenReturn(URI.create("http://localhost/temas/" + UUID.randomUUID()));

        Response response = resource.createTemaInArea(testId.toString(), tema, uriInfo);

        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());
        verify(temaDAO).crear(tema);
        assertEquals(entidad, tema.getAreaConocimiento());
    }

    @Test
    void createTemaInArea_ConTemaNulo_DebeRetornar400() {
        Response response = resource.createTemaInArea(testId.toString(), null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(areasDAO, temaDAO);
    }

    @Test
    void createTemaInArea_SinNombreTema_DebeRetornar400() {
        Tema sinNombre = new Tema();

        Response response = resource.createTemaInArea(testId.toString(), sinNombre, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(areasDAO, temaDAO);
    }

    @Test
    void createTemaInArea_ConNombreTemaEnBlanco_DebeRetornar400() {
        Tema conBlancos = new Tema();
        conBlancos.setNombreTema("   ");

        Response response = resource.createTemaInArea(testId.toString(), conBlancos, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(areasDAO, temaDAO);
    }

    @Test
    void createTemaInArea_ConAreaInexistente_DebeRetornar404() {
        Tema tema = new Tema();
        tema.setNombreTema("Álgebra");
        when(areasDAO.leer(testId)).thenReturn(null);

        Response response = resource.createTemaInArea(testId.toString(), tema, uriInfo);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(temaDAO, never()).crear(any());
    }

    @Test
    void createTemaInArea_ConUuidAreaInvalido_DebeRetornar409() {
        Tema tema = new Tema();
        tema.setNombreTema("Álgebra");

        Response response = resource.createTemaInArea("no-es-uuid", tema, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verifyNoInteractions(areasDAO, temaDAO);
    }

    @Test
    void createTemaInArea_ConIllegalArgumentEnDAO_DebeRetornar409() {
        Tema tema = new Tema();
        tema.setNombreTema("Álgebra");
        when(areasDAO.leer(testId)).thenReturn(entidad);
        doThrow(new IllegalArgumentException("Ciclo detectado en jerarquía"))
                .when(temaDAO).crear(any());

        Response response = resource.createTemaInArea(testId.toString(), tema, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void createTemaInArea_ConExcepcionEnDAO_DebeRetornar500() {
        Tema tema = new Tema();
        tema.setNombreTema("Álgebra");
        when(areasDAO.leer(testId)).thenReturn(entidad);
        doThrow(new RuntimeException("Error de BD"))
                .when(temaDAO).crear(any());

        Response response = resource.createTemaInArea(testId.toString(), tema, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
