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
import jakarta.ejb.EJBException;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AspirantesDatoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PruebasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ReglaNegocioException;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

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

        // ACTUALIZADO: Se agregan dos nulls al final
        Response response = resource.listAspirantes(0, 10, null, null, null);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200() {
        when(aspirantesDAO.count()).thenReturn(0);
        when(aspirantesDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        // ACTUALIZADO: Se agregan dos nulls al final
        Response response = resource.listAspirantes(0, 10, null, null, null);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(aspirantesDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        // ACTUALIZADO: Se agregan dos nulls al final
        Response response = resource.listAspirantes(0, 10, null, null, null);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void findRange_ConUsaSillaTrue_DebeRetornarListaFiltrada() {
        when(aspirantesDAO.findByRequiereSillaRuedas()).thenReturn(List.of(entidad));

        // ACTUALIZADO: Se agregan dos nulls al final
        Response response = resource.listAspirantes(0, 10, true, null, null);

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
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
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
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
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
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
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
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
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
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void create_ConDuiEnBlanco_DebeRetornar400() {
        AspirantesDato conDuiBlanco = new AspirantesDato();
        conDuiBlanco.setDui("   ");
        conDuiBlanco.setCorreo("test@example.com");

        Response response = resource.createAspirante(conDuiBlanco, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void create_ConCorreoEnBlanco_DebeRetornar400() {
        AspirantesDato conCorreoBlanco = new AspirantesDato();
        conCorreoBlanco.setDui("33333333-3");
        conCorreoBlanco.setCorreo("   ");

        Response response = resource.createAspirante(conCorreoBlanco, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void create_ConDuiDuplicado_DebeRetornar409() {
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setDui("01234567-8");
        nuevo.setCorreo("otro@example.com");
        doThrow(new ReglaNegocioException(ReglaNegocioException.Tipo.DUI_DUPLICADO, "DUI ya registrado"))
                .when(aspirantesDAO).crear(any());

        Response response = resource.createAspirante(nuevo, uriInfo);

        assertEquals(409, response.getStatus());
    }

    @Test
    void create_ConCorreoDuplicado_DebeRetornar409() {
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setDui("44444444-4");
        nuevo.setCorreo("juan.perez@example.com");
        doThrow(new ReglaNegocioException(ReglaNegocioException.Tipo.CORREO_DUPLICADO, "Correo ya registrado"))
                .when(aspirantesDAO).crear(any());

        Response response = resource.createAspirante(nuevo, uriInfo);

        assertEquals(409, response.getStatus());
    }

    @Test
    void create_ConEdadMinima_DebeRetornar400() {
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setDui("55555555-5");
        nuevo.setCorreo("menor@example.com");
        doThrow(new ReglaNegocioException(ReglaNegocioException.Tipo.EDAD_MINIMA, "El aspirante debe ser mayor de edad"))
                .when(aspirantesDAO).crear(any());

        Response response = resource.createAspirante(nuevo, uriInfo);

        assertEquals(400, response.getStatus());
    }

    @Test
    void create_ConIllegalArgumentEnDAO_DebeRetornar400() {
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setDui("66666666-6");
        nuevo.setCorreo("test@example.com");
        doThrow(new IllegalArgumentException("Datos inválidos"))
                .when(aspirantesDAO).crear(any());

        Response response = resource.createAspirante(nuevo, uriInfo);

        assertEquals(400, response.getStatus());
    }

    @Test
    void create_ConEJBException_DebeRetornar500() {
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setDui("77777777-7");
        nuevo.setCorreo("test@example.com");
        doThrow(new EJBException("Error EJB"))
                .when(aspirantesDAO).crear(any());

        Response response = resource.createAspirante(nuevo, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setDui("22222222-2");
        nuevo.setCorreo("test@example.com");
        doThrow(new RuntimeException("BD error")).when(aspirantesDAO).crear(any());

        Response response = resource.createAspirante(nuevo, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
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
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
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
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void update_ConEntidadNula_DebeRetornar400() {
        Response response = resource.updateAspirante(testId.toString(), null);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void update_ConCorreoEnBlanco_DebeRetornar400() {
        AspirantesDato sinCorreo = new AspirantesDato();
        sinCorreo.setDui("01234567-8");
        sinCorreo.setCorreo("   ");

        Response response = resource.updateAspirante(testId.toString(), sinCorreo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void update_ConUuidInvalido_DebeRetornar400() {
        AspirantesDato payload = new AspirantesDato();
        payload.setDui("01234567-8");
        payload.setCorreo("correo@example.com");

        Response response = resource.updateAspirante("no-es-uuid", payload);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(aspirantesDAO);
    }

    @Test
    void update_ConDuiDuplicado_DebeRetornar409() {
        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        AspirantesDato payload = new AspirantesDato();
        payload.setDui("99999999-9");
        payload.setCorreo("correo@example.com");
        when(aspirantesDAO.actualizar(payload))
                .thenThrow(new ReglaNegocioException(ReglaNegocioException.Tipo.DUI_DUPLICADO, "DUI ya registrado"));

        Response response = resource.updateAspirante(testId.toString(), payload);

        assertEquals(409, response.getStatus());
    }

    @Test
    void update_ConCorreoDuplicado_DebeRetornar409() {
        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        AspirantesDato payload = new AspirantesDato();
        payload.setDui("01234567-8");
        payload.setCorreo("duplicado@example.com");
        when(aspirantesDAO.actualizar(payload))
                .thenThrow(new ReglaNegocioException(ReglaNegocioException.Tipo.CORREO_DUPLICADO, "Correo ya registrado"));

        Response response = resource.updateAspirante(testId.toString(), payload);

        assertEquals(409, response.getStatus());
    }

    @Test
    void update_ConEdadMinima_DebeRetornar400() {
        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        AspirantesDato payload = new AspirantesDato();
        payload.setDui("01234567-8");
        payload.setCorreo("correo@example.com");
        when(aspirantesDAO.actualizar(payload))
                .thenThrow(new ReglaNegocioException(ReglaNegocioException.Tipo.EDAD_MINIMA, "Debe ser mayor de edad"));

        Response response = resource.updateAspirante(testId.toString(), payload);

        assertEquals(400, response.getStatus());
    }

    @Test
    void update_ConIllegalArgumentEnActualizar_DebeRetornar400() {
        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        AspirantesDato payload = new AspirantesDato();
        payload.setDui("01234567-8");
        payload.setCorreo("correo@example.com");
        when(aspirantesDAO.actualizar(payload))
                .thenThrow(new IllegalArgumentException("Datos inválidos"));

        Response response = resource.updateAspirante(testId.toString(), payload);

        assertEquals(400, response.getStatus());
    }

    @Test
    void update_ConEJBException_DebeRetornar500() {
        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        AspirantesDato payload = new AspirantesDato();
        payload.setDui("01234567-8");
        payload.setCorreo("correo@example.com");
        when(aspirantesDAO.actualizar(payload))
                .thenThrow(new EJBException("Error EJB"));

        Response response = resource.updateAspirante(testId.toString(), payload);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void update_ConExcepcionEnLeer_DebeRetornar500() {
        when(aspirantesDAO.leer(testId)).thenThrow(new RuntimeException("BD error"));

        AspirantesDato payload = new AspirantesDato();
        payload.setDui("01234567-8");
        payload.setCorreo("correo@example.com");

        Response response = resource.updateAspirante(testId.toString(), payload);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void update_ConExcepcionEnActualizar_DebeRetornar500() {
        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        AspirantesDato payload = new AspirantesDato();
        payload.setDui("01234567-8");
        payload.setCorreo("correo@example.com");
        when(aspirantesDAO.actualizar(payload))
                .thenThrow(new RuntimeException("BD error"));

        Response response = resource.updateAspirante(testId.toString(), payload);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
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
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
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
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(aspirantesDAO.leer(any())).thenThrow(new RuntimeException("BD error"));

        Response response = resource.deleteAspirante(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== listAspirantes (Filtros Nuevos: DUI y Correo) ====================

    @Test
    void listAspirantes_ConDuiExistente_DebeRetornarListaDeUno() {
        when(aspirantesDAO.findByDui("01234567-8")).thenReturn(entidad);

        // Simulamos que el usuario buscó por DUI
        Response response = resource.listAspirantes(0, 10, null, "01234567-8", null);

        assertEquals(200, response.getStatus());
        List<AspirantesDato> listaResultado = (List<AspirantesDato>) response.getEntity();
        assertEquals(1, listaResultado.size());
        assertEquals("01234567-8", listaResultado.get(0).getDui());
    }

    @Test
    void listAspirantes_ConDuiInexistente_DebeRetornarListaVacia() {
        when(aspirantesDAO.findByDui("99999999-9")).thenReturn(null);

        Response response = resource.listAspirantes(0, 10, null, "99999999-9", null);

        assertEquals(200, response.getStatus());
        List<AspirantesDato> listaResultado = (List<AspirantesDato>) response.getEntity();
        assertTrue(listaResultado.isEmpty());
    }

    @Test
    void listAspirantes_ConCorreoExistente_DebeRetornarListaDeUno() {
        when(aspirantesDAO.findByCorreo("juan.perez@example.com")).thenReturn(entidad);

        Response response = resource.listAspirantes(0, 10, null, null, "juan.perez@example.com");

        assertEquals(200, response.getStatus());
        List<AspirantesDato> listaResultado = (List<AspirantesDato>) response.getEntity();
        assertEquals(1, listaResultado.size());
        assertEquals("juan.perez@example.com", listaResultado.get(0).getCorreo());
    }

    @Test
    void listAspirantes_ConCorreoInexistente_DebeRetornarListaVacia() {
        when(aspirantesDAO.findByCorreo("noexiste@example.com")).thenReturn(null);

        Response response = resource.listAspirantes(0, 10, null, null, "noexiste@example.com");

        assertEquals(200, response.getStatus());
        List<?> listaResultado = (List<?>) response.getEntity();
        assertTrue(listaResultado.isEmpty());
    }

    // ==================== getInscripcionesPorAspirante (GET /{idAspirante}/inscripciones) ====================

    @Test
    void getInscripciones_ConAspiranteExistente_DebeRetornar200ConLista() {
        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        InscripcionesPrueba inscripcion = new InscripcionesPrueba();
        inscripcion.setIdInscripcionPrueba(UUID.randomUUID());
        when(inscripcionesDAO.findByAspiranteId(testId)).thenReturn(List.of(inscripcion));

        Response response = resource.getInscripcionesPorAspirante(testId.toString());

        assertEquals(200, response.getStatus());
        List<?> resultado = (List<?>) response.getEntity();
        assertEquals(1, resultado.size());
    }

    @Test
    void getInscripciones_ConAspiranteExistenteSinInscripciones_DebeRetornar200ConListaVacia() {
        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        when(inscripcionesDAO.findByAspiranteId(testId)).thenReturn(Collections.emptyList());

        Response response = resource.getInscripcionesPorAspirante(testId.toString());

        assertEquals(200, response.getStatus());
        List<?> resultado = (List<?>) response.getEntity();
        assertTrue(resultado.isEmpty());
    }

    @Test
    void getInscripciones_ConAspiranteInexistente_DebeRetornar404() {
        when(aspirantesDAO.leer(testId)).thenReturn(null);

        Response response = resource.getInscripcionesPorAspirante(testId.toString());

        assertEquals(404, response.getStatus());
        verifyNoInteractions(inscripcionesDAO);
    }

    @Test
    void getInscripciones_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getInscripcionesPorAspirante("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(aspirantesDAO, inscripcionesDAO);
    }

    @Test
    void getInscripciones_ConExcepcionEnDAO_DebeRetornar500() {
        when(aspirantesDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getInscripcionesPorAspirante(testId.toString());

        assertEquals(500, response.getStatus());
    }

    // ==================== crearInscripcionAspirante (POST /{idAspirante}/inscripciones) ====================

    @Test
    void crearInscripcion_ConDatosValidos_DebeRetornar201() {
        UUID idPrueba = UUID.randomUUID();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(idPrueba);

        InscripcionesPrueba nueva = new InscripcionesPrueba();
        nueva.setPruebaAdmision(prueba);

        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        when(pruebasDAO.leer(idPrueba)).thenReturn(prueba);
        doAnswer(inv -> {
            InscripcionesPrueba i = inv.getArgument(0);
            i.setIdInscripcionPrueba(UUID.randomUUID());
            return null;
        }).when(inscripcionesDAO).crear(any(InscripcionesPrueba.class));
        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/inscripciones_prueba/1"));

        Response response = resource.crearInscripcionAspirante(testId.toString(), nueva, uriInfo);

        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());
        verify(inscripcionesDAO).crear(nueva);
    }

    @Test
    void crearInscripcion_ConAspiranteInexistente_DebeRetornar404() {
        InscripcionesPrueba nueva = new InscripcionesPrueba();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(UUID.randomUUID());
        nueva.setPruebaAdmision(prueba);

        when(aspirantesDAO.leer(testId)).thenReturn(null);

        Response response = resource.crearInscripcionAspirante(testId.toString(), nueva, uriInfo);

        assertEquals(404, response.getStatus());
        verifyNoInteractions(inscripcionesDAO);
    }

    @Test
    void crearInscripcion_SinPruebaAdmision_DebeRetornar400() {
        InscripcionesPrueba nueva = new InscripcionesPrueba();

        when(aspirantesDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.crearInscripcionAspirante(testId.toString(), nueva, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesDAO);
    }

    @Test
    void crearInscripcion_ConPruebaAdmisionSinId_DebeRetornar400() {
        InscripcionesPrueba nueva = new InscripcionesPrueba();
        PruebasAdmision pruebaSinId = new PruebasAdmision();
        nueva.setPruebaAdmision(pruebaSinId);

        when(aspirantesDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.crearInscripcionAspirante(testId.toString(), nueva, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesDAO);
    }

    @Test
    void crearInscripcion_ConPruebaInexistente_DebeRetornar400() {
        UUID idPrueba = UUID.randomUUID();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(idPrueba);
        InscripcionesPrueba nueva = new InscripcionesPrueba();
        nueva.setPruebaAdmision(prueba);

        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        when(pruebasDAO.leer(idPrueba)).thenReturn(null);

        Response response = resource.crearInscripcionAspirante(testId.toString(), nueva, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesDAO);
    }

    @Test
    void crearInscripcion_ConUuidAspiranteInvalido_DebeRetornar409() {
        InscripcionesPrueba nueva = new InscripcionesPrueba();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(UUID.randomUUID());
        nueva.setPruebaAdmision(prueba);

        Response response = resource.crearInscripcionAspirante("no-es-uuid", nueva, uriInfo);

        assertEquals(409, response.getStatus());
        verifyNoInteractions(aspirantesDAO, inscripcionesDAO);
    }

    @Test
    void crearInscripcion_ConIllegalArgumentEnDAO_DebeRetornar409() {
        UUID idPrueba = UUID.randomUUID();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(idPrueba);
        InscripcionesPrueba nueva = new InscripcionesPrueba();
        nueva.setPruebaAdmision(prueba);

        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        when(pruebasDAO.leer(idPrueba)).thenReturn(prueba);
        doThrow(new IllegalArgumentException("Ya está inscrito en esta prueba"))
                .when(inscripcionesDAO).crear(any());

        Response response = resource.crearInscripcionAspirante(testId.toString(), nueva, uriInfo);

        assertEquals(409, response.getStatus());
    }

    @Test
    void crearInscripcion_ConEJBExceptionConIAECausa_DebeRetornar409() {
        UUID idPrueba = UUID.randomUUID();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(idPrueba);
        InscripcionesPrueba nueva = new InscripcionesPrueba();
        nueva.setPruebaAdmision(prueba);

        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        when(pruebasDAO.leer(idPrueba)).thenReturn(prueba);
        doThrow(new EJBException(new IllegalArgumentException("Duplicado")))
                .when(inscripcionesDAO).crear(any());

        Response response = resource.crearInscripcionAspirante(testId.toString(), nueva, uriInfo);

        assertEquals(409, response.getStatus());
    }

    @Test
    void crearInscripcion_ConEJBExceptionSinIAE_DebeRetornar500() {
        UUID idPrueba = UUID.randomUUID();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(idPrueba);
        InscripcionesPrueba nueva = new InscripcionesPrueba();
        nueva.setPruebaAdmision(prueba);

        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        when(pruebasDAO.leer(idPrueba)).thenReturn(prueba);
        doThrow(new EJBException("Error EJB genérico"))
                .when(inscripcionesDAO).crear(any());

        Response response = resource.crearInscripcionAspirante(testId.toString(), nueva, uriInfo);

        assertEquals(500, response.getStatus());
    }

    @Test
    void crearInscripcion_ConExcepcionEnDAO_DebeRetornar500() {
        UUID idPrueba = UUID.randomUUID();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(idPrueba);
        InscripcionesPrueba nueva = new InscripcionesPrueba();
        nueva.setPruebaAdmision(prueba);

        when(aspirantesDAO.leer(testId)).thenReturn(entidad);
        when(pruebasDAO.leer(idPrueba)).thenReturn(prueba);
        doThrow(new RuntimeException("Error de BD"))
                .when(inscripcionesDAO).crear(any());

        Response response = resource.crearInscripcionAspirante(testId.toString(), nueva, uriInfo);

        assertEquals(500, response.getStatus());
    }
}
