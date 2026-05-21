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
    private static final UUID ID_CLAVE_B    = UUID.fromString("08000000-0000-0000-0000-000000000002");
    private static final UUID ID_PREGUNTA_1 = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_3 = UUID.fromString("f1000000-0000-0000-0000-000000000003");

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
            nuevo.setIdClave(clave);
            nuevo.setIdPregunta(pregunta);

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
            nuevo.setIdClave(clave);
            nuevo.setIdPregunta(pregunta);

            cut.crear(nuevo);
            assertEquals(5, cut.count());

            cut.eliminar(nuevo);
            assertEquals(4, cut.count());

            return null;
        });
    }
}
