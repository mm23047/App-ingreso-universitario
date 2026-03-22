package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ClavesExamanDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_PRUEBA_1 = UUID.fromString("d1000000-0000-0000-0000-000000000001");

    @Test
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 2 claves de examen (Clave A, Clave B)
            assertTrue(resultado > 0);
            assertEquals(2, resultado);

            return null;
        });
    }

    @Test
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            List<ClavesExaman> resultado = cut.findRange(0, 10);

            // BD recién iniciada con init.sql → 2 claves de examen
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
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            // Asociar la nueva clave a la prueba de admisión 2026 (d1...001)
            PruebasAdmision prueba = em.find(PruebasAdmision.class, ID_PRUEBA_1);
            assertNotNull(prueba);

            ClavesExaman nueva = new ClavesExaman();
            nueva.setIdPrueba(prueba);
            nueva.setNombreClave("Clave C");

            cut.crear(nueva);

            // Validación dentro de la transacción
            assertEquals(3, cut.count());

            return null;
        });

        // Verificar rollback: vuelve a 2
        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            assertEquals(2, cut.count());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            // Obtener la primera clave del init.sql
            ClavesExaman clave = cut.findRange(0, 1).get(0);
            assertNotNull(clave);

            // Modificar dentro de la transacción
            clave.setNombreClave("Clave Actualizada");

            ClavesExaman resultado = cut.actualizar(clave);

            assertNotNull(resultado);
            assertEquals("Clave Actualizada", resultado.getNombreClave());

            return null;
        });
    }

    @Test
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            // Crear una nueva clave para eliminarla
            PruebasAdmision prueba = em.find(PruebasAdmision.class, ID_PRUEBA_1);
            assertNotNull(prueba);

            ClavesExaman nueva = new ClavesExaman();
            nueva.setIdPrueba(prueba);
            nueva.setNombreClave("Clave para eliminar");

            cut.crear(nueva);
            assertEquals(3, cut.count());

            // Eliminar la clave recién creada
            cut.eliminar(nueva);
            assertEquals(2, cut.count());

            return null;
        });
    }
}
