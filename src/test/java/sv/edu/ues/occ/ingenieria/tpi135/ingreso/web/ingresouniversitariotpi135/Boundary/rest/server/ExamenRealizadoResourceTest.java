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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenRealizado;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamenRealizadoResourceTest {

    @Mock private ExamenRealizadoDAO dao;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;
    @InjectMocks private ExamenRealizadoResource resource;

    private ExamenRealizado testExamen;
    private UUID examenId;

    @BeforeEach
    void setUp() {
        examenId = UUID.randomUUID();
        testExamen = new ExamenRealizado();
        testExamen.setIdExamenRealizado(examenId);
    }

    // ==================== iniciarExamen (POST /) ====================

    @Test
    void iniciarExamen_ConDtNulo_Retorna400() {
        Response response = resource.iniciarExamen(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-Parameter"));
        verifyNoInteractions(dao);
    }

    @Test
    void iniciarExamen_SinIdInscripcion_Retorna400() {
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdEtapa(UUID.randomUUID());
        // idInscripcion null

        Response response = resource.iniciarExamen(dto, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-Parameter"));
        verifyNoInteractions(dao);
    }

    @Test
    void iniciarExamen_ConDatosValidos_Retorna201() {
        UUID inscripcionId = UUID.randomUUID();
        UUID etapaId = UUID.randomUUID();

        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(inscripcionId);
        dto.setIdEtapa(etapaId);

        when(dao.iniciarExamenAspirante(inscripcionId, etapaId)).thenReturn(testExamen);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/examen_realizado/" + examenId));

        Response response = resource.iniciarExamen(dto, uriInfo);

        assertEquals(201, response.getStatus());
        assertSame(testExamen, response.getEntity());
        verify(dao).iniciarExamenAspirante(inscripcionId, etapaId);
    }

    @Test
    void iniciarExamen_ConExcepcionDeNegocio_Retorna409() {
        UUID inscripcionId = UUID.randomUUID();
        UUID etapaId = UUID.randomUUID();

        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(inscripcionId);
        dto.setIdEtapa(etapaId);

        when(dao.iniciarExamenAspirante(inscripcionId, etapaId))
            .thenThrow(new IllegalStateException("Ya existe examen para esta inscripción"));

        Response response = resource.iniciarExamen(dto, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    // ==================== calificarExamen (POST /{id}/calificar) ====================

    @Test
    void calificarExamen_ConIdExistente_Retorna200() {
        when(dao.leer(examenId)).thenReturn(testExamen);
        when(dao.calificarExamen(examenId)).thenReturn(testExamen);

        Response response = resource.calificarExamen(examenId.toString());

        assertEquals(200, response.getStatus());
        assertSame(testExamen, response.getEntity());
        verify(dao).calificarExamen(examenId);
    }

    @Test
    void calificarExamen_ConIdInexistente_Retorna404() {
        when(dao.leer(examenId)).thenReturn(null);

        Response response = resource.calificarExamen(examenId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(dao, never()).calificarExamen(any());
    }

    @Test
    void calificarExamen_ConIdFormatoInvalido_Retorna400() {
        Response response = resource.calificarExamen("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(dao);
    }

    // ==================== getExamen (GET /{id}) ====================

    @Test
    void getExamen_ConIdExistente_Retorna200() {
        when(dao.leer(examenId)).thenReturn(testExamen);

        Response response = resource.getExamen(examenId.toString());

        assertEquals(200, response.getStatus());
        assertSame(testExamen, response.getEntity());
    }

    @Test
    void getExamen_ConIdInexistente_Retorna404() {
        when(dao.leer(examenId)).thenReturn(null);

        Response response = resource.getExamen(examenId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void getExamen_ConIdFormatoInvalido_Retorna400() {
        Response response = resource.getExamen("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(dao);
    }

    @Test
    void getExamen_ConExcepcionEnDAO_Retorna500() {
        when(dao.leer(any())).thenThrow(new RuntimeException("BD error"));

        Response response = resource.getExamen(examenId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
