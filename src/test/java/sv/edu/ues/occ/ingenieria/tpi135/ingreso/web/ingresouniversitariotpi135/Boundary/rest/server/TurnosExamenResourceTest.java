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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TurnosExamenDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExamen;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnosExamenResourceTest {

    @Mock
    private TurnosExamenDAO turnosExamenDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private TurnosExamanResource resource;

    private TurnosExamen entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidad = new TurnosExamen();
        entidad.setIdTurnoExamen(testId);
        entidad.setNombreTurno("Turno Mañana");
        entidad.setFecha(LocalDate.of(2026, 3, 1));
        entidad.setHoraInicio(LocalTime.of(8, 0));
        entidad.setHoraFin(LocalTime.of(12, 0));
    }

    // ==================== listTurnos (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(turnosExamenDAO.count()).thenReturn(1);
        when(turnosExamenDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listTurnos(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(turnosExamenDAO).count();
        verify(turnosExamenDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(turnosExamenDAO.count()).thenReturn(0);
        when(turnosExamenDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listTurnos(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(turnosExamenDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listTurnos(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== getTurno (GET /{idTurno}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(turnosExamenDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getTurno(testId.toString());

        assertEquals(200, response.getStatus());
        TurnosExamen resultado = (TurnosExamen) response.getEntity();
        assertEquals(testId, resultado.getIdTurnoExamen());
        verify(turnosExamenDAO).leer(testId);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(turnosExamenDAO.leer(testId)).thenReturn(null);

        Response response = resource.getTurno(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.getTurno("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(turnosExamenDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(turnosExamenDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getTurno(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== createTurno (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        TurnosExamen nuevo = new TurnosExamen();
        nuevo.setNombreTurno("Turno Tarde");
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(UUID.randomUUID());
        nuevo.setPruebaAdmision(prueba);
        nuevo.setFecha(LocalDate.of(2026, 4, 1));
        nuevo.setHoraInicio(LocalTime.of(13, 0));
        nuevo.setHoraFin(LocalTime.of(17, 0));

        doAnswer(inv -> {
            TurnosExamen t = inv.getArgument(0);
            t.setIdTurnoExamen(UUID.randomUUID());
            return null;
        }).when(turnosExamenDAO).crear(any(TurnosExamen.class));

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/turnos/1"));

        Response response = resource.createTurno(nuevo, uriInfo);

        assertEquals(201, response.getStatus());
        verify(turnosExamenDAO).crear(nuevo);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar400() {
        Response response = resource.createTurno(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(turnosExamenDAO);
    }

    @Test
    void create_SinPruebaAdmision_DebeRetornar400() {
        TurnosExamen sinPrueba = new TurnosExamen();
        sinPrueba.setNombreTurno("Turno Sin Prueba");
        // pruebaAdmision null

        Response response = resource.createTurno(sinPrueba, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(turnosExamenDAO);
    }

    @Test
    void create_SinNombreTurno_DebeRetornar400() {
        TurnosExamen sinNombre = new TurnosExamen();
        sinNombre.setPruebaAdmision(new PruebasAdmision());
        // nombreTurno null

        Response response = resource.createTurno(sinNombre, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(turnosExamenDAO);
    }

    @Test
    void create_ConHoraInicioMayorQueHoraFin_DebeRetornar400() {
        TurnosExamen horasInvertidas = new TurnosExamen();
        horasInvertidas.setNombreTurno("Turno Invertido");
        horasInvertidas.setPruebaAdmision(new PruebasAdmision());
        horasInvertidas.setHoraInicio(LocalTime.of(17, 0));
        horasInvertidas.setHoraFin(LocalTime.of(8, 0));

        Response response = resource.createTurno(horasInvertidas, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(turnosExamenDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        TurnosExamen nuevo = new TurnosExamen();
        nuevo.setNombreTurno("Turno Tarde");
        nuevo.setPruebaAdmision(new PruebasAdmision());
        doThrow(new RuntimeException("Error de BD")).when(turnosExamenDAO).crear(any());

        Response response = resource.createTurno(nuevo, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== updateTurno (PUT /{idTurno}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(turnosExamenDAO.leer(testId)).thenReturn(entidad);
        TurnosExamen actualizado = new TurnosExamen();
        actualizado.setNombreTurno("Turno Mañana Actualizado");

        Response response = resource.updateTurno(testId.toString(), actualizado);

        assertEquals(200, response.getStatus());
        verify(turnosExamenDAO).actualizar(actualizado);
    }

    @Test
    void update_ConIdFormatoInvalido_DebeRetornar409() {
        // updateTurno captura IAE (UUID inválido) → CONFLICT 409
        Response response = resource.updateTurno("no-es-uuid", entidad);

        assertEquals(409, response.getStatus());
        verifyNoInteractions(turnosExamenDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(turnosExamenDAO.leer(testId)).thenReturn(null);

        Response response = resource.updateTurno(testId.toString(), entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(turnosExamenDAO.leer(testId)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateTurno(testId.toString(), entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== deleteTurno (DELETE /{idTurno}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(turnosExamenDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.deleteTurno(testId.toString());

        assertEquals(204, response.getStatus());
        verify(turnosExamenDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdFormatoInvalido_DebeRetornar500() {
        // deleteTurno no tiene catch(IAE), solo catch(Exception) → 500
        Response response = resource.deleteTurno("no-es-uuid");

        assertEquals(500, response.getStatus());
        verifyNoInteractions(turnosExamenDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(turnosExamenDAO.leer(testId)).thenReturn(null);

        Response response = resource.deleteTurno(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(turnosExamenDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteTurno(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
