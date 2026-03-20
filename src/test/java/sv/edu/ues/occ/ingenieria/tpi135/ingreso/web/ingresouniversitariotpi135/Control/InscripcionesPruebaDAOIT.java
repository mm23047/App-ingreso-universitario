package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InscripcionesPruebaDAOIT extends AbstractBaseIT {

    public InscripcionesPruebaDAOIT() {
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
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql  2 inscripciones
            assertTrue(resultado > 0);
            assertEquals(2, resultado);

            return null;
        });
    }

    @Test
    @Order(2)
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            List<InscripcionesPrueba> resultado = cut.findRange(0, 10);

            // Aún no se ha insertado nada  sigue habiendo 2
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size());

            return null;
        });
    }

    @Test
    @Order(3)
    public void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear una inscripción temporal y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            // Usar aspirante y prueba existentes del init.sql que no generan conflicto
            AspirantesDato aspirante = em.find(AspirantesDato.class,
                UUID.fromString("e1000000-0000-0000-0000-000000000001"));
            PruebasAdmision prueba = em.find(PruebasAdmision.class,
                UUID.fromString("d1000000-0000-0000-0000-000000000002"));

            InscripcionesPrueba nueva = new InscripcionesPrueba();
            nueva.setIdAspirante(aspirante);
            nueva.setIdPrueba(prueba);
            nueva.setEstado("PENDIENTE");

            cut.crear(nueva);

            assertNotNull(nueva.getId());
            assertEquals(3, cut.count());

            return null;
        });

        // Verificar que después del rollback implícito la BD queda con 2 inscripciones
        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            assertEquals(2, cut.count());
            return null;
        });
    }

    @Test
    @Order(4)
    public void testLeer() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            UUID idExistente = UUID.fromString("09000000-0000-0000-0000-000000000001");
            InscripcionesPrueba resultado = cut.leer(idExistente);

            assertNotNull(resultado);
            assertEquals(idExistente, resultado.getId());
            assertEquals("INSCRITO", resultado.getEstado());

            return null;
        });
    }

    @Test
    @Order(5)
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            UUID idExistente = UUID.fromString("09000000-0000-0000-0000-000000000001");
            InscripcionesPrueba inscripcion = cut.leer(idExistente);
            inscripcion.setEstado("PROCESADO");

            InscripcionesPrueba resultado = cut.actualizar(inscripcion);

            assertNotNull(resultado);
            assertEquals("PROCESADO", resultado.getEstado());

            // Dentro de la misma transacción el cambio es visible
            InscripcionesPrueba verificacion = cut.leer(idExistente);
            assertEquals("PROCESADO", verificacion.getEstado());

            return null;
        });
    }

    @Test
    @Order(6)
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        // Crear y eliminar una inscripción temporal dentro de una única transacción
        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            AspirantesDato aspirante = em.find(AspirantesDato.class,
                    UUID.fromString("e1000000-0000-0000-0000-000000000001"));
            PruebasAdmision prueba = em.find(PruebasAdmision.class,
                    UUID.fromString("d1000000-0000-0000-0000-000000000002"));

            InscripcionesPrueba nueva = new InscripcionesPrueba();
            nueva.setIdAspirante(aspirante);
            nueva.setIdPrueba(prueba);
            nueva.setEstado("PENDIENTE");

            cut.crear(nueva);
            assertEquals(3, cut.count());

            cut.eliminar(nueva);
            assertEquals(2, cut.count());

            return null;
        });
    }
}
