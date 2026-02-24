package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD - Fase RED: estas pruebas definen el contrato de BancoPreguntaResource
 * antes de que exista la implementación.
 */
@ExtendWith(MockitoExtension.class)
class BancoPreguntaResourceTest {

    @Mock private BancoPreguntaDAO bancoPreguntaDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private BancoPreguntaResource resource;
    private BancoPregunta entidad;

    @BeforeEach
    void setUp() {
        resource = new BancoPreguntaResource();
        resource.bancoPreguntaDAO = bancoPreguntaDAO;

        entidad = new BancoPregunta();
        entidad.setId(2);
        entidad.setEnunciado("¿Cuánto es 2+2?");
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(bancoPreguntaDAO.count()).thenReturn(1);
        when(bancoPreguntaDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(bancoPreguntaDAO).count();
        verify(bancoPreguntaDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(bancoPreguntaDAO.count()).thenReturn(0);
        when(bancoPreguntaDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoPreguntaDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findRange(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(bancoPreguntaDAO.leer(2)).thenReturn(entidad);

        Response response = resource.findById(2);

        assertEquals(200, response.getStatus());
        BancoPregunta resultado = (BancoPregunta) response.getEntity();
        assertEquals(2, resultado.getId());
        verify(bancoPreguntaDAO).leer(2);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(bancoPreguntaDAO.leer(999)).thenReturn(null);

        Response response = resource.findById(999);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        Response response = resource.findById(null);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoPreguntaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findById(2);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        BancoPregunta nueva = new BancoPregunta();
        nueva.setEnunciado("¿Cuál es la capital de El Salvador?");
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/banco_preguntas/1"));

        Response response = resource.create(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(bancoPreguntaDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void create_ConEntidadConIdYaAsignado_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        BancoPregunta nueva = new BancoPregunta();
        nueva.setEnunciado("¿Cuál es la capital de El Salvador?");
        doThrow(new RuntimeException("Error de BD")).when(bancoPreguntaDAO).crear(any());

        Response response = resource.create(nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== update (PUT /{id}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(bancoPreguntaDAO.leer(2)).thenReturn(entidad);
        BancoPregunta actualizada = new BancoPregunta();
        actualizada.setEnunciado("¿Cuánto es 3+3?");

        Response response = resource.update(2, actualizada);

        assertEquals(200, response.getStatus());
        verify(bancoPreguntaDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdNulo_DebeRetornar422() {
        Response response = resource.update(null, entidad);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(2, null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(bancoPreguntaDAO.leer(999)).thenReturn(null);

        Response response = resource.update(999, entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoPreguntaDAO.leer(2)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.update(2, entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== delete (DELETE /{id}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(bancoPreguntaDAO.leer(2)).thenReturn(entidad);

        Response response = resource.delete(2);

        assertEquals(204, response.getStatus());
        verify(bancoPreguntaDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() {
        Response response = resource.delete(null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(bancoPreguntaDAO.leer(999)).thenReturn(null);

        Response response = resource.delete(999);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoPreguntaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.delete(2);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
