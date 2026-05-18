package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AulaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AsignacionAulaAspiranteDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.DisponibilidadAulaTurnoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Aula;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionAulaAspirante;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsignacionAulaAspiranteResourceHardeningTest {

    @Mock
    private AsignacionAulaAspiranteDAO asignacionAulaAspiranteDAO;

    @Mock
    private AulaDAO aulaDAO;

    @Mock
    private DisponibilidadAulaTurnoDAO disponibilidadAulaTurnoDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private AsignacionAulaAspiranteResource resource;

    @BeforeEach
    void setUp() {
        resource = new AsignacionAulaAspiranteResource();
        resource.asignacionAulaAspiranteDAO = asignacionAulaAspiranteDAO;
        resource.aulaDAO = aulaDAO;
        resource.disponibilidadAulaTurnoDAO = disponibilidadAulaTurnoDAO;
    }

    @Test
    void create_ConDisponibilidadInexistente_DebeRetornar409() {
        AsignacionAulaAspirante payload = crearPayload();
        UUID idAula = payload.getDisponibilidad().getIdAula().getId();
        UUID idTurno = payload.getDisponibilidad().getIdTurno().getId();
        UUID idInscripcion = payload.getIdInscripcion().getId();

        when(disponibilidadAulaTurnoDAO.existsByAulaAndTurno(idAula, idTurno)).thenReturn(false);

        Response response = resource.create(payload, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString("Conflict-reason"));
        verify(asignacionAulaAspiranteDAO, never()).crear(payload);
        verifyNoInteractions(aulaDAO);
    }

    @Test
    void create_ConAsignacionDuplicada_DebeRetornar409() {
        AsignacionAulaAspirante payload = crearPayload();
        UUID idAula = payload.getDisponibilidad().getIdAula().getId();
        UUID idTurno = payload.getDisponibilidad().getIdTurno().getId();
        UUID idInscripcion = payload.getIdInscripcion().getId();

        when(disponibilidadAulaTurnoDAO.existsByAulaAndTurno(idAula, idTurno)).thenReturn(true);
        when(asignacionAulaAspiranteDAO.existsByInscripcionAndTurno(idInscripcion, idTurno)).thenReturn(true);

        Response response = resource.create(payload, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString("Conflict-reason"));
        verify(asignacionAulaAspiranteDAO, never()).crear(payload);
        verifyNoInteractions(aulaDAO);
    }

    @Test
    void create_ConAulaSinCapacidad_DebeRetornar409() {
        AsignacionAulaAspirante payload = crearPayload();
        UUID idAula = payload.getDisponibilidad().getIdAula().getId();
        UUID idTurno = payload.getDisponibilidad().getIdTurno().getId();
        UUID idInscripcion = payload.getIdInscripcion().getId();

        Aula aula = payload.getDisponibilidad().getIdAula();
        aula.setCapacidadFisica(1);

        when(disponibilidadAulaTurnoDAO.existsByAulaAndTurno(idAula, idTurno)).thenReturn(true);
        when(asignacionAulaAspiranteDAO.existsByInscripcionAndTurno(idInscripcion, idTurno)).thenReturn(false);
        when(aulaDAO.leer(idAula)).thenReturn(aula);
        when(asignacionAulaAspiranteDAO.countByAulaAndTurno(idAula, idTurno)).thenReturn(1L);

        Response response = resource.create(payload, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString("Conflict-reason"));
        verify(asignacionAulaAspiranteDAO, never()).crear(payload);
    }

    @Test
    void create_Valido_DebeRetornar201() {
        AsignacionAulaAspirante payload = crearPayload();
        UUID idAula = payload.getDisponibilidad().getIdAula().getId();
        UUID idTurno = payload.getDisponibilidad().getIdTurno().getId();
        UUID idInscripcion = payload.getIdInscripcion().getId();

        when(disponibilidadAulaTurnoDAO.existsByAulaAndTurno(idAula, idTurno)).thenReturn(true);
        when(asignacionAulaAspiranteDAO.existsByInscripcionAndTurno(idInscripcion, idTurno)).thenReturn(false);
        when(aulaDAO.leer(idAula)).thenReturn(payload.getDisponibilidad().getIdAula());
        when(asignacionAulaAspiranteDAO.countByAulaAndTurno(idAula, idTurno)).thenReturn(0L);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/asignacion_aula_aspirante/1"));

        Response response = resource.create(payload, uriInfo);

        assertEquals(201, response.getStatus());
        verify(asignacionAulaAspiranteDAO).crear(payload);
    }

    private AsignacionAulaAspirante crearPayload() {
        AsignacionAulaAspirante payload = new AsignacionAulaAspirante();

        InscripcionesPrueba inscripcion = new InscripcionesPrueba();
        inscripcion.setId(UUID.randomUUID());
        payload.setIdInscripcion(inscripcion);

        Aula aula = new Aula();
        aula.setId(UUID.randomUUID());
        aula.setCapacidadFisica(2);
        aula.setCodigoAulaApi("A-101");

        TurnosExaman turno = new TurnosExaman();
        turno.setId(UUID.randomUUID());

        DisponibilidadAulaTurno disponibilidad = new DisponibilidadAulaTurno();
        disponibilidad.setIdAula(aula);
        disponibilidad.setIdTurno(turno);
        payload.setDisponibilidad(disponibilidad);

        return payload;
    }
}
