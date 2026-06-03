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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AspirantesDatoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AspirantesDatoResourceTest {

    // Mockito inyecta por tipo en los campos privados: aspirantesDAO, pruebasDAO, inscripcionesDAO
    @Mock
    private AspirantesDatoDAO aspirantesDAO;

    @Mock
    private PruebasAdmisionDAO pruebasDAO;

    @Mock
    private InscripcionesPruebaDAO inscripcionesDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private AspirantesDatoResource resource;

    private AspirantesDato entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidad = new AspirantesDato();
        entidad.setId(testId);
        entidad.setNombres("Juan");
        entidad.setApellidos("Pérez");
        entidad.setDui("01234567-8");
        entidad.setCorreo("juan.perez@example.com");
        entidad.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        entidad.setUsaSillaRuedas(false);
    }

    // ==================== listAspirantes (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(aspirantesDAO.count()).thenReturn(1);
        when(aspirantesDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listAspirantes(0, 10, null);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200() {
        when(aspirantesDAO.count()).thenReturn(0);
        when(aspirantesDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listAspirantes(0, 10, null);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(aspirantesDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listAspirantes(0, 10, null);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    @Test
    void findRange_ConUsaSillaTrue_DebeRetornarListaFiltrada() {
        when(aspirantesDAO.findByRequiereSillaRuedas()).thenReturn(List.of(entidad));

        Response response = resource.listAspirantes(0, 10, true);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        verify(aspirantesDAO).findByRequiereSillaRuedas();
        verify(aspirantesDAO, never()).findRange(anyInt(), anyInt());
    }

    // ==================== getAspirante (GET /{idAspirante}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        when(aspirantesDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getAspirante(testId.toString());

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(aspirantesDAO.leer(testId)).thenReturn(null);

        Response response = resource.getAspirante(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.getAspirante("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(aspirantesDAO.leer(any())).thenThrow(new RuntimeException("BD error"));

        Response response = resource.getAspirante(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== createAspirante (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setNombres("Maria");
        nuevo.setApellidos("García");
        nuevo.setDui("09876543-2");
        nuevo.setCorreo("maria.garcia@example.com");
        nuevo.setFechaNacimiento(LocalDate.of(1999, 5, 5));
        nuevo.setUsaSillaRuedas(false);

        doAnswer(inv -> {
            AspirantesDato a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return null;
        }).when(aspirantesDAO).crear(any(AspirantesDato.class));

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/aspirantes/1"));

        Response response = resource.createAspirante(nuevo, uriInfo);

        assertEquals(201, response.getStatus());
        verify(aspirantesDAO).crear(any(AspirantesDato.class));
    }

    @Test
    void create_ConEntidadNula_DebeRetornar400() {
        Response response = resource.createAspirante(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void create_SinDui_DebeRetornar400() {
        AspirantesDato sinDui = new AspirantesDato();
        sinDui.setNombres("Carlos");
        sinDui.setApellidos("Mendez");
        sinDui.setCorreo("cmendez@example.com");
        sinDui.setFechaNacimiento(LocalDate.of(1992, 4, 4));

        Response response = resource.createAspirante(sinDui, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void create_SinCorreo_DebeRetornar400() {
        AspirantesDato sinCorreo = new AspirantesDato();
        sinCorreo.setNombres("Ana");
        sinCorreo.setApellidos("López");
        sinCorreo.setDui("11111111-1");
        sinCorreo.setFechaNacimiento(LocalDate.of(1998, 3, 3));

        Response response = resource.createAspirante(sinCorreo, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setDui("22222222-2");
        nuevo.setCorreo("test@example.com");
        doThrow(new RuntimeException("BD error")).when(aspirantesDAO).crear(any());

        Response response = resource.createAspirante(nuevo, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== updateAspirante (PUT /{idAspirante}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        when(aspirantesDAO.actualizar(any(AspirantesDato.class))).thenReturn(entidad);

        AspirantesDato payload = new AspirantesDato();
        payload.setDui("01234567-8");
        payload.setCorreo("actualizado@example.com");

        Response response = resource.updateAspirante(testId.toString(), payload);

        assertEquals(200, response.getStatus());
        verify(aspirantesDAO).actualizar(payload);
    }

    @Test
    void update_ConEntidadSinDui_DebeRetornar400() {
        AspirantesDato sinDui = new AspirantesDato();
        sinDui.setCorreo("correo@example.com");

        Response response = resource.updateAspirante(testId.toString(), sinDui);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void update_ConIdFormatoInvalido_SinDui_DebeRetornar400() {
        AspirantesDato sinDui = new AspirantesDato();
        sinDui.setCorreo("correo@example.com");

        Response response = resource.updateAspirante("no-es-uuid", sinDui);

        // Validación de DUI se hace antes de parsear UUID → 400
        assertEquals(400, response.getStatus());
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(aspirantesDAO.leer(testId)).thenReturn(null);

        AspirantesDato payload = new AspirantesDato();
        payload.setDui("01234567-8");
        payload.setCorreo("correo@example.com");

        Response response = resource.updateAspirante(testId.toString(), payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(aspirantesDAO.leer(testId)).thenThrow(new RuntimeException("BD error"));

        AspirantesDato payload = new AspirantesDato();
        payload.setDui("01234567-8");
        payload.setCorreo("correo@example.com");

        Response response = resource.updateAspirante(testId.toString(), payload);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== deleteAspirante (DELETE /{idAspirante}) ====================

    @Test
    void delete_ConIdExistente_SinInscripciones_DebeRetornar204() {
        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        when(inscripcionesDAO.findByAspiranteId(testId)).thenReturn(Collections.emptyList());

        Response response = resource.deleteAspirante(testId.toString());

        assertEquals(204, response.getStatus());
        verify(aspirantesDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdExistente_ConInscripciones_DebeRetornar409() {
        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        when(inscripcionesDAO.findByAspiranteId(testId)).thenReturn(List.of(
            new sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba()
        ));

        Response response = resource.deleteAspirante(testId.toString());

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString("Conflict-reason"));
        verify(aspirantesDAO, never()).eliminar(any());
    }

    @Test
    void delete_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.deleteAspirante("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(aspirantesDAO.leer(testId)).thenReturn(null);

        Response response = resource.deleteAspirante(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(aspirantesDAO.leer(any())).thenThrow(new RuntimeException("BD error"));

        Response response = resource.deleteAspirante(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
