package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CarrerasElegidaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId;
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
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private CarrerasElegidaResource resource;
    private CarrerasElegida entidad;
    private InscripcionesPrueba inscripcionEntidad;
    private CatalogoCarrera carreraEntidad;
    private UUID idInscripcion;
    private String idCarrera;

    @BeforeEach
    void setUp() {
        idInscripcion = UUID.randomUUID();
        idCarrera = "ISI";

        resource = new CarrerasElegidaResource();
        resource.carrerasElegidaDAO = carrerasElegidaDAO;

        CarrerasElegidaId pk = new CarrerasElegidaId();
        pk.setIdInscripcion(idInscripcion);
        pk.setIdCarrera(idCarrera);

        entidad = new CarrerasElegida();
        entidad.setId(pk);
        inscripcionEntidad = new InscripcionesPrueba();
        carreraEntidad = new CatalogoCarrera();

        entidad.setIdInscripcion(inscripcionEntidad);
        entidad.setIdCarrera(carreraEntidad);
        entidad.setPrioridad((short) 1);
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200() {
        when(carrerasElegidaDAO.count()).thenReturn(1);
        when(carrerasElegidaDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200() {
        when(carrerasElegidaDAO.count()).thenReturn(0);
        when(carrerasElegidaDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(carrerasElegidaDAO.count()).thenThrow(new RuntimeException("BD error"));
        Response response = resource.findRange(0, 10);
        assertEquals(500, response.getStatus());
    }

    // ==================== findById (GET /{idInscripcion}/{idCarrera}) ====================

    @Test
    void findById_ConPKExistente_DebeRetornar200() {
        when(carrerasElegidaDAO.leer(any(CarrerasElegidaId.class))).thenReturn(entidad);

        Response response = resource.findById(idInscripcion, idCarrera);

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
    }

    @Test
    void findById_ConPKInexistente_DebeRetornar404() {
        when(carrerasElegidaDAO.leer(any(CarrerasElegidaId.class))).thenReturn(null);

        Response response = resource.findById(idInscripcion, idCarrera);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConParametroNulo_DebeRetornar422() {
        Response response = resource.findById(null, idCarrera);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void findById_ConCarreraNula_DebeRetornar422() {
        Response response = resource.findById(idInscripcion, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(carrerasElegidaDAO.leer(any(CarrerasElegidaId.class))).thenThrow(new RuntimeException("BD error"));

        Response response = resource.findById(idInscripcion, idCarrera);

        assertEquals(500, response.getStatus());
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        CarrerasElegida nueva = new CarrerasElegida();
        nueva.setId(entidad.getId());
        nueva.setIdInscripcion(inscripcionEntidad);
        nueva.setIdCarrera(carreraEntidad);
        nueva.setPrioridad((short) 2);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/carreras_elegidas/" + idInscripcion + "/" + idCarrera));

        Response response = resource.create(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(carrerasElegidaDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void create_ConIdNulo_DebeRetornar422() {
        CarrerasElegida nueva = new CarrerasElegida();
        nueva.setPrioridad((short) 1);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void create_SinPrioridad_DebeRetornar422() {
        CarrerasElegida nueva = new CarrerasElegida();
        nueva.setId(entidad.getId());
        nueva.setIdInscripcion(inscripcionEntidad);
        nueva.setIdCarrera(carreraEntidad);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void create_SinIdInscripcionEnPk_DebeRetornar422() {
        CarrerasElegidaId pkInvalido = new CarrerasElegidaId();
        pkInvalido.setIdInscripcion(null);
        pkInvalido.setIdCarrera(idCarrera);

        CarrerasElegida nueva = new CarrerasElegida();
        nueva.setId(pkInvalido);
        nueva.setIdInscripcion(inscripcionEntidad);
        nueva.setIdCarrera(carreraEntidad);
        nueva.setPrioridad((short) 1);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void create_SinIdCarreraEnPk_DebeRetornar422() {
        CarrerasElegidaId pkInvalido = new CarrerasElegidaId();
        pkInvalido.setIdInscripcion(idInscripcion);
        pkInvalido.setIdCarrera(null);

        CarrerasElegida nueva = new CarrerasElegida();
        nueva.setId(pkInvalido);
        nueva.setIdInscripcion(inscripcionEntidad);
        nueva.setIdCarrera(carreraEntidad);
        nueva.setPrioridad((short) 1);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void create_SinEntidadInscripcion_DebeRetornar422() {
        CarrerasElegida nueva = new CarrerasElegida();
        nueva.setId(entidad.getId());
        nueva.setIdInscripcion(null);
        nueva.setIdCarrera(carreraEntidad);
        nueva.setPrioridad((short) 1);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void create_SinEntidadCarrera_DebeRetornar422() {
        CarrerasElegida nueva = new CarrerasElegida();
        nueva.setId(entidad.getId());
        nueva.setIdInscripcion(inscripcionEntidad);
        nueva.setIdCarrera(null);
        nueva.setPrioridad((short) 1);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        CarrerasElegida nueva = new CarrerasElegida();
        nueva.setId(entidad.getId());
        nueva.setIdInscripcion(inscripcionEntidad);
        nueva.setIdCarrera(carreraEntidad);
        nueva.setPrioridad((short) 3);

        doThrow(new RuntimeException("BD error")).when(carrerasElegidaDAO).crear(any());

        Response response = resource.create(nueva, uriInfo);

        assertEquals(500, response.getStatus());
    }

    // ==================== update (PUT /{idInscripcion}/{idCarrera}) ====================

    @Test
    void update_ConPKYEntidadValidos_DebeRetornar200() {
        when(carrerasElegidaDAO.leer(any(CarrerasElegidaId.class))).thenReturn(entidad);
        CarrerasElegida actualizada = new CarrerasElegida();
        actualizada.setPrioridad((short) 2);

        Response response = resource.update(idInscripcion, idCarrera, actualizada);

        assertEquals(200, response.getStatus());
        verify(carrerasElegidaDAO).actualizar(actualizada);
    }

    @Test
    void update_ConParametroNulo_DebeRetornar422() {
        Response response = resource.update(null, idCarrera, entidad);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void update_ConCarreraNula_DebeRetornar422() {
        Response response = resource.update(idInscripcion, null, entidad);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(idInscripcion, idCarrera, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void update_ConPKInexistente_DebeRetornar404() {
        when(carrerasElegidaDAO.leer(any(CarrerasElegidaId.class))).thenReturn(null);

        Response response = resource.update(idInscripcion, idCarrera, entidad);

        assertEquals(404, response.getStatus());
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(carrerasElegidaDAO.leer(any(CarrerasElegidaId.class))).thenThrow(new RuntimeException("BD error"));

        Response response = resource.update(idInscripcion, idCarrera, entidad);

        assertEquals(500, response.getStatus());
    }

    // ==================== delete (DELETE /{idInscripcion}/{idCarrera}) ====================

    @Test
    void delete_ConPKExistente_DebeRetornar204() {
        when(carrerasElegidaDAO.leer(any(CarrerasElegidaId.class))).thenReturn(entidad);

        Response response = resource.delete(idInscripcion, idCarrera);

        assertEquals(204, response.getStatus());
        verify(carrerasElegidaDAO).eliminar(entidad);
    }

    @Test
    void delete_ConParametroNulo_DebeRetornar422() {
        Response response = resource.delete(null, idCarrera);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void delete_ConCarreraNula_DebeRetornar422() {
        Response response = resource.delete(idInscripcion, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(carrerasElegidaDAO);
    }

    @Test
    void delete_ConPKInexistente_DebeRetornar404() {
        when(carrerasElegidaDAO.leer(any(CarrerasElegidaId.class))).thenReturn(null);

        Response response = resource.delete(idInscripcion, idCarrera);

        assertEquals(404, response.getStatus());
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(carrerasElegidaDAO.leer(any(CarrerasElegidaId.class))).thenThrow(new RuntimeException("BD error"));

        Response response = resource.delete(idInscripcion, idCarrera);

        assertEquals(500, response.getStatus());
    }
}
