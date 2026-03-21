package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntasPorClaveDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClaveId;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreguntasPorClaveResourceTest {

    @Mock private PreguntasPorClaveDAO preguntasPorClaveDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private PreguntasPorClaveResource resource;
    private PreguntasPorClave entidad;
    private ClavesExaman claveEntidad;
    private BancoPregunta preguntaEntidad;
    private UUID idClave;
    private UUID idPregunta;

    @BeforeEach
    void setUp() {
        idClave = UUID.randomUUID();
        idPregunta = UUID.randomUUID();

        resource = new PreguntasPorClaveResource();
        resource.preguntasPorClaveDAO = preguntasPorClaveDAO;

        PreguntasPorClaveId pk = new PreguntasPorClaveId();
        pk.setIdClave(idClave);
        pk.setIdPregunta(idPregunta);

        claveEntidad = new ClavesExaman();
        preguntaEntidad = new BancoPregunta();

        entidad = new PreguntasPorClave();
        entidad.setId(pk);
        entidad.setIdClave(claveEntidad);
        entidad.setIdPregunta(preguntaEntidad);
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200() {
        when(preguntasPorClaveDAO.count()).thenReturn(1);
        when(preguntasPorClaveDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200() {
        when(preguntasPorClaveDAO.count()).thenReturn(0);
        when(preguntasPorClaveDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(preguntasPorClaveDAO.count()).thenThrow(new RuntimeException("BD error"));
        Response response = resource.findRange(0, 10);
        assertEquals(500, response.getStatus());
    }

    // ==================== findById (GET /{idClave}/{idPregunta}) ====================

    @Test
    void findById_ConPKExistente_DebeRetornar200() {
        when(preguntasPorClaveDAO.leer(any(PreguntasPorClaveId.class))).thenReturn(entidad);

        Response response = resource.findById(idClave, idPregunta);

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
    }

    @Test
    void findById_ConPKInexistente_DebeRetornar404() {
        when(preguntasPorClaveDAO.leer(any(PreguntasPorClaveId.class))).thenReturn(null);

        Response response = resource.findById(idClave, idPregunta);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConParametroNulo_DebeRetornar422() {
        Response response = resource.findById(null, idPregunta);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void findById_ConPreguntaNula_DebeRetornar422() {
        Response response = resource.findById(idClave, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(preguntasPorClaveDAO.leer(any(PreguntasPorClaveId.class))).thenThrow(new RuntimeException("BD error"));

        Response response = resource.findById(idClave, idPregunta);

        assertEquals(500, response.getStatus());
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        PreguntasPorClave nueva = new PreguntasPorClave();
        nueva.setId(entidad.getId());
        nueva.setIdClave(claveEntidad);
        nueva.setIdPregunta(preguntaEntidad);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/preguntas_por_clave/" + idClave + "/" + idPregunta));

        Response response = resource.create(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(preguntasPorClaveDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void create_ConIdNulo_DebeRetornar422() {
        PreguntasPorClave nueva = new PreguntasPorClave();

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void create_SinIdClaveEnPk_DebeRetornar422() {
        PreguntasPorClaveId pkInvalido = new PreguntasPorClaveId();
        pkInvalido.setIdClave(null);
        pkInvalido.setIdPregunta(idPregunta);

        PreguntasPorClave nueva = new PreguntasPorClave();
        nueva.setId(pkInvalido);
        nueva.setIdClave(claveEntidad);
        nueva.setIdPregunta(preguntaEntidad);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void create_SinIdPreguntaEnPk_DebeRetornar422() {
        PreguntasPorClaveId pkInvalido = new PreguntasPorClaveId();
        pkInvalido.setIdClave(idClave);
        pkInvalido.setIdPregunta(null);

        PreguntasPorClave nueva = new PreguntasPorClave();
        nueva.setId(pkInvalido);
        nueva.setIdClave(claveEntidad);
        nueva.setIdPregunta(preguntaEntidad);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void create_SinEntidadClave_DebeRetornar422() {
        PreguntasPorClave nueva = new PreguntasPorClave();
        nueva.setId(entidad.getId());
        nueva.setIdClave(null);
        nueva.setIdPregunta(preguntaEntidad);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void create_SinEntidadPregunta_DebeRetornar422() {
        PreguntasPorClave nueva = new PreguntasPorClave();
        nueva.setId(entidad.getId());
        nueva.setIdClave(claveEntidad);
        nueva.setIdPregunta(null);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        PreguntasPorClave nueva = new PreguntasPorClave();
        nueva.setId(entidad.getId());
        nueva.setIdClave(claveEntidad);
        nueva.setIdPregunta(preguntaEntidad);

        doThrow(new RuntimeException("BD error")).when(preguntasPorClaveDAO).crear(any());

        Response response = resource.create(nueva, uriInfo);

        assertEquals(500, response.getStatus());
    }

    // ==================== update (PUT /{idClave}/{idPregunta}) ====================

    @Test
    void update_ConPKYEntidadValidos_DebeRetornar200() {
        when(preguntasPorClaveDAO.leer(any(PreguntasPorClaveId.class))).thenReturn(entidad);
        PreguntasPorClave actualizada = new PreguntasPorClave();

        Response response = resource.update(idClave, idPregunta, actualizada);

        assertEquals(200, response.getStatus());
        verify(preguntasPorClaveDAO).actualizar(actualizada);
    }

    @Test
    void update_ConParametroNulo_DebeRetornar422() {
        Response response = resource.update(null, idPregunta, entidad);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void update_ConPreguntaNula_DebeRetornar422() {
        Response response = resource.update(idClave, null, entidad);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(idClave, idPregunta, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void update_ConPKInexistente_DebeRetornar404() {
        when(preguntasPorClaveDAO.leer(any(PreguntasPorClaveId.class))).thenReturn(null);

        Response response = resource.update(idClave, idPregunta, entidad);

        assertEquals(404, response.getStatus());
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(preguntasPorClaveDAO.leer(any(PreguntasPorClaveId.class))).thenThrow(new RuntimeException("BD error"));

        Response response = resource.update(idClave, idPregunta, entidad);

        assertEquals(500, response.getStatus());
    }

    // ==================== delete (DELETE /{idClave}/{idPregunta}) ====================

    @Test
    void delete_ConPKExistente_DebeRetornar204() {
        when(preguntasPorClaveDAO.leer(any(PreguntasPorClaveId.class))).thenReturn(entidad);

        Response response = resource.delete(idClave, idPregunta);

        assertEquals(204, response.getStatus());
        verify(preguntasPorClaveDAO).eliminar(entidad);
    }

    @Test
    void delete_ConParametroNulo_DebeRetornar422() {
        Response response = resource.delete(null, idPregunta);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void delete_ConPreguntaNula_DebeRetornar422() {
        Response response = resource.delete(idClave, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void delete_ConPKInexistente_DebeRetornar404() {
        when(preguntasPorClaveDAO.leer(any(PreguntasPorClaveId.class))).thenReturn(null);

        Response response = resource.delete(idClave, idPregunta);

        assertEquals(404, response.getStatus());
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(preguntasPorClaveDAO.leer(any(PreguntasPorClaveId.class))).thenThrow(new RuntimeException("BD error"));

        Response response = resource.delete(idClave, idPregunta);

        assertEquals(500, response.getStatus());
    }
}
