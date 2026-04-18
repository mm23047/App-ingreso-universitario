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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuariosSistemaResourceTest {

    @Mock private UsuariosSistemaDAO usuariosSistemaDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private UsuariosSistemaResource resource;
    private UsuariosSistema entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        resource = new UsuariosSistemaResource();
        resource.usuariosSistemaDAO = usuariosSistemaDAO;

        entidad = new UsuariosSistema();
        entidad.setId(testId);
        entidad.setNombreUsuario("admin");
        entidad.setCorreo("admin@ues.edu.sv");
        entidad.setContrasenaHash("$2a$10$hash");
        entidad.setRol("ADMIN");
    }

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
    void findRange_ConParametrosInvalidos_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200() {
        when(usuariosSistemaDAO.count()).thenReturn(0);
        when(usuariosSistemaDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        when(usuariosSistemaDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.findById(testId);

        assertEquals(200, response.getStatus());
        UsuariosSistema resultado = (UsuariosSistema) response.getEntity();
        assertEquals(testId, resultado.getId());
        assertEquals("admin", resultado.getNombreUsuario());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(usuariosSistemaDAO.leer(testId)).thenReturn(null);

        Response response = resource.findById(testId);

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

        Response response = resource.findById(testId);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        UsuariosSistema nuevo = new UsuariosSistema();
        nuevo.setNombreUsuario("jperez");
        nuevo.setCorreo("jperez@correo.com");
        nuevo.setContrasenaHash("$2a$10$hash");
        nuevo.setRol("ASPIRANTE");

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/usuarios/1"));

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
    void create_ConEntidadConId_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        UsuariosSistema nuevo = new UsuariosSistema();
        nuevo.setNombreUsuario("jperez");
        nuevo.setCorreo("jperez@correo.com");
        nuevo.setContrasenaHash("$2a$10$hash");
        nuevo.setRol("ASPIRANTE");

        doThrow(new RuntimeException("Error de BD")).when(usuariosSistemaDAO).crear(any());

        Response response = resource.create(nuevo, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(usuariosSistemaDAO.leer(testId)).thenReturn(entidad);

        UsuariosSistema actualizada = new UsuariosSistema();
        actualizada.setNombreUsuario("admin2");
        actualizada.setCorreo("admin2@ues.edu.sv");
        actualizada.setContrasenaHash("$2a$10$hash");
        actualizada.setRol("ADMIN");

        Response response = resource.update(testId, actualizada);

        assertEquals(200, response.getStatus());
        verify(usuariosSistemaDAO).actualizar(actualizada);
        assertEquals(testId, actualizada.getId());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(usuariosSistemaDAO.leer(testId)).thenReturn(null);

        Response response = resource.update(testId, entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(usuariosSistemaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        UsuariosSistema actualizada = new UsuariosSistema();
        actualizada.setNombreUsuario("admin2");
        actualizada.setCorreo("admin2@ues.edu.sv");
        actualizada.setContrasenaHash("$2a$10$hash");
        actualizada.setRol("ADMIN");

        Response response = resource.update(testId, actualizada);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    @Test
    void update_ConParametrosNulos_DebeRetornar422() {
        Response response = resource.update(null, entidad);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(usuariosSistemaDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.delete(testId);

        assertEquals(204, response.getStatus());
        verify(usuariosSistemaDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(usuariosSistemaDAO.leer(testId)).thenReturn(null);

        Response response = resource.delete(testId);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
        verify(usuariosSistemaDAO, never()).eliminar(any());
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() {
        Response response = resource.delete(null);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(usuariosSistemaDAO);
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(usuariosSistemaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.delete(testId);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
