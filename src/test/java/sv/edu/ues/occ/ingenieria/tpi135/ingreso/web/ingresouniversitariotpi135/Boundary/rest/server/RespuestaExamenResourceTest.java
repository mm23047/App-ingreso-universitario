package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntasPorClaveDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.RespuestaExamenDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RespuestaExamenResourceTest {

    @Mock private RespuestaExamenDAO respuestaExamenDAO;
    @Mock private ExamenRealizadoDAO examenRealizadoDAO;
    @Mock private PreguntaOpcionDAO preguntaOpcionDAO;
    @Mock private PreguntasPorClaveDAO preguntasPorClaveDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private RespuestaExamenResource resource;
    private ExamenRealizado examen;
    private PreguntaOpcion opcion;

    @BeforeEach
    void setUp() {
        resource = new RespuestaExamenResource();
        resource.respuestaExamenDAO = respuestaExamenDAO;
        resource.examenRealizadoDAO = examenRealizadoDAO;
        resource.preguntaOpcionDAO = preguntaOpcionDAO;
        resource.preguntasPorClaveDAO = preguntasPorClaveDAO;

        examen = new ExamenRealizado();
        ClavesExamen clave = new ClavesExamen();
        clave.setIdClaveExaman(UUID.randomUUID());
        examen.setClaveExamen(clave);

        opcion = new PreguntaOpcion();
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setIdBancoPregunta(UUID.randomUUID());
        opcion.setBancoPregunta(pregunta);
    }

    @Test
    void create_ConPreguntaFueraDeClave_DebeRetornar422() {
        UUID examenId = UUID.randomUUID();
        UUID opcionId = UUID.randomUUID();
        examen.setIdExamenRealizado(examenId);
        opcion.setIdPreguntaOpcion(opcionId);

        RespuestaExamen payload = new RespuestaExamen();
        payload.setExamenRealizado(examen);
        payload.setPreguntaOpcion(opcion);

        when(examenRealizadoDAO.leer(examenId)).thenReturn(examen);
        when(preguntaOpcionDAO.leer(opcionId)).thenReturn(opcion);
        when(preguntasPorClaveDAO.existsByClaveAndPregunta(examen.getClaveExamen().getIdClaveExaman(), opcion.getBancoPregunta().getIdBancoPregunta())).thenReturn(false);

        Response response = resource.create(payload, uriInfo);

        assertEquals(422, response.getStatus());
        verify(respuestaExamenDAO, never()).crear(any());
    }

    @Test
    void create_ConDuplicado_DebeRetornar409() {
        UUID examenId = UUID.randomUUID();
        UUID opcionId = UUID.randomUUID();
        examen.setIdExamenRealizado(examenId);
        opcion.setIdPreguntaOpcion(opcionId);

        RespuestaExamen payload = new RespuestaExamen();
        payload.setExamenRealizado(examen);
        payload.setPreguntaOpcion(opcion);

        when(examenRealizadoDAO.leer(examenId)).thenReturn(examen);
        when(preguntaOpcionDAO.leer(opcionId)).thenReturn(opcion);
        when(preguntasPorClaveDAO.existsByClaveAndPregunta(examen.getClaveExamen().getIdClaveExaman(), opcion.getBancoPregunta().getIdBancoPregunta())).thenReturn(true);
        when(respuestaExamenDAO.existsByExamenAndPregunta(examenId, opcion.getBancoPregunta().getIdBancoPregunta())).thenReturn(true);

        Response response = resource.create(payload, uriInfo);

        assertEquals(409, response.getStatus());
        verify(respuestaExamenDAO, never()).crear(any());
    }

    @Test
    void create_Valido_DebePersistirYRetornar201() {
        UUID examenId = UUID.randomUUID();
        UUID opcionId = UUID.randomUUID();
        examen.setIdExamenRealizado(examenId);
        opcion.setIdPreguntaOpcion(opcionId);

        RespuestaExamen payload = new RespuestaExamen();
        payload.setExamenRealizado(examen);
        payload.setPreguntaOpcion(opcion);

        when(examenRealizadoDAO.leer(examenId)).thenReturn(examen);
        when(preguntaOpcionDAO.leer(opcionId)).thenReturn(opcion);
        when(preguntasPorClaveDAO.existsByClaveAndPregunta(examen.getClaveExamen().getIdClaveExaman(), opcion.getBancoPregunta().getIdBancoPregunta())).thenReturn(true);
        when(respuestaExamenDAO.existsByExamenAndPregunta(examenId, opcion.getBancoPregunta().getIdBancoPregunta())).thenReturn(false);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/respuesta_examen/1"));

        Response response = resource.create(payload, uriInfo);

        assertEquals(201, response.getStatus());
        verify(respuestaExamenDAO).crear(payload);
    }
}