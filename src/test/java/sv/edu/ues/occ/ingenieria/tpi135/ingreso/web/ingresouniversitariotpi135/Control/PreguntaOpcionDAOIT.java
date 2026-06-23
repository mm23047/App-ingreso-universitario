package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoRespuesta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PreguntaOpcionDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_PREGUNTA_55   = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ID_PREGUNTA_F1_1 = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_F1_3 = UUID.fromString("f1000000-0000-0000-0000-000000000003");
    private static final UUID ID_OPCION_BB     = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID ID_OPCION_CC     = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID ID_RESP_BA_001   = UUID.fromString("ba000001-0000-0000-0000-000000000001");
    private static final UUID ID_RESP_BA_002   = UUID.fromString("ba000001-0000-0000-0000-000000000002");
    private static final UUID ID_RESP_0C_001   = UUID.fromString("0c000000-0000-0000-0000-000000000001");

    // ===================== CRUD =====================

    @Test
    public void testCount() {
        System.out.println("PreguntaOpcionDAOIT.count()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            int resultado = cut.count();
            assertEquals(6, resultado);
            return null;
        });
    }

    @Test
    public void testFindRange() {
        System.out.println("PreguntaOpcionDAOIT.findRange()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            List<PreguntaOpcion> resultado = cut.findRange(0, 10);
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(6, resultado.size());
            return null;
        });
    }

    @Test
    public void testCrear() {
        System.out.println("PreguntaOpcionDAOIT.crear()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            // Pregunta f1...003 + respuesta 0c...001 no existe aun
            BancoPregunta pregunta = em.find(BancoPregunta.class, ID_PREGUNTA_F1_3);
            BancoRespuesta respuesta = em.find(BancoRespuesta.class, ID_RESP_0C_001);

            PreguntaOpcion nueva = new PreguntaOpcion();
            nueva.setBancoPregunta(pregunta);
            nueva.setIdRespuestaGlobal(respuesta);
            nueva.setEsCorrecta(false);

            cut.crear(nueva);

            assertNotNull(nueva.getIdPreguntaOpcion());
            assertEquals(7, cut.count());
            return null;
        });

        // Verificar rollback
        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;
            assertEquals(6, cut.count());
            return null;
        });
    }

    @Test
    public void testLeer() {
        System.out.println("PreguntaOpcionDAOIT.leer()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            // Opcion bbbbbbbb: pregunta 55555555, respuesta ba...001, esCorrecta=false
            PreguntaOpcion resultado = cut.leer(ID_OPCION_BB);

            assertNotNull(resultado);
            assertEquals(ID_OPCION_BB, resultado.getIdPreguntaOpcion());
            assertNotNull(resultado.getBancoPregunta());
            assertEquals(ID_PREGUNTA_55, resultado.getBancoPregunta().getIdBancoPregunta());
            assertNotNull(resultado.getIdRespuestaGlobal());
            assertFalse(resultado.getEsCorrecta());
            return null;
        });
    }

    @Test
    public void testLeerNoExiste() {
        System.out.println("PreguntaOpcionDAOIT.leer() - ID inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            PreguntaOpcion resultado = cut.leer(UUID.randomUUID());
            assertNull(resultado, "Debe retornar null si el ID no existe");
            return null;
        });
    }

    @Test
    public void testActualizar() {
        System.out.println("PreguntaOpcionDAOIT.actualizar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            // Opcion cccccccc ya es correcta → la dejamos correcta (idempotente)
            PreguntaOpcion opcion = cut.leer(ID_OPCION_CC);
            assertNotNull(opcion);
            assertTrue(opcion.getEsCorrecta());

            PreguntaOpcion resultado = cut.actualizar(opcion);
            assertNotNull(resultado);
            assertTrue(resultado.getEsCorrecta());
            return null;
        });
    }

    @Test
    public void testEliminar() {
        System.out.println("PreguntaOpcionDAOIT.eliminar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            // Crear temporal: pregunta f1...003 + respuesta 0c...001
            BancoPregunta pregunta = em.find(BancoPregunta.class, ID_PREGUNTA_F1_3);
            BancoRespuesta respuesta = em.find(BancoRespuesta.class, ID_RESP_0C_001);

            PreguntaOpcion temporal = new PreguntaOpcion();
            temporal.setBancoPregunta(pregunta);
            temporal.setIdRespuestaGlobal(respuesta);
            temporal.setEsCorrecta(false);

            cut.crear(temporal);
            assertEquals(7, cut.count());

            cut.eliminar(temporal);
            assertEquals(6, cut.count());

            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    public void testFindByPregunta() {
        System.out.println("PreguntaOpcionDAOIT.findByPregunta()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            // Pregunta 55555555 tiene 3 opciones
            List<PreguntaOpcion> resultado55 = cut.findByPregunta(ID_PREGUNTA_55);
            assertNotNull(resultado55);
            assertEquals(3, resultado55.size());

            // Pregunta f1...001 tiene 2 opciones
            List<PreguntaOpcion> resultadoF1 = cut.findByPregunta(ID_PREGUNTA_F1_1);
            assertNotNull(resultadoF1);
            assertEquals(2, resultadoF1.size());

            // Pregunta f1...003 tiene 1 opcion
            List<PreguntaOpcion> resultadoF3 = cut.findByPregunta(ID_PREGUNTA_F1_3);
            assertNotNull(resultadoF3);
            assertEquals(1, resultadoF3.size());

            return null;
        });
    }

    @Test
    public void testFindByPreguntaInexistente() {
        System.out.println("PreguntaOpcionDAOIT.findByPregunta() - inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            List<PreguntaOpcion> resultado = cut.findByPregunta(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    public void testFindByPreguntaNulo() {
        System.out.println("PreguntaOpcionDAOIT.findByPregunta() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByPregunta(null));
            return null;
        });
    }

    @Test
    public void testExistsByPreguntaAndRespuesta() {
        System.out.println("PreguntaOpcionDAOIT.existsByPreguntaAndRespuesta()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            // Pregunta 55555555 + respuesta ba...001 existe → true
            assertTrue(cut.existsByPreguntaAndRespuesta(ID_PREGUNTA_55, ID_RESP_BA_001));

            // Pregunta 55555555 + respuesta ba...002 existe → true
            assertTrue(cut.existsByPreguntaAndRespuesta(ID_PREGUNTA_55, ID_RESP_BA_002));

            // Pregunta 55555555 + respuesta 0c...001 NO existe → false
            assertFalse(cut.existsByPreguntaAndRespuesta(ID_PREGUNTA_55, ID_RESP_0C_001));

            // Pregunta inexistente → false
            assertFalse(cut.existsByPreguntaAndRespuesta(UUID.randomUUID(), ID_RESP_BA_001));

            return null;
        });
    }

    @Test
    public void testExistsByPreguntaAndRespuestaNulos() {
        System.out.println("PreguntaOpcionDAOIT.existsByPreguntaAndRespuesta() - nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByPreguntaAndRespuesta(null, ID_RESP_BA_001));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByPreguntaAndRespuesta(ID_PREGUNTA_55, null));
            return null;
        });
    }

    @Test
    public void testFindOpcionesCorrectasByPregunta() {
        System.out.println("PreguntaOpcionDAOIT.findOpcionesCorrectasByPregunta()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            // Pregunta 55555555 tiene 1 correcta (cccccccc)
            List<PreguntaOpcion> correctas55 = cut.findOpcionesCorrectasByPregunta(ID_PREGUNTA_55);
            assertNotNull(correctas55);
            assertEquals(1, correctas55.size());
            assertEquals(ID_OPCION_CC, correctas55.get(0).getIdPreguntaOpcion());

            // Pregunta f1...001 tiene 1 correcta (0b...002)
            List<PreguntaOpcion> correctasF1 = cut.findOpcionesCorrectasByPregunta(ID_PREGUNTA_F1_1);
            assertNotNull(correctasF1);
            assertEquals(1, correctasF1.size());

            // Pregunta f1...003 no tiene correctas
            List<PreguntaOpcion> correctasF3 = cut.findOpcionesCorrectasByPregunta(ID_PREGUNTA_F1_3);
            assertNotNull(correctasF3);
            assertTrue(correctasF3.isEmpty());

            return null;
        });
    }

    @Test
    public void testFindOpcionesCorrectasByPreguntaNulo() {
        System.out.println("PreguntaOpcionDAOIT.findOpcionesCorrectasByPregunta() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findOpcionesCorrectasByPregunta(null));
            return null;
        });
    }

    // ===================== VALIDACIONES CREAR =====================

    @Test
    public void testCrearNulo() {
        System.out.println("PreguntaOpcionDAOIT.crear() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    public void testCrearSinPregunta() {
        System.out.println("PreguntaOpcionDAOIT.crear() - sin pregunta");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            BancoRespuesta respuesta = em.find(BancoRespuesta.class, ID_RESP_0C_001);

            PreguntaOpcion sinPregunta = new PreguntaOpcion();
            sinPregunta.setIdRespuestaGlobal(respuesta);
            sinPregunta.setEsCorrecta(false);

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinPregunta));
            return null;
        });
    }

    @Test
    public void testCrearSinRespuesta() {
        System.out.println("PreguntaOpcionDAOIT.crear() - sin respuesta global");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            BancoPregunta pregunta = em.find(BancoPregunta.class, ID_PREGUNTA_F1_3);

            PreguntaOpcion sinRespuesta = new PreguntaOpcion();
            sinRespuesta.setBancoPregunta(pregunta);
            sinRespuesta.setEsCorrecta(false);

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinRespuesta));
            return null;
        });
    }

    @Test
    public void testCrearDuplicadoPreguntaRespuesta() {
        System.out.println("PreguntaOpcionDAOIT.crear() - duplicado pregunta+respuesta");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            // Pregunta 55555555 + respuesta ba...001 ya existe (opcion bbbbbbbb)
            BancoPregunta pregunta = em.find(BancoPregunta.class, ID_PREGUNTA_55);
            BancoRespuesta respuesta = em.find(BancoRespuesta.class, ID_RESP_BA_001);

            PreguntaOpcion duplicada = new PreguntaOpcion();
            duplicada.setBancoPregunta(pregunta);
            duplicada.setIdRespuestaGlobal(respuesta);
            duplicada.setEsCorrecta(false);

            assertThrows(IllegalArgumentException.class, () -> cut.crear(duplicada));
            return null;
        });
    }

    @Test
    public void testCrearSegundaCorrecta() {
        System.out.println("PreguntaOpcionDAOIT.crear() - segunda correcta en misma pregunta");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            // Pregunta 55555555 ya tiene cccccccc como correcta
            // Intentar agregar otra correcta con respuesta 0c...001 (no existe esa combinacion)
            BancoPregunta pregunta = em.find(BancoPregunta.class, ID_PREGUNTA_55);
            BancoRespuesta respuesta = em.find(BancoRespuesta.class, ID_RESP_0C_001);

            PreguntaOpcion segundaCorrecta = new PreguntaOpcion();
            segundaCorrecta.setBancoPregunta(pregunta);
            segundaCorrecta.setIdRespuestaGlobal(respuesta);
            segundaCorrecta.setEsCorrecta(true);

            assertThrows(IllegalArgumentException.class, () -> cut.crear(segundaCorrecta));
            return null;
        });
    }

    // ===================== VALIDACIONES ACTUALIZAR =====================

    @Test
    public void testActualizarNulo() {
        System.out.println("PreguntaOpcionDAOIT.actualizar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(null));
            return null;
        });
    }

    @Test
    public void testActualizarOtraCorrectaExiste() {
        System.out.println("PreguntaOpcionDAOIT.actualizar() - marcar correcta cuando ya hay otra");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PreguntaOpcionDAO cut = new PreguntaOpcionDAO();
            cut.em = em;

            // Opcion bbbbbbbb es false para pregunta 55555555 que ya tiene cccccccc como correcta
            PreguntaOpcion opcionFalsa = cut.leer(ID_OPCION_BB);
            assertNotNull(opcionFalsa);
            assertFalse(opcionFalsa.getEsCorrecta());

            opcionFalsa.setEsCorrecta(true);

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(opcionFalsa));
            return null;
        });
    }
}
