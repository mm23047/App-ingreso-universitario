package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.RespuestasExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestasExaman;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD - Fase RED: estas pruebas definen el contrato de RespuestasExamanResource
 * antes de que exista la implementación.
 */
@ExtendWith(MockitoExtension.class)
class RespuestasExamanResourceTest {

    @Mock private RespuestasExamanDAO respuestasExamanDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private RespuestasExamanResource resource;
    private RespuestasExaman entidad;

    @BeforeEach
    void setUp() {
        resource = new RespuestasExamanResource();
        resource.respuestasExamanDAO = respuestasExamanDAO;

        entidad = new RespuestasExaman();
        entidad.setId(5);
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(respuestasExamanDAO.count()).thenReturn(1);
        when(respuestasExamanDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(respuestasExamanDAO).count();
        verify(respuestasExamanDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(respuestasExamanDAO.count()).thenReturn(0);
        when(respuestasExamanDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(respuestasExamanDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findRange(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(respuestasExamanDAO.leer(5)).thenReturn(entidad);

        Response response = resource.findById(5);

        assertEquals(200, response.getStatus());
        RespuestasExaman resultado = (RespuestasExaman) response.getEntity();
        assertEquals(5, resultado.getId());
        verify(respuestasExamanDAO).leer(5);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(respuestasExamanDAO.leer(999)).thenReturn(null);

        Response response = resource.findById(999);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        Response response = resource.findById(null);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(respuestasExamanDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findById(5);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        RespuestasExaman nueva = new RespuestasExaman();
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/respuestas_examen/6"));

        Response response = resource.create(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(respuestasExamanDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void create_ConEntidadConIdYaAsignado_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        RespuestasExaman nueva = new RespuestasExaman();
        doThrow(new RuntimeException("Error de BD")).when(respuestasExamanDAO).crear(any());

        Response response = resource.create(nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== update (PUT /{id}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(respuestasExamanDAO.leer(5)).thenReturn(entidad);
        RespuestasExaman actualizada = new RespuestasExaman();

        Response response = resource.update(5, actualizada);

        assertEquals(200, response.getStatus());
        verify(respuestasExamanDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdNulo_DebeRetornar422() {
        Response response = resource.update(null, entidad);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(5, null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(respuestasExamanDAO.leer(999)).thenReturn(null);

        Response response = resource.update(999, entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(respuestasExamanDAO.leer(5)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.update(5, entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== delete (DELETE /{id}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(respuestasExamanDAO.leer(5)).thenReturn(entidad);

        Response response = resource.delete(5);

        assertEquals(204, response.getStatus());
        verify(respuestasExamanDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() {
        Response response = resource.delete(null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(respuestasExamanDAO.leer(999)).thenReturn(null);

        Response response = resource.delete(999);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(respuestasExamanDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.delete(5);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
