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
}
