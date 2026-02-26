package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD - Fase RED: estas pruebas definen el contrato de PruebasAdmisionResource
 * antes de que exista la implementación.
 */
@ExtendWith(MockitoExtension.class)
class PruebasAdmisionResourceTest {

    @Mock private PruebasAdmisionDAO pruebasAdmisionDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private PruebasAdmisionResource resource;
    private PruebasAdmision entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        resource = new PruebasAdmisionResource();
        resource.pruebasAdmisionDAO = pruebasAdmisionDAO;

        entidad = new PruebasAdmision();
        entidad.setId(testId);
        entidad.setNombrePrueba("Prueba 2026");
        entidad.setAnio(2026);
        entidad.setActiva(true);
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(pruebasAdmisionDAO.count()).thenReturn(1);
        when(pruebasAdmisionDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(pruebasAdmisionDAO).count();
        verify(pruebasAdmisionDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(pruebasAdmisionDAO.count()).thenReturn(0);
        when(pruebasAdmisionDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(pruebasAdmisionDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findRange(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.findById(testId);

        assertEquals(200, response.getStatus());
        PruebasAdmision resultado = (PruebasAdmision) response.getEntity();
        assertEquals(testId, resultado.getId());
        assertEquals("Prueba 2026", resultado.getNombrePrueba());
        verify(pruebasAdmisionDAO).leer(testId);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(null);

        Response response = resource.findById(testId);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        Response response = resource.findById(null);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(pruebasAdmisionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findById(testId);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        PruebasAdmision nueva = new PruebasAdmision();
        nueva.setNombrePrueba("Prueba 2027");
        nueva.setAnio(2027);
        nueva.setActiva(false);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/pruebas_admision/2"));

        Response response = resource.create(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(pruebasAdmisionDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void create_ConEntidadConIdYaAsignado_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        PruebasAdmision nueva = new PruebasAdmision();
        nueva.setNombrePrueba("Prueba 2027");
        nueva.setAnio(2027);
        nueva.setActiva(false);
        doThrow(new RuntimeException("Error de BD")).when(pruebasAdmisionDAO).crear(any());

        Response response = resource.create(nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== update (PUT /{id}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);
        PruebasAdmision actualizada = new PruebasAdmision();
        actualizada.setNombrePrueba("Prueba 2026 Actualizada");
        actualizada.setAnio(2026);
        actualizada.setActiva(false);

        Response response = resource.update(testId, actualizada);

        assertEquals(200, response.getStatus());
        verify(pruebasAdmisionDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdNulo_DebeRetornar422() {
        Response response = resource.update(null, entidad);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(testId, null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(null);

        Response response = resource.update(testId, entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(pruebasAdmisionDAO.leer(testId)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.update(testId, entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== delete (DELETE /{id}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.delete(testId);

        assertEquals(204, response.getStatus());
        verify(pruebasAdmisionDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() {
        Response response = resource.delete(null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(null);

        Response response = resource.delete(testId);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(pruebasAdmisionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.delete(testId);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
