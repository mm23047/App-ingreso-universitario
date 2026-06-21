package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ejb.EJBException;
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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntasPorClaveDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ProcesoAdmisionAspiranteDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamenRealizadoResourceTest {

    @Mock private ExamenRealizadoDAO examenRealizadoDAO;
    @Mock private PreguntasPorClaveDAO preguntasPorClaveDAO;
    @Mock private ProcesoAdmisionAspiranteDAO procesoAdmisionDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;
    @InjectMocks private ExamenRealizadoResource resource;

    private ExamenRealizado examen;
    private UUID examenId;
    private UUID inscripcionId;
    private UUID etapaId;
    private UUID aspiranteId;
    private UUID claveId;

    @BeforeEach
    void setUp() {
        examenId = UUID.randomUUID();
        inscripcionId = UUID.randomUUID();
        etapaId = UUID.randomUUID();
        aspiranteId = UUID.randomUUID();
        claveId = UUID.randomUUID();

        AspirantesDato aspirante = new AspirantesDato();
        aspirante.setId(aspiranteId);

        InscripcionesPrueba inscripcion = new InscripcionesPrueba();
        inscripcion.setIdInscripcionPrueba(inscripcionId);
        inscripcion.setAspiranteDato(aspirante);

        ClavesExamen clave = new ClavesExamen();
        clave.setIdClaveExaman(claveId);

        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setIdEtapaAdmision(etapaId);

        examen = new ExamenRealizado();
        examen.setIdExamenRealizado(examenId);
        examen.setInscripcionesPrueba(inscripcion);
        examen.setClaveExamen(clave);
        examen.setEtapaAdmision(etapa);
    }

    // ==================== getExamen (GET /{idExamen}) ====================

    @Test
    void getExamen_ConIdExistente_DebeRetornar200() {
        when(examenRealizadoDAO.leer(examenId)).thenReturn(examen);

        Response response = resource.getExamen(examenId.toString());

        assertEquals(200, response.getStatus());
        assertSame(examen, response.getEntity());
    }

    @Test
    void getExamen_ConIdInexistente_DebeRetornar404() {
        when(examenRealizadoDAO.leer(examenId)).thenReturn(null);

        Response response = resource.getExamen(examenId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void getExamen_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getExamen("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void getExamen_ConExcepcionEnDAO_DebeRetornar500() {
        when(examenRealizadoDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getExamen(examenId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== iniciarExamen (POST /) ====================

    @Test
    void iniciarExamen_ConDatosValidos_DebeRetornar201() {
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(inscripcionId);
        dto.setIdEtapa(etapaId);

        when(examenRealizadoDAO.iniciarExamenAspirante(inscripcionId, etapaId)).thenReturn(examen);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/examen_realizado/" + examenId));

        Response response = resource.iniciarExamen(dto, uriInfo);

        assertEquals(201, response.getStatus());
        assertSame(examen, response.getEntity());
    }

    @Test
    void iniciarExamen_ConDtoNulo_DebeRetornar400() {
        Response response = resource.iniciarExamen(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-Parameter"));
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void iniciarExamen_SinIdInscripcion_DebeRetornar400() {
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdEtapa(etapaId);

        Response response = resource.iniciarExamen(dto, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void iniciarExamen_SinIdEtapa_DebeRetornar400() {
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(inscripcionId);

        Response response = resource.iniciarExamen(dto, uriInfo);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void iniciarExamen_ConIllegalArgument_DebeRetornar400() {
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(inscripcionId);
        dto.setIdEtapa(etapaId);
        when(examenRealizadoDAO.iniciarExamenAspirante(inscripcionId, etapaId))
                .thenThrow(new IllegalArgumentException("Inscripción no encontrada"));

        Response response = resource.iniciarExamen(dto, uriInfo);

        assertEquals(400, response.getStatus());
    }

    @Test
    void iniciarExamen_ConIllegalState_DebeRetornar409() {
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(inscripcionId);
        dto.setIdEtapa(etapaId);
        when(examenRealizadoDAO.iniciarExamenAspirante(inscripcionId, etapaId))
                .thenThrow(new IllegalStateException("Ya existe examen para esta inscripción"));

        Response response = resource.iniciarExamen(dto, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void iniciarExamen_ConEJBExceptionConISECausa_DebeRetornar409() {
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(inscripcionId);
        dto.setIdEtapa(etapaId);
        when(examenRealizadoDAO.iniciarExamenAspirante(inscripcionId, etapaId))
                .thenThrow(new EJBException(new IllegalStateException("Conflicto")));

        Response response = resource.iniciarExamen(dto, uriInfo);

        assertEquals(409, response.getStatus());
    }

    @Test
    void iniciarExamen_ConEJBExceptionConIAECausa_DebeRetornar400() {
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(inscripcionId);
        dto.setIdEtapa(etapaId);
        when(examenRealizadoDAO.iniciarExamenAspirante(inscripcionId, etapaId))
                .thenThrow(new EJBException(new IllegalArgumentException("Datos inválidos")));

        Response response = resource.iniciarExamen(dto, uriInfo);

        assertEquals(400, response.getStatus());
    }

    @Test
    void iniciarExamen_ConEJBExceptionGenerica_DebeRetornar500() {
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(inscripcionId);
        dto.setIdEtapa(etapaId);
        when(examenRealizadoDAO.iniciarExamenAspirante(inscripcionId, etapaId))
                .thenThrow(new EJBException("Error EJB genérico"));

        Response response = resource.iniciarExamen(dto, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void iniciarExamen_ConExcepcionGenerica_DebeRetornar500() {
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(inscripcionId);
        dto.setIdEtapa(etapaId);
        when(examenRealizadoDAO.iniciarExamenAspirante(inscripcionId, etapaId))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.iniciarExamen(dto, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== calificarExamen (POST /{idExamen}/calificar) ====================

    @Test
    void calificarExamen_ConIdExistente_DebeRetornar200() {
        when(examenRealizadoDAO.leer(examenId)).thenReturn(examen);
        when(examenRealizadoDAO.calificarExamen(examenId)).thenReturn(examen);

        Response response = resource.calificarExamen(examenId.toString());

        assertEquals(200, response.getStatus());
        assertSame(examen, response.getEntity());
    }

    @Test
    void calificarExamen_ConIdInexistente_DebeRetornar404() {
        when(examenRealizadoDAO.leer(examenId)).thenReturn(null);

        Response response = resource.calificarExamen(examenId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(examenRealizadoDAO, never()).calificarExamen(any());
    }

    @Test
    void calificarExamen_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.calificarExamen("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void calificarExamen_ConIllegalArgumentEnDAO_DebeRetornar400() {
        when(examenRealizadoDAO.leer(examenId)).thenReturn(examen);
        when(examenRealizadoDAO.calificarExamen(examenId))
                .thenThrow(new IllegalArgumentException("Examen sin respuestas"));

        Response response = resource.calificarExamen(examenId.toString());

        assertEquals(400, response.getStatus());
    }

    @Test
    void calificarExamen_ConIllegalStateEnDAO_DebeRetornar409() {
        when(examenRealizadoDAO.leer(examenId)).thenReturn(examen);
        when(examenRealizadoDAO.calificarExamen(examenId))
                .thenThrow(new IllegalStateException("El examen ya fue calificado"));

        Response response = resource.calificarExamen(examenId.toString());

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void calificarExamen_ConExcepcionEnDAO_DebeRetornar500() {
        when(examenRealizadoDAO.leer(examenId)).thenReturn(examen);
        when(examenRealizadoDAO.calificarExamen(examenId))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.calificarExamen(examenId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getRanking (GET /ranking) ====================

    @Test
    void getRanking_ConParametrosValidos_DebeRetornar200() {
        UUID idPrueba = UUID.randomUUID();
        when(examenRealizadoDAO.findRankingByPruebaAndEtapa(eq(idPrueba), eq(etapaId), eq(0), eq(20)))
                .thenReturn(List.of(examen));

        Response response = resource.getRanking(idPrueba.toString(), etapaId.toString(), 0, 20);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getRanking_SinIdPrueba_DebeRetornar400() {
        Response response = resource.getRanking(null, etapaId.toString(), 0, 20);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void getRanking_SinIdEtapa_DebeRetornar400() {
        Response response = resource.getRanking(UUID.randomUUID().toString(), null, 0, 20);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void getRanking_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getRanking("no-es-uuid", etapaId.toString(), 0, 20);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void getRanking_ConExcepcionEnDAO_DebeRetornar500() {
        UUID idPrueba = UUID.randomUUID();
        when(examenRealizadoDAO.findRankingByPruebaAndEtapa(any(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getRanking(idPrueba.toString(), etapaId.toString(), 0, 20);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getExamenesPorAspirante (GET /aspirante/{idAspirante}) ====================

    @Test
    void getExamenesPorAspirante_ConResultados_DebeRetornar200() {
        when(examenRealizadoDAO.findByAspiranteId(aspiranteId)).thenReturn(List.of(examen));
        when(procesoAdmisionDAO.findByAspiranteId(aspiranteId)).thenReturn(Collections.emptyList());

        Response response = resource.getExamenesPorAspirante(aspiranteId.toString());

        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getExamenesPorAspirante_SinResultados_DebeRetornar200ConListaVacia() {
        when(examenRealizadoDAO.findByAspiranteId(aspiranteId)).thenReturn(Collections.emptyList());
        when(procesoAdmisionDAO.findByAspiranteId(aspiranteId)).thenReturn(Collections.emptyList());

        Response response = resource.getExamenesPorAspirante(aspiranteId.toString());

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getExamenesPorAspirante_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getExamenesPorAspirante("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void getExamenesPorAspirante_ConExcepcionEnDAO_DebeRetornar500() {
        when(examenRealizadoDAO.findByAspiranteId(any()))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getExamenesPorAspirante(aspiranteId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getExamenesPorPrueba (GET /prueba/{idPrueba}) ====================

    @Test
    void getExamenesPorPrueba_ConResultados_DebeRetornar200() {
        UUID idPrueba = UUID.randomUUID();
        when(examenRealizadoDAO.findByPruebaId(idPrueba)).thenReturn(List.of(examen));

        Response response = resource.getExamenesPorPrueba(idPrueba.toString());

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getExamenesPorPrueba_SinResultados_DebeRetornar200ConListaVacia() {
        UUID idPrueba = UUID.randomUUID();
        when(examenRealizadoDAO.findByPruebaId(idPrueba)).thenReturn(Collections.emptyList());

        Response response = resource.getExamenesPorPrueba(idPrueba.toString());

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getExamenesPorPrueba_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getExamenesPorPrueba("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void getExamenesPorPrueba_ConExcepcionEnDAO_DebeRetornar500() {
        when(examenRealizadoDAO.findByPruebaId(any()))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getExamenesPorPrueba(UUID.randomUUID().toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getPreguntasDelExamen (GET /{idExamen}/preguntas) ====================

    @Test
    void getPreguntasDelExamen_ConExamenExistente_DebeRetornar200() {
        when(examenRealizadoDAO.leer(examenId)).thenReturn(examen);
        PreguntasPorClave pregunta = new PreguntasPorClave();
        when(preguntasPorClaveDAO.findPreguntasByClave(claveId)).thenReturn(List.of(pregunta));

        Response response = resource.getPreguntasDelExamen(examenId.toString());

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
        verify(preguntasPorClaveDAO).findPreguntasByClave(claveId);
    }

    @Test
    void getPreguntasDelExamen_ConExamenSinPreguntas_DebeRetornar200ConListaVacia() {
        when(examenRealizadoDAO.leer(examenId)).thenReturn(examen);
        when(preguntasPorClaveDAO.findPreguntasByClave(claveId)).thenReturn(Collections.emptyList());

        Response response = resource.getPreguntasDelExamen(examenId.toString());

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getPreguntasDelExamen_ConExamenInexistente_DebeRetornar404() {
        when(examenRealizadoDAO.leer(examenId)).thenReturn(null);

        Response response = resource.getPreguntasDelExamen(examenId.toString());

        assertEquals(404, response.getStatus());
        verifyNoInteractions(preguntasPorClaveDAO);
    }

    @Test
    void getPreguntasDelExamen_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getPreguntasDelExamen("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO, preguntasPorClaveDAO);
    }

    @Test
    void getPreguntasDelExamen_ConExcepcionEnDAO_DebeRetornar500() {
        when(examenRealizadoDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getPreguntasDelExamen(examenId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== buscarExamenes (GET /buscar) ====================

    @Test
    void buscarExamenes_PorDuiConResultados_DebeRetornar200() {
        when(examenRealizadoDAO.findByAspiranteDui("01234567-8")).thenReturn(List.of(examen));
        when(procesoAdmisionDAO.findByAspiranteId(aspiranteId)).thenReturn(Collections.emptyList());

        Response response = resource.buscarExamenes("01234567-8", null);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void buscarExamenes_PorCorreoConResultados_DebeRetornar200() {
        when(examenRealizadoDAO.findByAspiranteCorreo("test@example.com")).thenReturn(List.of(examen));
        when(procesoAdmisionDAO.findByAspiranteId(aspiranteId)).thenReturn(Collections.emptyList());

        Response response = resource.buscarExamenes(null, "test@example.com");

        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void buscarExamenes_PorDuiSinResultados_DebeRetornar200ConListaVacia() {
        when(examenRealizadoDAO.findByAspiranteDui("99999999-9")).thenReturn(Collections.emptyList());

        Response response = resource.buscarExamenes("99999999-9", null);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void buscarExamenes_SinParametros_DebeRetornar400() {
        Response response = resource.buscarExamenes(null, null);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void buscarExamenes_ConParametrosVacios_DebeRetornar400() {
        Response response = resource.buscarExamenes("", "");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void buscarExamenes_ConDatosIncompletosEnBD_DebeRetornar500() {
        ExamenRealizado examenIncompleto = new ExamenRealizado();
        examenIncompleto.setIdExamenRealizado(examenId);
        examenIncompleto.setInscripcionesPrueba(null);
        when(examenRealizadoDAO.findByAspiranteDui("01234567-8")).thenReturn(List.of(examenIncompleto));

        Response response = resource.buscarExamenes("01234567-8", null);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void buscarExamenes_ConExcepcionEnEnriquecimiento_DebeRetornar500() {
        when(examenRealizadoDAO.findByAspiranteDui("01234567-8")).thenReturn(List.of(examen));
        when(procesoAdmisionDAO.findByAspiranteId(aspiranteId))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.buscarExamenes("01234567-8", null);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
