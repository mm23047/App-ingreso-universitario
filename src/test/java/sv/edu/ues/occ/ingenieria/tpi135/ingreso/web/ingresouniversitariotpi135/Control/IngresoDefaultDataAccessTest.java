package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * de los métodos CRUD definidos en la clase abstracta base.
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
     * Implementación concreta de IngresoDefaultDataAccess para pruebas.
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

    @BeforeEach
    void setUp() {
        dataAccess = new IngresoDefaultDataAccessImpl();
        entidadPrueba = new EtapasAdmision();
        entidadPrueba.setId((short) 1);
        entidadPrueba.setNombre("Etapa Preuniversitaria");
        entidadPrueba.setPuntajeMinimo(new BigDecimal("60.00"));
        entidadPrueba.setPuntajeMaximo(new BigDecimal("100.00"));
        entidadPrueba.setDescripcion("Primera etapa del proceso de admisión");
    }

    // ==================== PRUEBAS PARA CREAR ====================

    @Test
    void crear_ConRegistroValido_DebeGuardarExitosamente() {
        // Arrange - el entityManager mockeado ya está listo

        // Act
        assertDoesNotThrow(() -> dataAccess.crear(entidadPrueba));

        // Assert
        verify(entityManager, times(1)).persist(entidadPrueba);
        verify(entityManager, times(1)).flush();
    }

    @Test
    void crear_ConRegistroNulo_DebeLanzarIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dataAccess.crear(null));

        assertEquals("El registro no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).persist(any());
    }

    @Test
    void crear_ConEntityManagerNulo_DebeLanzarIllegalStateException() {
        // Arrange
        IngresoDefaultDataAccessImpl dataAccessSinEM = new IngresoDefaultDataAccessImpl() {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dataAccessSinEM.crear(entidadPrueba));

        assertEquals("EntityManager no inicializado", exception.getMessage());
        verify(entityManager, never()).persist(any());
    }

    @Test
    void crear_ConExcepcionAlPersistir_DebeLanzarIllegalStateException() {
        // Arrange
        doThrow(new RuntimeException("Error de persistencia"))
                .when(entityManager).persist(any());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dataAccess.crear(entidadPrueba));

        assertEquals("Error al ingresar el registro: Error de persistencia", exception.getMessage());
        verify(entityManager, times(1)).persist(entidadPrueba);
    }

    // ==================== PRUEBAS PARA ELIMINAR ====================

    @Test
    void eliminar_ConRegistroEnContexto_DebeEliminarExitosamente() {
        // Arrange
        when(entityManager.contains(entidadPrueba)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> dataAccess.eliminar(entidadPrueba));

        // Assert
        verify(entityManager, times(1)).remove(entidadPrueba);
        verify(entityManager, never()).merge(any());
    }

    @Test
    void eliminar_ConRegistroFueraDeContexto_DebeMergearYEliminar() {
        // Arrange
        when(entityManager.contains(entidadPrueba)).thenReturn(false);
        when(entityManager.merge(entidadPrueba)).thenReturn(entidadPrueba);

        // Act
        assertDoesNotThrow(() -> dataAccess.eliminar(entidadPrueba));

        // Assert
        verify(entityManager, times(1)).merge(entidadPrueba);
        verify(entityManager, times(1)).remove(entidadPrueba);
    }

    @Test
    void eliminar_ConRegistroNulo_DebeLanzarIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dataAccess.eliminar(null));

        assertEquals("El registro no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).remove(any());
    }

    @Test
    void eliminar_ConEntityManagerNulo_DebeLanzarIllegalStateException() {
        // Arrange
        IngresoDefaultDataAccessImpl dataAccessSinEM = new IngresoDefaultDataAccessImpl() {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dataAccessSinEM.eliminar(entidadPrueba));

        assertEquals("EntityManager no inicializado", exception.getMessage());
    }

    @Test
    void eliminar_ConExcepcionAlEliminar_DebeLanzarIllegalStateException() {
        // Arrange
        when(entityManager.contains(entidadPrueba)).thenReturn(true);
        doThrow(new RuntimeException("Error al eliminar"))
                .when(entityManager).remove(any());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dataAccess.eliminar(entidadPrueba));

        assertEquals("Error al eliminar el registro", exception.getMessage());
    }

    // ==================== PRUEBAS PARA ACTUALIZAR ====================

    @Test
    void actualizar_ConRegistroValido_DebeActualizarExitosamente() {
        // Arrange
        when(entityManager.merge(entidadPrueba)).thenReturn(entidadPrueba);

        // Act
        EtapasAdmision resultado = dataAccess.actualizar(entidadPrueba);

        // Assert
        assertNotNull(resultado);
        assertEquals(entidadPrueba.getId(), resultado.getId());
        assertEquals(entidadPrueba.getNombre(), resultado.getNombre());
        verify(entityManager, times(1)).merge(entidadPrueba);
        verify(entityManager, times(1)).flush();
    }

    @Test
    void actualizar_ConRegistroNulo_DebeLanzarIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dataAccess.actualizar(null));

        assertEquals("El registro a actualizar no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).merge(any());
    }

    @Test
    void actualizar_ConEntityManagerNulo_DebeLanzarIllegalStateException() {
        // Arrange
        IngresoDefaultDataAccessImpl dataAccessSinEM = new IngresoDefaultDataAccessImpl() {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dataAccessSinEM.actualizar(entidadPrueba));

        assertEquals("EntityManager no inicializado", exception.getMessage());
    }

    @Test
    void actualizar_ConExcepcionAlMergear_DebeLanzarIllegalStateException() {
        // Arrange
        when(entityManager.merge(entidadPrueba))
                .thenThrow(new RuntimeException("Error al actualizar"));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dataAccess.actualizar(entidadPrueba));

        assertTrue(exception.getMessage().contains("Error al actualizar registro de EtapasAdmision"));
    }

    // ==================== PRUEBAS PARA LEER ====================

    @Test
    void leer_ConIdValido_DebeRetornarEntidad() {
        // Arrange
        Short id = (short) 1;
        when(entityManager.find(EtapasAdmision.class, id)).thenReturn(entidadPrueba);

        // Act
        EtapasAdmision resultado = dataAccess.leer(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(entidadPrueba.getId(), resultado.getId());
        assertEquals(entidadPrueba.getNombre(), resultado.getNombre());
        verify(entityManager, times(1)).find(EtapasAdmision.class, id);
    }

    @Test
    void leer_ConIdInexistente_DebeRetornarNull() {
        // Arrange
        Short id = (short) 999;
        when(entityManager.find(EtapasAdmision.class, id)).thenReturn(null);

        // Act
        EtapasAdmision resultado = dataAccess.leer(id);

        // Assert
        assertNull(resultado);
        verify(entityManager, times(1)).find(EtapasAdmision.class, id);
    }

    @Test
    void leer_ConIdNulo_DebeLanzarIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dataAccess.leer(null));

        assertEquals("El id no puede ser nulo", exception.getMessage());
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void leer_ConEntityManagerNulo_DebeLanzarIllegalStateException() {
        // Arrange
        IngresoDefaultDataAccessImpl dataAccessSinEM = new IngresoDefaultDataAccessImpl() {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dataAccessSinEM.leer((short) 1));

        assertEquals("EntityManager no inicializado", exception.getMessage());
    }

    @Test
    void leer_ConExcepcionAlBuscar_DebeLanzarIllegalStateException() {
        // Arrange
        when(entityManager.find(any(), any()))
                .thenThrow(new RuntimeException("Error en la base de datos"));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dataAccess.leer((short) 1));

        assertTrue(exception.getMessage().contains("Error al leer registro de EtapasAdmision"));
    }

    // ==================== PRUEBAS PARA FINDRANGE ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornarLista() {
        // Arrange
        int first = 0;
        int max = 10;
        List<EtapasAdmision> listaEsperada = Arrays.asList(entidadPrueba);

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(EtapasAdmision.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(EtapasAdmision.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(first)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(max)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(listaEsperada);

        // Act
        List<EtapasAdmision> resultado = dataAccess.findRange(first, max);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(entidadPrueba.getId(), resultado.get(0).getId());
        verify(typedQuery, times(1)).setFirstResult(first);
        verify(typedQuery, times(1)).setMaxResults(max);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    void findRange_ConFirstNegativo_DebeLanzarIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> dataAccess.findRange(-1, 10));
        verify(entityManager, never()).getCriteriaBuilder();
    }

    @Test
    void findRange_ConMaxCero_DebeLanzarIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> dataAccess.findRange(0, 0));
        verify(entityManager, never()).getCriteriaBuilder();
    }

    @Test
    void findRange_ConMaxNegativo_DebeLanzarIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> dataAccess.findRange(0, -5));
        verify(entityManager, never()).getCriteriaBuilder();
    }

    @Test
    void findRange_ConEntityManagerNulo_DebeLanzarIllegalStateException() {
        // Arrange
        IngresoDefaultDataAccessImpl dataAccessSinEM = new IngresoDefaultDataAccessImpl() {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dataAccessSinEM.findRange(0, 10));

        assertEquals("EntityManager no inicializado", exception.getMessage());
    }

    @Test
    void findRange_ConExcepcionEnQuery_DebeLanzarIllegalStateException() {
        // Arrange
        when(entityManager.getCriteriaBuilder())
                .thenThrow(new RuntimeException("Error en criteriaBuilder"));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dataAccess.findRange(0, 10));

        assertEquals("No se pudo acceder al repositorio", exception.getMessage());
    }

    @Test
    void findRange_ConPaginacionCorrecta_DebeConfigurarseCorrectamente() {
        // Arrange
        int first = 10;
        int max = 5;

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(EtapasAdmision.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(EtapasAdmision.class)).thenReturn(root);
        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(first)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(max)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList());

        // Act
        dataAccess.findRange(first, max);

        // Assert
        verify(typedQuery).setFirstResult(10);
        verify(typedQuery).setMaxResults(5);
    }

    // ==================== PRUEBAS PARA COUNT ====================

    @Test
    void count_ConRegistrosExistentes_DebeRetornarTotal() {
        // Arrange
        Long totalEsperado = 21L;

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQueryLong);
        when(criteriaQueryLong.from(EtapasAdmision.class)).thenReturn(root);
        when(criteriaBuilder.count(root)).thenReturn(null);
        when(criteriaQueryLong.select(any())).thenReturn(criteriaQueryLong);
        when(entityManager.createQuery(criteriaQueryLong)).thenReturn(typedQueryLong);
        when(typedQueryLong.getSingleResult()).thenReturn(totalEsperado);

        // Act
        int resultado = dataAccess.count();

        // Assert
        assertEquals(21, resultado);
        verify(criteriaBuilder, times(1)).count(root);
        verify(typedQueryLong, times(1)).getSingleResult();
    }

    @Test
    void count_ConTablaVacia_DebeRetornarCero() {
        // Arrange
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQueryLong);
        when(criteriaQueryLong.from(EtapasAdmision.class)).thenReturn(root);
        when(criteriaBuilder.count(root)).thenReturn(null);
        when(criteriaQueryLong.select(any())).thenReturn(criteriaQueryLong);
        when(entityManager.createQuery(criteriaQueryLong)).thenReturn(typedQueryLong);
        when(typedQueryLong.getSingleResult()).thenReturn(0L);

        // Act
        int resultado = dataAccess.count();

        // Assert
        assertEquals(0, resultado);
    }

    @Test
    void count_ConEntityManagerNulo_DebeLanzarIllegalStateException() {
        // Arrange
        IngresoDefaultDataAccessImpl dataAccessSinEM = new IngresoDefaultDataAccessImpl() {
            @Override
            public EntityManager getEntityManager() {
                return null;
            }
        };

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dataAccessSinEM.count());

        assertEquals("EntityManager no inicializado", exception.getMessage());
    }

    @Test
    void count_ConExcepcionEnQuery_DebeLanzarIllegalStateException() {
        // Arrange
        when(entityManager.getCriteriaBuilder())
                .thenThrow(new RuntimeException("Error al contar"));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> dataAccess.count());

        assertEquals("No se pudo acceder al repositorio", exception.getMessage());
    }

    @Test
    void count_ConValorGrande_DebeConvertirCorrectamente() {
        // Arrange
        Long totalEsperado = 500000L;

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(criteriaQueryLong);
        when(criteriaQueryLong.from(EtapasAdmision.class)).thenReturn(root);
        when(criteriaBuilder.count(root)).thenReturn(null);
        when(criteriaQueryLong.select(any())).thenReturn(criteriaQueryLong);
        when(entityManager.createQuery(criteriaQueryLong)).thenReturn(typedQueryLong);
        when(typedQueryLong.getSingleResult()).thenReturn(totalEsperado);

        // Act
        int resultado = dataAccess.count();

        // Assert
        assertEquals(500000, resultado);
    }
}
