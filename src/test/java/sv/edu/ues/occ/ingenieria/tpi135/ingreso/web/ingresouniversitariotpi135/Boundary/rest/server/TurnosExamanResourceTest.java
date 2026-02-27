package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TurnosExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD - Fase RED: estas pruebas definen el contrato de TurnosExamanResource
 * antes de que exista la implementación.
 */
@ExtendWith(MockitoExtension.class)
class TurnosExamanResourceTest {

    @Mock private TurnosExamanDAO turnosExamanDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private TurnosExamanResource resource;
    private TurnosExaman entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        resource = new TurnosExamanResource();
        resource.turnosExamanDAO = turnosExamanDAO;

        entidad = new TurnosExaman();
        entidad.setId(testId);
        entidad.setNombreTurno("Turno Mañana");
        entidad.setFecha(LocalDate.of(2026, 3, 1));
        entidad.setHoraInicio(LocalTime.of(8, 0));
        entidad.setHoraFin(LocalTime.of(12, 0));
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(turnosExamanDAO.count()).thenReturn(1);
        when(turnosExamanDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(turnosExamanDAO).count();
        verify(turnosExamanDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(turnosExamanDAO.count()).thenReturn(0);
        when(turnosExamanDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(turnosExamanDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(turnosExamanDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(turnosExamanDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(turnosExamanDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findRange(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(turnosExamanDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.findById(testId);

        assertEquals(200, response.getStatus());
        TurnosExaman resultado = (TurnosExaman) response.getEntity();
        assertEquals(testId, resultado.getId());
        verify(turnosExamanDAO).leer(testId);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(turnosExamanDAO.leer(testId)).thenReturn(null);

        Response response = resource.findById(testId);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        Response response = resource.findById(null);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(turnosExamanDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(turnosExamanDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findById(testId);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        TurnosExaman nuevo = new TurnosExaman();
        nuevo.setNombreTurno("Turno Tarde");
        nuevo.setFecha(LocalDate.of(2026, 4, 1));
        nuevo.setHoraInicio(LocalTime.of(13, 0));
        nuevo.setHoraFin(LocalTime.of(17, 0));
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/turnos_examen/1"));

        Response response = resource.create(nuevo, uriInfo);

        assertEquals(201, response.getStatus());
        verify(turnosExamanDAO).crear(nuevo);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(turnosExamanDAO);
    }

    @Test
    void create_ConEntidadConIdYaAsignado_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(turnosExamanDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        TurnosExaman nuevo = new TurnosExaman();
        nuevo.setNombreTurno("Turno Tarde");
        nuevo.setFecha(LocalDate.of(2026, 4, 1));
        nuevo.setHoraInicio(LocalTime.of(13, 0));
        nuevo.setHoraFin(LocalTime.of(17, 0));
        doThrow(new RuntimeException("Error de BD")).when(turnosExamanDAO).crear(any());

        Response response = resource.create(nuevo, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== update (PUT /{id}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(turnosExamanDAO.leer(testId)).thenReturn(entidad);
        TurnosExaman actualizado = new TurnosExaman();
        actualizado.setNombreTurno("Turno Mañana Actualizado");
        actualizado.setFecha(LocalDate.of(2026, 3, 15));
        actualizado.setHoraInicio(LocalTime.of(7, 30));
        actualizado.setHoraFin(LocalTime.of(11, 30));

        Response response = resource.update(testId, actualizado);

        assertEquals(200, response.getStatus());
        verify(turnosExamanDAO).actualizar(actualizado);
    }

    @Test
    void update_ConIdNulo_DebeRetornar422() {
        Response response = resource.update(null, entidad);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(turnosExamanDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(testId, null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(turnosExamanDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(turnosExamanDAO.leer(testId)).thenReturn(null);

        Response response = resource.update(testId, entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(turnosExamanDAO.leer(testId)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.update(testId, entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== delete (DELETE /{id}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(turnosExamanDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.delete(testId);

        assertEquals(204, response.getStatus());
        verify(turnosExamanDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() {
        Response response = resource.delete(null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(turnosExamanDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(turnosExamanDAO.leer(testId)).thenReturn(null);

        Response response = resource.delete(testId);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(turnosExamanDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.delete(testId);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
