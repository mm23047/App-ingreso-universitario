package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ProcesoAdmisionAspiranteDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ProcesoAdmisionAspirante;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD - Fase RED: estas pruebas definen el contrato de ProcesoAdmisionAspiranteResource
 * antes de que exista la implementación.
 */
@ExtendWith(MockitoExtension.class)
class ProcesoAdmisionAspiranteResourceTest {

    @Mock private ProcesoAdmisionAspiranteDAO procesoAdmisionAspiranteDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private ProcesoAdmisionAspiranteResource resource;
    private ProcesoAdmisionAspirante entidad;

    @BeforeEach
    void setUp() {
        resource = new ProcesoAdmisionAspiranteResource();
        resource.procesoAdmisionAspiranteDAO = procesoAdmisionAspiranteDAO;

        entidad = new ProcesoAdmisionAspirante();
        entidad.setId(10);
        entidad.setEstado("ACTIVO");
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(procesoAdmisionAspiranteDAO.count()).thenReturn(1);
        when(procesoAdmisionAspiranteDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(procesoAdmisionAspiranteDAO).count();
        verify(procesoAdmisionAspiranteDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(procesoAdmisionAspiranteDAO.count()).thenReturn(0);
        when(procesoAdmisionAspiranteDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(procesoAdmisionAspiranteDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(procesoAdmisionAspiranteDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(procesoAdmisionAspiranteDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(procesoAdmisionAspiranteDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findRange(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(procesoAdmisionAspiranteDAO.leer(10)).thenReturn(entidad);

        Response response = resource.findById(10);

        assertEquals(200, response.getStatus());
        ProcesoAdmisionAspirante resultado = (ProcesoAdmisionAspirante) response.getEntity();
        assertEquals(10, resultado.getId());
        verify(procesoAdmisionAspiranteDAO).leer(10);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(procesoAdmisionAspiranteDAO.leer(999)).thenReturn(null);

        Response response = resource.findById(999);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        Response response = resource.findById(null);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(procesoAdmisionAspiranteDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(procesoAdmisionAspiranteDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findById(10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        ProcesoAdmisionAspirante nuevo = new ProcesoAdmisionAspirante();
        nuevo.setEstado("PENDIENTE");
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/proceso_admision_aspirante/11"));

        Response response = resource.create(nuevo, uriInfo);

        assertEquals(201, response.getStatus());
        verify(procesoAdmisionAspiranteDAO).crear(nuevo);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(procesoAdmisionAspiranteDAO);
    }

    @Test
    void create_ConEntidadConIdYaAsignado_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(procesoAdmisionAspiranteDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        ProcesoAdmisionAspirante nuevo = new ProcesoAdmisionAspirante();
        nuevo.setEstado("PENDIENTE");
        doThrow(new RuntimeException("Error de BD")).when(procesoAdmisionAspiranteDAO).crear(any());

        Response response = resource.create(nuevo, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== update (PUT /{id}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(procesoAdmisionAspiranteDAO.leer(10)).thenReturn(entidad);
        ProcesoAdmisionAspirante actualizado = new ProcesoAdmisionAspirante();
        actualizado.setEstado("COMPLETADO");

        Response response = resource.update(10, actualizado);

        assertEquals(200, response.getStatus());
        verify(procesoAdmisionAspiranteDAO).actualizar(actualizado);
    }

    @Test
    void update_ConIdNulo_DebeRetornar422() {
        Response response = resource.update(null, entidad);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(procesoAdmisionAspiranteDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(10, null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(procesoAdmisionAspiranteDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(procesoAdmisionAspiranteDAO.leer(999)).thenReturn(null);

        Response response = resource.update(999, entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(procesoAdmisionAspiranteDAO.leer(10)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.update(10, entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== delete (DELETE /{id}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(procesoAdmisionAspiranteDAO.leer(10)).thenReturn(entidad);

        Response response = resource.delete(10);

        assertEquals(204, response.getStatus());
        verify(procesoAdmisionAspiranteDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() {
        Response response = resource.delete(null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(procesoAdmisionAspiranteDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(procesoAdmisionAspiranteDAO.leer(999)).thenReturn(null);

        Response response = resource.delete(999);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(procesoAdmisionAspiranteDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.delete(10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
