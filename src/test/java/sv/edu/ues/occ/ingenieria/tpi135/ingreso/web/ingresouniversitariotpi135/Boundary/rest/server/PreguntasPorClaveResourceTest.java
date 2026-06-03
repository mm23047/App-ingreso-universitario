package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntasPorClaveDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClaveId;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para PreguntasPorClaveResource.
 * Endpoints reales:
 *   GET    /claves/{idClave}/preguntas
 *   POST   /claves/{idClave}/preguntas        (AsignarPreguntaDTO)
 *   POST   /claves/{idClave}/preguntas/masivo (AsignacionMasivaDTO)
 *   DELETE /claves/{idClave}/preguntas/{idPregunta}
 */
@ExtendWith(MockitoExtension.class)
class PreguntasPorClaveResourceTest {

    @Mock
    private PreguntasPorClaveDAO preguntasPorClaveDAO;

    @Mock
    private ClavesExamanDAO clavesDAO;

    @Mock
    private BancoPreguntaDAO preguntasDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private PreguntasPorClaveResource resource;

    private ClavesExamen clave;
    private BancoPregunta pregunta;
    private PreguntasPorClave entidad;
    private UUID idClave;
    private UUID idPregunta;

    @BeforeEach
    void setUp() {
        idClave = UUID.randomUUID();
        idPregunta = UUID.randomUUID();

        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setCantidadPreguntasRequeridas(10);

        clave = new ClavesExamen();
        clave.setIdClaveExaman(idClave);
        clave.setEtapaAdmision(etapa);

        pregunta = new BancoPregunta();
        pregunta.setIdBancoPregunta(idPregunta);

        PreguntasPorClaveId pk = new PreguntasPorClaveId();
        pk.setIdClave(idClave);
        pk.setIdPregunta(idPregunta);

        entidad = new PreguntasPorClave();
        entidad.setIdPreguntaPorClave(pk);
    }

    // ==================== getPreguntasByClave (GET /{idClave}/preguntas) ====================

    @Test
    void getPreguntasByClave_ConClaveExistente_DebeRetornar200ConLista() {
        when(clavesDAO.leer(idClave)).thenReturn(clave);
        when(preguntasPorClaveDAO.findPreguntasByClave(idClave)).thenReturn(List.of(entidad));

        Response response = resource.getPreguntasByClave(idClave.toString());

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        verify(preguntasPorClaveDAO).findPreguntasByClave(idClave);
    }

    @Test
    void getPreguntasByClave_ConClaveInexistente_DebeRetornar404() {
        when(clavesDAO.leer(idClave)).thenReturn(null);

        Response response = resource.getPreguntasByClave(idClave.toString());

        assertEquals(404, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void getPreguntasByClave_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.getPreguntasByClave("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesDAO, preguntasPorClaveDAO);
    }

    @Test
    void getPreguntasByClave_ConExcepcionEnDAO_DebeRetornar500() {
        when(clavesDAO.leer(any())).thenThrow(new RuntimeException("BD error"));

        Response response = resource.getPreguntasByClave(idClave.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== asignarPreguntaAClave (POST /{idClave}/preguntas) ====================

    @Test
    void asignar_ConDatosValidos_DebeRetornar201() {
        when(clavesDAO.findByIdWithEtapa(idClave)).thenReturn(clave);
        when(preguntasPorClaveDAO.countPreguntasByClave(idClave)).thenReturn(0L);
        when(preguntasDAO.leer(idPregunta)).thenReturn(pregunta);
        when(preguntasPorClaveDAO.existsByClaveAndPregunta(idClave, idPregunta)).thenReturn(false);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/claves/1/preguntas/1"));

        PreguntasPorClaveResource.AsignarPreguntaDTO payload =
            new PreguntasPorClaveResource.AsignarPreguntaDTO();
        payload.setIdPregunta(idPregunta);

        Response response = resource.asignarPreguntaAClave(idClave.toString(), payload, uriInfo);

        assertEquals(201, response.getStatus());
        verify(preguntasPorClaveDAO).crear(any(PreguntasPorClave.class));
    }

    @Test
    void asignar_ConPayloadNulo_DebeRetornar400() {
        Response response = resource.asignarPreguntaAClave(idClave.toString(), null, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesDAO, preguntasPorClaveDAO);
    }

    @Test
    void asignar_ConIdPreguntaNulo_DebeRetornar400() {
        PreguntasPorClaveResource.AsignarPreguntaDTO payload =
            new PreguntasPorClaveResource.AsignarPreguntaDTO();
        // idPregunta null

        Response response = resource.asignarPreguntaAClave(idClave.toString(), payload, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesDAO, preguntasPorClaveDAO);
    }

    @Test
    void asignar_ConClaveInexistente_DebeRetornar404() {
        when(clavesDAO.findByIdWithEtapa(idClave)).thenReturn(null);

        PreguntasPorClaveResource.AsignarPreguntaDTO payload =
            new PreguntasPorClaveResource.AsignarPreguntaDTO();
        payload.setIdPregunta(idPregunta);

        Response response = resource.asignarPreguntaAClave(idClave.toString(), payload, uriInfo);

        assertEquals(404, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void asignar_ConLimiteAlcanzado_DebeRetornar409() {
        when(clavesDAO.findByIdWithEtapa(idClave)).thenReturn(clave);
        when(preguntasPorClaveDAO.countPreguntasByClave(idClave)).thenReturn(10L); // igual al limite

        PreguntasPorClaveResource.AsignarPreguntaDTO payload =
            new PreguntasPorClaveResource.AsignarPreguntaDTO();
        payload.setIdPregunta(idPregunta);

        Response response = resource.asignarPreguntaAClave(idClave.toString(), payload, uriInfo);

        assertEquals(409, response.getStatus());
        verify(preguntasPorClaveDAO, never()).crear(any());
    }

    @Test
    void asignar_ConPreguntaYaAsignada_DebeRetornar409() {
        when(clavesDAO.findByIdWithEtapa(idClave)).thenReturn(clave);
        when(preguntasPorClaveDAO.countPreguntasByClave(idClave)).thenReturn(0L);
        when(preguntasDAO.leer(idPregunta)).thenReturn(pregunta);
        when(preguntasPorClaveDAO.existsByClaveAndPregunta(idClave, idPregunta)).thenReturn(true);

        PreguntasPorClaveResource.AsignarPreguntaDTO payload =
            new PreguntasPorClaveResource.AsignarPreguntaDTO();
        payload.setIdPregunta(idPregunta);

        Response response = resource.asignarPreguntaAClave(idClave.toString(), payload, uriInfo);

        assertEquals(409, response.getStatus());
        verify(preguntasPorClaveDAO, never()).crear(any());
    }

    // ==================== desasignarPregunta (DELETE /{idClave}/preguntas/{idPregunta}) ====================

    @Test
    void desasignar_ConAsignacionExistente_DebeRetornar204() {
        when(preguntasPorClaveDAO.leer(any(PreguntasPorClaveId.class))).thenReturn(entidad);

        Response response = resource.desasignarPregunta(
            idClave.toString(), idPregunta.toString());

        assertEquals(204, response.getStatus());
        verify(preguntasPorClaveDAO).eliminar(entidad);
    }

    @Test
    void desasignar_ConAsignacionInexistente_DebeRetornar404() {
        when(preguntasPorClaveDAO.leer(any(PreguntasPorClaveId.class))).thenReturn(null);

        Response response = resource.desasignarPregunta(
            idClave.toString(), idPregunta.toString());

        assertEquals(404, response.getStatus());
        verify(preguntasPorClaveDAO, never()).eliminar(any());
    }

    @Test
    void desasignar_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.desasignarPregunta("no-uuid", idPregunta.toString());

        assertEquals(400, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void desasignar_ConExcepcionEnDAO_DebeRetornar500() {
        when(preguntasPorClaveDAO.leer(any(PreguntasPorClaveId.class)))
            .thenThrow(new RuntimeException("BD error"));

        Response response = resource.desasignarPregunta(
            idClave.toString(), idPregunta.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
