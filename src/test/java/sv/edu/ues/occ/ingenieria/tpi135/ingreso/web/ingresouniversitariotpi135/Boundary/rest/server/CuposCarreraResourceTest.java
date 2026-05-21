package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CuposCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuposCarreraResourceTest {

    @Mock private CuposCarreraDAO cuposCarreraDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private CuposCarreraResource resource;
    private CuposCarrera entidad;
    private PruebasAdmision prueba;
    private CatalogoCarrera carrera;
    private EtapasAdmision etapa;
    private UUID idPrueba;
    private UUID idEtapa;
    private String idCarrera;

    @BeforeEach
    void setUp() {
        idPrueba = UUID.randomUUID();
        idEtapa = UUID.randomUUID();
        idCarrera = "ICS";

        resource = new CuposCarreraResource();
        resource.cuposCarreraDAO = cuposCarreraDAO;

        prueba = new PruebasAdmision();
        carrera = new CatalogoCarrera();
        etapa = new EtapasAdmision();

        CuposCarreraId pk = new CuposCarreraId();
        pk.setIdPrueba(idPrueba);
        pk.setIdCarrera(idCarrera);
        pk.setIdEtapa(idEtapa);

        entidad = new CuposCarrera();
        entidad.setIdCupoCarrera(pk);
    entidad.setPruebaAdmision(prueba);
    entidad.setCatalogoCarrera(carrera);
    entidad.setEtapaAdmision(etapa);
        entidad.setCupos(50);
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200() {
        when(cuposCarreraDAO.count()).thenReturn(1);
        when(cuposCarreraDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200() {
        when(cuposCarreraDAO.count()).thenReturn(0);
        when(cuposCarreraDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(cuposCarreraDAO.count()).thenThrow(new RuntimeException("BD error"));
        Response response = resource.findRange(0, 10);
        assertEquals(500, response.getStatus());
    }

    // ==================== findById (GET /{idPrueba}/{idCarrera}/{idEtapa}) ====================

    @Test
    void findById_ConPKExistente_DebeRetornar200() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(entidad);

        Response response = resource.findById(idPrueba, idCarrera, idEtapa);

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
    }

    @Test
    void findById_ConPKInexistente_DebeRetornar404() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(null);

        Response response = resource.findById(idPrueba, idCarrera, idEtapa);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConParametroNulo_DebeRetornar422() {
        Response response = resource.findById(null, idCarrera, idEtapa);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void findById_ConCarreraNula_DebeRetornar422() {
        Response response = resource.findById(idPrueba, null, idEtapa);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void findById_ConEtapaNula_DebeRetornar422() {
        Response response = resource.findById(idPrueba, idCarrera, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenThrow(new RuntimeException("BD error"));

        Response response = resource.findById(idPrueba, idCarrera, idEtapa);

        assertEquals(500, response.getStatus());
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        CuposCarrera nueva = new CuposCarrera();
        nueva.setIdCupoCarrera(entidad.getIdCupoCarrera());
        nueva.setPruebaAdmision(prueba);
        nueva.setCatalogoCarrera(carrera);
        nueva.setEtapaAdmision(etapa);
        nueva.setCupos(60);

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/cupos_carrera/" + idPrueba + "/" + idCarrera + "/" + idEtapa));

        Response response = resource.create(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(cuposCarreraDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void create_ConIdNulo_DebeRetornar422() {
        CuposCarrera nueva = new CuposCarrera();
        nueva.setCupos(10);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void create_SinCupos_DebeRetornar422() {
        CuposCarrera nueva = new CuposCarrera();
        nueva.setIdCupoCarrera(entidad.getIdCupoCarrera());
        nueva.setPruebaAdmision(prueba);
        nueva.setCatalogoCarrera(carrera);
        nueva.setEtapaAdmision(etapa);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void create_SinIdPruebaEnPk_DebeRetornar422() {
        CuposCarreraId pkInvalido = new CuposCarreraId();
        pkInvalido.setIdPrueba(null);
        pkInvalido.setIdCarrera(idCarrera);
        pkInvalido.setIdEtapa(idEtapa);

        CuposCarrera nueva = new CuposCarrera();
        nueva.setIdCupoCarrera(pkInvalido);
        nueva.setPruebaAdmision(prueba);
        nueva.setCatalogoCarrera(carrera);
        nueva.setEtapaAdmision(etapa);
        nueva.setCupos(10);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void create_SinIdCarreraEnPk_DebeRetornar422() {
        CuposCarreraId pkInvalido = new CuposCarreraId();
        pkInvalido.setIdPrueba(idPrueba);
        pkInvalido.setIdCarrera(null);
        pkInvalido.setIdEtapa(idEtapa);

        CuposCarrera nueva = new CuposCarrera();
        nueva.setIdCupoCarrera(pkInvalido);
        nueva.setPruebaAdmision(prueba);
        nueva.setCatalogoCarrera(carrera);
        nueva.setEtapaAdmision(etapa);
        nueva.setCupos(10);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void create_SinIdEtapaEnPk_DebeRetornar422() {
        CuposCarreraId pkInvalido = new CuposCarreraId();
        pkInvalido.setIdPrueba(idPrueba);
        pkInvalido.setIdCarrera(idCarrera);
        pkInvalido.setIdEtapa(null);

        CuposCarrera nueva = new CuposCarrera();
        nueva.setIdCupoCarrera(pkInvalido);
        nueva.setPruebaAdmision(prueba);
        nueva.setCatalogoCarrera(carrera);
        nueva.setEtapaAdmision(etapa);
        nueva.setCupos(10);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void create_SinEntidadPrueba_DebeRetornar422() {
        CuposCarrera nueva = new CuposCarrera();
        nueva.setIdCupoCarrera(entidad.getIdCupoCarrera());
        nueva.setPruebaAdmision(null);
        nueva.setCatalogoCarrera(carrera);
        nueva.setEtapaAdmision(etapa);
        nueva.setCupos(10);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void create_SinEntidadCarrera_DebeRetornar422() {
        CuposCarrera nueva = new CuposCarrera();
        nueva.setIdCupoCarrera(entidad.getIdCupoCarrera());
        nueva.setPruebaAdmision(prueba);
        nueva.setCatalogoCarrera(null);
        nueva.setEtapaAdmision(etapa);
        nueva.setCupos(10);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void create_SinEntidadEtapa_DebeRetornar422() {
        CuposCarrera nueva = new CuposCarrera();
        nueva.setIdCupoCarrera(entidad.getIdCupoCarrera());
        nueva.setPruebaAdmision(prueba);
        nueva.setCatalogoCarrera(carrera);
        nueva.setEtapaAdmision(null);
        nueva.setCupos(10);

        Response response = resource.create(nueva, uriInfo);

        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        CuposCarrera nueva = new CuposCarrera();
        nueva.setIdCupoCarrera(entidad.getIdCupoCarrera());
        nueva.setPruebaAdmision(prueba);
        nueva.setCatalogoCarrera(carrera);
        nueva.setEtapaAdmision(etapa);
        nueva.setCupos(10);

        doThrow(new RuntimeException("BD error")).when(cuposCarreraDAO).crear(any());

        Response response = resource.create(nueva, uriInfo);

        assertEquals(500, response.getStatus());
    }

    // ==================== update (PUT /{idPrueba}/{idCarrera}/{idEtapa}) ====================

    @Test
    void update_ConPKYEntidadValidos_DebeRetornar200() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(entidad);
        CuposCarrera actualizada = new CuposCarrera();
        actualizada.setCupos(70);

        Response response = resource.update(idPrueba, idCarrera, idEtapa, actualizada);

        assertEquals(200, response.getStatus());
        verify(cuposCarreraDAO).actualizar(actualizada);
    }

    @Test
    void update_ConParametroNulo_DebeRetornar422() {
        Response response = resource.update(null, idCarrera, idEtapa, entidad);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void update_ConCarreraNula_DebeRetornar422() {
        Response response = resource.update(idPrueba, null, idEtapa, entidad);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void update_ConEtapaNula_DebeRetornar422() {
        Response response = resource.update(idPrueba, idCarrera, null, entidad);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(idPrueba, idCarrera, idEtapa, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void update_ConPKInexistente_DebeRetornar404() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(null);

        Response response = resource.update(idPrueba, idCarrera, idEtapa, entidad);

        assertEquals(404, response.getStatus());
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenThrow(new RuntimeException("BD error"));

        Response response = resource.update(idPrueba, idCarrera, idEtapa, entidad);

        assertEquals(500, response.getStatus());
    }

    // ==================== delete (DELETE /{idPrueba}/{idCarrera}/{idEtapa}) ====================

    @Test
    void delete_ConPKExistente_DebeRetornar204() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(entidad);

        Response response = resource.delete(idPrueba, idCarrera, idEtapa);

        assertEquals(204, response.getStatus());
        verify(cuposCarreraDAO).eliminar(entidad);
    }

    @Test
    void delete_ConParametroNulo_DebeRetornar422() {
        Response response = resource.delete(null, idCarrera, idEtapa);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void delete_ConCarreraNula_DebeRetornar422() {
        Response response = resource.delete(idPrueba, null, idEtapa);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void delete_ConEtapaNula_DebeRetornar422() {
        Response response = resource.delete(idPrueba, idCarrera, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void delete_ConPKInexistente_DebeRetornar404() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(null);

        Response response = resource.delete(idPrueba, idCarrera, idEtapa);

        assertEquals(404, response.getStatus());
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenThrow(new RuntimeException("BD error"));

        Response response = resource.delete(idPrueba, idCarrera, idEtapa);

        assertEquals(500, response.getStatus());
    }
}
