package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenesRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ExamenesRealizadoResource.
 * Recurso de solo lectura: solo expone GET (findRange y findById).
 */
@ExtendWith(MockitoExtension.class)
class ExamenesRealizadoResourceTest {

    @Mock private ExamenesRealizadoDAO examenesRealizadoDAO;

    private ExamenesRealizadoResource resource;
    private ExamenesRealizado entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        resource = new ExamenesRealizadoResource();
        resource.examenesRealizadoDAO = examenesRealizadoDAO;

        entidad = new ExamenesRealizado();
        entidad.setId(testId);
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200() {
        when(examenesRealizadoDAO.count()).thenReturn(1);
        when(examenesRealizadoDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(examenesRealizadoDAO).count();
        verify(examenesRealizadoDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200() {
        when(examenesRealizadoDAO.count()).thenReturn(0);
        when(examenesRealizadoDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(examenesRealizadoDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(examenesRealizadoDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(examenesRealizadoDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(examenesRealizadoDAO.count()).thenThrow(new RuntimeException("BD error"));
        Response response = resource.findRange(0, 10);
        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    @Test
    void findRange_ConAspiranteIdValido_DebeRetornar200YListaFiltrada() {
        UUID aspiranteId = UUID.randomUUID();
        resource.aspiranteIdParam = aspiranteId.toString();

        when(examenesRealizadoDAO.findByAspiranteId(any())).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        @SuppressWarnings("unchecked")
        List<ExamenesRealizado> cuerpo = (List<ExamenesRealizado>) response.getEntity();
        assertNotNull(cuerpo);
        assertEquals(1, cuerpo.size());
        assertSame(entidad, cuerpo.get(0));
        verify(examenesRealizadoDAO).findByAspiranteId(any());
    }

    @Test
    void findRange_ConAspiranteIdInvalido_DebeRetornar422() {
        resource.aspiranteIdParam = "no-es-uuid";

        Response response = resource.findRange(0, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(examenesRealizadoDAO);
    }

    @Test
    void findRange_ConPruebaIdValido_DebeRetornar200YListaFiltrada() {
        UUID pruebaId = UUID.randomUUID();
        resource.pruebaIdParam = pruebaId.toString();

        when(examenesRealizadoDAO.findByPruebaId(any())).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        @SuppressWarnings("unchecked")
        List<ExamenesRealizado> cuerpo = (List<ExamenesRealizado>) response.getEntity();
        assertNotNull(cuerpo);
        assertEquals(1, cuerpo.size());
        assertSame(entidad, cuerpo.get(0));
        verify(examenesRealizadoDAO).findByPruebaId(any());
    }

    @Test
    void findRange_ConPruebaIdInvalido_DebeRetornar422() {
        resource.pruebaIdParam = "no-es-uuid";

        Response response = resource.findRange(0, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(examenesRealizadoDAO);
    }

    @Test
    void findRange_ConErrorEnDAOAlFiltrar_DebeRetornar500() {
        UUID aspiranteId = UUID.randomUUID();
        resource.aspiranteIdParam = aspiranteId.toString();

        when(examenesRealizadoDAO.findByAspiranteId(any()))
                .thenThrow(new IllegalStateException("Cannot access db"));

        Response response = resource.findRange(0, 10);

        assertEquals(500, response.getStatus());
        assertEquals("Cannot access db", response.getHeaderString("Server-exception"));
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        when(examenesRealizadoDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.findById(testId);

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
        verify(examenesRealizadoDAO).leer(testId);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(examenesRealizadoDAO.leer(testId)).thenReturn(null);

        Response response = resource.findById(testId);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        Response response = resource.findById(null);
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(examenesRealizadoDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(examenesRealizadoDAO.leer(any())).thenThrow(new RuntimeException("BD error"));
        Response response = resource.findById(testId);
        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== calificar (POST /{id}/calificar) ====================

    @Test
    void calificar_ConIdExistente_DebeRetornar200ConExamenActualizado() {
        when(examenesRealizadoDAO.calificarExamen(testId)).thenReturn(entidad);

        Response response = resource.calificar(testId);

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
        verify(examenesRealizadoDAO).calificarExamen(testId);
    }

    @Test
    void calificar_ConIdInexistente_DebeRetornar404() {
        when(examenesRealizadoDAO.calificarExamen(testId)).thenReturn(null);

        Response response = resource.calificar(testId);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void calificar_ConIdNulo_DebeRetornar422() {
        Response response = resource.calificar(null);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(examenesRealizadoDAO);
    }

    @Test
    void calificar_ConExcepcionEnDAO_DebeRetornar500() {
        when(examenesRealizadoDAO.calificarExamen(any())).thenThrow(new RuntimeException("BD error"));

        Response response = resource.calificar(testId);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
