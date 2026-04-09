package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AsignacionesAulaPupitreDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionesAulaPupitreResourceTest {

    @Mock private AsignacionesAulaPupitreDAO asignacionesAulaPupitreDAO;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    private AsignacionesAulaPupitreResource resource;
    private AsignacionesAulaPupitre entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        resource = new AsignacionesAulaPupitreResource();
        resource.asignacionesAulaPupitreDAO = asignacionesAulaPupitreDAO;

        InscripcionesPrueba inscripcion = new InscripcionesPrueba();
        AulasExaman aula = new AulasExaman();

        entidad = new AsignacionesAulaPupitre();
        entidad.setId(testId);
        entidad.setIdInscripcion(inscripcion);
        entidad.setIdAula(aula);
        entidad.setPupitre("A-01");
    }

    /**
     * Helper para setear campos privados usando reflection (para QueryParam)
     */
    private void setFieldValue(Object obj, String fieldName, String value) throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    // ==================== findRange (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(asignacionesAulaPupitreDAO.count()).thenReturn(1);
        when(asignacionesAulaPupitreDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(asignacionesAulaPupitreDAO).count();
        verify(asignacionesAulaPupitreDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200() {
        when(asignacionesAulaPupitreDAO.count()).thenReturn(0);
        when(asignacionesAulaPupitreDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConFirstNegativo_DebeRetornar422() {
        Response response = resource.findRange(-1, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(asignacionesAulaPupitreDAO);
    }

    @Test
    void findRange_ConMaxCero_DebeRetornar422() {
        Response response = resource.findRange(0, 0);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(asignacionesAulaPupitreDAO);
    }

    @Test
    void findRange_ConMaxMayorA100_DebeRetornar422() {
        Response response = resource.findRange(0, 101);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(asignacionesAulaPupitreDAO);
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(asignacionesAulaPupitreDAO.count()).thenThrow(new RuntimeException("Error de BD"));
        Response response = resource.findRange(0, 10);
        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== findRange con filtros (inscripcionId, aspiranteId) ====================

    @Test
    void findRange_ConInscripcionIdValido_DebeRetornarRegistrosFiltrados() throws NoSuchFieldException, IllegalAccessException {
        UUID inscripcionId = UUID.randomUUID();
        setFieldValue(resource, "inscripcionIdParam", inscripcionId.toString());
        
        when(asignacionesAulaPupitreDAO.findByInscripcionId(inscripcionId))
                .thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        verify(asignacionesAulaPupitreDAO).findByInscripcionId(inscripcionId);
    }

    @Test
    void findRange_ConInscripcionIdInvalido_DebeRetornar422() throws NoSuchFieldException, IllegalAccessException {
        setFieldValue(resource, "inscripcionIdParam", "invalid-uuid");

        Response response = resource.findRange(0, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    @Test
    void findRange_ConAspiranteIdValido_DebeRetornarRegistrosFiltrados() throws NoSuchFieldException, IllegalAccessException {
        UUID aspiranteId = UUID.randomUUID();
        setFieldValue(resource, "aspiranteIdParam", aspiranteId.toString());
        
        when(asignacionesAulaPupitreDAO.findByAspiranteId(aspiranteId))
                .thenReturn(List.of(entidad));

        Response response = resource.findRange(0, 10);

        assertEquals(200, response.getStatus());
        verify(asignacionesAulaPupitreDAO).findByAspiranteId(aspiranteId);
    }

    @Test
    void findRange_ConAspiranteIdInvalido_DebeRetornar422() throws NoSuchFieldException, IllegalAccessException {
        setFieldValue(resource, "aspiranteIdParam", "not-a-uuid");

        Response response = resource.findRange(0, 10);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    // ==================== findById (GET /{id}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        when(asignacionesAulaPupitreDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.findById(testId);

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(asignacionesAulaPupitreDAO.leer(testId)).thenReturn(null);

        Response response = resource.findById(testId);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdNulo_DebeRetornar422() {
        Response response = resource.findById(null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(asignacionesAulaPupitreDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(asignacionesAulaPupitreDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));
        Response response = resource.findById(testId);
        assertEquals(500, response.getStatus());
    }

    // ==================== create (POST /) ====================

    @Test
    void create_ConEntidadValida_DebeRetornar201() {
        AsignacionesAulaPupitre nueva = new AsignacionesAulaPupitre();
        nueva.setIdInscripcion(new InscripcionesPrueba());
        nueva.setIdAula(new AulasExaman());
        nueva.setPupitre("B-05");
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/asignaciones/1"));

        Response response = resource.create(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        verify(asignacionesAulaPupitreDAO).crear(nueva);
    }

    @Test
    void create_ConEntidadNula_DebeRetornar422() {
        Response response = resource.create(null, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(asignacionesAulaPupitreDAO);
    }

    @Test
    void create_ConIdYaAsignado_DebeRetornar422() {
        Response response = resource.create(entidad, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(asignacionesAulaPupitreDAO);
    }

    @Test
    void create_SinInscripcion_DebeRetornar422() {
        AsignacionesAulaPupitre nueva = new AsignacionesAulaPupitre();
        nueva.setIdAula(new AulasExaman());
        nueva.setPupitre("C-01");
        Response response = resource.create(nueva, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(asignacionesAulaPupitreDAO);
    }

    @Test
    void create_SinAula_DebeRetornar422() {
        AsignacionesAulaPupitre nueva = new AsignacionesAulaPupitre();
        nueva.setIdInscripcion(new InscripcionesPrueba());
        nueva.setPupitre("D-01");
        Response response = resource.create(nueva, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(asignacionesAulaPupitreDAO);
    }

    @Test
    void create_SinPupitre_DebeRetornar422() {
        AsignacionesAulaPupitre nueva = new AsignacionesAulaPupitre();
        nueva.setIdInscripcion(new InscripcionesPrueba());
        nueva.setIdAula(new AulasExaman());
        Response response = resource.create(nueva, uriInfo);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(asignacionesAulaPupitreDAO);
    }

    @Test
    void create_ConExcepcionEnDAO_DebeRetornar500() {
        AsignacionesAulaPupitre nueva = new AsignacionesAulaPupitre();
        nueva.setIdInscripcion(new InscripcionesPrueba());
        nueva.setIdAula(new AulasExaman());
        nueva.setPupitre("E-01");
        doThrow(new RuntimeException("Error de BD")).when(asignacionesAulaPupitreDAO).crear(any());
        Response response = resource.create(nueva, uriInfo);
        assertEquals(500, response.getStatus());
    }

    // ==================== update (PUT /{id}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(asignacionesAulaPupitreDAO.leer(testId)).thenReturn(entidad);
        AsignacionesAulaPupitre actualizada = new AsignacionesAulaPupitre();
        actualizada.setIdInscripcion(new InscripcionesPrueba());
        actualizada.setIdAula(new AulasExaman());
        actualizada.setPupitre("F-01");

        Response response = resource.update(testId, actualizada);

        assertEquals(200, response.getStatus());
        verify(asignacionesAulaPupitreDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdNulo_DebeRetornar422() {
        Response response = resource.update(null, entidad);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(asignacionesAulaPupitreDAO);
    }

    @Test
    void update_ConEntidadNula_DebeRetornar422() {
        Response response = resource.update(testId, null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(asignacionesAulaPupitreDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(asignacionesAulaPupitreDAO.leer(testId)).thenReturn(null);
        Response response = resource.update(testId, entidad);
        assertEquals(404, response.getStatus());
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(asignacionesAulaPupitreDAO.leer(testId)).thenThrow(new RuntimeException("Error"));
        Response response = resource.update(testId, entidad);
        assertEquals(500, response.getStatus());
    }

    // ==================== delete (DELETE /{id}) ====================

    @Test
    void delete_ConIdExistente_DebeRetornar204() {
        when(asignacionesAulaPupitreDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.delete(testId);

        assertEquals(204, response.getStatus());
        verify(asignacionesAulaPupitreDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdNulo_DebeRetornar422() {
        Response response = resource.delete(null);
        assertEquals(422, response.getStatus());
        verifyNoInteractions(asignacionesAulaPupitreDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(asignacionesAulaPupitreDAO.leer(testId)).thenReturn(null);
        Response response = resource.delete(testId);
        assertEquals(404, response.getStatus());
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(asignacionesAulaPupitreDAO.leer(any())).thenThrow(new RuntimeException("Error"));
        Response response = resource.delete(testId);
        assertEquals(500, response.getStatus());
    }
}
