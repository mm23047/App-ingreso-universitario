package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarrerasElegidaDAOIT extends AbstractBaseIT {

    public CarrerasElegidaDAOIT() {
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
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql  4 carreras elegidas
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
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            List<CarrerasElegida> resultado = cut.findRange(0, 10);

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

        // Crear una carrera elegida temporal y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            // Usar una combinacion inscripcion+carrera que no existe en el init.sql
            // inscripcion 001 ya tiene ICS y ISI → usamos MAT (existe en catalogo)
            UUID idInscripcion = UUID.fromString("09000000-0000-0000-0000-000000000001");
            InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, idInscripcion);
            CatalogoCarrera carrera = em.find(CatalogoCarrera.class, "MAT");

            CarrerasElegidaId clave = new CarrerasElegidaId();
            clave.setIdInscripcion(idInscripcion);
            clave.setIdCarrera("MAT");

            CarrerasElegida nueva = new CarrerasElegida();
            nueva.setIdCarreraElegida(clave);
            nueva.setInscripcionesPrueba(inscripcion);
            nueva.setCatalogoCarrera(carrera);
            nueva.setPrioridad((short) 3);

            cut.crear(nueva);

            assertEquals(5, cut.count());

            return null;
        });

        // Verificar que después del rollback implícito la BD queda con 4 registros
        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
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
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            // Leer la primera carrera elegida del init.sql: inscripcion 001 → ICS, prioridad 1
            CarrerasElegidaId clave = new CarrerasElegidaId();
            clave.setIdInscripcion(UUID.fromString("09000000-0000-0000-0000-000000000001"));
            clave.setIdCarrera("ICS");

            CarrerasElegida resultado = cut.leer(clave);

            assertNotNull(resultado);
            assertEquals("ICS", resultado.getIdCarreraElegida().getIdCarrera());
            assertEquals(UUID.fromString("09000000-0000-0000-0000-000000000001"),
                    resultado.getIdCarreraElegida().getIdInscripcion());
            assertEquals((short) 1, resultado.getPrioridad());

            return null;
        });
    }

    @Test
    @Order(5)
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            // Leer inscripcion 001 → ISI, prioridad 2 → cambiar prioridad a 5
            CarrerasElegidaId clave = new CarrerasElegidaId();
            clave.setIdInscripcion(UUID.fromString("09000000-0000-0000-0000-000000000001"));
            clave.setIdCarrera("ISI");

            CarrerasElegida elegida = cut.leer(clave);
            elegida.setPrioridad((short) 5);

            CarrerasElegida resultado = cut.actualizar(elegida);

            assertNotNull(resultado);
            assertEquals((short) 5, resultado.getPrioridad());

            // Dentro de la misma transacción el cambio es visible
            CarrerasElegida verificacion = cut.leer(clave);
            assertEquals((short) 5, verificacion.getPrioridad());

            return null;
        });
    }

    @Test
    @Order(6)
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        // Crear y eliminar una carrera elegida temporal dentro de una única transacción
        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            UUID idInscripcion = UUID.fromString("09000000-0000-0000-0000-000000000001");
            InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, idInscripcion);
            CatalogoCarrera carrera = em.find(CatalogoCarrera.class, "MAT");

            CarrerasElegidaId clave = new CarrerasElegidaId();
            clave.setIdInscripcion(idInscripcion);
            clave.setIdCarrera("MAT");

            CarrerasElegida nueva = new CarrerasElegida();
            nueva.setIdCarreraElegida(clave);
            nueva.setInscripcionesPrueba(inscripcion);
            nueva.setCatalogoCarrera(carrera);
            nueva.setPrioridad((short) 3);

            cut.crear(nueva);
            assertEquals(5, cut.count());

            cut.eliminar(nueva);
            assertEquals(4, cut.count());

            return null;
        });
    }

    // ===================== NAMED QUERIES - CAMINO FELIZ =====================

    @Test
    @Order(7)
    public void testExistsByInscripcionAndPrioridad() {
        System.out.println("CarrerasElegidaDAOIT.existsByInscripcionAndPrioridad()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            UUID idInscripcion001 = UUID.fromString("09000000-0000-0000-0000-000000000001");

            // Prioridad 1 está ocupada por ICS → true
            assertTrue(cut.existsByInscripcionAndPrioridad(idInscripcion001, (short) 1));
            // Prioridad 2 está ocupada por ISI → true
            assertTrue(cut.existsByInscripcionAndPrioridad(idInscripcion001, (short) 2));
            // Prioridad 3 está libre → false
            assertFalse(cut.existsByInscripcionAndPrioridad(idInscripcion001, (short) 3));

            return null;
        });
    }

    @Test
    @Order(8)
    public void testFindByInscripcionOrderByPrioridad() {
        System.out.println("CarrerasElegidaDAOIT.findByInscripcionOrderByPrioridad()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            UUID idInscripcion001 = UUID.fromString("09000000-0000-0000-0000-000000000001");
            List<CarrerasElegida> resultado = cut.findByInscripcionOrderByPrioridad(idInscripcion001);

            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            // Orden ASC: prioridad 1 (ICS) primero, prioridad 2 (ISI) después
            assertEquals((short) 1, resultado.get(0).getPrioridad());
            assertEquals("ICS", resultado.get(0).getIdCarreraElegida().getIdCarrera());
            assertEquals((short) 2, resultado.get(1).getPrioridad());
            assertEquals("ISI", resultado.get(1).getIdCarreraElegida().getIdCarrera());

            return null;
        });
    }

    @Test
    @Order(9)
    public void testFindByInscripcionOrderByPrioridadInexistente() {
        System.out.println("CarrerasElegidaDAOIT.findByInscripcionOrderByPrioridad() - inscripcion inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            List<CarrerasElegida> resultado = cut.findByInscripcionOrderByPrioridad(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());

            return null;
        });
    }

    @Test
    @Order(10)
    public void testFindByInscripcionAndCarrera() {
        System.out.println("CarrerasElegidaDAOIT.findByInscripcionAndCarrera()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            UUID idInscripcion001 = UUID.fromString("09000000-0000-0000-0000-000000000001");

            // Inscripción 001 tiene ICS → debe encontrarla
            CarrerasElegida resultado = cut.findByInscripcionAndCarrera(idInscripcion001, "ICS");
            assertNotNull(resultado);
            assertEquals("ICS", resultado.getIdCarreraElegida().getIdCarrera());
            assertEquals((short) 1, resultado.getPrioridad());

            // Inscripción 001 NO tiene MAT → null
            CarrerasElegida noExiste = cut.findByInscripcionAndCarrera(idInscripcion001, "MAT");
            assertNull(noExiste);

            return null;
        });
    }

    @Test
    @Order(11)
    public void testExistsByInscripcionAndCarrera() {
        System.out.println("CarrerasElegidaDAOIT.existsByInscripcionAndCarrera()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            UUID idInscripcion001 = UUID.fromString("09000000-0000-0000-0000-000000000001");

            // ICS está elegida → true
            assertTrue(cut.existsByInscripcionAndCarrera(idInscripcion001, "ICS"));
            // ISI está elegida → true
            assertTrue(cut.existsByInscripcionAndCarrera(idInscripcion001, "ISI"));
            // MAT no está elegida → false
            assertFalse(cut.existsByInscripcionAndCarrera(idInscripcion001, "MAT"));

            return null;
        });
    }

    @Test
    @Order(12)
    public void testFindByInscripcionAndPrioridadLevel() {
        System.out.println("CarrerasElegidaDAOIT.findByInscripcionAndPrioridadLevel()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            UUID idInscripcion001 = UUID.fromString("09000000-0000-0000-0000-000000000001");

            // Prioridad 1 → ICS
            CarrerasElegida prio1 = cut.findByInscripcionAndPrioridadLevel(idInscripcion001, (short) 1);
            assertNotNull(prio1);
            assertEquals("ICS", prio1.getIdCarreraElegida().getIdCarrera());

            // Prioridad 2 → ISI
            CarrerasElegida prio2 = cut.findByInscripcionAndPrioridadLevel(idInscripcion001, (short) 2);
            assertNotNull(prio2);
            assertEquals("ISI", prio2.getIdCarreraElegida().getIdCarrera());

            // Prioridad 3 → null (no existe)
            CarrerasElegida prio3 = cut.findByInscripcionAndPrioridadLevel(idInscripcion001, (short) 3);
            assertNull(prio3);

            return null;
        });
    }

    // ===================== VALIDACIONES - CAMINO DE ERROR =====================

    @Test
    @Order(13)
    public void testExistsByInscripcionAndPrioridadNulos() {
        System.out.println("CarrerasElegidaDAOIT.existsByInscripcionAndPrioridad() - parametros nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByInscripcionAndPrioridad(null, (short) 1));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByInscripcionAndPrioridad(UUID.randomUUID(), null));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByInscripcionAndPrioridad(null, null));

            return null;
        });
    }

    @Test
    @Order(14)
    public void testFindByInscripcionOrderByPrioridadNulo() {
        System.out.println("CarrerasElegidaDAOIT.findByInscripcionOrderByPrioridad() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByInscripcionOrderByPrioridad(null));

            return null;
        });
    }

    @Test
    @Order(15)
    public void testFindByInscripcionAndCarreraNulos() {
        System.out.println("CarrerasElegidaDAOIT.findByInscripcionAndCarrera() - parametros nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByInscripcionAndCarrera(null, "ICS"));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByInscripcionAndCarrera(UUID.randomUUID(), null));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByInscripcionAndCarrera(UUID.randomUUID(), "   "));

            return null;
        });
    }

    @Test
    @Order(16)
    public void testExistsByInscripcionAndCarreraNulos() {
        System.out.println("CarrerasElegidaDAOIT.existsByInscripcionAndCarrera() - parametros nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByInscripcionAndCarrera(null, "ICS"));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByInscripcionAndCarrera(UUID.randomUUID(), null));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByInscripcionAndCarrera(UUID.randomUUID(), ""));

            return null;
        });
    }

    @Test
    @Order(17)
    public void testFindByInscripcionAndPrioridadLevelNulos() {
        System.out.println("CarrerasElegidaDAOIT.findByInscripcionAndPrioridadLevel() - parametros nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByInscripcionAndPrioridadLevel(null, (short) 1));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByInscripcionAndPrioridadLevel(UUID.randomUUID(), null));

            return null;
        });
    }
}
