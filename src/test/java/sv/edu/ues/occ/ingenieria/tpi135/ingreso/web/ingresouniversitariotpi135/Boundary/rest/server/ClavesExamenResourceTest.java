package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClavesExamenResourceTest {

    @Mock private ClavesExamanDAO clavesExamanDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private ClavesExamanResource resource;
    private ClavesExamen entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        resource = new ClavesExamanResource();
        resource.clavesExamanDAO = clavesExamanDAO;

        PruebasAdmision prueba = new PruebasAdmision();

        entidad = new ClavesExamen();
        entidad.setIdClaveExaman(testId);
        entidad.setIdPrueba(prueba);
        entidad.setNombreClave("Clave A");
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200() {
        when(clavesExamanDAO.count()).thenReturn(1);
        when(clavesExamanDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200() {
        when(clavesExamanDAO.count()).thenReturn(0);
        when(clavesExamanDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);
        assertEquals(200, response.getStatus());
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(clavesExamanDAO.count()).thenThrow(new RuntimeException("BD error"));
        Response response = resource.findRange(0, 10);
        assertEquals(500, response.getStatus());
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        when(clavesExamanDAO.leer(testId)).thenReturn(entidad);
        Response response = resource.findById(testId);
        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(clavesExamanDAO.leer(testId)).thenReturn(null);
        Response response = resource.findById(testId);
        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        Response response = resource.findById(null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(clavesExamanDAO.leer(any())).thenThrow(new RuntimeException("BD error"));
        Response response = resource.findById(testId);
        assertEquals(500, response.getStatus());
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        ClavesExamen nueva = new ClavesExamen();
        nueva.setIdPrueba(new PruebasAdmision());
        nueva.setNombreClave("Clave B");
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/claves/1"));

        Response response = resource.create(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(clavesExamanDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void create_ConIdYaAsignado_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void create_SinPrueba_DebeRetornar422() {
        ClavesExamen nueva = new ClavesExamen();
        nueva.setNombreClave("Clave C");
        Response response = resource.create(nueva, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void create_SinNombreClave_DebeRetornar422() {
        ClavesExamen nueva = new ClavesExamen();
        nueva.setIdPrueba(new PruebasAdmision());
        Response response = resource.create(nueva, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void create_ConNombreClaveEnBlanco_DebeRetornar422() {
        ClavesExamen nueva = new ClavesExamen();
        nueva.setIdPrueba(new PruebasAdmision());
        nueva.setNombreClave("   ");
        Response response = resource.create(nueva, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        ClavesExamen nueva = new ClavesExamen();
        nueva.setIdPrueba(new PruebasAdmision());
        nueva.setNombreClave("Clave D");
        doThrow(new RuntimeException("BD error")).when(clavesExamanDAO).crear(any());
        Response response = resource.create(nueva, uriInfo);
        assertEquals(500, response.getStatus());
    }

    // ==================== update (PUT /{id}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(clavesExamanDAO.leer(testId)).thenReturn(entidad);
        ClavesExamen actualizada = new ClavesExamen();
        actualizada.setIdPrueba(new PruebasAdmision());
        actualizada.setNombreClave("Clave A modificada");

        Response response = resource.update(testId, actualizada);

        assertEquals(200, response.getStatus());
        verify(clavesExamanDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdNulo_DebeRetornar422() {
        Response response = resource.update(null, entidad);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(testId, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(clavesExamanDAO.leer(testId)).thenReturn(null);
        Response response = resource.update(testId, entidad);
        assertEquals(404, response.getStatus());
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(clavesExamanDAO.leer(testId)).thenThrow(new RuntimeException("BD error"));
        Response response = resource.update(testId, entidad);
        assertEquals(500, response.getStatus());
    }

    // ==================== delete (DELETE /{id}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(clavesExamanDAO.leer(testId)).thenReturn(entidad);
        Response response = resource.delete(testId);
        assertEquals(204, response.getStatus());
        verify(clavesExamanDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() {
        Response response = resource.delete(null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(clavesExamanDAO.leer(testId)).thenReturn(null);
        Response response = resource.delete(testId);
        assertEquals(404, response.getStatus());
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(clavesExamanDAO.leer(any())).thenThrow(new RuntimeException("BD error"));
        Response response = resource.delete(testId);
        assertEquals(500, response.getStatus());
    }
}
