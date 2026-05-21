package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PruebasAdmisionDAOIT extends AbstractBaseIT {


    public PruebasAdmisionDAOIT() {
    }

    @Test
    public void testCount() {
        System.out.println("TEST PruebasAdmision DAOIT COUNT");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            int resultado = cut.count();

            // Tenemos 2 registros iniciales en la BD
            assertEquals(2, resultado);
            return null;
        });
    }

    @Test
    public void testFindRange() {
        System.out.println("TEST PruebasAdmision DAOIT FIND RANGE");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            List<PruebasAdmision> resultado = cut.findRange(0, 2);

            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size());

            return null;
        });
    }

    @Test
    public void testCrear() {
        System.out.println("TEST PruebasAdmision DAOIT CREAR");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            PruebasAdmision nuevo = new PruebasAdmision();
            nuevo.setNombrePrueba("PRUEBA DE ADMISION 2027");
            nuevo.setAnio(2027);
            nuevo.setActiva(false);

            cut.crear(nuevo);

            // Verificamos que se sumó una prueba en esta transacción
            assertEquals(3, cut.count());
            assertNotNull(nuevo.getIdPruebaAdmision());
            //Para verificar por consola
            System.out.println("NUmeor de registros actaules: "+cut.count());
            return null;
        });
        //Verificar el rollback: no debemos ensuciar la BD
        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;
            assertEquals(2, cut.count());
            return null;
        });
    }

    @Test
    public void testLeer() {
        System.out.println("TEST PruebasAdmision DAOIT LEER");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            //Ler el primer registro que viene de la BD
            PruebasAdmision pruebaExistente = cut.findRange(0, 1).get(0);

            //Probar método leer
            PruebasAdmision resultado = cut.leer(pruebaExistente.getIdPruebaAdmision());

            assertNotNull(resultado, "El ID de la prueba no puede ser nulo porque ya existe");
            assertEquals(pruebaExistente.getIdPruebaAdmision(), resultado.getIdPruebaAdmision());
            assertEquals(pruebaExistente.getNombrePrueba(), resultado.getNombrePrueba());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        System.out.println("TEST PruebasAdmision DAOIT ACTUALIZAR");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            // Tomamos una prueba existente
            PruebasAdmision pruebaActualizada = cut.findRange(0, 1).get(0);
            assertNotNull(pruebaActualizada);

            // Modificamos sus datos
            pruebaActualizada.setNombrePrueba("PRUEBA MODIFICADA ST");
            pruebaActualizada.setActiva(true);
            pruebaActualizada.setAnio(2030);

            // Guardamos los cambios
            PruebasAdmision resultado = cut.actualizar(pruebaActualizada);

            // Verificamos
            assertNotNull(resultado);
            assertTrue(resultado.getActiva());
            assertEquals(2030, resultado.getAnio());
            assertEquals("PRUEBA MODIFICADA ST", resultado.getNombrePrueba());

            return null;
        });
    }

    @Test
    public void testEliminar() {
        System.out.println("TEST PruebasAdmision DAOIT ELIMINAR");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            // Crear un dato temporal para eliminarlo
            PruebasAdmision pruebaAEliminar = new PruebasAdmision();
            pruebaAEliminar.setNombrePrueba("PRUEBA TEMPORAL PARA BORRAR");
            pruebaAEliminar.setAnio(2099);
            pruebaAEliminar.setActiva(false);

            cut.crear(pruebaAEliminar);

            // Verificamos que se creó
            assertEquals(3, cut.count());

            //Eliminamos
            cut.eliminar(pruebaAEliminar);

            // Verificamos que bajó de nuevo a 2 y que el ID ya no existe
            assertEquals(2, cut.count());
            assertNull(cut.leer(pruebaAEliminar.getIdPruebaAdmision()), "La prueba debería haber sido eliminada y retornar null");
            //Verificamos en consola
            System.out.println("Dato eliminado: "+ cut.leer(pruebaAEliminar.getIdPruebaAdmision()));
            return null;
        });
    }

}
