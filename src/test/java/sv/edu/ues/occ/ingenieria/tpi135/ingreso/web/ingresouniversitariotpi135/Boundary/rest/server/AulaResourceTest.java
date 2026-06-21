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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AulaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Aula;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AulaResourceTest {

    @Mock
    private AulaDAO aulaDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private AulaResource resource;

    private Aula entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidad = new Aula();
        entidad.setIdAula(testId);
        entidad.setCodigoAulaApi("AULA-001");
        entidad.setCapacidadFisica(30);
        entidad.setNombreSede("Sede Central");
        entidad.setDepartamento("San Salvador");
        entidad.setAccesibleSillaRuedas(false);
    }

    // ==================== listAulas (GET /) ====================

    @Test
    void listAulas_ConParametrosValidos_DebeRetornar200ConLista() {
        when(aulaDAO.findRange(0, 10)).thenReturn(List.of(entidad));
        when(aulaDAO.count()).thenReturn(1);

        Response response = resource.listAulas(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
        verify(aulaDAO).findRange(0, 10);
        verify(aulaDAO).count();
    }

    @Test
    void listAulas_ConListaVacia_DebeRetornar200ConTotalCero() {
        when(aulaDAO.findRange(0, 10)).thenReturn(Collections.emptyList());
        when(aulaDAO.count()).thenReturn(0);

        Response response = resource.listAulas(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void listAulas_ConExcepcionEnDAO_DebeRetornar500() {
        when(aulaDAO.findRange(anyInt(), anyInt())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listAulas(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== createAula (POST /) ====================

    @Test
    void createAula_ConDatosValidos_DebeRetornar201() {
        doAnswer(inv -> {
            Aula a = inv.getArgument(0);
            a.setIdAula(UUID.randomUUID());
            return null;
        }).when(aulaDAO).crear(any(Aula.class));

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/aulas/1"));

        Response response = resource.createAula(entidad, uriInfo);

        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());
        verify(aulaDAO).crear(entidad);
    }

    @Test
    void createAula_ConEntidadNula_DebeRetornar400() {
        Response response = resource.createAula(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(aulaDAO);
    }

    @Test
    void createAula_ConIllegalArgumentEnDAO_DebeRetornar400() {
        doThrow(new IllegalArgumentException("El nombre de la sede es obligatorio."))
                .when(aulaDAO).crear(any());

        Aula sinSede = new Aula();
        sinSede.setCodigoAulaApi("AULA-002");
        sinSede.setCapacidadFisica(20);

        Response response = resource.createAula(sinSede, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
    }

    @Test
    void createAula_ConCodigoDuplicado_DebeRetornar409() {
        doThrow(new IllegalStateException("Ya existe un Aula registrada con el código API: AULA-001"))
                .when(aulaDAO).crear(any());

        Response response = resource.createAula(entidad, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void createAula_ConExcepcionEnDAO_DebeRetornar500() {
        doThrow(new RuntimeException("Error de BD"))
                .when(aulaDAO).crear(any());

        Response response = resource.createAula(entidad, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getAula (GET /{idAula}) ====================

    @Test
    void getAula_ConIdExistente_DebeRetornar200() {
        when(aulaDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getAula(testId.toString());

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
        verify(aulaDAO).leer(testId);
    }

    @Test
    void getAula_ConIdInexistente_DebeRetornar404() {
        when(aulaDAO.leer(testId)).thenReturn(null);

        Response response = resource.getAula(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void getAula_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getAula("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(aulaDAO);
    }

    @Test
    void getAula_ConExcepcionEnDAO_DebeRetornar500() {
        when(aulaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getAula(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== updateAula (PUT /{idAula}) ====================

    @Test
    void updateAula_ConDatosValidos_DebeRetornar200() {
        when(aulaDAO.leer(testId)).thenReturn(entidad);
        Aula actualizada = new Aula();
        actualizada.setCodigoAulaApi("AULA-001-MOD");
        actualizada.setCapacidadFisica(50);
        when(aulaDAO.actualizar(actualizada)).thenReturn(actualizada);

        Response response = resource.updateAula(testId.toString(), actualizada);

        assertEquals(200, response.getStatus());
        assertSame(actualizada, response.getEntity());
        assertEquals(testId, actualizada.getIdAula());
        verify(aulaDAO).actualizar(actualizada);
    }

    @Test
    void updateAula_ConEntidadNula_DebeRetornar400() {
        Response response = resource.updateAula(testId.toString(), null);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(aulaDAO);
    }

    @Test
    void updateAula_ConIdInexistente_DebeRetornar404() {
        when(aulaDAO.leer(testId)).thenReturn(null);
        Aula datos = new Aula();
        datos.setCodigoAulaApi("AULA-X");

        Response response = resource.updateAula(testId.toString(), datos);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(aulaDAO, never()).actualizar(any());
    }

    @Test
    void updateAula_ConUuidInvalido_DebeRetornar400() {
        Aula datos = new Aula();
        datos.setCodigoAulaApi("AULA-X");

        Response response = resource.updateAula("no-es-uuid", datos);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(aulaDAO);
    }

    @Test
    void updateAula_ConIllegalArgumentEnActualizar_DebeRetornar400() {
        when(aulaDAO.leer(testId)).thenReturn(entidad);
        Aula datos = new Aula();
        datos.setCodigoAulaApi("AULA-X");
        when(aulaDAO.actualizar(datos))
                .thenThrow(new IllegalArgumentException("La capacidad física es obligatoria."));

        Response response = resource.updateAula(testId.toString(), datos);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
    }

    @Test
    void updateAula_ConCodigoDuplicado_DebeRetornar409() {
        when(aulaDAO.leer(testId)).thenReturn(entidad);
        Aula datos = new Aula();
        datos.setCodigoAulaApi("AULA-EXISTENTE");
        when(aulaDAO.actualizar(datos))
                .thenThrow(new IllegalStateException("Ya existe un Aula con el código API: AULA-EXISTENTE"));

        Response response = resource.updateAula(testId.toString(), datos);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void updateAula_ConExcepcionEnLeer_DebeRetornar500() {
        when(aulaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));
        Aula datos = new Aula();
        datos.setCodigoAulaApi("AULA-X");

        Response response = resource.updateAula(testId.toString(), datos);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void updateAula_ConExcepcionEnActualizar_DebeRetornar500() {
        when(aulaDAO.leer(testId)).thenReturn(entidad);
        Aula datos = new Aula();
        datos.setCodigoAulaApi("AULA-X");
        when(aulaDAO.actualizar(datos)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateAula(testId.toString(), datos);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== deleteAula (DELETE /{idAula}) ====================

    @Test
    void deleteAula_ConIdExistente_DebeRetornar204() {
        when(aulaDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.deleteAula(testId.toString());

        assertEquals(204, response.getStatus());
        verify(aulaDAO).eliminar(entidad);
    }

    @Test
    void deleteAula_ConIdInexistente_DebeRetornar404() {
        when(aulaDAO.leer(testId)).thenReturn(null);

        Response response = resource.deleteAula(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(aulaDAO, never()).eliminar(any());
    }

    @Test
    void deleteAula_ConDependenciasAsociadas_DebeRetornar409() {
        when(aulaDAO.leer(testId)).thenReturn(entidad);
        doThrow(new IllegalStateException("No se puede eliminar el aula, tiene turnos asociados"))
                .when(aulaDAO).eliminar(entidad);

        Response response = resource.deleteAula(testId.toString());

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void deleteAula_ConUuidInvalido_DebeRetornar500() {
        Response response = resource.deleteAula("no-es-uuid");

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
        verifyNoInteractions(aulaDAO);
    }

    @Test
    void deleteAula_ConExcepcionEnDAO_DebeRetornar500() {
        when(aulaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteAula(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
