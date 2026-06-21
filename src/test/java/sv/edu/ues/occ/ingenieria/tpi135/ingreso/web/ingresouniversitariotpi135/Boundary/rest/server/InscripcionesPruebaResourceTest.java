package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para InscripcionesPruebaResource.
 * NOTA: No existe endpoint POST en el raíz. Las inscripciones se crean desde
 * AspirantesDatoResource (POST /aspirantes/{id}/inscripciones).
 */
@ExtendWith(MockitoExtension.class)
class InscripcionesPruebaResourceTest {

    @Mock
    private InscripcionesPruebaDAO inscripcionesPruebaDAO;

    @Mock
    private ClavesExamanDAO clavesExamanDAO;

    @Mock
    private ExamenRealizadoDAO examenRealizadoDAO;

    @InjectMocks
    private InscripcionesPruebaResource resource;

    private InscripcionesPrueba entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidad = new InscripcionesPrueba();
        entidad.setIdInscripcionPrueba(testId);
        entidad.setAspiranteDato(new AspirantesDato());
        entidad.setPruebaAdmision(new PruebasAdmision());
        entidad.setEstado("ACTIVO");
    }

    // ==================== listInscripciones (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200() {
        when(inscripcionesPruebaDAO.count()).thenReturn(1);
        when(inscripcionesPruebaDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listInscripciones(null, null, 0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200() {
        when(inscripcionesPruebaDAO.count()).thenReturn(0);
        when(inscripcionesPruebaDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listInscripciones(null, null, 0, 10);

        assertEquals(200, response.getStatus());
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(inscripcionesPruebaDAO.count()).thenThrow(new RuntimeException("BD error"));

        Response response = resource.listInscripciones(null, null, 0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void findRange_ConFiltroPruebaYEstado_DebeRetornarListaFiltrada() {
        UUID pruebaId = UUID.randomUUID();
        when(inscripcionesPruebaDAO.findByPruebaAndEstado(pruebaId, "ACTIVO")).thenReturn(List.of(entidad));

        Response response = resource.listInscripciones(pruebaId.toString(), "ACTIVO", 0, 10);

        assertEquals(200, response.getStatus());
        verify(inscripcionesPruebaDAO).findByPruebaAndEstado(pruebaId, "ACTIVO");
        verify(inscripcionesPruebaDAO, never()).findRange(anyInt(), anyInt());
    }

    @Test
    void findRange_ConIdPruebaPeroSinEstado_DebeUsarFindRange() {
        UUID pruebaId = UUID.randomUUID();
        when(inscripcionesPruebaDAO.count()).thenReturn(1);
        when(inscripcionesPruebaDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listInscripciones(pruebaId.toString(), null, 0, 10);

        assertEquals(200, response.getStatus());
        verify(inscripcionesPruebaDAO).findRange(0, 10);
        verify(inscripcionesPruebaDAO, never()).findByPruebaAndEstado(any(), any());
    }

    @Test
    void findRange_ConFiltroPruebaInvalida_DebeRetornar400() {
        Response response = resource.listInscripciones("no-es-uuid", "ACTIVO", 0, 10);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    // ==================== getInscripcion (GET /{idInscripcion}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getInscripcion(testId.toString());

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(null);

        Response response = resource.getInscripcion(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void findById_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.getInscripcion("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(inscripcionesPruebaDAO.leer(any())).thenThrow(new RuntimeException("BD error"));

        Response response = resource.getInscripcion(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== updateInscripcion (PUT /{idInscripcion}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(entidad);
        InscripcionesPrueba payload = new InscripcionesPrueba();
        payload.setEstado("FINALIZADO");

        Response response = resource.updateInscripcion(testId.toString(), payload);

        assertEquals(200, response.getStatus());
        verify(inscripcionesPruebaDAO).actualizar(payload);
    }

    @Test
    void update_ConIdFormatoInvalido_DebeRetornar409() {
        // updateInscripcion captura IAE (UUID inválido) → CONFLICT 409
        Response response = resource.updateInscripcion("no-es-uuid",
            new InscripcionesPrueba());

        assertEquals(409, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(null);

        Response response = resource.updateInscripcion(testId.toString(), entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void update_ConIllegalArgumentEnActualizar_DebeRetornar409() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(entidad);
        InscripcionesPrueba payload = new InscripcionesPrueba();
        payload.setEstado("INVALIDO");
        when(inscripcionesPruebaDAO.actualizar(payload))
                .thenThrow(new IllegalArgumentException("Estado no válido"));

        Response response = resource.updateInscripcion(testId.toString(), payload);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void update_ConExcepcionEnLeer_DebeRetornar500() {
        when(inscripcionesPruebaDAO.leer(testId)).thenThrow(new RuntimeException("BD error"));

        Response response = resource.updateInscripcion(testId.toString(), entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void update_ConExcepcionEnActualizar_DebeRetornar500() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(entidad);
        InscripcionesPrueba payload = new InscripcionesPrueba();
        payload.setEstado("FINALIZADO");
        when(inscripcionesPruebaDAO.actualizar(payload))
                .thenThrow(new RuntimeException("BD error"));

        Response response = resource.updateInscripcion(testId.toString(), payload);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== deleteInscripcion (DELETE /{idInscripcion}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.deleteInscripcion(testId.toString());

        assertEquals(204, response.getStatus());
        verify(inscripcionesPruebaDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.deleteInscripcion("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(null);

        Response response = resource.deleteInscripcion(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(inscripcionesPruebaDAO.leer(any())).thenThrow(new RuntimeException("BD error"));

        Response response = resource.deleteInscripcion(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== generarExamen (POST /{id}/examen/generar) ====================

    @Test
    void generarExamen_SinIdEtapa_DebeRetornar400() {
        Response response = resource.generarExamen(testId.toString(), null);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO, clavesExamanDAO, examenRealizadoDAO);
    }

    @Test
    void generarExamen_ConInscripcionInexistente_DebeRetornar404() {
        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(null);

        Response response = resource.generarExamen(testId.toString(), UUID.randomUUID().toString());

        assertEquals(404, response.getStatus());
        verifyNoInteractions(clavesExamanDAO, examenRealizadoDAO);
    }

    @Test
    void generarExamen_SinClavesDisponibles_DebeRetornar412() {
        UUID pruebaId = UUID.randomUUID();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(pruebaId);
        InscripcionesPrueba inscripcionActiva = new InscripcionesPrueba();
        inscripcionActiva.setIdInscripcionPrueba(testId);
        inscripcionActiva.setPruebaAdmision(prueba);
        inscripcionActiva.setEstado("ACTIVO");

        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(inscripcionActiva);
        when(clavesExamanDAO.findByPrueba(pruebaId)).thenReturn(Collections.emptyList());

        Response response = resource.generarExamen(testId.toString(), UUID.randomUUID().toString());

        assertEquals(412, response.getStatus());
        verifyNoInteractions(examenRealizadoDAO);
    }

    @Test
    void generarExamen_ConDatosValidos_DebeRetornar201() {
        UUID pruebaId = UUID.randomUUID();
        UUID etapaId = UUID.randomUUID();

        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(pruebaId);

        InscripcionesPrueba inscripcionActiva = new InscripcionesPrueba();
        inscripcionActiva.setIdInscripcionPrueba(testId);
        inscripcionActiva.setPruebaAdmision(prueba);
        inscripcionActiva.setEstado("ACTIVO");

        ClavesExamen clave = new ClavesExamen();
        clave.setIdClaveExaman(UUID.randomUUID());
        clave.setNombreClave("A");

        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(inscripcionActiva);
        when(clavesExamanDAO.findByPrueba(pruebaId)).thenReturn(List.of(clave));
        when(examenRealizadoDAO.findByPruebaId(pruebaId)).thenReturn(Collections.emptyList());

        Response response = resource.generarExamen(testId.toString(), etapaId.toString());

        assertEquals(201, response.getStatus());
        verify(examenRealizadoDAO).crear(any());
        verify(inscripcionesPruebaDAO).actualizar(inscripcionActiva);
        assertEquals("EXAMEN_GENERADO", inscripcionActiva.getEstado());
    }

    @Test
    void generarExamen_ConExamenYaGenerado_DebeRetornar409() {
        InscripcionesPrueba inscripcionConExamen = new InscripcionesPrueba();
        inscripcionConExamen.setIdInscripcionPrueba(testId);
        inscripcionConExamen.setEstado("EXAMEN_GENERADO");
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(UUID.randomUUID());
        inscripcionConExamen.setPruebaAdmision(prueba);

        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(inscripcionConExamen);

        Response response = resource.generarExamen(testId.toString(), UUID.randomUUID().toString());

        assertEquals(409, response.getStatus());
        verifyNoInteractions(clavesExamanDAO, examenRealizadoDAO);
    }

    @Test
    void generarExamen_ConIdEtapaEnBlanco_DebeRetornar400() {
        Response response = resource.generarExamen(testId.toString(), "   ");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO, clavesExamanDAO, examenRealizadoDAO);
    }

    @Test
    void generarExamen_ConUuidInscripcionInvalido_DebeRetornar400() {
        Response response = resource.generarExamen("no-es-uuid", UUID.randomUUID().toString());

        assertEquals(400, response.getStatus());
        verifyNoInteractions(inscripcionesPruebaDAO);
    }

    @Test
    void generarExamen_ConExcepcionEnDAO_DebeRetornar500() {
        when(inscripcionesPruebaDAO.leer(any())).thenThrow(new RuntimeException("BD error"));

        Response response = resource.generarExamen(testId.toString(), UUID.randomUUID().toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void generarExamen_ConEstadoCalificado_DebeRetornar409() {
        InscripcionesPrueba inscripcionCalificada = new InscripcionesPrueba();
        inscripcionCalificada.setIdInscripcionPrueba(testId);
        inscripcionCalificada.setEstado("CALIFICADO");
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(UUID.randomUUID());
        inscripcionCalificada.setPruebaAdmision(prueba);

        when(inscripcionesPruebaDAO.leer(testId)).thenReturn(inscripcionCalificada);

        Response response = resource.generarExamen(testId.toString(), UUID.randomUUID().toString());

        assertEquals(409, response.getStatus());
        verifyNoInteractions(clavesExamanDAO, examenRealizadoDAO);
    }
}
