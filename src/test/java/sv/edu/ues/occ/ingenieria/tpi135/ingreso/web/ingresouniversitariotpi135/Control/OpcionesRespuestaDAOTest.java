package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;

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
    private TypedQuery<OpcionesRespuesta> queryOpciones;

    @Mock
    private TypedQuery<Long> queryLong;

    @Test
    void leer_CuandoExiste_DebeHacerRefresh() {
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = entityManager;

        UUID id = UUID.randomUUID();
        OpcionesRespuesta entity = new OpcionesRespuesta();
        entity.setId(id);

        when(entityManager.find(eq(OpcionesRespuesta.class), eq(id))).thenReturn(entity);

        OpcionesRespuesta result = cut.leer(id);

        assertSame(entity, result);
        verify(entityManager).refresh(entity);
    }

    @Test
    void leer_CuandoNoExiste_NoDebeHacerRefresh() {
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = entityManager;

        UUID id = UUID.randomUUID();
        when(entityManager.find(eq(OpcionesRespuesta.class), eq(id))).thenReturn(null);

        OpcionesRespuesta result = cut.leer(id);

        assertNull(result);
        verify(entityManager, never()).refresh(any());
    }

    @Test
    void findByPreguntaId_DebeAplicarPaginacionYRetornarLista() {
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = entityManager;

        UUID preguntaId = UUID.randomUUID();
        int first = 5;
        int max = 10;

        when(entityManager.createNamedQuery(eq("OpcionesRespuesta.findByPreguntaId"), eq(OpcionesRespuesta.class)))
                .thenReturn(queryOpciones);
        when(queryOpciones.setParameter(eq("preguntaId"), eq(preguntaId))).thenReturn(queryOpciones);
        when(queryOpciones.setFirstResult(first)).thenReturn(queryOpciones);
        when(queryOpciones.setMaxResults(max)).thenReturn(queryOpciones);

        List<OpcionesRespuesta> esperado = List.of(new OpcionesRespuesta());
        when(queryOpciones.getResultList()).thenReturn(esperado);

        List<OpcionesRespuesta> result = cut.findByPreguntaId(preguntaId, first, max);

        assertSame(esperado, result);
        verify(entityManager).createNamedQuery("OpcionesRespuesta.findByPreguntaId", OpcionesRespuesta.class);
        verify(queryOpciones).setParameter("preguntaId", preguntaId);
        verify(queryOpciones).setFirstResult(first);
        verify(queryOpciones).setMaxResults(max);
        verify(queryOpciones).getResultList();
    }

    @Test
    void countByPreguntaId_CuandoHayTotal_DebeRetornarEntero() {
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = entityManager;

        UUID preguntaId = UUID.randomUUID();
        when(entityManager.createNamedQuery(eq("OpcionesRespuesta.countByPreguntaId"), eq(Long.class)))
                .thenReturn(queryLong);
        when(queryLong.setParameter(eq("preguntaId"), eq(preguntaId))).thenReturn(queryLong);
        when(queryLong.getSingleResult()).thenReturn(3L);

        int result = cut.countByPreguntaId(preguntaId);

        assertEquals(3, result);
        verify(entityManager).createNamedQuery("OpcionesRespuesta.countByPreguntaId", Long.class);
        verify(queryLong).setParameter("preguntaId", preguntaId);
        verify(queryLong).getSingleResult();
    }

    @Test
    void countByPreguntaId_CuandoTotalEsNull_DebeRetornarCero() {
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = entityManager;

        UUID preguntaId = UUID.randomUUID();
        when(entityManager.createNamedQuery(eq("OpcionesRespuesta.countByPreguntaId"), eq(Long.class)))
                .thenReturn(queryLong);
        when(queryLong.setParameter(eq("preguntaId"), eq(preguntaId))).thenReturn(queryLong);
        when(queryLong.getSingleResult()).thenReturn(null);

        int result = cut.countByPreguntaId(preguntaId);

        assertEquals(0, result);
    }
}
