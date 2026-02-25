package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AulasExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD - Fase RED: estas pruebas definen el contrato de AulasExamanResource
 * antes de que exista la implementación.
 */
@ExtendWith(MockitoExtension.class)
class AulasExamanResourceTest {

    @Mock private AulasExamanDAO aulasExamanDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private AulasExamanResource resource;
    private AulasExaman entidad;

    @BeforeEach
    void setUp() {
        resource = new AulasExamanResource();
        resource.aulasExamanDAO = aulasExamanDAO;

        entidad = new AulasExaman();
        entidad.setId(5);
        entidad.setIdAulaApi("AULA-01");
        entidad.setCapacidad(30);
        entidad.setCuposOcupados(0);
        entidad.setAccesibleSillaRuedas(false);
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(aulasExamanDAO.count()).thenReturn(1);
        when(aulasExamanDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(aulasExamanDAO).count();
        verify(aulasExamanDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(aulasExamanDAO.count()).thenReturn(0);
        when(aulasExamanDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(aulasExamanDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(aulasExamanDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(aulasExamanDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(aulasExamanDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findRange(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(aulasExamanDAO.leer(5)).thenReturn(entidad);

        Response response = resource.findById(5);

        assertEquals(200, response.getStatus());
        AulasExaman resultado = (AulasExaman) response.getEntity();
        assertEquals(5, resultado.getId());
        verify(aulasExamanDAO).leer(5);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(aulasExamanDAO.leer(999)).thenReturn(null);

        Response response = resource.findById(999);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        Response response = resource.findById(null);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(aulasExamanDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(aulasExamanDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findById(5);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        AulasExaman nueva = new AulasExaman();
        nueva.setIdAulaApi("AULA-02");
        nueva.setCapacidad(25);
        nueva.setCuposOcupados(0);
        nueva.setAccesibleSillaRuedas(true);
        TurnosExaman turno = new TurnosExaman();
        turno.setId(1);
        nueva.setIdTurno(turno);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/aulas/1"));

        Response response = resource.create(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(aulasExamanDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(aulasExamanDAO);
    }

    @Test
    void create_ConEntidadConIdYaAsignado_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(aulasExamanDAO);
    }

    @Test
    void create_ConIdTurnoNulo_DebeRetornar422() {
        AulasExaman sinTurno = new AulasExaman();
        sinTurno.setIdAulaApi("AULA-03");
        sinTurno.setCapacidad(20);
        sinTurno.setAccesibleSillaRuedas(false);
        // idTurno es null → FK NOT NULL en BD

        Response response = resource.create(sinTurno, uriInfo);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(aulasExamanDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        AulasExaman nueva = new AulasExaman();
        nueva.setIdAulaApi("AULA-02");
        nueva.setCapacidad(25);
        nueva.setAccesibleSillaRuedas(false);
        TurnosExaman turno = new TurnosExaman();
        turno.setId(1);
        nueva.setIdTurno(turno);
        doThrow(new RuntimeException("Error de BD")).when(aulasExamanDAO).crear(any());

        Response response = resource.create(nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== update (PUT /{id}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(aulasExamanDAO.leer(5)).thenReturn(entidad);
        AulasExaman actualizada = new AulasExaman();
        actualizada.setIdAulaApi("AULA-01-MOD");
        actualizada.setCapacidad(35);
        actualizada.setAccesibleSillaRuedas(true);

        Response response = resource.update(5, actualizada);

        assertEquals(200, response.getStatus());
        verify(aulasExamanDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdNulo_DebeRetornar422() {
        Response response = resource.update(null, entidad);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(aulasExamanDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(5, null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(aulasExamanDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(aulasExamanDAO.leer(999)).thenReturn(null);

        Response response = resource.update(999, entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(aulasExamanDAO.leer(5)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.update(5, entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== delete (DELETE /{id}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(aulasExamanDAO.leer(5)).thenReturn(entidad);

        Response response = resource.delete(5);

        assertEquals(204, response.getStatus());
        verify(aulasExamanDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() {
        Response response = resource.delete(null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(aulasExamanDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(aulasExamanDAO.leer(999)).thenReturn(null);

        Response response = resource.delete(999);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(aulasExamanDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.delete(5);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
