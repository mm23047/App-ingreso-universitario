package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AspirantesDatoDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql

    @Test
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 2 aspirantes (jperez, mmartinez)
            assertTrue(resultado > 0);
            assertEquals(2, resultado);

            return null;
        });
    }

    @Test
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            List<AspirantesDato> resultado = cut.findRange(0, 10);

            // BD recién iniciada con init.sql → 2 aspirantes
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size());

            return null;
        });
    }

    @Test
    public void testFindByDui() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            AspirantesDato resultado = cut.findByDui("01234567-8");

            assertNotNull(resultado);
            assertEquals("01234567-8", resultado.getDui());

            return null;
        });
    }

    @Test
    public void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            AspirantesDato nuevo = new AspirantesDato();
            nuevo.setNombres("Test Aspirante");
            nuevo.setApellidos("Apellido Prueba");
            nuevo.setDui("98765432-1");
            nuevo.setCorreo("test.aspirante@example.com");
            nuevo.setFechaNacimiento(LocalDate.of(1990,1,1));
            nuevo.setUsaSillaRuedas(false);

            cut.crear(nuevo);

            // Validación dentro de la transacción
            assertEquals(3, cut.count());

            return null;
        });

        // Verificar rollback: vuelve a 2
        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            assertEquals(2, cut.count());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            // Obtener el primer aspirante del init.sql
            AspirantesDato aspirante = cut.findRange(0, 1).get(0);
            assertNotNull(aspirante);

            // Modificar dentro de la transacción
            aspirante.setNombres("Aspirante Actualizado");

            AspirantesDato resultado = cut.actualizar(aspirante);

            assertNotNull(resultado);
            assertEquals("Aspirante Actualizado", resultado.getNombres());

            return null;
        });
    }

    @Test
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            // Crear un nuevo aspirante para eliminarlo
            AspirantesDato nuevo = new AspirantesDato();
            nuevo.setNombres("Aspirante para eliminar");
            nuevo.setApellidos("Apellido");
            nuevo.setDui("87654321-0");
            nuevo.setCorreo("elim@example.com");
            nuevo.setFechaNacimiento(LocalDate.of(1992,2,2));
            nuevo.setUsaSillaRuedas(false);

            cut.crear(nuevo);
            assertEquals(3, cut.count());

            // Eliminar el aspirante recién creado
            cut.eliminar(nuevo);
            assertEquals(2, cut.count());

            return null;
        });
    }
}
