package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OpcionesRespuestaDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    // f1...003 = "¿Cuántos planetas tiene el sistema solar?" — se usará para la nueva opción
    private static final UUID ID_PREGUNTA_3 = UUID.fromString("f1000000-0000-0000-0000-000000000003");

    @Test
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 10 opciones de respuesta en total
            assertTrue(resultado > 0);
            assertEquals(10, resultado);

            return null;
        });
    }

    @Test
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
            cut.em = em;

            List<OpcionesRespuesta> resultado = cut.findRange(0, 15);

            // BD recién iniciada con init.sql → 10 opciones
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(10, resultado.size());

            return null;
        });
    }

    @Test
    public void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
            cut.em = em;

            // Agregar una nueva opción a la pregunta f1...003 ("¿Cuántos planetas...?")
            BancoPregunta pregunta = em.find(BancoPregunta.class, ID_PREGUNTA_3);
            assertNotNull(pregunta);

            OpcionesRespuesta nueva = new OpcionesRespuesta();
            nueva.setIdPregunta(pregunta);
            nueva.setTextoOpcion("9");
            nueva.setEsCorrecta(false);

            cut.crear(nueva);

            // Validación dentro de la transacción
            assertEquals(11, cut.count());

            return null;
        });

        // Verificar rollback: vuelve a 10
        ejecutarEnTransaccion(em -> {
            OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
            cut.em = em;

            assertEquals(10, cut.count());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
            cut.em = em;

            // Obtener la primera opción del init.sql
            OpcionesRespuesta opcion = cut.findRange(0, 1).get(0);
            assertNotNull(opcion);

            // Modificar dentro de la transacción
            opcion.setTextoOpcion("Opción Actualizada");

            OpcionesRespuesta resultado = cut.actualizar(opcion);

            assertNotNull(resultado);
            assertEquals("Opción Actualizada", resultado.getTextoOpcion());

            return null;
        });
    }

    @Test
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
            cut.em = em;

            // Crear una nueva opción para eliminarla
            BancoPregunta pregunta = em.find(BancoPregunta.class, ID_PREGUNTA_3);
            assertNotNull(pregunta);

            OpcionesRespuesta nueva = new OpcionesRespuesta();
            nueva.setIdPregunta(pregunta);
            nueva.setTextoOpcion("Opción para eliminar");
            nueva.setEsCorrecta(false);

            cut.crear(nueva);
            assertEquals(11, cut.count());

            // Eliminar la opción recién creada
            cut.eliminar(nueva);
            assertEquals(10, cut.count());

            return null;
        });
    }
}
