package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AspirantesDatoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.UsuariosSistemaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AspirantesDatoResourceTest {

    @Mock private AspirantesDatoDAO aspirantesDatoDAO;
    @Mock private UsuariosSistemaDAO usuariosSistemaDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private AspirantesDatoResource resource;
    private AspirantesDato entidad;
    private UUID testId;
    private UUID usuarioId;
    private UsuariosSistema usuario;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        resource = new AspirantesDatoResource();
        resource.aspirantesDatoDAO = aspirantesDatoDAO;
        resource.usuariosSistemaDAO = usuariosSistemaDAO;

        usuario = new UsuariosSistema();
        usuario.setId(usuarioId);

        entidad = new AspirantesDato();
        entidad.setId(testId);
        entidad.setIdUsuario(usuario);
        entidad.setNombres("Juan");
        entidad.setApellidos("Pérez");
        entidad.setDui("01234567-8");
        entidad.setUsaSillaRuedas(false);
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(aspirantesDatoDAO.count()).thenReturn(1);
        when(aspirantesDatoDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200() {
        when(aspirantesDatoDAO.count()).thenReturn(0);
        when(aspirantesDatoDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(aspirantesDatoDAO.count()).thenThrow(new RuntimeException("Error de BD"));
        Response response = resource.findRange(0, 10);
        assertEquals(500, response.getStatus());
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        when(aspirantesDatoDAO.leer(testId)).thenReturn(entidad);
        Response response = resource.findById(testId);
        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(aspirantesDatoDAO.leer(testId)).thenReturn(null);
        Response response = resource.findById(testId);
        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        Response response = resource.findById(null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(aspirantesDatoDAO.leer(any())).thenThrow(new RuntimeException("BD error"));
        Response response = resource.findById(testId);
        assertEquals(500, response.getStatus());
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        AspirantesDato nuevo = new AspirantesDato();
        UsuariosSistema u = new UsuariosSistema();
        u.setId(usuarioId);
        nuevo.setIdUsuario(u);
        nuevo.setNombres("Maria");
        nuevo.setApellidos("García");
        nuevo.setDui("09876543-2");
        nuevo.setUsaSillaRuedas(false);

        when(usuariosSistemaDAO.leer(usuarioId)).thenReturn(usuario);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/aspirantes/1"));

        Response response = resource.create(nuevo, uriInfo);

        assertEquals(201, response.getStatus());
        verify(usuariosSistemaDAO).leer(usuarioId);
        verify(aspirantesDatoDAO).crear(any(AspirantesDato.class));
    }

    @Test
    void create_ConUsuarioInexistente_DebeRetornar404() {
        AspirantesDato nuevo = new AspirantesDato();
        UsuariosSistema u = new UsuariosSistema();
        u.setId(usuarioId);
        nuevo.setIdUsuario(u);
        nuevo.setNombres("Maria");
        nuevo.setApellidos("García");
        nuevo.setDui("09876543-2");

        when(usuariosSistemaDAO.leer(usuarioId)).thenReturn(null);

        Response response = resource.create(nuevo, uriInfo);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
        verify(usuariosSistemaDAO).leer(usuarioId);
        verifyNoInteractions(aspirantesDatoDAO);
        verifyNoInteractions(uriInfo);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO);
    }

    @Test
    void create_ConIdYaAsignado_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO);
    }

    @Test
    void create_SinUsuario_DebeRetornar422() {
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setNombres("Ana");
        nuevo.setApellidos("López");
        nuevo.setDui("11111111-1");
        Response response = resource.create(nuevo, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO);
    }

    @Test
    void create_ConIdUsuarioSinId_DebeRetornar422() {
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setIdUsuario(new UsuariosSistema());
        nuevo.setNombres("Ana");
        nuevo.setApellidos("López");
        nuevo.setDui("11111111-1");

        Response response = resource.create(nuevo, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO, usuariosSistemaDAO, uriInfo);
    }

    @Test
    void create_SinNombres_DebeRetornar422() {
        AspirantesDato nuevo = new AspirantesDato();
        UsuariosSistema u = new UsuariosSistema();
        u.setId(usuarioId);
        nuevo.setIdUsuario(u);
        nuevo.setApellidos("López");
        nuevo.setDui("11111111-1");
        Response response = resource.create(nuevo, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO, usuariosSistemaDAO);
    }

    @Test
    void create_SinApellidos_DebeRetornar422() {
        AspirantesDato nuevo = new AspirantesDato();
        UsuariosSistema u = new UsuariosSistema();
        u.setId(usuarioId);
        nuevo.setIdUsuario(u);
        nuevo.setNombres("Carlos");
        nuevo.setDui("11111111-1");

        Response response = resource.create(nuevo, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO, usuariosSistemaDAO, uriInfo);
    }

    @Test
    void create_SinDui_DebeRetornar422() {
        AspirantesDato nuevo = new AspirantesDato();
        UsuariosSistema u = new UsuariosSistema();
        u.setId(usuarioId);
        nuevo.setIdUsuario(u);
        nuevo.setNombres("Carlos");
        nuevo.setApellidos("Mendez");
        Response response = resource.create(nuevo, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO, usuariosSistemaDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        AspirantesDato nuevo = new AspirantesDato();
        UsuariosSistema u = new UsuariosSistema();
        u.setId(usuarioId);
        nuevo.setIdUsuario(u);
        nuevo.setNombres("Pedro");
        nuevo.setApellidos("Martinez");
        nuevo.setDui("22222222-2");

        when(usuariosSistemaDAO.leer(usuarioId)).thenReturn(usuario);
        doThrow(new RuntimeException("BD error")).when(aspirantesDatoDAO).crear(any());
        Response response = resource.create(nuevo, uriInfo);
        assertEquals(500, response.getStatus());
    }

    // ==================== update (PUT /{id}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(aspirantesDatoDAO.leer(testId)).thenReturn(entidad);
        when(aspirantesDatoDAO.actualizar(any(AspirantesDato.class))).thenAnswer(inv -> inv.getArgument(0));
        AspirantesDato actualizado = new AspirantesDato();
        actualizado.setNombres("Juan Carlos");
        actualizado.setApellidos("Pérez López");
        actualizado.setDui("01234567-8");

        Response response = resource.update(testId, actualizado);

        assertEquals(200, response.getStatus());
        verify(aspirantesDatoDAO).actualizar(entidad);
    }

    @Test
    void update_ConCambioUsuarioInexistente_DebeRetornar404() {
        UUID nuevoUsuarioId = UUID.randomUUID();
        when(aspirantesDatoDAO.leer(testId)).thenReturn(entidad);
        when(usuariosSistemaDAO.leer(nuevoUsuarioId)).thenReturn(null);

        AspirantesDato payload = new AspirantesDato();
        UsuariosSistema u = new UsuariosSistema();
        u.setId(nuevoUsuarioId);
        payload.setIdUsuario(u);

        Response response = resource.update(testId, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
        verify(aspirantesDatoDAO).leer(testId);
        verify(usuariosSistemaDAO).leer(nuevoUsuarioId);
        verify(aspirantesDatoDAO, never()).actualizar(any());
    }

    @Test
    void update_ConCambioUsuarioExistente_DebeActualizarRelacion() {
        UUID nuevoUsuarioId = UUID.randomUUID();
        UsuariosSistema nuevoUsuario = new UsuariosSistema();
        nuevoUsuario.setId(nuevoUsuarioId);
        when(aspirantesDatoDAO.leer(testId)).thenReturn(entidad);
        when(usuariosSistemaDAO.leer(nuevoUsuarioId)).thenReturn(nuevoUsuario);
        when(aspirantesDatoDAO.actualizar(any(AspirantesDato.class))).thenAnswer(inv -> inv.getArgument(0));

        AspirantesDato payload = new AspirantesDato();
        UsuariosSistema u = new UsuariosSistema();
        u.setId(nuevoUsuarioId);
        payload.setIdUsuario(u);

        Response response = resource.update(testId, payload);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertSame(nuevoUsuario, entidad.getIdUsuario());
        verify(aspirantesDatoDAO).actualizar(entidad);
    }

    @Test
    void update_ConUsaSillaRuedasNoNulo_DebeActualizarCampo() {
        entidad.setUsaSillaRuedas(false);
        when(aspirantesDatoDAO.leer(testId)).thenReturn(entidad);
        when(aspirantesDatoDAO.actualizar(any(AspirantesDato.class))).thenAnswer(inv -> inv.getArgument(0));

        AspirantesDato payload = new AspirantesDato();
        payload.setUsaSillaRuedas(true);

        Response response = resource.update(testId, payload);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        AspirantesDato respEntity = (AspirantesDato) response.getEntity();
        assertTrue(respEntity.getUsaSillaRuedas());
        verify(aspirantesDatoDAO).actualizar(entidad);
    }

    @Test
    void update_ConIdNulo_DebeRetornar422() {
        Response response = resource.update(null, entidad);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(testId, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(aspirantesDatoDAO.leer(testId)).thenReturn(null);
        Response response = resource.update(testId, entidad);
        assertEquals(404, response.getStatus());
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(aspirantesDatoDAO.leer(testId)).thenThrow(new RuntimeException("BD error"));
        Response response = resource.update(testId, entidad);
        assertEquals(500, response.getStatus());
    }

    // ==================== delete (DELETE /{id}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(aspirantesDatoDAO.leer(testId)).thenReturn(entidad);
        Response response = resource.delete(testId);
        assertEquals(204, response.getStatus());
        verify(aspirantesDatoDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() {
        Response response = resource.delete(null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(aspirantesDatoDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(aspirantesDatoDAO.leer(testId)).thenReturn(null);
        Response response = resource.delete(testId);
        assertEquals(404, response.getStatus());
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(aspirantesDatoDAO.leer(any())).thenThrow(new RuntimeException("BD error"));
        Response response = resource.delete(testId);
        assertEquals(500, response.getStatus());
    }

    // ==================== getExpediente (GET /{id}/expediente) ====================

    @Test
    void getExpediente_ConAspiranteExistente_DebeRetornar200ConExpediente() {
        when(aspirantesDatoDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getExpediente(testId);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.DTO.ExpedienteAspiranteDTO);

        sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.DTO.ExpedienteAspiranteDTO expediente =
                (sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.DTO.ExpedienteAspiranteDTO) response.getEntity();

        assertNotNull(expediente.getAspirante());
        assertEquals(testId, expediente.getAspirante().getId());
        assertNull(expediente.getInscripcion());
        assertNull(expediente.getCarrera());
        assertNull(expediente.getAsignacion());
        assertNull(expediente.getExamen());
        assertNull(expediente.getProceso());

        verify(aspirantesDatoDAO).leer(testId);
    }

    @Test
    void getExpediente_ConAspiranteNoExistente_DebeRetornar404() {
        when(aspirantesDatoDAO.leer(testId)).thenReturn(null);

        Response response = resource.getExpediente(testId);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
        verify(aspirantesDatoDAO).leer(testId);
    }

    @Test
    void getExpediente_ConIdNulo_DebeRetornar422() {
        Response response = resource.getExpediente(null);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(aspirantesDatoDAO);
    }

    @Test
    void getExpediente_ConExcepcionEnDAO_DebeRetornar500() {
        when(aspirantesDatoDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getExpediente(testId);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
