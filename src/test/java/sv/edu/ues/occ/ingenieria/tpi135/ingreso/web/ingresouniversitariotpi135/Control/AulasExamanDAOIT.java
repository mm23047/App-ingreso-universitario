package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AulasExamanDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_TURNO_1 = UUID.fromString("07000000-0000-0000-0000-000000000001");

    @Test
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulasExamanDAO cut = new AulasExamanDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 2 aulas de examen (AULA-101, AULA-201)
            assertTrue(resultado > 0);
            assertEquals(2, resultado);

            return null;
        });
    }

    @Test
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulasExamanDAO cut = new AulasExamanDAO();
            cut.em = em;

            List<AulasExaman> resultado = cut.findRange(0, 10);

            // BD recién iniciada con init.sql → 2 aulas
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size());

            return null;
        });
    }

    @Test
    public void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            AulasExamanDAO cut = new AulasExamanDAO();
            cut.em = em;

            // Asociar el nuevo aula al turno mañana (07...001)
            TurnosExaman turno = em.find(TurnosExaman.class, ID_TURNO_1);
            assertNotNull(turno);

            AulasExaman nueva = new AulasExaman();
            nueva.setIdTurno(turno);
            nueva.setIdAulaApi("AULA-301");
            nueva.setCapacidad(30);
            nueva.setCuposOcupados(0);
            nueva.setAccesibleSillaRuedas(false);

            cut.crear(nueva);

            // Validación dentro de la transacción
            assertEquals(3, cut.count());

            return null;
        });

        // Verificar rollback: vuelve a 2
        ejecutarEnTransaccion(em -> {
            AulasExamanDAO cut = new AulasExamanDAO();
            cut.em = em;

            assertEquals(2, cut.count());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulasExamanDAO cut = new AulasExamanDAO();
            cut.em = em;

            // Obtener el primer aula del init.sql
            AulasExaman aula = cut.findRange(0, 1).get(0);
            assertNotNull(aula);

            // Modificar dentro de la transacción
            aula.setCapacidad(40);

            AulasExaman resultado = cut.actualizar(aula);

            assertNotNull(resultado);
            assertEquals(40, resultado.getCapacidad());

            return null;
        });
    }

    @Test
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulasExamanDAO cut = new AulasExamanDAO();
            cut.em = em;

            // Crear una nueva aula para eliminarla
            TurnosExaman turno = em.find(TurnosExaman.class, ID_TURNO_1);
            assertNotNull(turno);

            AulasExaman nueva = new AulasExaman();
            nueva.setIdTurno(turno);
            nueva.setIdAulaApi("AULA-401");
            nueva.setCapacidad(35);
            nueva.setCuposOcupados(0);
            nueva.setAccesibleSillaRuedas(true);

            cut.crear(nueva);
            assertEquals(3, cut.count());

            // Eliminar el aula recién creada
            cut.eliminar(nueva);
            assertEquals(2, cut.count());

            return null;
        });
    }
}
