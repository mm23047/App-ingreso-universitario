package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;


import org.junit.jupiter.api.Test;

import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.util.List;
import java.util.UUID;

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

            // Obtenemos un tema gestionado (managed) para evitar cascade PERSIST
            Tema tema = em.createQuery("SELECT t FROM Tema t WHERE t.areaConocimiento = :area", Tema.class)
                    .setParameter("area", area)
                    .setMaxResults(1)
                    .getSingleResult();
            assertNotNull(tema);

            //Creamos la pregunta
            BancoPregunta nuevoBancoPregunta = new BancoPregunta();
            nuevoBancoPregunta.setEnunciado("¿Cuál es la capital de Francia?");
            nuevoBancoPregunta.setTema(tema);

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

            // Obtenemos un tema gestionado (managed)
            Tema tema = em.createQuery("SELECT t FROM Tema t WHERE t.areaConocimiento = :area", Tema.class)
                    .setParameter("area", area)
                    .setMaxResults(1)
                    .getSingleResult();

            // Creamos dato temporal
            BancoPregunta preguntaTemporal = new BancoPregunta();
            preguntaTemporal.setEnunciado("Pregunta temporal a eliminar");
            preguntaTemporal.setTema(tema);

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

    // ===================== NAMED QUERIES =====================

    @Test
    public void testFindByTema() {
        System.out.println("BancoPreguntaDAOIT.findByTema()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            // Tema Comprensión Lectora tiene 2 preguntas
            UUID idComprensionLectora = UUID.fromString("f0000003-0000-0000-0000-000000000003");
            List<BancoPregunta> resultado = cut.findByTema(idComprensionLectora);
            assertNotNull(resultado);
            assertEquals(2, resultado.size());

            // Tema Álgebra tiene 1 pregunta
            UUID idAlgebra = UUID.fromString("f0000001-0000-0000-0000-000000000001");
            List<BancoPregunta> resultadoAlgebra = cut.findByTema(idAlgebra);
            assertNotNull(resultadoAlgebra);
            assertEquals(1, resultadoAlgebra.size());

            return null;
        });
    }

    @Test
    public void testFindByTemaInexistente() {
        System.out.println("BancoPreguntaDAOIT.findByTema() - tema inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            List<BancoPregunta> resultado = cut.findByTema(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    public void testFindByTemaNulo() {
        System.out.println("BancoPreguntaDAOIT.findByTema() - id nulo");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByTema(null));
            return null;
        });
    }

    @Test
    public void testLeerNoExiste() {
        System.out.println("BancoPreguntaDAOIT.leer() - ID inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            BancoPregunta resultado = cut.leer(UUID.randomUUID());
            assertNull(resultado, "Debe retornar null si el ID no existe");
            return null;
        });
    }

    // ===================== VALIDACIONES CREAR =====================

    @Test
    public void testCrearNulo() {
        System.out.println("BancoPreguntaDAOIT.crear() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    public void testCrearSinEnunciado() {
        System.out.println("BancoPreguntaDAOIT.crear() - enunciado null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            BancoPregunta sinEnunciado = new BancoPregunta();
            // No seteamos enunciado (queda null)

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinEnunciado));
            return null;
        });
    }

    @Test
    public void testCrearEnunciadoBlank() {
        System.out.println("BancoPreguntaDAOIT.crear() - enunciado en blanco");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            BancoPregunta enunciadoBlank = new BancoPregunta();
            enunciadoBlank.setEnunciado("   ");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(enunciadoBlank));
            return null;
        });
    }

    @Test
    public void testCrearSinTema() {
        System.out.println("BancoPreguntaDAOIT.crear() - sin tema");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            BancoPregunta sinTema = new BancoPregunta();
            sinTema.setEnunciado("Pregunta sin tema asociado");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinTema));
            return null;
        });
    }

    @Test
    public void testCrearEnunciadoDuplicado() {
        System.out.println("BancoPreguntaDAOIT.crear() - enunciado duplicado en mismo tema");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            // Obtener el tema de una pregunta existente
            Tema temaAlgebra = em.find(Tema.class,
                    UUID.fromString("f0000001-0000-0000-0000-000000000001"));

            // Intentar crear con enunciado que ya existe en BD
            BancoPregunta duplicada = new BancoPregunta();
            duplicada.setEnunciado("¿Cuánto es la raíz cuadrada de 16?");
            duplicada.setTema(temaAlgebra);

            assertThrows(IllegalArgumentException.class, () -> cut.crear(duplicada));
            return null;
        });
    }

    @Test
    public void testCrearConflictoArea() {
        System.out.println("BancoPreguntaDAOIT.crear() - enunciado existe en otra area");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            // "¿Cuánto es la raíz cuadrada de 16?" existe en Álgebra (Área Matemática)
            // Intentar crearla bajo Comprensión Lectora (Área Lenguaje)
            Tema temaLenguaje = em.find(Tema.class,
                    UUID.fromString("f0000003-0000-0000-0000-000000000003"));

            BancoPregunta conflicto = new BancoPregunta();
            conflicto.setEnunciado("¿Cuánto es la raíz cuadrada de 16?");
            conflicto.setTema(temaLenguaje);

            assertThrows(IllegalArgumentException.class, () -> cut.crear(conflicto));
            return null;
        });
    }

    // ===================== VALIDACIONES ACTUALIZAR =====================

    @Test
    public void testActualizarNulo() {
        System.out.println("BancoPreguntaDAOIT.actualizar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(null));
            return null;
        });
    }

    @Test
    public void testActualizarSinId() {
        System.out.println("BancoPreguntaDAOIT.actualizar() - sin ID");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            BancoPregunta sinId = new BancoPregunta();
            sinId.setEnunciado("Pregunta sin ID");

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(sinId));
            return null;
        });
    }

    @Test
    public void testActualizarEnunciadoDuplicado() {
        System.out.println("BancoPreguntaDAOIT.actualizar() - enunciado de otra pregunta");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoPreguntaDAO cut = new BancoPreguntaDAO();
            cut.em = em;

            // Obtener la pregunta de Álgebra e intentar cambiarle el enunciado a uno de Comprensión Lectora
            BancoPregunta pregunta = cut.leer(
                    UUID.fromString("f1000000-0000-0000-0000-000000000001"));
            assertNotNull(pregunta);

            pregunta.setEnunciado("¿Cuál es la idea principal de un texto?");

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(pregunta));
            return null;
        });
    }
}
