package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ProcesoAdmisionAspiranteDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ProcesoAdmisionAspirante;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ProcesoAdmisionAspiranteResource.
 * NOTA: Este recurso NO expone endpoints POST/PUT/DELETE genéricos.
 * Expone: GET /, GET /{id}, POST /asignar-masivo, POST /{id}/asignar
 */
@ExtendWith(MockitoExtension.class)
class ProcesoAdmisionAspiranteResourceTest {

    // Mockito inyecta en el campo privado "procesoAspiranteDAO" por tipo
    @Mock
    private ProcesoAdmisionAspiranteDAO procesoAspiranteDAO;

    @InjectMocks
    private ProcesoAdmisionAspiranteResource resource;

    private ProcesoAdmisionAspirante entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidad = new ProcesoAdmisionAspirante();
        entidad.setIdProcesoAdmisionAspirante(testId);
        entidad.setEstado("ACTIVO");
    }

    // ==================== listProcesos (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(procesoAspiranteDAO.count()).thenReturn(1);
        when(procesoAspiranteDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listProcesos(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(procesoAspiranteDAO).count();
        verify(procesoAspiranteDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(procesoAspiranteDAO.count()).thenReturn(0);
        when(procesoAspiranteDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listProcesos(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(procesoAspiranteDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listProcesos(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== getProceso (GET /{idInscripcion}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(procesoAspiranteDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getProceso(testId.toString());

        assertEquals(200, response.getStatus());
        ProcesoAdmisionAspirante resultado = (ProcesoAdmisionAspirante) response.getEntity();
        assertEquals(testId, resultado.getIdProcesoAdmisionAspirante());
        verify(procesoAspiranteDAO).leer(testId);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(procesoAspiranteDAO.leer(testId)).thenReturn(null);

        Response response = resource.getProceso(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.getProceso("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(procesoAspiranteDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(procesoAspiranteDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getProceso(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== asignarAspiranteIndividual (POST /{idInscripcion}/asignar) ====================

    @Test
    void asignarCarrera_ConIdExistente_DebeRetornar200ConResultado() {
        ProcesoAdmisionAspirante esperado = new ProcesoAdmisionAspirante();
        esperado.setIdProcesoAdmisionAspirante(testId);
        esperado.setEstado("ADMITIDO");
        CatalogoCarrera carrera = new CatalogoCarrera();
        carrera.setIdCarrera("ISI");
        esperado.setCarreraAsignada(carrera);

        when(procesoAspiranteDAO.leer(testId)).thenReturn(entidad);
        when(procesoAspiranteDAO.asignarCarreraFinal(testId)).thenReturn(esperado);

        Response response = resource.asignarAspiranteIndividual(testId.toString());

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        ProcesoAdmisionAspirante actual = (ProcesoAdmisionAspirante) response.getEntity();
        assertEquals("ADMITIDO", actual.getEstado());
        verify(procesoAspiranteDAO).asignarCarreraFinal(testId);
    }

    @Test
    void asignarCarrera_ConIdInexistente_DebeRetornar404() {
        when(procesoAspiranteDAO.leer(testId)).thenReturn(null);

        Response response = resource.asignarAspiranteIndividual(testId.toString());

        assertEquals(404, response.getStatus());
        verify(procesoAspiranteDAO, never()).asignarCarreraFinal(any());
    }

    @Test
    void asignarCarrera_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.asignarAspiranteIndividual("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(procesoAspiranteDAO);
    }

    @Test
    void asignarCarrera_ConExcepcionEnDAO_DebeRetornar500() {
        when(procesoAspiranteDAO.leer(testId)).thenReturn(entidad);
        when(procesoAspiranteDAO.asignarCarreraFinal(any())).thenThrow(new RuntimeException("BD error"));

        Response response = resource.asignarAspiranteIndividual(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== asignarMasivo (POST /asignar-masivo) ====================

    @Test
    void asignarMasivo_ConPayloadNulo_DebeRetornar400() {
        Response response = resource.asignarMasivo(null);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(procesoAspiranteDAO);
    }

    @Test
    void asignarMasivo_SinIdEtapa_DebeRetornar400() {
        ProcesoAdmisionAspiranteResource.AsignacionMasivaPayload payload =
            new ProcesoAdmisionAspiranteResource.AsignacionMasivaPayload();
        // idEtapa null

        Response response = resource.asignarMasivo(payload);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(procesoAspiranteDAO);
    }

    @Test
    void asignarMasivo_ConDatosValidos_DebeRetornar200() {
        ProcesoAdmisionAspiranteResource.AsignacionMasivaPayload payload =
            new ProcesoAdmisionAspiranteResource.AsignacionMasivaPayload();
        payload.setIdEtapa(UUID.randomUUID().toString());

        Response response = resource.asignarMasivo(payload);

        assertEquals(200, response.getStatus());
        verify(procesoAspiranteDAO).procesarAsignacionMasiva(any(UUID.class));
    }
}
