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
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(areasDAO).count();
        verify(areasDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(areasDAO.count()).thenReturn(0);
        when(areasDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listAreas(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(areasDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listAreas(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
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
        assertNotNull(response.getHeaderString("Not-found-id"));
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
        assertNotNull(response.getHeaderString("Server-exception"));
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
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(areasDAO);
    }

    @Test
    void create_SinNombreArea_DebeRetornar400() {
        AreasConocimiento sinNombre = new AreasConocimiento();

        Response response = resource.createArea(sinNombre, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(areasDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        AreasConocimiento nueva = new AreasConocimiento();
        nueva.setNombreArea("Ciencias");
        doThrow(new RuntimeException("Error de BD")).when(areasDAO).crear(any());

        Response response = resource.createArea(nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
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
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(areasDAO.leer(testId)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateArea(testId.toString(), entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
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
        assertNotNull(response.getHeaderString("Conflict-reason"));
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
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(areasDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteArea(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
