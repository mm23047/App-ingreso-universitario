package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.EtapasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD - Pruebas unitarias para EtapasAdmisionResource.
 * Las pruebas definen el comportamiento esperado del recurso REST
 * antes y durante su implementación.
 */
@ExtendWith(MockitoExtension.class)
class EtapasAdmisionResourceTest {

    @Mock
    private EtapasAdmisionDAO etapasAdmisionDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private EtapasAdmisionResource resource;
    private EtapasAdmision entidadPrueba;

    @BeforeEach
    void setUp() {
        resource = new EtapasAdmisionResource();
        resource.etapasAdmisionDAO = etapasAdmisionDAO;

        entidadPrueba = new EtapasAdmision();
        entidadPrueba.setId((short) 1);
        entidadPrueba.setNombre("Etapa Preuniversitaria");
        entidadPrueba.setPuntajeMinimo(new BigDecimal("60.00"));
        entidadPrueba.setPuntajeMaximo(new BigDecimal("100.00"));
        entidadPrueba.setDescripcion("Primera etapa del proceso de admisión");
    }

    // ==================== PRUEBAS PARA findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() throws Exception {
        // Arrange
        List<EtapasAdmision> lista = Arrays.asList(entidadPrueba);
        when(etapasAdmisionDAO.count()).thenReturn(1);
        when(etapasAdmisionDAO.findRange(0, 10)).thenReturn(lista);

        // Act
        Response response = resource.findRange(0, 10);

        // Assert
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(etapasAdmisionDAO, times(1)).count();
        verify(etapasAdmisionDAO, times(1)).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() throws Exception {
        // Arrange
        when(etapasAdmisionDAO.count()).thenReturn(0);
        when(etapasAdmisionDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        // Act
        Response response = resource.findRange(0, 10);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        // Act
        Response response = resource.findRange(-1, 10);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        // Act
        Response response = resource.findRange(0, 0);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        // Act
        Response response = resource.findRange(0, 101);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() throws Exception {
        // Arrange
        when(etapasAdmisionDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = resource.findRange(0, 10);

        // Assert
        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== PRUEBAS PARA findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        // Arrange
        when(etapasAdmisionDAO.leer((short) 1)).thenReturn(entidadPrueba);

        // Act
        Response response = resource.findById((short) 1);

        // Assert
        assertEquals(200, response.getStatus());
        EtapasAdmision resultado = (EtapasAdmision) response.getEntity();
        assertEquals(entidadPrueba.getId(), resultado.getId());
        assertEquals(entidadPrueba.getNombre(), resultado.getNombre());
        verify(etapasAdmisionDAO, times(1)).leer((short) 1);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        // Arrange
        when(etapasAdmisionDAO.leer((short) 999)).thenReturn(null);

        // Act
        Response response = resource.findById((short) 999);

        // Assert
        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        // Act
        Response response = resource.findById(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        // Arrange
        when(etapasAdmisionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = resource.findById((short) 1);

        // Assert
        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== PRUEBAS PARA create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() throws Exception {
        // Arrange
        EtapasAdmision nueva = new EtapasAdmision();
        nueva.setNombre("Nueva Etapa");
        // id null → la BD genera el id automáticamente
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/etapas/1"));

        // Act
        Response response = resource.create(nueva, uriInfo);

        // Assert
        assertEquals(201, response.getStatus());
        verify(etapasAdmisionDAO, times(1)).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() throws Exception {
        // Act
        Response response = resource.create(null, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void create_ConEntidadConIdYaAsignado_DebeRetornar422() throws Exception {
        // Arrange - el cliente no debe proveer id en POST (la BD lo genera)
        EtapasAdmision conId = new EtapasAdmision();
        conId.setId((short) 5);
        conId.setNombre("Etapa con ID");

        // Act
        Response response = resource.create(conId, uriInfo);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() throws Exception {
        // Arrange
        EtapasAdmision nueva = new EtapasAdmision();
        nueva.setNombre("Nueva Etapa");
        doThrow(new RuntimeException("Error de BD")).when(etapasAdmisionDAO).crear(any());

        // Act
        Response response = resource.create(nueva, uriInfo);

        // Assert
        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== PRUEBAS PARA update (PUT /{id}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() throws Exception {
        // Arrange
        when(etapasAdmisionDAO.leer((short) 1)).thenReturn(entidadPrueba);
        when(etapasAdmisionDAO.actualizar(any())).thenReturn(entidadPrueba);

        EtapasAdmision actualizada = new EtapasAdmision();
        actualizada.setNombre("Nombre Actualizado");

        // Act
        Response response = resource.update((short) 1, actualizada);

        // Assert
        assertEquals(200, response.getStatus());
        verify(etapasAdmisionDAO, times(1)).actualizar(actualizada);
    }

    @Test
    void update_ConIdNulo_DebeRetornar422() throws Exception {
        // Act
        Response response = resource.update(null, entidadPrueba);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() throws Exception {
        // Act
        Response response = resource.update((short) 1, null);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() throws Exception {
        // Arrange
        when(etapasAdmisionDAO.leer((short) 999)).thenReturn(null);

        // Act
        Response response = resource.update((short) 999, entidadPrueba);

        // Assert
        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() throws Exception {
        // Arrange
        when(etapasAdmisionDAO.leer((short) 1)).thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = resource.update((short) 1, entidadPrueba);

        // Assert
        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== PRUEBAS PARA delete (DELETE /{id}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() throws Exception {
        // Arrange
        when(etapasAdmisionDAO.leer((short) 1)).thenReturn(entidadPrueba);

        // Act
        Response response = resource.delete((short) 1);

        // Assert
        assertEquals(204, response.getStatus());
        verify(etapasAdmisionDAO, times(1)).eliminar(entidadPrueba);
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() throws Exception {
        // Act
        Response response = resource.delete(null);

        // Assert
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(etapasAdmisionDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() throws Exception {
        // Arrange
        when(etapasAdmisionDAO.leer((short) 999)).thenReturn(null);

        // Act
        Response response = resource.delete((short) 999);

        // Assert
        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() throws Exception {
        // Arrange
        when(etapasAdmisionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        // Act
        Response response = resource.delete((short) 1);

        // Assert
        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
