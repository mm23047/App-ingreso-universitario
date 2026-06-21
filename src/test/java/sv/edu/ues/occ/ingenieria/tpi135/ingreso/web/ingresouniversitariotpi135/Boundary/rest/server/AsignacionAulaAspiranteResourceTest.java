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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AsignacionAulaAspiranteDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.DisponibilidadAulaTurnoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionAulaAspirante;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionAulaAspiranteResourceTest {

    @Mock
    private AsignacionAulaAspiranteDAO asignacionAulaAspiranteDAO;

    @Mock
    private InscripcionesPruebaDAO inscripcionesDAO;

    @Mock
    private DisponibilidadAulaTurnoDAO disponibilidadDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private AsignacionAulaAspiranteResource resource;

    private UUID idInscripcion;
    private UUID idAula;
    private UUID idTurno;
    private InscripcionesPrueba inscripcion;
    private DisponibilidadAulaTurno disponibilidad;

    @BeforeEach
    void setUp() {
        idInscripcion = UUID.randomUUID();
        idAula = UUID.randomUUID();
        idTurno = UUID.randomUUID();

        inscripcion = new InscripcionesPrueba();
        inscripcion.setIdInscripcionPrueba(idInscripcion);

        disponibilidad = new DisponibilidadAulaTurno();
    }

    private AsignacionAulaAspiranteResource.AsignacionRequest crearPayload() {
        AsignacionAulaAspiranteResource.AsignacionRequest payload =
                new AsignacionAulaAspiranteResource.AsignacionRequest();
        payload.setIdAula(idAula);
        payload.setIdTurno(idTurno);
        return payload;
    }

    // ==================== asignarAulaAspirante (POST /inscripciones/{idInscripcion}) ====================

    @Test
    void asignar_ConDatosValidos_DebeRetornar201() {
        AsignacionAulaAspiranteResource.AsignacionRequest payload = crearPayload();

        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(inscripcion);
        when(disponibilidadDAO.findFiltrado(idAula, idTurno, 0, 1)).thenReturn(List.of(disponibilidad));
        doAnswer(inv -> {
            AsignacionAulaAspirante a = inv.getArgument(0);
            a.setIdAsignacionAulaAspirante(UUID.randomUUID());
            return null;
        }).when(asignacionAulaAspiranteDAO).crear(any(AsignacionAulaAspirante.class));
        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/asignaciones_aula/1"));

        Response response = resource.asignarAulaAspirante(idInscripcion.toString(), payload, uriInfo);

        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());
        verify(asignacionAulaAspiranteDAO).crear(any(AsignacionAulaAspirante.class));
    }

    @Test
    void asignar_ConPayloadNulo_DebeRetornar400() {
        Response response = resource.asignarAulaAspirante(idInscripcion.toString(), null, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesDAO, disponibilidadDAO, asignacionAulaAspiranteDAO);
    }

    @Test
    void asignar_ConIdAulaNulo_DebeRetornar400() {
        AsignacionAulaAspiranteResource.AsignacionRequest payload =
                new AsignacionAulaAspiranteResource.AsignacionRequest();
        payload.setIdTurno(idTurno);

        Response response = resource.asignarAulaAspirante(idInscripcion.toString(), payload, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesDAO);
    }

    @Test
    void asignar_ConIdTurnoNulo_DebeRetornar400() {
        AsignacionAulaAspiranteResource.AsignacionRequest payload =
                new AsignacionAulaAspiranteResource.AsignacionRequest();
        payload.setIdAula(idAula);

        Response response = resource.asignarAulaAspirante(idInscripcion.toString(), payload, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesDAO);
    }

    @Test
    void asignar_ConInscripcionInexistente_DebeRetornar404() {
        AsignacionAulaAspiranteResource.AsignacionRequest payload = crearPayload();
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(null);

        Response response = resource.asignarAulaAspirante(idInscripcion.toString(), payload, uriInfo);

        assertEquals(404, response.getStatus());
        verifyNoInteractions(disponibilidadDAO, asignacionAulaAspiranteDAO);
    }

    @Test
    void asignar_SinDisponibilidadAulaTurno_DebeRetornar400() {
        AsignacionAulaAspiranteResource.AsignacionRequest payload = crearPayload();
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(inscripcion);
        when(disponibilidadDAO.findFiltrado(idAula, idTurno, 0, 1)).thenReturn(Collections.emptyList());

        Response response = resource.asignarAulaAspirante(idInscripcion.toString(), payload, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(asignacionAulaAspiranteDAO);
    }

    @Test
    void asignar_ConCapacidadExcedida_DebeRetornar409() {
        AsignacionAulaAspiranteResource.AsignacionRequest payload = crearPayload();
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(inscripcion);
        when(disponibilidadDAO.findFiltrado(idAula, idTurno, 0, 1)).thenReturn(List.of(disponibilidad));
        doThrow(new IllegalStateException("El aula seleccionada ha alcanzado su capacidad máxima"))
                .when(asignacionAulaAspiranteDAO).crear(any());

        Response response = resource.asignarAulaAspirante(idInscripcion.toString(), payload, uriInfo);

        assertEquals(409, response.getStatus());
    }

    @Test
    void asignar_ConUuidInscripcionInvalido_DebeRetornar400() {
        AsignacionAulaAspiranteResource.AsignacionRequest payload = crearPayload();

        Response response = resource.asignarAulaAspirante("no-es-uuid", payload, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesDAO, disponibilidadDAO, asignacionAulaAspiranteDAO);
    }

    @Test
    void asignar_ConExcepcionEnDAO_DebeRetornar500() {
        AsignacionAulaAspiranteResource.AsignacionRequest payload = crearPayload();
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(inscripcion);
        when(disponibilidadDAO.findFiltrado(idAula, idTurno, 0, 1)).thenReturn(List.of(disponibilidad));
        doThrow(new RuntimeException("Error de BD"))
                .when(asignacionAulaAspiranteDAO).crear(any());

        Response response = resource.asignarAulaAspirante(idInscripcion.toString(), payload, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getAsignacionesPorAulaYTurno (GET /disponibilidad/{idAula}/{idTurno}) ====================

    @Test
    void getAsignaciones_ConParametrosValidos_DebeRetornar200ConLista() {
        AsignacionAulaAspirante asignacion = new AsignacionAulaAspirante();
        asignacion.setIdAsignacionAulaAspirante(UUID.randomUUID());
        when(asignacionAulaAspiranteDAO.findByAulaAndTurno(idAula, idTurno, 0, 50))
                .thenReturn(List.of(asignacion));
        when(asignacionAulaAspiranteDAO.countByAulaAndTurno(idAula, idTurno)).thenReturn(1L);

        Response response = resource.getAsignacionesPorAulaYTurno(
                idAula.toString(), idTurno.toString(), 0, 50);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
        verify(asignacionAulaAspiranteDAO).findByAulaAndTurno(idAula, idTurno, 0, 50);
    }

    @Test
    void getAsignaciones_ConListaVacia_DebeRetornar200ConTotalCero() {
        when(asignacionAulaAspiranteDAO.findByAulaAndTurno(idAula, idTurno, 0, 50))
                .thenReturn(Collections.emptyList());
        when(asignacionAulaAspiranteDAO.countByAulaAndTurno(idAula, idTurno)).thenReturn(0L);

        Response response = resource.getAsignacionesPorAulaYTurno(
                idAula.toString(), idTurno.toString(), 0, 50);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getAsignaciones_ConIdAulaInvalido_DebeRetornar400() {
        Response response = resource.getAsignacionesPorAulaYTurno(
                "no-es-uuid", idTurno.toString(), 0, 50);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(asignacionAulaAspiranteDAO);
    }

    @Test
    void getAsignaciones_ConIdTurnoInvalido_DebeRetornar400() {
        Response response = resource.getAsignacionesPorAulaYTurno(
                idAula.toString(), "no-es-uuid", 0, 50);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(asignacionAulaAspiranteDAO);
    }

    @Test
    void getAsignaciones_ConExcepcionEnDAO_DebeRetornar500() {
        when(asignacionAulaAspiranteDAO.findByAulaAndTurno(any(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getAsignacionesPorAulaYTurno(
                idAula.toString(), idTurno.toString(), 0, 50);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== deleteAsignacion (DELETE /{idAsignacion}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        UUID idAsignacion = UUID.randomUUID();
        AsignacionAulaAspirante existente = new AsignacionAulaAspirante();
        when(asignacionAulaAspiranteDAO.leer(idAsignacion)).thenReturn(existente);

        Response response = resource.deleteAsignacion(idAsignacion.toString());

        assertEquals(204, response.getStatus());
        verify(asignacionAulaAspiranteDAO).eliminar(existente);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        UUID idAsignacion = UUID.randomUUID();
        when(asignacionAulaAspiranteDAO.leer(idAsignacion)).thenReturn(null);

        Response response = resource.deleteAsignacion(idAsignacion.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(asignacionAulaAspiranteDAO, never()).eliminar(any());
    }

    @Test
    void delete_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.deleteAsignacion("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(asignacionAulaAspiranteDAO);
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        UUID idAsignacion = UUID.randomUUID();
        when(asignacionAulaAspiranteDAO.leer(any()))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteAsignacion(idAsignacion.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
