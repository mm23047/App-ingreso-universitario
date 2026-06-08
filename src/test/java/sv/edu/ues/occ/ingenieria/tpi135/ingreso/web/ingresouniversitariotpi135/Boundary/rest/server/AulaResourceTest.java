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

    @Mock private AulaDAO dao;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    @InjectMocks private AulaResource resource;

    private Aula testAula;

    @BeforeEach
    void setUp() {
        testAula = new Aula();
        testAula.setCodigoAulaApi("AULA-001");
        testAula.setCapacidadFisica(30);
        testAula.setNombreSede("Sede Central");
        testAula.setDepartamento("San Salvador");
        testAula.setAccesibleSillaRuedas(false);
    }

    // ── GET (listAulas) ───────────────────────────────────────────────────────

    @Test
    void findRange_ConParametrosValidos_Retorna200ConLista() {
        List<Aula> lista = List.of(testAula);
        when(dao.count()).thenReturn(1);
        when(dao.findRange(0, 10)).thenReturn(lista);

        Response response = resource.listAulas(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void findRange_ConListaVacia_Retorna200ConTotalCero() {
        when(dao.count()).thenReturn(0);
        when(dao.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listAulas(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void findRange_ConExcepcionEnDAO_Retorna500() {
        when(dao.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listAulas(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ── POST (createAula) ─────────────────────────────────────────────────────

    @Test
    void createAula_ConCuerpoNulo_Retorna400() {
        Response response = resource.createAula(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(dao);
    }

    @Test
    void createAula_ConDatosCompletosIncluyendoSede_Retorna201() {
        doAnswer(inv -> {
            Aula a = inv.getArgument(0);
            a.setIdAula(UUID.randomUUID());
            return null;
        }).when(dao).crear(any(Aula.class));

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost:9080/resources/v1/aulas/test-id"));

        Response response = resource.createAula(testAula, uriInfo);

        assertEquals(201, response.getStatus());
        verify(dao).crear(testAula);
    }

    @Test
    void createAula_CuandoDAOLanzaIllegalArgumentPorSedeAusente_Retorna400() {
        // AulaDAO.validarLogicaNegocio() lanza IllegalArgumentException cuando nombreSede es null
        doThrow(new IllegalArgumentException("El nombre de la sede es obligatorio."))
                .when(dao).crear(any(Aula.class));

        Aula aulaSinSede = new Aula();
        aulaSinSede.setCodigoAulaApi("AULA-SIN-SEDE");
        aulaSinSede.setCapacidadFisica(20);
        // nombreSede intencionalmente ausente

        Response response = resource.createAula(aulaSinSede, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
    }

    @Test
    void createAula_CuandoDAOLanzaIllegalStatePorCodigoDuplicado_Retorna409() {
        // AulaDAO.crear() lanza IllegalStateException cuando el codigoAulaApi ya existe
        doThrow(new IllegalStateException("Ya existe un Aula registrada con el código API: AULA-001"))
                .when(dao).crear(any(Aula.class));

        Response response = resource.createAula(testAula, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }
}
