package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClaveId;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PreguntasPorClaveDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_CLAVE_A    = UUID.fromString("08000000-0000-0000-0000-000000000001");
    private static final UUID ID_CLAVE_B    = UUID.fromString("aaaabbbb-cccc-dddd-eeee-ffffffffffff");
    private static final UUID ID_PREGUNTA_1 = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_3 = UUID.fromString("f1000000-0000-0000-0000-000000000003");
    private static final UUID ID_PREGUNTA_55 = UUID.fromString("55555555-5555-5555-5555-555555555555");

    public PreguntasPorClaveDAOIT() {
    }

    @BeforeAll
    void inicializar() {
        // La configuracion de postgres y emf se realiza en AbstractBaseIT
    }

    @Test
    @Order(1)
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql  4 registros: Clave A→{pregunta1, pregunta2}, Clave B→{pregunta3, pregunta4}
            assertTrue(resultado > 0);
            assertEquals(4, resultado);

            return null;
        });
    }

    @Test
    @Order(2)
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            List<PreguntasPorClave> resultado = cut.findRange(0, 10);

            // Aún no se ha insertado nada  sigue habiendo 4
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(4, resultado.size());

            return null;
        });
    }

    @Test
    @Order(3)
    public void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear un registro temporal de PreguntasPorClave y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            // Clave A solo tiene pregunta1 y pregunta2 en el init.sql
            //  (Clave_A, pregunta3) es una combinación nueva sin conflicto de clave primaria
            ClavesExamen clave    = em.find(ClavesExamen.class, ID_CLAVE_A);
            BancoPregunta pregunta = em.find(BancoPregunta.class, ID_PREGUNTA_3);

            PreguntasPorClaveId id = new PreguntasPorClaveId();
            id.setIdClave(ID_CLAVE_A);
            id.setIdPregunta(ID_PREGUNTA_3);

            PreguntasPorClave nuevo = new PreguntasPorClave();
            nuevo.setIdPreguntaPorClave(id);
            nuevo.setClaveExamen(clave);
            nuevo.setBancoPregunta(pregunta);

            cut.crear(nuevo);

            assertEquals(5, cut.count());

            return null;
        });

        // Verificar que después del rollback implícito la BD queda con 4 registros
        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            assertEquals(4, cut.count());
            return null;
        });
    }

    @Test
    @Order(4)
    public void testLeer() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            // Leer Clave A + Pregunta 1 — primer registro del init.sql
            PreguntasPorClaveId id = new PreguntasPorClaveId();
            id.setIdClave(ID_CLAVE_A);
            id.setIdPregunta(ID_PREGUNTA_1);

            PreguntasPorClave resultado = cut.leer(id);

            assertNotNull(resultado);
            assertEquals(ID_CLAVE_A,    resultado.getIdPreguntaPorClave().getIdClave());
            assertEquals(ID_PREGUNTA_1, resultado.getIdPreguntaPorClave().getIdPregunta());

            return null;
        });
    }

    @Test
    @Order(5)
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            // PreguntasPorClave es una tabla de unión pura: solo tiene los dos UUIDs de la clave
            // compuesta y no posee columnas mutables adicionales.
            // Se verifica que actualizar() no lanza excepción y devuelve el mismo registro intacto.
            PreguntasPorClaveId id = new PreguntasPorClaveId();
            id.setIdClave(ID_CLAVE_B);
            id.setIdPregunta(ID_PREGUNTA_3);

            PreguntasPorClave registro = cut.leer(id);
            assertNotNull(registro);

            PreguntasPorClave resultado = cut.actualizar(registro);

            // No hay campos que cambien; se verifica que la operación es idempotente
            assertNotNull(resultado);
            assertEquals(ID_CLAVE_B,    resultado.getIdPreguntaPorClave().getIdClave());
            assertEquals(ID_PREGUNTA_3, resultado.getIdPreguntaPorClave().getIdPregunta());

            // El conteo se mantiene en 4 dentro de la transacción
            assertEquals(4, cut.count());

            return null;
        });
    }

    @Test
    @Order(6)
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        // Crear y eliminar un registro temporal de PreguntasPorClave dentro de una única transacción
        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            ClavesExamen clave    = em.find(ClavesExamen.class, ID_CLAVE_A);
            BancoPregunta pregunta = em.find(BancoPregunta.class, ID_PREGUNTA_3);

            PreguntasPorClaveId id = new PreguntasPorClaveId();
            id.setIdClave(ID_CLAVE_A);
            id.setIdPregunta(ID_PREGUNTA_3);

            PreguntasPorClave nuevo = new PreguntasPorClave();
            nuevo.setIdPreguntaPorClave(id);
            nuevo.setClaveExamen(clave);
            nuevo.setBancoPregunta(pregunta);

            cut.crear(nuevo);
            assertEquals(5, cut.count());

            cut.eliminar(nuevo);
            assertEquals(4, cut.count());

            return null;
        });
    }

    // ===================== CRUD FALTANTE =====================

    @Test
    @Order(7)
    public void testLeerNoExiste() {
        System.out.println("PreguntasPorClaveDAOIT.leer() - PK inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            PreguntasPorClaveId idInexistente = new PreguntasPorClaveId();
            idInexistente.setIdClave(UUID.randomUUID());
            idInexistente.setIdPregunta(UUID.randomUUID());

            PreguntasPorClave resultado = cut.leer(idInexistente);
            assertNull(resultado, "Debe retornar null si la PK no existe");
            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    @Order(8)
    public void testExistsByClaveAndPregunta() {
        System.out.println("PreguntasPorClaveDAOIT.existsByClaveAndPregunta()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            // Clave A + pregunta1 existe → true
            assertTrue(cut.existsByClaveAndPregunta(ID_CLAVE_A, ID_PREGUNTA_1));

            // Clave A + pregunta 55555555 existe → true
            assertTrue(cut.existsByClaveAndPregunta(ID_CLAVE_A, ID_PREGUNTA_55));

            // Clave A + pregunta3 NO existe → false
            assertFalse(cut.existsByClaveAndPregunta(ID_CLAVE_A, ID_PREGUNTA_3));

            // Clave B + pregunta3 existe → true
            assertTrue(cut.existsByClaveAndPregunta(ID_CLAVE_B, ID_PREGUNTA_3));

            // Clave B + pregunta1 NO existe → false
            assertFalse(cut.existsByClaveAndPregunta(ID_CLAVE_B, ID_PREGUNTA_1));

            return null;
        });
    }

    @Test
    @Order(9)
    public void testExistsByClaveAndPreguntaNulos() {
        System.out.println("PreguntasPorClaveDAOIT.existsByClaveAndPregunta() - nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByClaveAndPregunta(null, ID_PREGUNTA_1));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByClaveAndPregunta(ID_CLAVE_A, null));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByClaveAndPregunta(null, null));
            return null;
        });
    }

    @Test
    @Order(10)
    public void testFindPreguntasByClave() {
        System.out.println("PreguntasPorClaveDAOIT.findPreguntasByClave()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            // Clave A tiene 2 preguntas
            List<PreguntasPorClave> resultadoA = cut.findPreguntasByClave(ID_CLAVE_A);
            assertNotNull(resultadoA);
            assertEquals(2, resultadoA.size());

            // Clave B tiene 2 preguntas
            List<PreguntasPorClave> resultadoB = cut.findPreguntasByClave(ID_CLAVE_B);
            assertNotNull(resultadoB);
            assertEquals(2, resultadoB.size());

            return null;
        });
    }

    @Test
    @Order(11)
    public void testFindPreguntasByClaveInexistente() {
        System.out.println("PreguntasPorClaveDAOIT.findPreguntasByClave() - clave inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            List<PreguntasPorClave> resultado = cut.findPreguntasByClave(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    @Order(12)
    public void testFindPreguntasByClaveNulo() {
        System.out.println("PreguntasPorClaveDAOIT.findPreguntasByClave() - null retorna lista vacia");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            // El metodo retorna Collections.emptyList() si idClave es null
            List<PreguntasPorClave> resultado = cut.findPreguntasByClave(null);
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    @Order(13)
    public void testCountPreguntasByClave() {
        System.out.println("PreguntasPorClaveDAOIT.countPreguntasByClave()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            // Clave A tiene 2 preguntas
            assertEquals(2, cut.countPreguntasByClave(ID_CLAVE_A));

            // Clave B tiene 2 preguntas
            assertEquals(2, cut.countPreguntasByClave(ID_CLAVE_B));

            // Clave inexistente → 0
            assertEquals(0, cut.countPreguntasByClave(UUID.randomUUID()));

            return null;
        });
    }

    @Test
    @Order(14)
    public void testCountPreguntasByClaveNulo() {
        System.out.println("PreguntasPorClaveDAOIT.countPreguntasByClave() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.countPreguntasByClave(null));
            return null;
        });
    }
}
