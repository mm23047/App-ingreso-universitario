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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestasExaman;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RespuestasExamanResourceTest {

    @Mock private RespuestasExamanDAO respuestasExamanDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private RespuestasExamanResource resource;
    private RespuestasExaman entidad;
    private UUID testId;
    private UUID examenId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        examenId = UUID.randomUUID();

        resource = new RespuestasExamanResource();
        resource.respuestasExamanDAO = respuestasExamanDAO;

        entidad = new RespuestasExaman();
        entidad.setId(testId);

        ExamenesRealizado examen = new ExamenesRealizado();
        examen.setId(examenId);
        entidad.setIdExamen(examen);

        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setId(UUID.randomUUID());
        entidad.setIdPregunta(pregunta);
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_SinExamenId_ConParametrosValidos_DebeRetornar200() {
        when(respuestasExamanDAO.count()).thenReturn(1);
        when(respuestasExamanDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString("Total-records"));
        assertNotNull(response.getEntity());
        verify(respuestasExamanDAO).count();
        verify(respuestasExamanDAO).findRange(0, 10);
    }

    @Test
    void findRange_SinExamenId_ConListaVacia_DebeRetornar200() {
        when(respuestasExamanDAO.count()).thenReturn(0);
        when(respuestasExamanDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_SinExamenId_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void findRange_SinExamenId_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void findRange_SinExamenId_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void findRange_SinExamenId_ConExcepcionEnDAO_DebeRetornar500() {
        when(respuestasExamanDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findRange(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    @Test
    void findRange_ConExamenIdValido_DebeRetornar200ConLista() {
        resource.examenIdParam = examenId.toString();
        when(respuestasExamanDAO.findByExamenId(examenId)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        verify(respuestasExamanDAO).findByExamenId(examenId);
        verify(respuestasExamanDAO, never()).count();
        verify(respuestasExamanDAO, never()).findRange(anyInt(), anyInt());
    }

    @Test
    void findRange_ConExamenIdInvalido_DebeRetornar422() {
        resource.examenIdParam = "no-es-uuid";

        Response response = resource.findRange(0, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void findRange_ConExamenIdValidoYExcepcionEnDAO_DebeRetornar500() {
        resource.examenIdParam = examenId.toString();
        when(respuestasExamanDAO.findByExamenId(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findRange(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(respuestasExamanDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.findById(testId);

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
        verify(respuestasExamanDAO).leer(testId);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(respuestasExamanDAO.leer(testId)).thenReturn(null);

        Response response = resource.findById(testId);

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

        Response response = resource.findById(testId);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        RespuestasExaman nueva = new RespuestasExaman();

        ExamenesRealizado examen = new ExamenesRealizado();
        examen.setId(examenId);
        nueva.setIdExamen(examen);

        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setId(UUID.randomUUID());
        nueva.setIdPregunta(pregunta);

        OpcionesRespuesta opcion = new OpcionesRespuesta();
        opcion.setId(UUID.randomUUID());
        nueva.setIdOpcionSeleccionada(opcion);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/respuestas_examen/" + testId));

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
    void create_ConIdYaAsignado_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void create_SinExamen_DebeRetornar422() {
        RespuestasExaman nueva = new RespuestasExaman();
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setId(UUID.randomUUID());
        nueva.setIdPregunta(pregunta);

        OpcionesRespuesta opcion = new OpcionesRespuesta();
        opcion.setId(UUID.randomUUID());
        nueva.setIdOpcionSeleccionada(opcion);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void create_SinPregunta_DebeRetornar422() {
        RespuestasExaman nueva = new RespuestasExaman();
        ExamenesRealizado examen = new ExamenesRealizado();
        examen.setId(examenId);
        nueva.setIdExamen(examen);

        OpcionesRespuesta opcion = new OpcionesRespuesta();
        opcion.setId(UUID.randomUUID());
        nueva.setIdOpcionSeleccionada(opcion);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(respuestasExamanDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        RespuestasExaman nueva = new RespuestasExaman();

        ExamenesRealizado examen = new ExamenesRealizado();
        examen.setId(examenId);
        nueva.setIdExamen(examen);

        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setId(UUID.randomUUID());
        nueva.setIdPregunta(pregunta);

        OpcionesRespuesta opcion = new OpcionesRespuesta();
        opcion.setId(UUID.randomUUID());
        nueva.setIdOpcionSeleccionada(opcion);

        doThrow(new RuntimeException("Error de BD")).when(respuestasExamanDAO).crear(any());

        Response response = resource.create(nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
