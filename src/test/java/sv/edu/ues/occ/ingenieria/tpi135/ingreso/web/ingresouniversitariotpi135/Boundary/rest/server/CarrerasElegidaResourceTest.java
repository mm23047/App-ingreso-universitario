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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CarrerasElegidaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CatalogoCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarrerasElegidaResourceTest {

    @Mock private CarrerasElegidaDAO carrerasElegidaDAO;
    @Mock private InscripcionesPruebaDAO inscripcionesDAO;
    @Mock private CatalogoCarreraDAO catalogoCarreraDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    @InjectMocks private CarrerasElegidaResource resource;

    private InscripcionesPrueba inscripcion;
    private UUID idInscripcion;
    private String idInscripcionStr;
    private CatalogoCarrera carreraISI;

    @BeforeEach
    void setUp() {
        idInscripcion = UUID.randomUUID();
        idInscripcionStr = idInscripcion.toString();
        inscripcion = new InscripcionesPrueba();
        inscripcion.setIdInscripcionPrueba(idInscripcion);

        carreraISI = new CatalogoCarrera();
        carreraISI.setIdCarrera("ISI");
    }

    private CarrerasElegida crearEleccion(String idCarrera, short prioridad) {
        CatalogoCarrera cat = new CatalogoCarrera();
        cat.setIdCarrera(idCarrera);
        CarrerasElegida eleccion = new CarrerasElegida();
        eleccion.setCatalogoCarrera(cat);
        eleccion.setPrioridad(prioridad);
        return eleccion;
    }

    // ==================== getCarrerasElegidas (GET /) ====================

    @Test
    void getCarreras_ConInscripcionExistente_DebeRetornar200ConLista() {
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(inscripcion);
        List<CarrerasElegida> lista = List.of(crearEleccion("ISI", (short) 1));
        when(carrerasElegidaDAO.findByInscripcionOrderByPrioridad(idInscripcion)).thenReturn(lista);

        Response response = resource.getCarrerasElegidas(idInscripcionStr);

        assertEquals(200, response.getStatus());
        assertSame(lista, response.getEntity());
    }

    @Test
    void getCarreras_ConListaVacia_DebeRetornar200() {
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(inscripcion);
        when(carrerasElegidaDAO.findByInscripcionOrderByPrioridad(idInscripcion)).thenReturn(Collections.emptyList());

        Response response = resource.getCarrerasElegidas(idInscripcionStr);

        assertEquals(200, response.getStatus());
    }

    @Test
    void getCarreras_ConInscripcionInexistente_DebeRetornar404() {
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(null);

        Response response = resource.getCarrerasElegidas(idInscripcionStr);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void getCarreras_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getCarrerasElegidas("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesDAO, carrerasElegidaDAO);
    }

    @Test
    void getCarreras_ConExcepcionEnDAO_DebeRetornar500() {
        when(inscripcionesDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getCarrerasElegidas(idInscripcionStr);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== addCarreraElegida (POST /) ====================

    @Test
    void addCarrera_ConDatosValidos_DebeRetornar201() {
        CarrerasElegida nueva = crearEleccion("ISI", (short) 1);

        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(inscripcion);
        when(catalogoCarreraDAO.leer("ISI")).thenReturn(carreraISI);
        when(carrerasElegidaDAO.existsByInscripcionAndCarrera(idInscripcion, "ISI")).thenReturn(false);
        when(carrerasElegidaDAO.existsByInscripcionAndPrioridad(idInscripcion, (short) 1)).thenReturn(false);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/carreras/ISI"));

        Response response = resource.addCarreraElegida(idInscripcionStr, nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(carrerasElegidaDAO).crear(nueva);
    }

    @Test
    void addCarrera_ConPayloadNulo_DebeRetornar400() {
        Response response = resource.addCarreraElegida(idInscripcionStr, null, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesDAO, carrerasElegidaDAO);
    }

    @Test
    void addCarrera_SinCatalogoCarrera_DebeRetornar400() {
        CarrerasElegida sinCatalogo = new CarrerasElegida();
        sinCatalogo.setPrioridad((short) 1);

        Response response = resource.addCarreraElegida(idInscripcionStr, sinCatalogo, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesDAO, carrerasElegidaDAO);
    }

    @Test
    void addCarrera_SinPrioridad_DebeRetornar400() {
        CarrerasElegida sinPrioridad = crearEleccion("ISI", (short) 0);
        sinPrioridad.setPrioridad(null);

        Response response = resource.addCarreraElegida(idInscripcionStr, sinPrioridad, uriInfo);

        assertEquals(400, response.getStatus());
    }

    @Test
    void addCarrera_ConPrioridadMenorA1_DebeRetornar400() {
        CarrerasElegida prioridadInvalida = crearEleccion("ISI", (short) 0);

        Response response = resource.addCarreraElegida(idInscripcionStr, prioridadInvalida, uriInfo);

        assertEquals(400, response.getStatus());
    }

    @Test
    void addCarrera_ConInscripcionInexistente_DebeRetornar404() {
        CarrerasElegida nueva = crearEleccion("ISI", (short) 1);
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(null);

        Response response = resource.addCarreraElegida(idInscripcionStr, nueva, uriInfo);

        assertEquals(404, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void addCarrera_ConCarreraNoExisteEnCatalogo_DebeRetornar404() {
        CarrerasElegida nueva = crearEleccion("XXX", (short) 1);
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(inscripcion);
        when(catalogoCarreraDAO.leer("XXX")).thenReturn(null);

        Response response = resource.addCarreraElegida(idInscripcionStr, nueva, uriInfo);

        assertEquals(404, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void addCarrera_ConCarreraDuplicada_DebeRetornar409() {
        CarrerasElegida duplicada = crearEleccion("ISI", (short) 2);
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(inscripcion);
        when(catalogoCarreraDAO.leer("ISI")).thenReturn(carreraISI);
        when(carrerasElegidaDAO.existsByInscripcionAndCarrera(idInscripcion, "ISI")).thenReturn(true);

        Response response = resource.addCarreraElegida(idInscripcionStr, duplicada, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verify(carrerasElegidaDAO, never()).crear(any());
    }

    @Test
    void addCarrera_ConPrioridadOcupada_DebeRetornar409() {
        CarrerasElegida nueva = crearEleccion("MED", (short) 1);
        CatalogoCarrera carreraMED = new CatalogoCarrera();
        carreraMED.setIdCarrera("MED");
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(inscripcion);
        when(catalogoCarreraDAO.leer("MED")).thenReturn(carreraMED);
        when(carrerasElegidaDAO.existsByInscripcionAndCarrera(idInscripcion, "MED")).thenReturn(false);
        when(carrerasElegidaDAO.existsByInscripcionAndPrioridad(idInscripcion, (short) 1)).thenReturn(true);

        Response response = resource.addCarreraElegida(idInscripcionStr, nueva, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verify(carrerasElegidaDAO, never()).crear(any());
    }

    @Test
    void addCarrera_ConUuidInvalido_DebeRetornar400() {
        CarrerasElegida nueva = crearEleccion("ISI", (short) 1);

        Response response = resource.addCarreraElegida("no-es-uuid", nueva, uriInfo);

        assertEquals(400, response.getStatus());
    }

    @Test
    void addCarrera_ConExcepcionEnDAO_DebeRetornar500() {
        CarrerasElegida nueva = crearEleccion("ISI", (short) 1);
        when(inscripcionesDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.addCarreraElegida(idInscripcionStr, nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== updatePrioridad (PUT /{idCarrera}) ====================

    @Test
    void updatePrioridad_ConDatosValidos_DebeRetornar200() {
        CarrerasElegida existente = new CarrerasElegida();
        existente.setPrioridad((short) 1);
        CarrerasElegida datos = new CarrerasElegida();
        datos.setPrioridad((short) 2);

        when(carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, "ISI")).thenReturn(existente);
        when(carrerasElegidaDAO.existsByInscripcionAndPrioridad(idInscripcion, (short) 2)).thenReturn(false);
        when(carrerasElegidaDAO.actualizar(existente)).thenReturn(existente);

        Response response = resource.updatePrioridad(idInscripcionStr, "ISI", datos);

        assertEquals(200, response.getStatus());
        verify(carrerasElegidaDAO).actualizar(existente);
    }

    @Test
    void updatePrioridad_ConMismaPrioridad_DebeRetornar200SinValidarColision() {
        CarrerasElegida existente = new CarrerasElegida();
        existente.setPrioridad((short) 1);
        CarrerasElegida datos = new CarrerasElegida();
        datos.setPrioridad((short) 1);

        when(carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, "ISI")).thenReturn(existente);
        when(carrerasElegidaDAO.actualizar(existente)).thenReturn(existente);

        Response response = resource.updatePrioridad(idInscripcionStr, "ISI", datos);

        assertEquals(200, response.getStatus());
        verify(carrerasElegidaDAO, never()).existsByInscripcionAndPrioridad(any(), anyShort());
    }

    @Test
    void updatePrioridad_ConPayloadNulo_DebeRetornar400() {
        Response response = resource.updatePrioridad(idInscripcionStr, "ISI", null);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void updatePrioridad_SinPrioridad_DebeRetornar400() {
        CarrerasElegida sinPrioridad = new CarrerasElegida();

        Response response = resource.updatePrioridad(idInscripcionStr, "ISI", sinPrioridad);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void updatePrioridad_ConCarreraInexistente_DebeRetornar404() {
        CarrerasElegida datos = new CarrerasElegida();
        datos.setPrioridad((short) 2);
        when(carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, "ISI")).thenReturn(null);

        Response response = resource.updatePrioridad(idInscripcionStr, "ISI", datos);

        assertEquals(404, response.getStatus());
    }

    @Test
    void updatePrioridad_ConColisionDePrioridades_DebeRetornar409() {
        CarrerasElegida existente = new CarrerasElegida();
        existente.setPrioridad((short) 1);
        CarrerasElegida datos = new CarrerasElegida();
        datos.setPrioridad((short) 2);

        when(carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, "ISI")).thenReturn(existente);
        when(carrerasElegidaDAO.existsByInscripcionAndPrioridad(idInscripcion, (short) 2)).thenReturn(true);

        Response response = resource.updatePrioridad(idInscripcionStr, "ISI", datos);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verify(carrerasElegidaDAO, never()).actualizar(any());
    }

    @Test
    void updatePrioridad_ConUuidInvalido_DebeRetornar400() {
        CarrerasElegida datos = new CarrerasElegida();
        datos.setPrioridad((short) 2);

        Response response = resource.updatePrioridad("no-es-uuid", "ISI", datos);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void updatePrioridad_ConExcepcionEnDAO_DebeRetornar500() {
        CarrerasElegida datos = new CarrerasElegida();
        datos.setPrioridad((short) 2);
        when(carrerasElegidaDAO.findByInscripcionAndCarrera(any(), any()))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updatePrioridad(idInscripcionStr, "ISI", datos);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== deleteCarreraElegida (DELETE /{idCarrera}) ====================

    @Test
    void deleteCarrera_ConEntidadExistente_DebeRetornar204() {
        CarrerasElegida existente = new CarrerasElegida();
        when(carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, "ARQ")).thenReturn(existente);

        Response response = resource.deleteCarreraElegida(idInscripcionStr, "ARQ");

        assertEquals(204, response.getStatus());
        verify(carrerasElegidaDAO).eliminar(existente);
    }

    @Test
    void deleteCarrera_ConEntidadInexistente_DebeRetornar404() {
        when(carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, "ISI")).thenReturn(null);

        Response response = resource.deleteCarreraElegida(idInscripcionStr, "ISI");

        assertEquals(404, response.getStatus());
        verify(carrerasElegidaDAO, never()).eliminar(any());
    }

    @Test
    void deleteCarrera_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.deleteCarreraElegida("no-es-uuid", "ISI");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void deleteCarrera_ConExcepcionEnDAO_DebeRetornar500() {
        when(carrerasElegidaDAO.findByInscripcionAndCarrera(any(), any()))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteCarreraElegida(idInscripcionStr, "ISI");

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getPrimeraOpcion (GET /primera-opcion) ====================

    @Test
    void getPrimeraOpcion_ConOpcionRegistrada_DebeRetornar200() {
        CarrerasElegida primera = crearEleccion("ISI", (short) 1);
        when(carrerasElegidaDAO.findByInscripcionAndPrioridadLevel(idInscripcion, (short) 1))
                .thenReturn(primera);

        Response response = resource.getPrimeraOpcion(idInscripcionStr);

        assertEquals(200, response.getStatus());
        assertSame(primera, response.getEntity());
    }

    @Test
    void getPrimeraOpcion_SinOpcionRegistrada_DebeRetornar404() {
        when(carrerasElegidaDAO.findByInscripcionAndPrioridadLevel(idInscripcion, (short) 1))
                .thenReturn(null);

        Response response = resource.getPrimeraOpcion(idInscripcionStr);

        assertEquals(404, response.getStatus());
    }

    @Test
    void getPrimeraOpcion_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getPrimeraOpcion("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void getPrimeraOpcion_ConExcepcionEnDAO_DebeRetornar500() {
        when(carrerasElegidaDAO.findByInscripcionAndPrioridadLevel(any(), anyShort()))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getPrimeraOpcion(idInscripcionStr);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== reordenarCarreras (PATCH /reordenar) ====================

    @Test
    void reordenar_ConOrdenValido_DebeRetornar200() {
        CarrerasElegida c1 = crearEleccion("ISI", (short) 1);
        CarrerasElegida c2 = crearEleccion("MED", (short) 2);
        when(carrerasElegidaDAO.findByInscripcionOrderByPrioridad(idInscripcion))
                .thenReturn(List.of(c1, c2))
                .thenReturn(List.of(c2, c1));
        when(carrerasElegidaDAO.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));

        Response response = resource.reordenarCarreras(idInscripcionStr, List.of("MED", "ISI"));

        assertEquals(200, response.getStatus());
        verify(carrerasElegidaDAO, times(2)).actualizar(any());
    }

    @Test
    void reordenar_ConListaNula_DebeRetornar400() {
        Response response = resource.reordenarCarreras(idInscripcionStr, null);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void reordenar_ConListaVacia_DebeRetornar400() {
        Response response = resource.reordenarCarreras(idInscripcionStr, Collections.emptyList());

        assertEquals(400, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void reordenar_ConCantidadIncorrecta_DebeRetornar400() {
        CarrerasElegida c1 = crearEleccion("ISI", (short) 1);
        when(carrerasElegidaDAO.findByInscripcionOrderByPrioridad(idInscripcion))
                .thenReturn(List.of(c1));

        Response response = resource.reordenarCarreras(idInscripcionStr, List.of("ISI", "MED"));

        assertEquals(400, response.getStatus());
    }

    @Test
    void reordenar_ConCarreraNoRegistrada_DebeRetornar400() {
        CarrerasElegida c1 = crearEleccion("ISI", (short) 1);
        when(carrerasElegidaDAO.findByInscripcionOrderByPrioridad(idInscripcion))
                .thenReturn(List.of(c1));

        Response response = resource.reordenarCarreras(idInscripcionStr, List.of("XXX"));

        assertEquals(400, response.getStatus());
    }

    @Test
    void reordenar_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.reordenarCarreras("no-es-uuid", List.of("ISI"));

        assertEquals(400, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void reordenar_ConExcepcionEnDAO_DebeRetornar500() {
        when(carrerasElegidaDAO.findByInscripcionOrderByPrioridad(any()))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.reordenarCarreras(idInscripcionStr, List.of("ISI"));

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
