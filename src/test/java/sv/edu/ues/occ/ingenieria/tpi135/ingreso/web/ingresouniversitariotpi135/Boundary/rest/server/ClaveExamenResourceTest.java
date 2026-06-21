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
import java.util.Collections;
import java.util.List;
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

    private ClavesExamen entidad;
    private UUID idClave;

    @BeforeEach
    void setUp() {
        idClave = UUID.randomUUID();
        entidad = new ClavesExamen();
        entidad.setIdClaveExaman(idClave);
        entidad.setNombreClave("Clave-001");
    }

    // ==================== listClaves (GET /) ====================

    @Test
    void listClaves_SinFiltro_DebeRetornar200ConListaPaginada() {
        when(clavesExamanDAO.findRange(0, 50)).thenReturn(List.of(entidad));
        when(clavesExamanDAO.count()).thenReturn(1);

        Response response = resource.listClaves(0, 50, null);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void listClaves_ConFiltroPorPrueba_DebeRetornar200ConListaFiltrada() {
        UUID idPrueba = UUID.randomUUID();
        when(clavesExamanDAO.findByPrueba(idPrueba)).thenReturn(List.of(entidad));

        Response response = resource.listClaves(0, 50, idPrueba);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
        verify(clavesExamanDAO).findByPrueba(idPrueba);
        verify(clavesExamanDAO, never()).findRange(anyInt(), anyInt());
    }

    @Test
    void listClaves_ConFiltroPorPruebaSinResultados_DebeRetornar200ConListaVacia() {
        UUID idPrueba = UUID.randomUUID();
        when(clavesExamanDAO.findByPrueba(idPrueba)).thenReturn(Collections.emptyList());

        Response response = resource.listClaves(0, 50, idPrueba);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void listClaves_ConPaginacionEnFiltro_DebeRetornarSubLista() {
        UUID idPrueba = UUID.randomUUID();
        ClavesExamen c1 = new ClavesExamen();
        ClavesExamen c2 = new ClavesExamen();
        ClavesExamen c3 = new ClavesExamen();
        when(clavesExamanDAO.findByPrueba(idPrueba)).thenReturn(List.of(c1, c2, c3));

        Response response = resource.listClaves(1, 1, idPrueba);

        assertEquals(200, response.getStatus());
        List<?> resultado = (List<?>) response.getEntity();
        assertEquals(1, resultado.size());
        assertEquals("3", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void listClaves_ConExcepcionEnDAO_DebeRetornar500() {
        when(clavesExamanDAO.findRange(anyInt(), anyInt())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listClaves(0, 50, null);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getClave (GET /{idClave}) ====================

    @Test
    void getClave_ConIdExistente_DebeRetornar200() {
        when(clavesExamanDAO.leer(idClave)).thenReturn(entidad);

        Response response = resource.getClave(idClave.toString());

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
    }

    @Test
    void getClave_ConIdInexistente_DebeRetornar404() {
        when(clavesExamanDAO.leer(idClave)).thenReturn(null);

        Response response = resource.getClave(idClave.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void getClave_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getClave("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void getClave_ConExcepcionEnDAO_DebeRetornar500() {
        when(clavesExamanDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getClave(idClave.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== crearClave (POST /) ====================

    @Test
    void crearClave_ConDatosValidos_DebeRetornar201() {
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
        assertNotNull(response.getEntity());
        verify(clavesExamanDAO).crear(nueva);
    }

    @Test
    void crearClave_ConPayloadNulo_DebeRetornar400() {
        Response response = resource.crearClave(null, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void crearClave_SinNombreClave_DebeRetornar400() {
        ClavesExamen sinNombre = new ClavesExamen();
        sinNombre.setPruebaAdmision(new PruebasAdmision());

        Response response = resource.crearClave(sinNombre, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void crearClave_ConNombreClaveEnBlanco_DebeRetornar400() {
        ClavesExamen conBlanco = new ClavesExamen();
        conBlanco.setNombreClave("   ");
        conBlanco.setPruebaAdmision(new PruebasAdmision());

        Response response = resource.crearClave(conBlanco, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void crearClave_SinPruebaAdmision_DebeRetornar400() {
        ClavesExamen sinPrueba = new ClavesExamen();
        sinPrueba.setNombreClave("Clave");

        Response response = resource.crearClave(sinPrueba, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void crearClave_ConNombreDuplicado_DebeRetornar409() {
        ClavesExamen nueva = new ClavesExamen();
        nueva.setNombreClave("Clave-001");
        nueva.setPruebaAdmision(new PruebasAdmision());
        doThrow(new IllegalArgumentException("Ya existe una clave con ese nombre"))
                .when(clavesExamanDAO).crear(any());

        Response response = resource.crearClave(nueva, uriInfo);

        assertEquals(409, response.getStatus());
    }

    @Test
    void crearClave_ConExcepcionEnDAO_DebeRetornar500() {
        ClavesExamen nueva = new ClavesExamen();
        nueva.setNombreClave("Clave-X");
        nueva.setPruebaAdmision(new PruebasAdmision());
        doThrow(new RuntimeException("Error de BD"))
                .when(clavesExamanDAO).crear(any());

        Response response = resource.crearClave(nueva, uriInfo);

        assertEquals(500, response.getStatus());
    }

    // ==================== updateClave (PUT /{idClave}) ====================

    @Test
    void updateClave_ConDatosValidos_DebeRetornar200() {
        ClavesExamen datos = new ClavesExamen();
        datos.setNombreClave("Clave Actualizada");
        when(clavesExamanDAO.leer(idClave)).thenReturn(entidad);
        when(clavesExamanDAO.actualizar(entidad)).thenReturn(entidad);

        Response response = resource.updateClave(idClave.toString(), datos);

        assertEquals(200, response.getStatus());
        verify(clavesExamanDAO).actualizar(entidad);
        assertEquals("Clave Actualizada", entidad.getNombreClave());
    }

    @Test
    void updateClave_ConPayloadNulo_DebeRetornar400() {
        Response response = resource.updateClave(idClave.toString(), null);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void updateClave_SinNombreClave_DebeRetornar400() {
        ClavesExamen sinNombre = new ClavesExamen();

        Response response = resource.updateClave(idClave.toString(), sinNombre);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void updateClave_ConNombreEnBlanco_DebeRetornar400() {
        ClavesExamen conBlanco = new ClavesExamen();
        conBlanco.setNombreClave("   ");

        Response response = resource.updateClave(idClave.toString(), conBlanco);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void updateClave_ConIdInexistente_DebeRetornar404() {
        ClavesExamen datos = new ClavesExamen();
        datos.setNombreClave("Nuevo Nombre");
        when(clavesExamanDAO.leer(idClave)).thenReturn(null);

        Response response = resource.updateClave(idClave.toString(), datos);

        assertEquals(404, response.getStatus());
        verify(clavesExamanDAO, never()).actualizar(any());
    }

    @Test
    void updateClave_ConUuidInvalido_DebeRetornar409() {
        ClavesExamen datos = new ClavesExamen();
        datos.setNombreClave("Nuevo Nombre");

        Response response = resource.updateClave("no-es-uuid", datos);

        assertEquals(409, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void updateClave_ConIllegalArgumentEnActualizar_DebeRetornar409() {
        ClavesExamen datos = new ClavesExamen();
        datos.setNombreClave("Duplicada");
        when(clavesExamanDAO.leer(idClave)).thenReturn(entidad);
        when(clavesExamanDAO.actualizar(entidad))
                .thenThrow(new IllegalArgumentException("Nombre duplicado"));

        Response response = resource.updateClave(idClave.toString(), datos);

        assertEquals(409, response.getStatus());
    }

    @Test
    void updateClave_ConExcepcionEnLeer_DebeRetornar500() {
        ClavesExamen datos = new ClavesExamen();
        datos.setNombreClave("Nuevo");
        when(clavesExamanDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateClave(idClave.toString(), datos);

        assertEquals(500, response.getStatus());
    }

    @Test
    void updateClave_ConExcepcionEnActualizar_DebeRetornar500() {
        ClavesExamen datos = new ClavesExamen();
        datos.setNombreClave("Nuevo");
        when(clavesExamanDAO.leer(idClave)).thenReturn(entidad);
        when(clavesExamanDAO.actualizar(entidad)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateClave(idClave.toString(), datos);

        assertEquals(500, response.getStatus());
    }

    // ==================== deleteClave (DELETE /{idClave}) ====================

    @Test
    void deleteClave_SinExamenesAsociados_DebeRetornar204() {
        when(clavesExamanDAO.leer(idClave)).thenReturn(entidad);
        when(examenRealizadoDAO.countByClaveExamen(idClave)).thenReturn(0L);

        Response response = resource.deleteClave(idClave.toString());

        assertEquals(204, response.getStatus());
        verify(clavesExamanDAO).eliminar(entidad);
    }

    @Test
    void deleteClave_ConExamenesAsociados_DebeRetornar409() {
        when(clavesExamanDAO.leer(idClave)).thenReturn(entidad);
        when(examenRealizadoDAO.countByClaveExamen(idClave)).thenReturn(3L);

        Response response = resource.deleteClave(idClave.toString());

        assertEquals(409, response.getStatus());
        verify(clavesExamanDAO, never()).eliminar(any());
    }

    @Test
    void deleteClave_ConIdInexistente_DebeRetornar404() {
        when(clavesExamanDAO.leer(idClave)).thenReturn(null);

        Response response = resource.deleteClave(idClave.toString());

        assertEquals(404, response.getStatus());
        verify(clavesExamanDAO, never()).eliminar(any());
    }

    @Test
    void deleteClave_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.deleteClave("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(clavesExamanDAO);
    }

    @Test
    void deleteClave_ConExcepcionEnDAO_DebeRetornar500() {
        when(clavesExamanDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteClave(idClave.toString());

        assertEquals(500, response.getStatus());
    }
}
