package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestaExamen;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RespuestaExamenDAOTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<RespuestaExamen> queryRespuestas;

    @Mock
    private TypedQuery<Long> queryLong;

    @Test
    void findByExamenId_DebeUsarNamedQuery() {
        RespuestaExamenDAO cut = new RespuestaExamenDAO();
        cut.em = entityManager;

        UUID examenId = UUID.randomUUID();
        List<RespuestaExamen> esperado = List.of(new RespuestaExamen());

        when(entityManager.createNamedQuery("RespuestaExamen.findByExamenId", RespuestaExamen.class)).thenReturn(queryRespuestas);
        when(queryRespuestas.setParameter(eq("idExamen"), eq(examenId))).thenReturn(queryRespuestas);
        when(queryRespuestas.getResultList()).thenReturn(esperado);

        List<RespuestaExamen> resultado = cut.findByExamenId(examenId);

        assertSame(esperado, resultado);
        verify(entityManager).createNamedQuery("RespuestaExamen.findByExamenId", RespuestaExamen.class);
        verify(queryRespuestas).setParameter("idExamen", examenId);
        verify(queryRespuestas).getResultList();
    }

    @Test
    void existsByExamenAndPregunta_DebeContarPorExamenYPregunta() {
        RespuestaExamenDAO cut = new RespuestaExamenDAO();
        cut.em = entityManager;

        UUID examenId = UUID.randomUUID();
        UUID preguntaId = UUID.randomUUID();

        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(queryLong);
        when(queryLong.setParameter(eq("idExamen"), eq(examenId))).thenReturn(queryLong);
        when(queryLong.setParameter(eq("idPregunta"), eq(preguntaId))).thenReturn(queryLong);
        when(queryLong.getSingleResult()).thenReturn(1L);

        assertTrue(cut.existsByExamenAndPregunta(examenId, preguntaId));
    }

    @Test
    void existsByExamenAndPregunta_CuandoNoHayCoincidencia_DebeRetornarFalse() {
        RespuestaExamenDAO cut = new RespuestaExamenDAO();
        cut.em = entityManager;

        UUID examenId = UUID.randomUUID();
        UUID preguntaId = UUID.randomUUID();

        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(queryLong);
        when(queryLong.setParameter(eq("idExamen"), eq(examenId))).thenReturn(queryLong);
        when(queryLong.setParameter(eq("idPregunta"), eq(preguntaId))).thenReturn(queryLong);
        when(queryLong.getSingleResult()).thenReturn(0L);

        assertFalse(cut.existsByExamenAndPregunta(examenId, preguntaId));
    }
}