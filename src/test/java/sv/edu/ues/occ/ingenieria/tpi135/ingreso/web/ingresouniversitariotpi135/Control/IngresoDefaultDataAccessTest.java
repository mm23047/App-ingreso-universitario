package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas de cobertura para IngresoDefaultDataAccess.
 * Utiliza Mockito para simular el EntityManager y validar el comportamiento
 * de los mÃ©todos CRUD definidos en la clase abstracta base.
 */
@ExtendWith(MockitoExtension.class)
class IngresoDefaultDataAccessTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<EtapasAdmision> criteriaQuery;

    @Mock
    private CriteriaQuery<Long> criteriaQueryLong;

    @Mock
    private Root<EtapasAdmision> root;

    @Mock
    private TypedQuery<EtapasAdmision> typedQuery;

    @Mock
    private TypedQuery<Long> typedQueryLong;

    private IngresoDefaultDataAccessImpl dataAccess;
    private EtapasAdmision entidadPrueba;

    /**
     * ImplementaciÃ³n concreta de IngresoDefaultDataAccess para pruebas.
     * Permite inyectar el EntityManager mockeado.
     */
    class IngresoDefaultDataAccessImpl extends IngresoDefaultDataAccess<EtapasAdmision> {
        public IngresoDefaultDataAccessImpl() {
            super(EtapasAdmision.class);
        }

        @Override
        public EntityManager getEntityManager() {
            return entityManager;
        }
    }

    /**
     * ImplementaciÃ³n auxiliar que retorna EntityManager nulo,
     * reutilizada en mÃºltiples pruebas de EntityManager nulo.
     */
    private IngresoDefaultDataAccess<EtapasAdmision> dataAccessSinEM() {
        return new IngresoDefaultDataAccess<>(EtapasAdmision.class) {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };
    }

    @BeforeEach
    void setUp() {
        dataAccess = new IngresoDefaultDataAccessImpl();
        entidadPrueba = new EtapasAdmision();
        entidadPrueba.setId((short) 1);
        entidadPrueba.setNombre("Etapa Preuniversitaria");
        entidadPrueba.setPuntajeMinimo(new BigDecimal("60.00"));
        entidadPrueba.setPuntajeMaximo(new BigDecimal("100.00"));
        entidadPrueba.setDescripcion("Primera etapa del proceso de admisiÃ³n");
    }

    // ==================== PRUEBAS PARA CREAR ====================

    @Test
    void crear_ConRegistroValido_DebeGuardarExitosamente() {
        assertDoesNotThrow(() -> dataAccess.crear(entidadPrueba));

        // Verificar orden: persist antes que flush
        InOrder orden = inOrder(entityManager);
        orden.verify(entityManager).persist(entidadPrueba);
        orden.verify(entityManager).flush();
    }

    @Test
    void crear_ConRegistroNulo_DebeLanzarIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> dataAccess.crear(null));

        assertEquals("El registro no puede ser nulo", ex.getMessage());
        verify(entityManager, never()).persist(any());
        verify(entityManager, never()).flush();
    }

    @Test
    void crear_ConEntityManagerNulo_DebeLanzarIllegalStateException() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dataAccessSinEM().crear(entidadPrueba));

        assertEquals("EntityManager no inicializado", ex.getMessage());
        verify(entityManager, never()).persist(any());
    }

    @Test
    void crear_ConExcepcionAlPersistir_DebeLanzarIllegalStateException() {
        doThrow(new RuntimeException("Error de persistencia"))
                .when(entityManager).persist(any());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dataAccess.crear(entidadPrueba));

        assertEquals("Error al ingresar el registro: Error de persistencia", ex.getMessage());
        assertNotNull(ex.getCause());
        verify(entityManager).persist(entidadPrueba);
    }

    // ==================== PRUEBAS PARA ELIMINAR ====================

    @Test
    void eliminar_ConRegistroEnContexto_DebeEliminarSinMerge() {
        when(entityManager.contains(entidadPrueba)).thenReturn(true);

        assertDoesNotThrow(() -> dataAccess.eliminar(entidadPrueba));

        verify(entityManager, never()).merge(any());
        verify(entityManager).remove(entidadPrueba);
    }

    @Test
    void eliminar_ConRegistroFueraDeContexto_DebeMergearYEliminarResultadoDelMerge() {
        EtapasAdmision entidadMergeada = new EtapasAdmision();
        entidadMergeada.setId((short) 1);

        when(entityManager.contains(entidadPrueba)).thenReturn(false);
        when(entityManager.merge(entidadPrueba)).thenReturn(entidadMergeada);

        assertDoesNotThrow(() -> dataAccess.eliminar(entidadPrueba));

        // Debe eliminar la instancia retornada por merge, no la original
        verify(entityManager).merge(entidadPrueba);
        verify(entityManager).remove(entidadMergeada);
        verify(entityManager, never()).remove(entidadPrueba);
    }

    @Test
    void eliminar_ConRegistroNulo_DebeLanzarIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> dataAccess.eliminar(null));

        assertEquals("El registro no puede ser nulo", ex.getMessage());
        verify(entityManager, never()).remove(any());
    }

    @Test
    void eliminar_ConEntityManagerNulo_DebeLanzarIllegalStateException() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dataAccessSinEM().eliminar(entidadPrueba));

        assertEquals("EntityManager no inicializado", ex.getMessage());
    }

    @Test
    void eliminar_ConExcepcionAlEliminar_DebeLanzarIllegalStateException() {
        when(entityManager.contains(entidadPrueba)).thenReturn(true);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(entityManager).remove(any());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dataAccess.eliminar(entidadPrueba));

        assertEquals("Error al eliminar el registro", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    // ==================== PRUEBAS PARA ACTUALIZAR ====================

    @Test
    void actualizar_ConRegistroValido_DebeRetornarInstanciaDelMerge() {
        EtapasAdmision entidadMergeada = new EtapasAdmision();
        entidadMergeada.setId((short) 1);
        entidadMergeada.setNombre("Nombre modificado");

        when(entityManager.merge(entidadPrueba)).thenReturn(entidadMergeada);

        EtapasAdmision resultado = dataAccess.actualizar(entidadPrueba);

        // Debe ser la instancia del merge, no la original
        assertSame(entidadMergeada, resultado);
        assertNotSame(entidadPrueba, resultado);

        InOrder orden = inOrder(entityManager);
        orden.verify(entityManager).merge(entidadPrueba);
        orden.verify(entityManager).flush();
    }

    @Test
    void actualizar_ConRegistroNulo_DebeLanzarIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> dataAccess.actualizar(null));

        assertEquals("El registro a actualizar no puede ser nulo", ex.getMessage());
        verify(entityManager, never()).merge(any());
    }

    @Test
    void actualizar_ConEntityManagerNulo_DebeLanzarIllegalStateException() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dataAccessSinEM().actualizar(entidadPrueba));

        assertEquals("EntityManager no inicializado", ex.getMessage());
    }

    @Test
    void actualizar_ConExcepcionAlMergear_DebeLanzarIllegalStateException() {
        when(entityManager.merge(entidadPrueba))
                .thenThrow(new RuntimeException("Error al actualizar"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dataAccess.actualizar(entidadPrueba));

        assertTrue(ex.getMessage().contains("Error al actualizar registro de EtapasAdmision"));
        assertNotNull(ex.getCause());
    }

    // ==================== PRUEBAS PARA LEER ====================

    @Test
    void leer_ConIdValido_DebeRetornarEntidad() {
        Short id = (short) 1;
        when(entityManager.find(EtapasAdmision.class, id)).thenReturn(entidadPrueba);

        EtapasAdmision resultado = dataAccess.leer(id);

        assertNotNull(resultado);
        assertSame(entidadPrueba, resultado);
        verify(entityManager).find(EtapasAdmision.class, id);
    }

    @Test
    void leer_ConIdInexistente_DebeRetornarNull() {
        Short id = (short) 999;
        when(entityManager.find(EtapasAdmision.class, id)).thenReturn(null);

        EtapasAdmision resultado = dataAccess.leer(id);

        assertNull(resultado);
        verify(entityManager).find(EtapasAdmision.class, id);
    }

    @Test
    void leer_ConIdNulo_DebeLanzarIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> dataAccess.leer(null));

        assertEquals("El id no puede ser nulo", ex.getMessage());
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void leer_ConEntityManagerNulo_DebeLanzarIllegalStateException() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dataAccessSinEM().leer((short) 1));

        assertEquals("EntityManager no inicializado", ex.getMessage());
    }

    @Test
    void leer_ConExcepcionAlBuscar_DebeLanzarIllegalStateException() {
        when(entityManager.find(any(), any()))
                .thenThrow(new RuntimeException("Error en la base de datos"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dataAccess.leer((short) 1));

        assertTrue(ex.getMessage().contains("Error al leer registro de EtapasAdmision"));
        assertNotNull(ex.getCause());
    }

    // ==================== PRUEBAS PARA FINDRANGE ====================

    private void configurarMocksFindRange(int first, int max, List<EtapasAdmision> resultado) {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(EtapasAdmision.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(EtapasAdmision.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(first)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(max)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(resultado);
    }

    @Test
    void findRange_ConParametrosValidos_DebeRetornarLista() {
        List<EtapasAdmision> listaEsperada = Arrays.asList(entidadPrueba);
        configurarMocksFindRange(0, 10, listaEsperada);

        List<EtapasAdmision> resultado = dataAccess.findRange(0, 10);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertSame(entidadPrueba, resultado.get(0));
        verify(typedQuery).setFirstResult(0);
        verify(typedQuery).setMaxResults(10);
        verify(typedQuery).getResultList();
    }

    @Test
    void findRange_ConListaVacia_DebeRetornarListaVaciaNoNula() {
        configurarMocksFindRange(0, 10, List.of());

        List<EtapasAdmision> resultado = dataAccess.findRange(0, 10);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void findRange_ConPaginacionCorrecta_DebeConfigurarOffsetYLimite() {
        configurarMocksFindRange(10, 5, List.of());

        dataAccess.findRange(10, 5);

        verify(typedQuery).setFirstResult(10);
        verify(typedQuery).setMaxResults(5);
    }

    @Test
    void findRange_ConFirstNegativo_DebeLanzarIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dataAccess.findRange(-1, 10));
        verify(entityManager, never()).getCriteriaBuilder();
    }

    @Test
    void findRange_ConMaxCero_DebeLanzarIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dataAccess.findRange(0, 0));
        verify(entityManager, never()).getCriteriaBuilder();
    }

    @Test
    void findRange_ConMaxNegativo_DebeLanzarIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> dataAccess.findRange(0, -5));
        verify(entityManager, never()).getCriteriaBuilder();
    }

    @Test
    void findRange_ConEntityManagerNulo_DebeLanzarIllegalStateException() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dataAccessSinEM().findRange(0, 10));

        assertEquals("EntityManager no inicializado", ex.getMessage());
    }

    @Test
    void findRange_ConExcepcionEnQuery_DebeLanzarIllegalStateException() {
        when(entityManager.getCriteriaBuilder())
                .thenThrow(new RuntimeException("Error en criteriaBuilder"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dataAccess.findRange(0, 10));

        assertEquals("No se pudo acceder al repositorio", ex.getMessage());
        assertNotNull(ex.getCause());
    }

    // ==================== PRUEBAS PARA COUNT ====================

    private void configurarMocksCount(Long total) {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQueryLong);
        when(criteriaQueryLong.from(EtapasAdmision.class)).thenReturn(root);
        when(criteriaBuilder.count(root)).thenReturn(null);
        when(criteriaQueryLong.select(any())).thenReturn(criteriaQueryLong);
        when(entityManager.createQuery(criteriaQueryLong)).thenReturn(typedQueryLong);
        when(typedQueryLong.getSingleResult()).thenReturn(total);
    }

    @Test
    void count_ConRegistrosExistentes_DebeRetornarTotal() {
        configurarMocksCount(21L);

        int resultado = dataAccess.count();

        assertEquals(21, resultado);
        verify(criteriaBuilder).count(root);
        verify(typedQueryLong).getSingleResult();
    }

    @Test
    void count_ConTablaVacia_DebeRetornarCero() {
        configurarMocksCount(0L);

        assertEquals(0, dataAccess.count());
    }

    @Test
    void count_ConValorGrande_DebeConvertirCorrectamente() {
        configurarMocksCount(500000L);

        assertEquals(500000, dataAccess.count());
    }

    @Test
    void count_ConEntityManagerNulo_DebeLanzarIllegalStateException() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dataAccessSinEM().count());

        assertEquals("EntityManager no inicializado", ex.getMessage());
    }

    @Test
    void count_ConExcepcionEnQuery_DebeLanzarIllegalStateException() {
        when(entityManager.getCriteriaBuilder())
                .thenThrow(new RuntimeException("Error al contar"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dataAccess.count());

        assertEquals("No se pudo acceder al repositorio", ex.getMessage());
        assertNotNull(ex.getCause());
    }
}
