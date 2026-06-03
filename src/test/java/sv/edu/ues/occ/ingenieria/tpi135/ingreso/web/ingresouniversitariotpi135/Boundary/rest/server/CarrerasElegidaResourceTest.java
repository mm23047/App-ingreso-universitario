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

    private InscripcionesPrueba testInscripcion;
    private UUID idInscripcion;
    private String idInscripcionStr;

    @BeforeEach
    void setUp() {
        idInscripcion = UUID.randomUUID();
        idInscripcionStr = idInscripcion.toString();
        testInscripcion = new InscripcionesPrueba();
        testInscripcion.setIdInscripcionPrueba(idInscripcion);
    }

    // ==================== getCarrerasElegidas (GET /{idInscripcion}/carreras) ====================

    @Test
    void getCarrerasElegidas_ConInscripcionExistente_Retorna200ConLista() {
        CarrerasElegida carrera = new CarrerasElegida();
        carrera.setPrioridad((short) 1);
        List<CarrerasElegida> lista = List.of(carrera);
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(testInscripcion);
        when(carrerasElegidaDAO.findByInscripcionOrderByPrioridad(idInscripcion)).thenReturn(lista);

        Response response = resource.getCarrerasElegidas(idInscripcionStr);

        assertEquals(200, response.getStatus());
        assertSame(lista, response.getEntity());
    }

    @Test
    void getCarrerasElegidas_ConListaVacia_Retorna200() {
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(testInscripcion);
        when(carrerasElegidaDAO.findByInscripcionOrderByPrioridad(idInscripcion)).thenReturn(Collections.emptyList());

        Response response = resource.getCarrerasElegidas(idInscripcionStr);

        assertEquals(200, response.getStatus());
    }

    @Test
    void getCarrerasElegidas_ConInscripcionInexistente_Retorna404() {
        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(null);

        Response response = resource.getCarrerasElegidas(idInscripcionStr);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verifyNoInteractions(carrerasElegidaDAO);
    }

    // ==================== addCarreraElegida (POST /{idInscripcion}/carreras) ====================

    @Test
    void addCarreraElegida_ConDatosValidos_Retorna201() {
        CatalogoCarrera catalogo = new CatalogoCarrera();
        catalogo.setIdCarrera("ISI");
        CarrerasElegida nuevaEleccion = new CarrerasElegida();
        nuevaEleccion.setCatalogoCarrera(catalogo);
        nuevaEleccion.setPrioridad((short) 1);

        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(testInscripcion);
        when(catalogoCarreraDAO.leer("ISI")).thenReturn(catalogo);
        when(carrerasElegidaDAO.existsByInscripcionAndCarrera(idInscripcion, "ISI")).thenReturn(false);
        when(carrerasElegidaDAO.existsByInscripcionAndPrioridad(idInscripcion, (short) 1)).thenReturn(false);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/inscripciones_prueba/1/carreras/ISI"));

        Response response = resource.addCarreraElegida(idInscripcionStr, nuevaEleccion, uriInfo);

        assertEquals(201, response.getStatus());
        verify(carrerasElegidaDAO).crear(nuevaEleccion);
    }

    @Test
    void addCarreraElegida_SinCatalogoCarrera_Retorna400() {
        CarrerasElegida sinCatalogo = new CarrerasElegida();
        sinCatalogo.setPrioridad((short) 1);

        Response response = resource.addCarreraElegida(idInscripcionStr, sinCatalogo, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesDAO, carrerasElegidaDAO);
    }

    @Test
    void addCarreraElegida_SinPrioridad_Retorna400() {
        CatalogoCarrera catalogo = new CatalogoCarrera();
        catalogo.setIdCarrera("MED");
        CarrerasElegida sinPrioridad = new CarrerasElegida();
        sinPrioridad.setCatalogoCarrera(catalogo);
        // prioridad null

        Response response = resource.addCarreraElegida(idInscripcionStr, sinPrioridad, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesDAO, carrerasElegidaDAO);
    }

    @Test
    void addCarreraElegida_ConCarreraDuplicada_Retorna409() {
        CatalogoCarrera catalogo = new CatalogoCarrera();
        catalogo.setIdCarrera("ISI");
        CarrerasElegida duplicada = new CarrerasElegida();
        duplicada.setCatalogoCarrera(catalogo);
        duplicada.setPrioridad((short) 2);

        when(inscripcionesDAO.leer(idInscripcion)).thenReturn(testInscripcion);
        when(catalogoCarreraDAO.leer("ISI")).thenReturn(catalogo);
        when(carrerasElegidaDAO.existsByInscripcionAndCarrera(idInscripcion, "ISI")).thenReturn(true);

        Response response = resource.addCarreraElegida(idInscripcionStr, duplicada, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verify(carrerasElegidaDAO, never()).crear(any());
    }

    // ==================== updatePrioridad (PUT /{idInscripcion}/carreras/{idCarrera}) ====================

    @Test
    void updatePrioridad_ConDatosValidos_Retorna200() {
        String idCarrera = "ISI";
        CarrerasElegida existente = new CarrerasElegida();
        existente.setPrioridad((short) 1);
        CarrerasElegida datosActualizacion = new CarrerasElegida();
        datosActualizacion.setPrioridad((short) 2);

        when(carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, idCarrera)).thenReturn(existente);
        when(carrerasElegidaDAO.existsByInscripcionAndPrioridad(idInscripcion, (short) 2)).thenReturn(false);
        when(carrerasElegidaDAO.actualizar(existente)).thenReturn(existente);

        Response response = resource.updatePrioridad(idInscripcionStr, idCarrera, datosActualizacion);

        assertEquals(200, response.getStatus());
        verify(carrerasElegidaDAO).actualizar(existente);
    }

    @Test
    void updatePrioridad_SinPrioridad_Retorna400() {
        CarrerasElegida sinPrioridad = new CarrerasElegida();

        Response response = resource.updatePrioridad(idInscripcionStr, "ISI", sinPrioridad);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void updatePrioridad_ConCarreraInexistente_Retorna404() {
        CarrerasElegida datosActualizacion = new CarrerasElegida();
        datosActualizacion.setPrioridad((short) 2);

        when(carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, "ISI")).thenReturn(null);

        Response response = resource.updatePrioridad(idInscripcionStr, "ISI", datosActualizacion);

        assertEquals(404, response.getStatus());
    }

    // ==================== deleteCarreraElegida (DELETE /{idInscripcion}/carreras/{idCarrera}) ====================

    @Test
    void deleteCarreraElegida_ConEntidadExistente_Retorna204() {
        String idCarrera = "ARQ";
        CarrerasElegida existente = new CarrerasElegida();
        when(carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, idCarrera)).thenReturn(existente);

        Response response = resource.deleteCarreraElegida(idInscripcionStr, idCarrera);

        assertEquals(204, response.getStatus());
        verify(carrerasElegidaDAO).eliminar(existente);
    }

    @Test
    void deleteCarreraElegida_ConEntidadInexistente_Retorna404() {
        when(carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, "ISI")).thenReturn(null);

        Response response = resource.deleteCarreraElegida(idInscripcionStr, "ISI");

        assertEquals(404, response.getStatus());
        verify(carrerasElegidaDAO, never()).eliminar(any());
    }
}
