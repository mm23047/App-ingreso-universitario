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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AulaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.DisponibilidadAulaTurnoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TurnosExamenDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Aula;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurnoId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExamen;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadAulaTurnoResourceTest {

    @Mock private DisponibilidadAulaTurnoDAO disponibilidadDAO;
    @Mock private AulaDAO aulaDAO;
    @Mock private TurnosExamenDAO turnosDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;
    @InjectMocks private DisponibilidadAulaTurnoResource resource;

    private UUID idAula;
    private UUID idTurno;
    private String idAulaStr;
    private String idTurnoStr;

    @BeforeEach
    void setUp() {
        idAula = UUID.randomUUID();
        idTurno = UUID.randomUUID();
        idAulaStr = idAula.toString();
        idTurnoStr = idTurno.toString();
    }

    // ==================== listDisponibilidad (GET /disponibilidad) ====================

    @Test
    void listDisponibilidad_ConFiltros_Retorna200ConLista() {
        DisponibilidadAulaTurno disp = new DisponibilidadAulaTurno();
        when(disponibilidadDAO.findFiltrado(idAula, idTurno, 0, 50)).thenReturn(List.of(disp));

        Response response = resource.listDisponibilidad(idAulaStr, idTurnoStr, 0, 50);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void listDisponibilidad_ConListaVacia_Retorna200() {
        when(disponibilidadDAO.findFiltrado(idAula, idTurno, 0, 50)).thenReturn(Collections.emptyList());

        Response response = resource.listDisponibilidad(idAulaStr, idTurnoStr, 0, 50);

        assertEquals(200, response.getStatus());
    }

    @Test
    void listDisponibilidad_SinFiltros_Retorna200() {
        when(disponibilidadDAO.findFiltrado(null, null, 0, 50)).thenReturn(Collections.emptyList());

        Response response = resource.listDisponibilidad(null, null, 0, 50);

        assertEquals(200, response.getStatus());
    }

    @Test
    void listDisponibilidad_ConIdAulaFormatoInvalido_Retorna400() {
        Response response = resource.listDisponibilidad("no-uuid", idTurnoStr, 0, 50);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(disponibilidadDAO);
    }

    @Test
    void listDisponibilidad_ConIdTurnoFormatoInvalido_Retorna400() {
        Response response = resource.listDisponibilidad(idAulaStr, "no-uuid", 0, 50);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(disponibilidadDAO);
    }

    @Test
    void listDisponibilidad_ConExcepcionEnDAO_Retorna500() {
        when(disponibilidadDAO.findFiltrado(any(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listDisponibilidad(idAulaStr, idTurnoStr, 0, 50);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== createDisponibilidad (POST /aulas/{idAula}/disponibilidad/{idTurno}) ====================

    @Test
    void createDisponibilidad_ConDatosValidos_Retorna201() {
        Aula aula = new Aula();
        aula.setIdAula(idAula);
        TurnosExamen turno = new TurnosExamen();
        turno.setIdTurnoExamen(idTurno);

        when(aulaDAO.leer(idAula)).thenReturn(aula);
        when(turnosDAO.leer(idTurno)).thenReturn(turno);
        when(disponibilidadDAO.existsByAulaAndTurno(idAula, idTurno)).thenReturn(false);
        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.queryParam(anyString(), any())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/disponibilidad?idAula=x&idTurno=y"));

        Response response = resource.createDisponibilidad(idAulaStr, idTurnoStr, uriInfo);

        assertEquals(201, response.getStatus());
        verify(disponibilidadDAO).crear(any(DisponibilidadAulaTurno.class));
    }

    @Test
    void createDisponibilidad_ConAulaInexistente_Retorna404() {
        when(aulaDAO.leer(idAula)).thenReturn(null);

        Response response = resource.createDisponibilidad(idAulaStr, idTurnoStr, uriInfo);

        assertEquals(404, response.getStatus());
        verify(disponibilidadDAO, never()).crear(any());
    }

    @Test
    void createDisponibilidad_ConTurnoInexistente_Retorna404() {
        Aula aula = new Aula();
        aula.setIdAula(idAula);
        when(aulaDAO.leer(idAula)).thenReturn(aula);
        when(turnosDAO.leer(idTurno)).thenReturn(null);

        Response response = resource.createDisponibilidad(idAulaStr, idTurnoStr, uriInfo);

        assertEquals(404, response.getStatus());
        verify(disponibilidadDAO, never()).crear(any());
    }

    @Test
    void createDisponibilidad_ConDuplicado_Retorna409() {
        Aula aula = new Aula();
        aula.setIdAula(idAula);
        TurnosExamen turno = new TurnosExamen();
        turno.setIdTurnoExamen(idTurno);

        when(aulaDAO.leer(idAula)).thenReturn(aula);
        when(turnosDAO.leer(idTurno)).thenReturn(turno);
        when(disponibilidadDAO.existsByAulaAndTurno(idAula, idTurno)).thenReturn(true);

        Response response = resource.createDisponibilidad(idAulaStr, idTurnoStr, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verify(disponibilidadDAO, never()).crear(any());
    }

    @Test
    void createDisponibilidad_ConUuidInvalido_Retorna400() {
        Response response = resource.createDisponibilidad("no-uuid", idTurnoStr, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(aulaDAO, turnosDAO, disponibilidadDAO);
    }

    @Test
    void createDisponibilidad_ConExcepcionEnDAO_Retorna500() {
        when(aulaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.createDisponibilidad(idAulaStr, idTurnoStr, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== deleteDisponibilidad (DELETE /aulas/{idAula}/disponibilidad/{idTurno}) ====================

    @Test
    void deleteDisponibilidad_ConDisponibilidadExistente_Retorna204() {
        DisponibilidadAulaTurno existente = new DisponibilidadAulaTurno();
        when(disponibilidadDAO.leer(any(DisponibilidadAulaTurnoId.class))).thenReturn(existente);

        Response response = resource.deleteDisponibilidad(idAulaStr, idTurnoStr);

        assertEquals(204, response.getStatus());
        verify(disponibilidadDAO).eliminar(existente);
    }

    @Test
    void deleteDisponibilidad_ConDisponibilidadInexistente_Retorna404() {
        when(disponibilidadDAO.leer(any(DisponibilidadAulaTurnoId.class))).thenReturn(null);

        Response response = resource.deleteDisponibilidad(idAulaStr, idTurnoStr);

        assertEquals(404, response.getStatus());
        verify(disponibilidadDAO, never()).eliminar(any());
    }

    @Test
    void deleteDisponibilidad_ConIdAulaFormatoInvalido_Retorna400() {
        Response response = resource.deleteDisponibilidad("no-uuid", idTurnoStr);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(disponibilidadDAO);
    }

    @Test
    void deleteDisponibilidad_ConIdTurnoFormatoInvalido_Retorna400() {
        Response response = resource.deleteDisponibilidad(idAulaStr, "no-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(disponibilidadDAO);
    }

    @Test
    void deleteDisponibilidad_ConExcepcionEnDAO_Retorna500() {
        when(disponibilidadDAO.leer(any(DisponibilidadAulaTurnoId.class)))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteDisponibilidad(idAulaStr, idTurnoStr);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
