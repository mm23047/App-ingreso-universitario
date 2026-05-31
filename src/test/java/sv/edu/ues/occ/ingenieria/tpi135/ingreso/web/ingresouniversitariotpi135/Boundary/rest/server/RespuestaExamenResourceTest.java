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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.RespuestaExamenDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para RespuestaExamenResource.
 * Endpoint principal: POST /respuestas (submitRespuesta) con lógica de UPSERT.
 */
@ExtendWith(MockitoExtension.class)
class RespuestaExamenResourceTest {

    // Mockito inyecta por tipo: respuestaExamenDAO, examenDAO (ExamenRealizadoDAO), opcionDAO (PreguntaOpcionDAO)
    @Mock
    private RespuestaExamenDAO respuestaExamenDAO;

    @Mock
    private ExamenRealizadoDAO examenDAO;

    @Mock
    private PreguntaOpcionDAO opcionDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private RespuestaExamenResource resource;

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
        // puntajeFinal null → examen abierto

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
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/respuestas/1"));

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
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void getRespuesta_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.getRespuesta("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO);
    }

    // ==================== deleteRespuesta (DELETE /{idRespuesta}) ====================

    @Test
    void delete_ConIdExistente_ExamenAbierto_DebeRetornar204() {
        // examen.puntajeFinal = null → examen abierto → se puede borrar
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
    void delete_ConIdFormatoInvalido_DebeRetornar500() {
        // deleteRespuesta solo captura Exception → IAE → 500
        Response response = resource.deleteRespuesta("no-es-uuid");

        assertEquals(500, response.getStatus());
        verifyNoInteractions(respuestaExamenDAO);
    }
}
