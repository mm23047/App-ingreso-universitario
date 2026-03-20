package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AreasConocimientoDAOIT extends AbstractBaseIT {

    @Test
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            int resultado = cut.count();

            assertTrue(resultado > 0);
            assertEquals(3, resultado);

            return null;
        });
    }

    @Test
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            List<AreasConocimiento> resultado = cut.findRange(0, 10);

            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(3, resultado.size());

            return null;
        });
    }

    @Test
    public void testCrear() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            AreasConocimiento nuevo = new AreasConocimiento();
            nuevo.setNombreArea("registro prueba");

            cut.crear(nuevo);

            // Validación dentro de la transacción
            assertEquals(4, cut.count());

            return null;
        });

        // ✅ ÚNICA verificación de rollback (correcto)
        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            assertEquals(3, cut.count());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            AreasConocimiento area = cut.findRange(0, 1).get(0);

            area.setNombreArea("modificado");

            AreasConocimiento actualizado = cut.actualizar(area);

            assertEquals("modificado", actualizado.getNombreArea());

            return null;
        });
    }

   @Test
public void testEliminar() {
    assertTrue(postgres.isRunning());

    ejecutarEnTransaccion(em -> {
        AreasConocimientoDAO cut = new AreasConocimientoDAO();
        cut.em = em;

        // Crear dato aislado
        AreasConocimiento area = new AreasConocimiento();
        area.setNombreArea("temporal");

        cut.crear(area);

        assertEquals(4, cut.count());

        // Eliminar
        cut.eliminar(area);

        assertEquals(3, cut.count());

        return null;
    });
}
}