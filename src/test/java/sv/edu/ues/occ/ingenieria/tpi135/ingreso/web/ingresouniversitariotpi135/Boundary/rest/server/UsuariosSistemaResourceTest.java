package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.UsuariosSistemaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD - Fase RED: estas pruebas definen el contrato de UsuariosSistemaResource
 * antes de que exista la implementación.
 */
@ExtendWith(MockitoExtension.class)
class UsuariosSistemaResourceTest {

    @Mock private UsuariosSistemaDAO usuariosSistemaDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private UsuariosSistemaResource resource;
    private UsuariosSistema entidad;

    @BeforeEach
    void setUp() {
        resource = new UsuariosSistemaResource();
        resource.usuariosSistemaDAO = usuariosSistemaDAO;

        entidad = new UsuariosSistema();
        entidad.setId(1);
        entidad.setNombreUsuario("admin");
        entidad.setCorreo("admin@ues.edu.sv");
        entidad.setContrasenaHash("hash123");
        entidad.setRol("ADMIN");
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(usuariosSistemaDAO.count()).thenReturn(1);
        when(usuariosSistemaDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(usuariosSistemaDAO).count();
        verify(usuariosSistemaDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(usuariosSistemaDAO.count()).thenReturn(0);
        when(usuariosSistemaDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(usuariosSistemaDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findRange(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(usuariosSistemaDAO.leer(1)).thenReturn(entidad);

        Response response = resource.findById(1);

        assertEquals(200, response.getStatus());
        UsuariosSistema resultado = (UsuariosSistema) response.getEntity();
        assertEquals(1, resultado.getId());
        assertEquals("admin", resultado.getNombreUsuario());
        verify(usuariosSistemaDAO).leer(1);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(usuariosSistemaDAO.leer(999)).thenReturn(null);

        Response response = resource.findById(999);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        Response response = resource.findById(null);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(usuariosSistemaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.findById(1);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        UsuariosSistema nuevo = new UsuariosSistema();
        nuevo.setNombreUsuario("operador");
        nuevo.setCorreo("operador@ues.edu.sv");
        nuevo.setContrasenaHash("hashOp");
        nuevo.setRol("OPERADOR");
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/usuarios_sistema/2"));

        Response response = resource.create(nuevo, uriInfo);

        assertEquals(201, response.getStatus());
        verify(usuariosSistemaDAO).crear(nuevo);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void create_ConEntidadConIdYaAsignado_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        UsuariosSistema nuevo = new UsuariosSistema();
        nuevo.setNombreUsuario("operador");
        nuevo.setCorreo("operador@ues.edu.sv");
        nuevo.setContrasenaHash("hashOp");
        nuevo.setRol("OPERADOR");
        doThrow(new RuntimeException("Error de BD")).when(usuariosSistemaDAO).crear(any());

        Response response = resource.create(nuevo, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== update (PUT /{id}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(usuariosSistemaDAO.leer(1)).thenReturn(entidad);
        UsuariosSistema actualizado = new UsuariosSistema();
        actualizado.setNombreUsuario("admin_v2");
        actualizado.setCorreo("admin2@ues.edu.sv");
        actualizado.setContrasenaHash("newHash");
        actualizado.setRol("ADMIN");

        Response response = resource.update(1, actualizado);

        assertEquals(200, response.getStatus());
        verify(usuariosSistemaDAO).actualizar(actualizado);
    }

    @Test
    void update_ConIdNulo_DebeRetornar422() {
        Response response = resource.update(null, entidad);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(1, null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(usuariosSistemaDAO.leer(999)).thenReturn(null);

        Response response = resource.update(999, entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(usuariosSistemaDAO.leer(1)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.update(1, entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== delete (DELETE /{id}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(usuariosSistemaDAO.leer(1)).thenReturn(entidad);

        Response response = resource.delete(1);

        assertEquals(204, response.getStatus());
        verify(usuariosSistemaDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() {
        Response response = resource.delete(null);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(usuariosSistemaDAO.leer(999)).thenReturn(null);

        Response response = resource.delete(999);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(usuariosSistemaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.delete(1);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
