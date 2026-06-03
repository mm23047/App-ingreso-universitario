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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PruebasAdmisionResourceTest {

    @Mock
    private PruebasAdmisionDAO pruebasAdmisionDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private PruebasAdmisionResource resource;

    private PruebasAdmision entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidad = new PruebasAdmision();
        entidad.setIdPruebaAdmision(testId);
        entidad.setNombrePrueba("Prueba 2026");
        entidad.setAnio(2026);
        entidad.setActiva(true);
    }

    // ==================== listPruebas (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(pruebasAdmisionDAO.count()).thenReturn(1);
        when(pruebasAdmisionDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listPruebas(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(pruebasAdmisionDAO).count();
        verify(pruebasAdmisionDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(pruebasAdmisionDAO.count()).thenReturn(0);
        when(pruebasAdmisionDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listPruebas(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(pruebasAdmisionDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listPruebas(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== getPrueba (GET /{idPrueba}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getPrueba(testId.toString());

        assertEquals(200, response.getStatus());
        PruebasAdmision resultado = (PruebasAdmision) response.getEntity();
        assertEquals(testId, resultado.getIdPruebaAdmision());
        assertEquals("Prueba 2026", resultado.getNombrePrueba());
        verify(pruebasAdmisionDAO).leer(testId);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(null);

        Response response = resource.getPrueba(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.getPrueba("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(pruebasAdmisionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getPrueba(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== createPrueba (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        PruebasAdmision nueva = new PruebasAdmision();
        nueva.setNombrePrueba("Prueba 2027");
        nueva.setAnio(2027);
        nueva.setActiva(false);

        doAnswer(inv -> {
            PruebasAdmision p = inv.getArgument(0);
            p.setIdPruebaAdmision(UUID.randomUUID());
            return null;
        }).when(pruebasAdmisionDAO).crear(any(PruebasAdmision.class));

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/pruebas_admision/1"));

        Response response = resource.createPrueba(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(pruebasAdmisionDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar400() {
        Response response = resource.createPrueba(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void create_SinNombrePrueba_DebeRetornar400() {
        PruebasAdmision sinNombre = new PruebasAdmision();
        sinNombre.setAnio(2027);

        Response response = resource.createPrueba(sinNombre, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        PruebasAdmision nueva = new PruebasAdmision();
        nueva.setNombrePrueba("Prueba 2027");
        nueva.setAnio(2027);
        doThrow(new RuntimeException("Error de BD")).when(pruebasAdmisionDAO).crear(any());

        Response response = resource.createPrueba(nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== updatePrueba (PUT /{idPrueba}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);
        PruebasAdmision actualizada = new PruebasAdmision();
        actualizada.setNombrePrueba("Prueba 2026 Actualizada");

        Response response = resource.updatePrueba(testId.toString(), actualizada);

        assertEquals(200, response.getStatus());
        verify(pruebasAdmisionDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdFormatoInvalido_DebeRetornar409() {
        // updatePrueba captura IAE (UUID inválido) → CONFLICT 409
        Response response = resource.updatePrueba("no-es-uuid", entidad);

        assertEquals(409, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(null);

        Response response = resource.updatePrueba(testId.toString(), entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(pruebasAdmisionDAO.leer(testId)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updatePrueba(testId.toString(), entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== deletePrueba (DELETE /{idPrueba}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.deletePrueba(testId.toString());

        assertEquals(204, response.getStatus());
        verify(pruebasAdmisionDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.deletePrueba("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(pruebasAdmisionDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(pruebasAdmisionDAO.leer(testId)).thenReturn(null);

        Response response = resource.deletePrueba(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(pruebasAdmisionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deletePrueba(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
