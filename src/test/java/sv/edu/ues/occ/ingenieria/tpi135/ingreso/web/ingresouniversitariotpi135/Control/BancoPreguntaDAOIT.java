package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;


import org.junit.jupiter.api.Test;

import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BancoPreguntaDAOIT extends AbstractBaseIT {



    public BancoPreguntaDAOIT() {
    }

    @Test
    public void testCount() {
        System.out.println("INICIANDO TEST COUNT");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            int resultado = cut.count();
            System.out.println("Registros encontrados en BD: " + resultado);
            assertEquals(4, resultado);

            return null;
        });
    }

    @Test
    public void testFindRange() {
        System.out.println("INICIANDO TEST FIND RANGE");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            List<BancoPregunta> resultado = cut.findRange(0, 10);
            System.out.println("Cantidad de preguntas obtenidas: " + resultado.size());

            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(4, resultado.size());

            return null;
        });
    }

    @Test
    public void testCrear() {
        System.out.println("INICIANDO TEST CREAR ");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            //Obtenemos un área existente para la llave foránea
            AreasConocimiento area = em.createQuery("SELECT a FROM AreasConocimiento a", AreasConocimiento.class)
                    .setMaxResults(1)
                    .getSingleResult();
            assertNotNull(area);
            System.out.println(" Área de conocimiento asignada: " + area.getNombreArea());

            //Creamos la pregunta
            BancoPregunta nuevoBancoPregunta = new BancoPregunta();
            nuevoBancoPregunta.setEnunciado("¿Cuál es la capital de Francia?");
            nuevoBancoPregunta.setIdArea(area);

            cut.crear(nuevoBancoPregunta);

            //Verificamos que se creó en esta transacción
            System.out.println("Pregunta creada con ID: " + nuevoBancoPregunta.getIdBancoPregunta());
            int conteoActual = cut.count();
            System.out.println("Conteo actual en transacción (Debe subir a 5): " + conteoActual);

            assertNotNull(nuevoBancoPregunta.getIdBancoPregunta());
            assertEquals(5, conteoActual);

            return null;
        });

        // Verificamos el ROLLBACK
        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;
            int conteoPostRollback = cut.count();
            System.out.println("Conteo tras el ROLLBACK(Deben de ser 4): " + conteoPostRollback);
            assertEquals(4, conteoPostRollback);
            return null;
        });
    }


    @Test
    public void testLeer() {
        System.out.println("INICIANDO TEST LEER ");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            // Tomamos una pregunta de la BD
            BancoPregunta preguntaExistente = cut.findRange(0, 1).get(0);
            System.out.println("Leer pregunta con ID existente: " + preguntaExistente.getIdBancoPregunta());

            BancoPregunta resultado = cut.leer(preguntaExistente.getIdBancoPregunta());

            assertNotNull(resultado);
            System.out.println("RESULTADO LEER ENUNCIADO: " + resultado.getEnunciado());
            assertEquals(preguntaExistente.getIdBancoPregunta(), resultado.getIdBancoPregunta());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        System.out.println("INICIANDO TEST ACTUALIZAR ");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            // Leemos una de la BD
            BancoPregunta bancoPreguntaExistente = cut.findRange(0, 1).get(0);
            assertNotNull(bancoPreguntaExistente);
            System.out.println("Enunciado original: " + bancoPreguntaExistente.getEnunciado());

            // Modificamos
            bancoPreguntaExistente.setEnunciado("¿Cuál es la capital de España? (MODIFICADO)");

            BancoPregunta resultado = cut.actualizar(bancoPreguntaExistente);

            assertNotNull(resultado);
            System.out.println("RESULTADO ACTUALIZADO: " + resultado.getEnunciado());
            assertEquals("¿Cuál es la capital de España? (MODIFICADO)", resultado.getEnunciado());

            return null;
        });
    }

    @Test
    public void testEliminar() {
        System.out.println("INICIANDO TEST ELIMINAR ");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            // Obtenemos Área para la pregunta temporal
            AreasConocimiento area = em.createQuery("SELECT a FROM AreasConocimiento a", AreasConocimiento.class)
                    .setMaxResults(1)
                    .getSingleResult();

            // Creamos dato temporal
            BancoPregunta preguntaTemporal = new BancoPregunta();
            preguntaTemporal.setEnunciado("Pregunta temporal a eliminar");
            preguntaTemporal.setIdArea(area);

            cut.crear(preguntaTemporal);
            System.out.println("Pregunta temporal creada con ID: " + preguntaTemporal.getIdBancoPregunta());
            System.out.println("Conteo antes de eliminar: " + cut.count());
            // Sube a 5
            assertEquals(5, cut.count());

            // Eliminamos el dato temporal
            cut.eliminar(preguntaTemporal);

            // Verificamos que bajó a 4 y la base de datos devuelve null
            int conteoFinal = cut.count();
            System.out.println("Conteo después de eliminar (Debe ser 4): " + conteoFinal);
            assertEquals(4, conteoFinal);

            BancoPregunta resultadoLectura = cut.leer(preguntaTemporal.getIdBancoPregunta());
            System.out.println("Intentando leer el ID borrado. Resultado obtenido: " + resultadoLectura);
            assertNull(resultadoLectura, "La pregunta debería retornar null tras ser eliminada");

            return null;
        });
    }
}
