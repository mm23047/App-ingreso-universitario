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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.EtapasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtapasAdmisionResourceTest {

    @Mock
    private EtapasAdmisionDAO etapasAdmisionDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private EtapasAdmisionResource resource;

    private EtapasAdmision entidadPrueba;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidadPrueba = new EtapasAdmision();
        entidadPrueba.setIdEtapaAdmision(testId);
        entidadPrueba.setNombre("Etapa Preuniversitaria");
        entidadPrueba.setPuntajeMinimo(new BigDecimal("60.00"));
        entidadPrueba.setPuntajeMaximo(new BigDecimal("100.00"));
        entidadPrueba.setDescripcion("Primera etapa del proceso de admisión");
        entidadPrueba.setCantidadPreguntasRequeridas(30);
    }

    // ==================== PRUEBAS PARA listEtapas (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(etapasAdmisionDAO.count()).thenReturn(1);
        when(etapasAdmisionDAO.findRange(0, 10)).thenReturn(Arrays.asList(entidadPrueba));

        Response response = resource.listEtapas(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(etapasAdmisionDAO).count();
        verify(etapasAdmisionDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(etapasAdmisionDAO.count()).thenReturn(0);
        when(etapasAdmisionDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listEtapas(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(etapasAdmisionDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listEtapas(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== PRUEBAS PARA getEtapa (GET /{idEtapa}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(etapasAdmisionDAO.leer(testId)).thenReturn(entidadPrueba);

        Response response = resource.getEtapa(testId.toString());

        assertEquals(200, response.getStatus());
        EtapasAdmision resultado = (EtapasAdmision) response.getEntity();
        assertEquals(testId, resultado.getIdEtapaAdmision());
        assertEquals("Etapa Preuniversitaria", resultado.getNombre());
        verify(etapasAdmisionDAO).leer(testId);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(etapasAdmisionDAO.leer(testId)).thenReturn(null);

        Response response = resource.getEtapa(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.getEtapa("no-es-un-uuid-valido");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(etapasAdmisionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getEtapa(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== PRUEBAS PARA createEtapa (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        EtapasAdmision nueva = new EtapasAdmision();
        nueva.setNombre("Nueva Etapa");
        nueva.setCantidadPreguntasRequeridas(20);

        doAnswer(inv -> {
            EtapasAdmision e = inv.getArgument(0);
            e.setIdEtapaAdmision(UUID.randomUUID());
            return null;
        }).when(etapasAdmisionDAO).crear(any(EtapasAdmision.class));

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/etapas/1"));

        Response response = resource.createEtapa(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(etapasAdmisionDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar400() {
        Response response = resource.createEtapa(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void create_SinNombre_DebeRetornar400() {
        EtapasAdmision sinNombre = new EtapasAdmision();
        sinNombre.setCantidadPreguntasRequeridas(20);

        Response response = resource.createEtapa(sinNombre, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void create_SinCantidadPreguntasRequeridas_DebeRetornar400() {
        EtapasAdmision sinCantidad = new EtapasAdmision();
        sinCantidad.setNombre("Etapa Sin Cantidad");

        Response response = resource.createEtapa(sinCantidad, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void create_ConCantidadPreguntasCero_DebeRetornar400() {
        EtapasAdmision conCero = new EtapasAdmision();
        conCero.setNombre("Etapa");
        conCero.setCantidadPreguntasRequeridas(0);

        Response response = resource.createEtapa(conCero, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        EtapasAdmision nueva = new EtapasAdmision();
        nueva.setNombre("Nueva Etapa");
        nueva.setCantidadPreguntasRequeridas(20);
        doThrow(new RuntimeException("Error de BD")).when(etapasAdmisionDAO).crear(any());

        Response response = resource.createEtapa(nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== PRUEBAS PARA updateEtapa (PUT /{idEtapa}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(etapasAdmisionDAO.leer(testId)).thenReturn(entidadPrueba);
        when(etapasAdmisionDAO.actualizar(any())).thenReturn(entidadPrueba);

        EtapasAdmision actualizada = new EtapasAdmision();
        actualizada.setNombre("Nombre Actualizado");

        Response response = resource.updateEtapa(testId.toString(), actualizada);

        assertEquals(200, response.getStatus());
        verify(etapasAdmisionDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdFormatoInvalido_DebeRetornar409() {
        // updateEtapa captura IllegalArgumentException (UUID inválido) → CONFLICT 409
        Response response = resource.updateEtapa("no-es-uuid", entidadPrueba);

        assertEquals(409, response.getStatus());
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(etapasAdmisionDAO.leer(testId)).thenReturn(null);

        Response response = resource.updateEtapa(testId.toString(), entidadPrueba);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(etapasAdmisionDAO.leer(testId)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateEtapa(testId.toString(), entidadPrueba);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== PRUEBAS PARA deleteEtapa (DELETE /{idEtapa}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(etapasAdmisionDAO.leer(testId)).thenReturn(entidadPrueba);

        Response response = resource.deleteEtapa(testId.toString());

        assertEquals(204, response.getStatus());
        verify(etapasAdmisionDAO).eliminar(entidadPrueba);
    }

    @Test
    void delete_ConIdFormatoInvalido_DebeRetornar500() {
        // deleteEtapa no tiene catch(IAE), solo catch(Exception) → 500
        Response response = resource.deleteEtapa("no-es-uuid");

        assertEquals(500, response.getStatus());
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(etapasAdmisionDAO.leer(testId)).thenReturn(null);

        Response response = resource.deleteEtapa(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(etapasAdmisionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteEtapa(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
