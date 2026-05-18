package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpcionesRespuestaDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<PreguntaOpcion> queryOpciones;

    @Mock
    private TypedQuery<Long> queryLong;

    @Test
    void leer_CuandoExiste_DebeHacerRefresh() {
        PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
        cut.em = entityManager;

        UUID id = UUID.randomUUID();
        PreguntaOpcion entity = new PreguntaOpcion();
        entity.setId(id);

        when(entityManager.find(eq(PreguntaOpcion.class), eq(id))).thenReturn(entity);

        PreguntaOpcion result = cut.leer(id);

        assertSame(entity, result);
        verify(entityManager).find(PreguntaOpcion.class, id);
    }

    @Test
    void leer_CuandoNoExiste_NoDebeHacerRefresh() {
        PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
        cut.em = entityManager;

        UUID id = UUID.randomUUID();
        when(entityManager.find(eq(PreguntaOpcion.class), eq(id))).thenReturn(null);

        PreguntaOpcion result = cut.leer(id);

        assertNull(result);
        verify(entityManager, never()).refresh(any());
    }

    @Test
    void findByPregunta_DebeRetornarLista() {
        PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
        cut.em = entityManager;

        UUID preguntaId = UUID.randomUUID();

        String expectedQuery = "SELECT p FROM PreguntaOpcion p WHERE p.idPregunta.id = :idPregunta ORDER BY p.id";
        when(entityManager.createQuery(eq(expectedQuery), eq(PreguntaOpcion.class))).thenReturn(queryOpciones);
        when(queryOpciones.setParameter(eq("idPregunta"), eq(preguntaId))).thenReturn(queryOpciones);

        List<PreguntaOpcion> esperado = List.of(new PreguntaOpcion());
        when(queryOpciones.getResultList()).thenReturn(esperado);

        List<PreguntaOpcion> result = cut.findByPregunta(preguntaId);

        assertSame(esperado, result);
        verify(entityManager).createQuery(expectedQuery, PreguntaOpcion.class);
        verify(queryOpciones).setParameter("idPregunta", preguntaId);
        verify(queryOpciones).getResultList();
    }

    @Test
    void existsByPreguntaAndRespuesta_CuandoHay_ReturnsTrue() {
        PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
        cut.em = entityManager;

        UUID preguntaId = UUID.randomUUID();
        UUID respuestaId = UUID.randomUUID();

        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(queryLong);
        when(queryLong.setParameter(eq("idPregunta"), eq(preguntaId))).thenReturn(queryLong);
        when(queryLong.setParameter(eq("idRespuestaGlobal"), eq(respuestaId))).thenReturn(queryLong);
        when(queryLong.getSingleResult()).thenReturn(2L);

        boolean result = cut.existsByPreguntaAndRespuesta(preguntaId, respuestaId);
        assertTrue(result);
    }

    @Test
    void existsByPreguntaAndRespuesta_CuandoNoHay_ReturnsFalse() {
        PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
        cut.em = entityManager;

        UUID preguntaId = UUID.randomUUID();
        UUID respuestaId = UUID.randomUUID();

        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(queryLong);
        when(queryLong.setParameter(eq("idPregunta"), eq(preguntaId))).thenReturn(queryLong);
        when(queryLong.setParameter(eq("idRespuestaGlobal"), eq(respuestaId))).thenReturn(queryLong);
        when(queryLong.getSingleResult()).thenReturn(0L);

        boolean result = cut.existsByPreguntaAndRespuesta(preguntaId, respuestaId);
        assertFalse(result);
    }
}
