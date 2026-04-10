package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscripcionesPruebaResourceTest {

    @Mock private InscripcionesPruebaDAO inscripcionesPruebaDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private InscripcionesPruebaResource resource;
    private InscripcionesPrueba entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        resource = new InscripcionesPruebaResource();
        resource.inscripcionesPruebaDAO = inscripcionesPruebaDAO;

        entidad = new InscripcionesPrueba();
        entidad.setId(testId);
        entidad.setIdAspirante(new AspirantesDato());
        entidad.setIdPrueba(new PruebasAdmision());
        entidad.setEstado("ACTIVO");
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200() {
        when(inscripcionesPruebaDAO.count()).thenReturn(1);
        when(inscripcionesPruebaDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200() {
        when(inscripcionesPruebaDAO.count()).thenReturn(0);
        when(inscripcionesPruebaDAO.findRange(0, 10)).thenReturn(Collections.emptyList());
        Response response = resource.findRange(0, 10);
        assertEquals(200, response.getStatus());
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(inscripcionesPruebaDAO.count()).thenThrow(new RuntimeException("BD error"));
        Response response = resource.findRange(0, 10);
        assertEquals(500, response.getStatus());
    }
    // ==================== Tests para nuevos filtros (AspiranteId y PruebaId) ====================

    @Test
    void findRange_ConAspiranteIdValido_DebeRetornar200() {
        resource.aspiranteIdParam = testId.toString();
        when(inscripcionesPruebaDAO.findByAspiranteId(testId)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        verify(inscripcionesPruebaDAO).findByAspiranteId(testId);
        // Aseguramos que no llame a los métodos de paginación normal
        verify(inscripcionesPruebaDAO, never()).count();
        verify(inscripcionesPruebaDAO, never()).findRange(anyInt(), anyInt());
    }

    @Test
    void findRange_ConAspiranteIdInvalido_DebeRetornar422() {
        resource.aspiranteIdParam = "texto-no-es-uuid";

        Response response = resource.findRange(0, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void findRange_ConAspiranteIdValido_YExcepcionEnDAO_DebeRetornar500() {
        resource.aspiranteIdParam = testId.toString();
        when(inscripcionesPruebaDAO.findByAspiranteId(any())).thenThrow(new RuntimeException("BD error"));

        Response response = resource.findRange(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    @Test
    void findRange_ConPruebaIdValido_DebeRetornar200() {
        resource.pruebaIdParam = testId.toString();
        when(inscripcionesPruebaDAO.findByPruebaId(testId)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        verify(inscripcionesPruebaDAO).findByPruebaId(testId);
        verify(inscripcionesPruebaDAO, never()).count();
        verify(inscripcionesPruebaDAO, never()).findRange(anyInt(), anyInt());
    }

    @Test
    void findRange_ConPruebaIdInvalido_DebeRetornar422() {
        resource.pruebaIdParam = "texto-no-es-uuid";

        Response response = resource.findRange(0, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void findRange_ConPruebaIdValido_YExcepcionEnDAO_DebeRetornar500() {
        resource.pruebaIdParam = testId.toString();
        when(inscripcionesPruebaDAO.findByPruebaId(any())).thenThrow(new RuntimeException("BD error"));

        Response response = resource.findRange(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
    @Test
    void findRange_ConAspiranteIdEnBlanco_DebeIgnorarFiltroYPagsinarNormal() {
        // Asignamos una cadena con espacios (no es nulo, pero isBlank() será true)
        resource.aspiranteIdParam = "   ";

        // Mockeamos el comportamiento de paginación normal porque se saltará el 'if'
        when(inscripcionesPruebaDAO.count()).thenReturn(1);
        when(inscripcionesPruebaDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        // Validamos que respondió bien y que usó la paginación normal, NO el filtro
        assertEquals(200, response.getStatus());
        verify(inscripcionesPruebaDAO, never()).findByAspiranteId(any());
        verify(inscripcionesPruebaDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConPruebaIdEnBlanco_DebeIgnorarFiltroYPagsinarNormal() {
        // Asignamos una cadena vacía (no es nulo, pero isBlank() será true)
        resource.pruebaIdParam = "";

        // Mockeamos el comportamiento de paginación normal
        when(inscripcionesPruebaDAO.count()).thenReturn(1);
        when(inscripcionesPruebaDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        // Validamos que respondió bien y que usó la paginación normal, NO el filtro
        assertEquals(200, response.getStatus());
        verify(inscripcionesPruebaDAO, never()).findByPruebaId(any());
        verify(inscripcionesPruebaDAO).findRange(0, 10);
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(entidad);
        Response response = resource.findById(testId);
        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(null);
        Response response = resource.findById(testId);
        assertEquals(404, response.getStatus());
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        Response response = resource.findById(null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(inscripcionesPruebaDAO.leer(any())).thenThrow(new RuntimeException("BD error"));
        Response response = resource.findById(testId);
        assertEquals(500, response.getStatus());
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        InscripcionesPrueba nueva = new InscripcionesPrueba();
        nueva.setIdAspirante(new AspirantesDato());
        nueva.setIdPrueba(new PruebasAdmision());
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/inscripciones/1"));

        Response response = resource.create(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(inscripcionesPruebaDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void create_ConIdYaAsignado_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void create_SinAspirante_DebeRetornar422() {
        InscripcionesPrueba nueva = new InscripcionesPrueba();
        nueva.setIdPrueba(new PruebasAdmision());
        Response response = resource.create(nueva, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void create_SinPrueba_DebeRetornar422() {
        InscripcionesPrueba nueva = new InscripcionesPrueba();
        nueva.setIdAspirante(new AspirantesDato());
        Response response = resource.create(nueva, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        InscripcionesPrueba nueva = new InscripcionesPrueba();
        nueva.setIdAspirante(new AspirantesDato());
        nueva.setIdPrueba(new PruebasAdmision());
        doThrow(new RuntimeException("BD error")).when(inscripcionesPruebaDAO).crear(any());
        Response response = resource.create(nueva, uriInfo);
        assertEquals(500, response.getStatus());
    }

    // ==================== update (PUT /{id}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(entidad);
        InscripcionesPrueba actualizada = new InscripcionesPrueba();
        actualizada.setEstado("FINALIZADO");

        Response response = resource.update(testId, actualizada);

        assertEquals(200, response.getStatus());
        verify(inscripcionesPruebaDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdNulo_DebeRetornar422() {
        Response response = resource.update(null, entidad);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(testId, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(null);
        Response response = resource.update(testId, entidad);
        assertEquals(404, response.getStatus());
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(inscripcionesPruebaDAO.leer(testId)).thenThrow(new RuntimeException("BD error"));
        Response response = resource.update(testId, entidad);
        assertEquals(500, response.getStatus());
    }

    // ==================== delete (DELETE /{id}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(entidad);
        Response response = resource.delete(testId);
        assertEquals(204, response.getStatus());
        verify(inscripcionesPruebaDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() {
        Response response = resource.delete(null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(null);
        Response response = resource.delete(testId);
        assertEquals(404, response.getStatus());
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(inscripcionesPruebaDAO.leer(any())).thenThrow(new RuntimeException("BD error"));
        Response response = resource.delete(testId);
        assertEquals(500, response.getStatus());
    }
}
