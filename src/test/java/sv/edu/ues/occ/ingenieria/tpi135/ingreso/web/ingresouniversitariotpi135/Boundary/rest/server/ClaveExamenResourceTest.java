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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaveExamenResourceTest {

    @Mock private ClavesExamanDAO clavesExamanDAO;
    @Mock private ExamenRealizadoDAO examenRealizadoDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;
    @InjectMocks private ClaveExamenResource resource;

    private ClavesExamen testClave;
    private UUID idClave;
    private String idClaveStr;

    @BeforeEach
    void setUp() {
        idClave = UUID.randomUUID();
        idClaveStr = idClave.toString();
        testClave = new ClavesExamen();
        testClave.setIdClaveExaman(idClave);
        testClave.setNombreClave("Clave-001");
    }

    // ==================== getClave (GET /{idClave}) ====================

    @Test
    void getClave_ConIdExistente_Retorna200() {
        when(clavesExamanDAO.leer(idClave)).thenReturn(testClave);

        Response response = resource.getClave(idClaveStr);

        assertEquals(200, response.getStatus());
        assertSame(testClave, response.getEntity());
    }

    @Test
    void getClave_ConIdInexistente_Retorna404() {
        when(clavesExamanDAO.leer(idClave)).thenReturn(null);

        Response response = resource.getClave(idClaveStr);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void getClave_ConIdFormatoInvalido_Retorna400() {
        Response response = resource.getClave("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    // ==================== crearClave (POST /) ====================

    @Test
    void crearClave_ConDatosValidos_Retorna201() {
        ClavesExamen nueva = new ClavesExamen();
        nueva.setNombreClave("Nueva-Clave");
        nueva.setPruebaAdmision(new PruebasAdmision());

        doAnswer(inv -> {
            ClavesExamen c = inv.getArgument(0);
            c.setIdClaveExaman(UUID.randomUUID());
            return null;
        }).when(clavesExamanDAO).crear(any(ClavesExamen.class));
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/claves/1"));

        Response response = resource.crearClave(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(clavesExamanDAO).crear(nueva);
    }

    @Test
    void crearClave_SinPruebaAdmision_Retorna400() {
        ClavesExamen sinPrueba = new ClavesExamen();
        sinPrueba.setNombreClave("Clave");
        // pruebaAdmision null

        Response response = resource.crearClave(sinPrueba, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void crearClave_SinNombreClave_Retorna400() {
        ClavesExamen sinNombre = new ClavesExamen();
        sinNombre.setPruebaAdmision(new PruebasAdmision());

        Response response = resource.crearClave(sinNombre, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    // ==================== updateClave (PUT /{idClave}) ====================

    @Test
    void updateClave_ConDatosValidos_Retorna200() {
        ClavesExamen datosEntrantes = new ClavesExamen();
        datosEntrantes.setNombreClave("Clave Actualizada");

        when(clavesExamanDAO.leer(idClave)).thenReturn(testClave);
        when(clavesExamanDAO.actualizar(testClave)).thenReturn(testClave);

        Response response = resource.updateClave(idClaveStr, datosEntrantes);

        assertEquals(200, response.getStatus());
        verify(clavesExamanDAO).actualizar(testClave);
        assertEquals("Clave Actualizada", testClave.getNombreClave());
    }

    @Test
    void updateClave_SinNombreClave_Retorna400() {
        ClavesExamen sinNombre = new ClavesExamen();

        Response response = resource.updateClave(idClaveStr, sinNombre);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void updateClave_ConIdInexistente_Retorna404() {
        ClavesExamen datosEntrantes = new ClavesExamen();
        datosEntrantes.setNombreClave("Nuevo Nombre");
        when(clavesExamanDAO.leer(idClave)).thenReturn(null);

        Response response = resource.updateClave(idClaveStr, datosEntrantes);

        assertEquals(404, response.getStatus());
    }

    // ==================== deleteClave (DELETE /{idClave}) ====================

    @Test
    void deleteClave_SinExamenesAsociados_Retorna204() {
        when(clavesExamanDAO.leer(idClave)).thenReturn(testClave);
        when(examenRealizadoDAO.countByClaveExamen(idClave)).thenReturn(0L);

        Response response = resource.deleteClave(idClaveStr);

        assertEquals(204, response.getStatus());
        verify(clavesExamanDAO).eliminar(testClave);
    }

    @Test
    void deleteClave_ConExamenesAsociados_Retorna409() {
        when(clavesExamanDAO.leer(idClave)).thenReturn(testClave);
        when(examenRealizadoDAO.countByClaveExamen(idClave)).thenReturn(3L);

        Response response = resource.deleteClave(idClaveStr);

        assertEquals(409, response.getStatus());
        verify(clavesExamanDAO, never()).eliminar(any());
    }

    @Test
    void deleteClave_ConIdInexistente_Retorna404() {
        when(clavesExamanDAO.leer(idClave)).thenReturn(null);

        Response response = resource.deleteClave(idClaveStr);

        assertEquals(404, response.getStatus());
        verify(clavesExamanDAO, never()).eliminar(any());
    }
}
