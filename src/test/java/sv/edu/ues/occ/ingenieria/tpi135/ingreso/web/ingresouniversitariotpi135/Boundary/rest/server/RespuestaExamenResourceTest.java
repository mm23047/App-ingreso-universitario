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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.dto.RespuestasLoteDTO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.RespuestaExamenDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RespuestaExamenResourceTest {

    @Mock private RespuestaExamenDAO respuestaExamenDAO;
    @Mock private ExamenRealizadoDAO examenDAO;
    @Mock private PreguntaOpcionDAO opcionDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    @InjectMocks private RespuestaExamenResource resource;

    private ExamenRealizado examen;
    private PreguntaOpcion opcion;
    private RespuestaExamen entidad;
    private UUID examenId;
    private UUID opcionId;
    private UUID preguntaId;

    @BeforeEach
    void setUp() {
        examenId = UUID.randomUUID();
        opcionId = UUID.randomUUID();
        preguntaId = UUID.randomUUID();

        examen = new ExamenRealizado();
        examen.setIdExamenRealizado(examenId);

        BancoPregunta bancoPregunta = new BancoPregunta();
        bancoPregunta.setIdBancoPregunta(preguntaId);

        opcion = new PreguntaOpcion();
        opcion.setIdPreguntaOpcion(opcionId);
        opcion.setBancoPregunta(bancoPregunta);

        entidad = new RespuestaExamen();
        entidad.setIdRespuestaExamen(UUID.randomUUID());
        entidad.setExamenRealizado(examen);
        entidad.setPreguntaOpcion(opcion);
    }

    // ==================== listRespuestas (GET /) ====================

    @Test
    void listRespuestas_ConParametrosValidos_DebeRetornar200() {
        when(respuestaExamenDAO.findRange(0, 10)).thenReturn(List.of(entidad));
        when(respuestaExamenDAO.count()).thenReturn(1);

        Response response = resource.listRespuestas(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void listRespuestas_ConListaVacia_DebeRetornar200ConTotalCero() {
        when(respuestaExamenDAO.findRange(0, 10)).thenReturn(Collections.emptyList());
        when(respuestaExamenDAO.count()).thenReturn(0);

        Response response = resource.listRespuestas(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void listRespuestas_ConExcepcionEnDAO_DebeRetornar500() {
        when(respuestaExamenDAO.findRange(anyInt(), anyInt())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listRespuestas(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== submitRespuesta (POST /) ====================

    @Test
    void submit_ConDatosValidos_DebeRetornar201() {
        RespuestaExamen nueva = new RespuestaExamen();
        nueva.setExamenRealizado(examen);
        nueva.setPreguntaOpcion(opcion);

        when(examenDAO.leer(examenId)).thenReturn(examen);
        when(opcionDAO.leer(opcionId)).thenReturn(opcion);
        when(respuestaExamenDAO.findByExamenAndPregunta(examenId, preguntaId)).thenReturn(null);
        doAnswer(inv -> {
            RespuestaExamen r = inv.getArgument(0);
            r.setIdRespuestaExamen(UUID.randomUUID());
            return null;
        }).when(respuestaExamenDAO).crear(any(RespuestaExamen.class));
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/respuestas_examen/1"));

        Response response = resource.submitRespuesta(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(respuestaExamenDAO).crear(nueva);
    }

    @Test
    void submit_ConRespuestaPreviaDeMismaPregunta_DebeRetornar200Upsert() {
        RespuestaExamen nueva = new RespuestaExamen();
        nueva.setExamenRealizado(examen);
        nueva.setPreguntaOpcion(opcion);

        when(examenDAO.leer(examenId)).thenReturn(examen);
        when(opcionDAO.leer(opcionId)).thenReturn(opcion);
        when(respuestaExamenDAO.findByExamenAndPregunta(examenId, preguntaId)).thenReturn(entidad);
        when(respuestaExamenDAO.actualizar(entidad)).thenReturn(entidad);

        Response response = resource.submitRespuesta(nueva, uriInfo);

        assertEquals(200, response.getStatus());
        verify(respuestaExamenDAO).actualizar(entidad);
        verify(respuestaExamenDAO, never()).crear(any());
    }

    @Test
    void submit_ConPayloadNulo_DebeRetornar400() {
        Response response = resource.submitRespuesta(null, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO, examenDAO, opcionDAO);
    }

    @Test
    void submit_SinExamen_DebeRetornar400() {
        RespuestaExamen sinExamen = new RespuestaExamen();
        sinExamen.setPreguntaOpcion(opcion);

        Response response = resource.submitRespuesta(sinExamen, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO, examenDAO, opcionDAO);
    }

    @Test
    void submit_SinOpcion_DebeRetornar400() {
        RespuestaExamen sinOpcion = new RespuestaExamen();
        sinOpcion.setExamenRealizado(examen);

        Response response = resource.submitRespuesta(sinOpcion, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO, examenDAO, opcionDAO);
    }

    @Test
    void submit_ConExamenInexistente_DebeRetornar404() {
        RespuestaExamen nueva = new RespuestaExamen();
        nueva.setExamenRealizado(examen);
        nueva.setPreguntaOpcion(opcion);
        when(examenDAO.leer(examenId)).thenReturn(null);

        Response response = resource.submitRespuesta(nueva, uriInfo);

        assertEquals(404, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO);
    }

    @Test
    void submit_ConExamenYaCalificado_DebeRetornar409() {
        examen.setPuntajeFinal(new BigDecimal("85.00"));
        RespuestaExamen nueva = new RespuestaExamen();
        nueva.setExamenRealizado(examen);
        nueva.setPreguntaOpcion(opcion);
        when(examenDAO.leer(examenId)).thenReturn(examen);

        Response response = resource.submitRespuesta(nueva, uriInfo);

        assertEquals(409, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO);
    }

    @Test
    void submit_ConOpcionInexistente_DebeRetornar404() {
        RespuestaExamen nueva = new RespuestaExamen();
        nueva.setExamenRealizado(examen);
        nueva.setPreguntaOpcion(opcion);
        when(examenDAO.leer(examenId)).thenReturn(examen);
        when(opcionDAO.leer(opcionId)).thenReturn(null);

        Response response = resource.submitRespuesta(nueva, uriInfo);

        assertEquals(404, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO);
    }

    @Test
    void submit_ConExcepcionEnDAO_DebeRetornar500() {
        RespuestaExamen nueva = new RespuestaExamen();
        nueva.setExamenRealizado(examen);
        nueva.setPreguntaOpcion(opcion);
        when(examenDAO.leer(examenId)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.submitRespuesta(nueva, uriInfo);

        assertEquals(500, response.getStatus());
    }

    // ==================== submitRespuestasBatch (POST /lote) ====================

    @Test
    void submitBatch_ConDatosValidos_DebeRetornar201() {
        RespuestasLoteDTO payload = new RespuestasLoteDTO();
        payload.setIdExamen(examenId);
        payload.setOpcionesSeleccionadas(List.of(UUID.randomUUID(), UUID.randomUUID()));

        when(examenDAO.leer(examenId)).thenReturn(examen);

        Response response = resource.submitRespuestasBatch(payload);

        assertEquals(201, response.getStatus());
        verify(respuestaExamenDAO).guardarLoteMejorado(eq(examenId), anyList());
    }

    @Test
    void submitBatch_ConPayloadNulo_DebeRetornar400() {
        Response response = resource.submitRespuestasBatch(null);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenDAO, respuestaExamenDAO);
    }

    @Test
    void submitBatch_SinIdExamen_DebeRetornar400() {
        RespuestasLoteDTO payload = new RespuestasLoteDTO();
        payload.setOpcionesSeleccionadas(List.of(UUID.randomUUID()));

        Response response = resource.submitRespuestasBatch(payload);

        assertEquals(400, response.getStatus());
    }

    @Test
    void submitBatch_SinOpciones_DebeRetornar400() {
        RespuestasLoteDTO payload = new RespuestasLoteDTO();
        payload.setIdExamen(examenId);

        Response response = resource.submitRespuestasBatch(payload);

        assertEquals(400, response.getStatus());
    }

    @Test
    void submitBatch_ConOpcionesVacias_DebeRetornar400() {
        RespuestasLoteDTO payload = new RespuestasLoteDTO();
        payload.setIdExamen(examenId);
        payload.setOpcionesSeleccionadas(Collections.emptyList());

        Response response = resource.submitRespuestasBatch(payload);

        assertEquals(400, response.getStatus());
    }

    @Test
    void submitBatch_ConExamenInexistente_DebeRetornar404() {
        RespuestasLoteDTO payload = new RespuestasLoteDTO();
        payload.setIdExamen(examenId);
        payload.setOpcionesSeleccionadas(List.of(UUID.randomUUID()));
        when(examenDAO.leer(examenId)).thenReturn(null);

        Response response = resource.submitRespuestasBatch(payload);

        assertEquals(404, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO);
    }

    @Test
    void submitBatch_ConExamenYaCalificado_DebeRetornar409() {
        examen.setPuntajeFinal(new BigDecimal("90.00"));
        RespuestasLoteDTO payload = new RespuestasLoteDTO();
        payload.setIdExamen(examenId);
        payload.setOpcionesSeleccionadas(List.of(UUID.randomUUID()));
        when(examenDAO.leer(examenId)).thenReturn(examen);

        Response response = resource.submitRespuestasBatch(payload);

        assertEquals(409, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO);
    }

    @Test
    void submitBatch_ConExcepcionEnDAO_DebeRetornar500() {
        RespuestasLoteDTO payload = new RespuestasLoteDTO();
        payload.setIdExamen(examenId);
        payload.setOpcionesSeleccionadas(List.of(UUID.randomUUID()));
        when(examenDAO.leer(examenId)).thenReturn(examen);
        doThrow(new RuntimeException("Error de BD"))
                .when(respuestaExamenDAO).guardarLoteMejorado(any(), anyList());

        Response response = resource.submitRespuestasBatch(payload);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getRespuesta (GET /{idRespuesta}) ====================

    @Test
    void getRespuesta_ConIdExistente_DebeRetornar200() {
        when(respuestaExamenDAO.leer(entidad.getIdRespuestaExamen())).thenReturn(entidad);

        Response response = resource.getRespuesta(entidad.getIdRespuestaExamen().toString());

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
    }

    @Test
    void getRespuesta_ConIdInexistente_DebeRetornar404() {
        UUID idRespuesta = UUID.randomUUID();
        when(respuestaExamenDAO.leer(idRespuesta)).thenReturn(null);

        Response response = resource.getRespuesta(idRespuesta.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void getRespuesta_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getRespuesta("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO);
    }

    @Test
    void getRespuesta_ConExcepcionEnDAO_DebeRetornar500() {
        when(respuestaExamenDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getRespuesta(UUID.randomUUID().toString());

        assertEquals(500, response.getStatus());
    }

    // ==================== getRespuestasByExamen (GET /examen/{idExamen}) ====================

    @Test
    void getRespuestasByExamen_ConResultados_DebeRetornar200() {
        when(respuestaExamenDAO.findByExamenId(examenId)).thenReturn(List.of(entidad));

        Response response = resource.getRespuestasByExamen(examenId.toString());

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void getRespuestasByExamen_SinResultados_DebeRetornar200ConListaVacia() {
        when(respuestaExamenDAO.findByExamenId(examenId)).thenReturn(Collections.emptyList());

        Response response = resource.getRespuestasByExamen(examenId.toString());

        assertEquals(200, response.getStatus());
    }

    @Test
    void getRespuestasByExamen_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getRespuestasByExamen("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO);
    }

    @Test
    void getRespuestasByExamen_ConExcepcionEnDAO_DebeRetornar500() {
        when(respuestaExamenDAO.findByExamenId(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getRespuestasByExamen(examenId.toString());

        assertEquals(500, response.getStatus());
    }

    // ==================== getConteoRespuestas (GET /examen/{idExamen}/conteo) ====================

    @Test
    void getConteo_ConExamenExistente_DebeRetornar200() {
        when(respuestaExamenDAO.countRespuestasByExamen(examenId)).thenReturn(15L);

        Response response = resource.getConteoRespuestas(examenId.toString());

        assertEquals(200, response.getStatus());
        assertEquals(15L, response.getEntity());
    }

    @Test
    void getConteo_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getConteoRespuestas("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO);
    }

    @Test
    void getConteo_ConExcepcionEnDAO_DebeRetornar500() {
        when(respuestaExamenDAO.countRespuestasByExamen(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getConteoRespuestas(examenId.toString());

        assertEquals(500, response.getStatus());
    }

    // ==================== deleteRespuesta (DELETE /{idRespuesta}) ====================

    @Test
    void delete_ConIdExistente_ExamenAbierto_DebeRetornar204() {
        when(respuestaExamenDAO.leer(entidad.getIdRespuestaExamen())).thenReturn(entidad);

        Response response = resource.deleteRespuesta(entidad.getIdRespuestaExamen().toString());

        assertEquals(204, response.getStatus());
        verify(respuestaExamenDAO).eliminar(entidad);
    }

    @Test
    void delete_ConExamenYaCalificado_DebeRetornar409() {
        examen.setPuntajeFinal(new BigDecimal("90.00"));
        when(respuestaExamenDAO.leer(entidad.getIdRespuestaExamen())).thenReturn(entidad);

        Response response = resource.deleteRespuesta(entidad.getIdRespuestaExamen().toString());

        assertEquals(409, response.getStatus());
        verify(respuestaExamenDAO, never()).eliminar(any());
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        UUID idRespuesta = UUID.randomUUID();
        when(respuestaExamenDAO.leer(idRespuesta)).thenReturn(null);

        Response response = resource.deleteRespuesta(idRespuesta.toString());

        assertEquals(404, response.getStatus());
        verify(respuestaExamenDAO, never()).eliminar(any());
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(respuestaExamenDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteRespuesta(UUID.randomUUID().toString());

        assertEquals(500, response.getStatus());
    }
}
