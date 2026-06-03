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
    void leer_CuandoExiste_DebeRetornarEntidad() {
        PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
        cut.em = entityManager;

        UUID id = UUID.randomUUID();
        PreguntaOpcion entity = new PreguntaOpcion();
        entity.setIdPreguntaOpcion(id);

        // 1. Simulamos el NamedQuery
        when(entityManager.createNamedQuery("PreguntaOpcion.findByIdConRelaciones", PreguntaOpcion.class))
                .thenReturn(queryOpciones);
        // 2. Simulamos la inyección del parámetro
        when(queryOpciones.setParameter("id", id)).thenReturn(queryOpciones);
        // 3. Simulamos que encontró el resultado
        when(queryOpciones.getSingleResult()).thenReturn(entity);

        PreguntaOpcion result = cut.leer(id);

        assertSame(entity, result);
        verify(entityManager).createNamedQuery("PreguntaOpcion.findByIdConRelaciones", PreguntaOpcion.class);
    }

    @Test
    void leer_CuandoNoExiste_DebeRetornarNull() {
        PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
        cut.em = entityManager;

        UUID id = UUID.randomUUID();

        // 1. Simulamos el NamedQuery
        when(entityManager.createNamedQuery("PreguntaOpcion.findByIdConRelaciones", PreguntaOpcion.class))
                .thenReturn(queryOpciones);
        // 2. Simulamos la inyección del parámetro
        when(queryOpciones.setParameter("id", id)).thenReturn(queryOpciones);
        // 3. Simulamos que NO encontró nada lanzando la excepción nativa de JPA
        when(queryOpciones.getSingleResult()).thenThrow(new jakarta.persistence.NoResultException());

        PreguntaOpcion result = cut.leer(id);

        assertNull(result); // Tu DAO debe atrapar la excepción y devolver null
        verify(entityManager).createNamedQuery("PreguntaOpcion.findByIdConRelaciones", PreguntaOpcion.class);
    }

    @Test
    void findByPregunta_DebeRetornarLista() {
        PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
        cut.em = entityManager;

        UUID preguntaId = UUID.randomUUID();

        when(entityManager.createNamedQuery("PreguntaOpcion.findByPregunta", PreguntaOpcion.class)).thenReturn(queryOpciones);
        when(queryOpciones.setParameter(eq("idPregunta"), eq(preguntaId))).thenReturn(queryOpciones);

        List<PreguntaOpcion> esperado = List.of(new PreguntaOpcion());
        when(queryOpciones.getResultList()).thenReturn(esperado);

        List<PreguntaOpcion> result = cut.findByPregunta(preguntaId);

        assertSame(esperado, result);

        verify(entityManager).createNamedQuery("PreguntaOpcion.findByPregunta", PreguntaOpcion.class);
        verify(queryOpciones).setParameter("idPregunta", preguntaId);
        verify(queryOpciones).getResultList();
    }

    @Test
    void existsByPreguntaAndRespuesta_CuandoHay_ReturnsTrue() {
        PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
        cut.em = entityManager;

        UUID preguntaId = UUID.randomUUID();
        UUID respuestaId = UUID.randomUUID();

        when(entityManager.createNamedQuery("PreguntaOpcion.countByPreguntaAndRespuesta", Long.class)).thenReturn(queryLong);
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

        when(entityManager.createNamedQuery("PreguntaOpcion.countByPreguntaAndRespuesta", Long.class)).thenReturn(queryLong);
        when(queryLong.setParameter(eq("idPregunta"), eq(preguntaId))).thenReturn(queryLong);
        when(queryLong.setParameter(eq("idRespuestaGlobal"), eq(respuestaId))).thenReturn(queryLong);
        when(queryLong.getSingleResult()).thenReturn(0L);

        boolean result = cut.existsByPreguntaAndRespuesta(preguntaId, respuestaId);
        assertFalse(result);
    }
}