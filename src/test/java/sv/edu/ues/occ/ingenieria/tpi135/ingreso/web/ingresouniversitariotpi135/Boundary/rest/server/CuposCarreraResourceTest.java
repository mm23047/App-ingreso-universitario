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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CuposCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuposCarreraResourceTest {

    @Mock
    private CuposCarreraDAO cuposCarreraDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private CuposCarreraResource resource;

    private CuposCarrera entidad;
    private CuposCarreraId idCompuesto;
    private UUID idPrueba;
    private String idCarrera;
    private UUID idEtapa;

    @BeforeEach
    void setUp() {
        idPrueba = UUID.randomUUID();
        idCarrera = "ING01";
        idEtapa = UUID.randomUUID();

        idCompuesto = new CuposCarreraId();
        idCompuesto.setIdPrueba(idPrueba);
        idCompuesto.setIdCarrera(idCarrera);
        idCompuesto.setIdEtapa(idEtapa);

        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(idPrueba);

        CatalogoCarrera carrera = new CatalogoCarrera();
        carrera.setIdCarrera(idCarrera);

        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setIdEtapaAdmision(idEtapa);

        entidad = new CuposCarrera();
        entidad.setIdCupoCarrera(idCompuesto);
        entidad.setPruebaAdmision(prueba);
        entidad.setCatalogoCarrera(carrera);
        entidad.setEtapaAdmision(etapa);
        entidad.setCupos(30);
    }

    // ==================== listCupos (GET /) ====================

    @Test
    void listCupos_ConParametrosValidos_DebeRetornar200ConLista() {
        when(cuposCarreraDAO.findRange(0, 10)).thenReturn(List.of(entidad));
        when(cuposCarreraDAO.count()).thenReturn(1);

        Response response = resource.listCupos(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(cuposCarreraDAO).findRange(0, 10);
        verify(cuposCarreraDAO).count();
    }

    @Test
    void listCupos_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(cuposCarreraDAO.findRange(0, 50)).thenReturn(Collections.emptyList());
        when(cuposCarreraDAO.count()).thenReturn(0);

        Response response = resource.listCupos(0, 50);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void listCupos_ConExcepcionEnDAO_DebeRetornar500() {
        when(cuposCarreraDAO.findRange(anyInt(), anyInt())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listCupos(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== createCupo (POST /) ====================

    @Test
    void createCupo_ConDatosValidos_DebeRetornar201() {
        doAnswer(inv -> {
            CuposCarrera c = inv.getArgument(0);
            if (c.getIdCupoCarrera() == null) {
                CuposCarreraId pk = new CuposCarreraId();
                pk.setIdPrueba(c.getPruebaAdmision().getIdPruebaAdmision());
                pk.setIdCarrera(c.getCatalogoCarrera().getIdCarrera());
                pk.setIdEtapa(c.getEtapaAdmision().getIdEtapaAdmision());
                c.setIdCupoCarrera(pk);
            }
            return null;
        }).when(cuposCarreraDAO).crear(any(CuposCarrera.class));

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/cupos_carrera/" + idPrueba + "/" + idCarrera + "/" + idEtapa));

        Response response = resource.createCupo(entidad, uriInfo);

        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());
        verify(cuposCarreraDAO).crear(entidad);
    }

    @Test
    void createCupo_ConEntidadNula_DebeRetornar400() {
        Response response = resource.createCupo(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void createCupo_SinPruebaAdmision_DebeRetornar400() {
        entidad.setPruebaAdmision(null);

        Response response = resource.createCupo(entidad, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void createCupo_SinCatalogoCarrera_DebeRetornar400() {
        entidad.setCatalogoCarrera(null);

        Response response = resource.createCupo(entidad, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void createCupo_SinEtapaAdmision_DebeRetornar400() {
        entidad.setEtapaAdmision(null);

        Response response = resource.createCupo(entidad, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void createCupo_SinCupos_DebeRetornar400() {
        entidad.setCupos(null);

        Response response = resource.createCupo(entidad, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void createCupo_ConIllegalArgumentEnDAO_DebeRetornar400() {
        doThrow(new IllegalArgumentException("Cupos negativos"))
                .when(cuposCarreraDAO).crear(any());

        Response response = resource.createCupo(entidad, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    @Test
    void createCupo_ConExcepcionEnDAO_DebeRetornar500() {
        doThrow(new RuntimeException("Error de BD"))
                .when(cuposCarreraDAO).crear(any());

        Response response = resource.createCupo(entidad, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== getCupo (GET /{idPrueba}/{idCarrera}/{idEtapa}) ====================

    @Test
    void getCupo_ConIdExistente_DebeRetornar200() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(entidad);

        Response response = resource.getCupo(idPrueba.toString(), idCarrera, idEtapa.toString());

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
    }

    @Test
    void getCupo_ConIdInexistente_DebeRetornar404() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(null);

        Response response = resource.getCupo(idPrueba.toString(), idCarrera, idEtapa.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void getCupo_ConUUIDFormatoInvalido_DebeRetornar400() {
        Response response = resource.getCupo("no-es-uuid", idCarrera, idEtapa.toString());

        assertEquals(400, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void getCupo_ConEtapaFormatoInvalido_DebeRetornar400() {
        Response response = resource.getCupo(idPrueba.toString(), idCarrera, "no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void getCupo_ConExcepcionEnDAO_DebeRetornar500() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class)))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getCupo(idPrueba.toString(), idCarrera, idEtapa.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== updateCupo (PUT /{idPrueba}/{idCarrera}/{idEtapa}) ====================

    @Test
    void updateCupo_ConDatosValidos_DebeRetornar200() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(entidad);

        CuposCarrera datosActualizacion = new CuposCarrera();
        datosActualizacion.setCupos(50);

        CuposCarrera actualizado = new CuposCarrera();
        actualizado.setIdCupoCarrera(idCompuesto);
        actualizado.setCupos(50);
        when(cuposCarreraDAO.actualizar(entidad)).thenReturn(actualizado);

        Response response = resource.updateCupo(
                idPrueba.toString(), idCarrera, idEtapa.toString(), datosActualizacion);

        assertEquals(200, response.getStatus());
        CuposCarrera resultado = (CuposCarrera) response.getEntity();
        assertEquals(50, resultado.getCupos());
        verify(cuposCarreraDAO).actualizar(entidad);
    }

    @Test
    void updateCupo_ConIdInexistente_DebeRetornar404() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(null);

        CuposCarrera datosActualizacion = new CuposCarrera();
        datosActualizacion.setCupos(50);

        Response response = resource.updateCupo(
                idPrueba.toString(), idCarrera, idEtapa.toString(), datosActualizacion);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
        verify(cuposCarreraDAO, never()).actualizar(any());
    }

    @Test
    void updateCupo_ConDatosNulos_DebeRetornar400() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(entidad);

        Response response = resource.updateCupo(
                idPrueba.toString(), idCarrera, idEtapa.toString(), null);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(cuposCarreraDAO, never()).actualizar(any());
    }

    @Test
    void updateCupo_SinCupos_DebeRetornar400() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(entidad);

        CuposCarrera sinCupos = new CuposCarrera();

        Response response = resource.updateCupo(
                idPrueba.toString(), idCarrera, idEtapa.toString(), sinCupos);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verify(cuposCarreraDAO, never()).actualizar(any());
    }

    @Test
    void updateCupo_ConUUIDFormatoInvalido_DebeRetornar400() {
        CuposCarrera datosActualizacion = new CuposCarrera();
        datosActualizacion.setCupos(50);

        Response response = resource.updateCupo(
                "no-es-uuid", idCarrera, idEtapa.toString(), datosActualizacion);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void updateCupo_ConIllegalArgumentEnDAO_DebeRetornar400() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(entidad);

        CuposCarrera datosActualizacion = new CuposCarrera();
        datosActualizacion.setCupos(-5);

        when(cuposCarreraDAO.actualizar(entidad))
                .thenThrow(new IllegalArgumentException("Cupos negativos"));

        Response response = resource.updateCupo(
                idPrueba.toString(), idCarrera, idEtapa.toString(), datosActualizacion);

        assertEquals(400, response.getStatus());
    }

    @Test
    void updateCupo_ConExcepcionEnDAO_DebeRetornar500() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(entidad);

        CuposCarrera datosActualizacion = new CuposCarrera();
        datosActualizacion.setCupos(50);

        when(cuposCarreraDAO.actualizar(any()))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateCupo(
                idPrueba.toString(), idCarrera, idEtapa.toString(), datosActualizacion);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== deleteCupo (DELETE /{idPrueba}/{idCarrera}/{idEtapa}) ====================

    @Test
    void deleteCupo_ConIdExistente_DebeRetornar204() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(entidad);

        Response response = resource.deleteCupo(idPrueba.toString(), idCarrera, idEtapa.toString());

        assertEquals(204, response.getStatus());
        verify(cuposCarreraDAO).eliminar(entidad);
    }

    @Test
    void deleteCupo_ConIdInexistente_DebeRetornar404() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class))).thenReturn(null);

        Response response = resource.deleteCupo(idPrueba.toString(), idCarrera, idEtapa.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
        verify(cuposCarreraDAO, never()).eliminar(any());
    }

    @Test
    void deleteCupo_ConUUIDFormatoInvalido_DebeRetornar500() {
        Response response = resource.deleteCupo("no-es-uuid", idCarrera, idEtapa.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
        verifyNoInteractions(cuposCarreraDAO);
    }

    @Test
    void deleteCupo_ConExcepcionEnDAO_DebeRetornar500() {
        when(cuposCarreraDAO.leer(any(CuposCarreraId.class)))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteCupo(idPrueba.toString(), idCarrera, idEtapa.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }
}
