package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoRespuesta;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BancoRespuestaDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_RESP_BA_001    = UUID.fromString("ba000001-0000-0000-0000-000000000001");
    private static final UUID ID_AREA_MATE      = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ID_AREA_LENGUAJE  = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ID_AREA_CIENCIAS  = UUID.fromString("33333333-3333-3333-3333-333333333333");

    // ===================== CRUD =====================

    @Test
    void testCount() {
        System.out.println("BancoRespuestaDAOIT.count()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            int resultado = cut.count();
            assertEquals(6, resultado);
            return null;
        });
    }

    @Test
    void testFindRange() {
        System.out.println("BancoRespuestaDAOIT.findRange()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            List<BancoRespuesta> resultado = cut.findRange(0, 10);
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(6, resultado.size());
            return null;
        });
    }

    @Test
    void testCrear() {
        System.out.println("BancoRespuestaDAOIT.crear() - respuesta local");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            AreasConocimiento areaCiencias = em.find(AreasConocimiento.class, ID_AREA_CIENCIAS);

            BancoRespuesta nueva = new BancoRespuesta();
            nueva.setTextoRespuesta("Respuesta de Ciencias IT");
            nueva.setAreaConocimiento(areaCiencias);

            cut.crear(nueva);

            assertNotNull(nueva.getIdBancoRespuesta());
            assertEquals(7, cut.count());
            return null;
        });

        // Verificar rollback
        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;
            assertEquals(6, cut.count());
            return null;
        });
    }

    @Test
    void testCrearGlobal() {
        System.out.println("BancoRespuestaDAOIT.crear() - respuesta global (sin area)");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            BancoRespuesta global = new BancoRespuesta();
            global.setTextoRespuesta("Respuesta Global IT");
            // areaConocimiento queda null → global

            cut.crear(global);

            assertNotNull(global.getIdBancoRespuesta());
            assertEquals(7, cut.count());
            return null;
        });
    }

    @Test
    void testLeer() {
        System.out.println("BancoRespuestaDAOIT.leer()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            // ba...001: "X = 1", área Matemática
            BancoRespuesta resultado = cut.leer(ID_RESP_BA_001);

            assertNotNull(resultado);
            assertEquals(ID_RESP_BA_001, resultado.getIdBancoRespuesta());
            assertEquals("X = 1", resultado.getTextoRespuesta());
            assertNotNull(resultado.getAreaConocimiento());
            assertEquals(ID_AREA_MATE, resultado.getAreaConocimiento().getIdAreaConocimiento());
            return null;
        });
    }

    @Test
    void testLeerNoExiste() {
        System.out.println("BancoRespuestaDAOIT.leer() - ID inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            BancoRespuesta resultado = cut.leer(UUID.randomUUID());
            assertNull(resultado, "Debe retornar null si el ID no existe");
            return null;
        });
    }

    @Test
    void testActualizar() {
        System.out.println("BancoRespuestaDAOIT.actualizar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            BancoRespuesta respuesta = cut.leer(ID_RESP_BA_001);
            assertNotNull(respuesta);

            respuesta.setTextoRespuesta("X = 1 (Modificado)");

            BancoRespuesta resultado = cut.actualizar(respuesta);

            assertNotNull(resultado);
            assertEquals("X = 1 (Modificado)", resultado.getTextoRespuesta());
            return null;
        });
    }

    @Test
    void testEliminar() {
        System.out.println("BancoRespuestaDAOIT.eliminar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            AreasConocimiento areaCiencias = em.find(AreasConocimiento.class, ID_AREA_CIENCIAS);

            BancoRespuesta temporal = new BancoRespuesta();
            temporal.setTextoRespuesta("Temporal para eliminar");
            temporal.setAreaConocimiento(areaCiencias);

            cut.crear(temporal);
            assertEquals(7, cut.count());

            cut.eliminar(temporal);
            assertEquals(6, cut.count());

            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    void testObtenerRespuestasGlobales() {
        System.out.println("BancoRespuestaDAOIT.obtenerRespuestasGlobales()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            // No hay respuestas globales en el init.sql (todas tienen area)
            List<BancoRespuesta> resultado = cut.obtenerRespuestasGlobales();
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    void testObtenerRespuestasParaPregunta() {
        System.out.println("BancoRespuestaDAOIT.obtenerRespuestasParaPregunta()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            // Area Matemática tiene 4 respuestas + 0 globales = 4
            List<BancoRespuesta> resultadoMate = cut.obtenerRespuestasParaPregunta(ID_AREA_MATE);
            assertNotNull(resultadoMate);
            assertEquals(4, resultadoMate.size());

            // Area Lenguaje tiene 2 respuestas + 0 globales = 2
            List<BancoRespuesta> resultadoLeng = cut.obtenerRespuestasParaPregunta(ID_AREA_LENGUAJE);
            assertNotNull(resultadoLeng);
            assertEquals(2, resultadoLeng.size());

            return null;
        });
    }

    @Test
    void testFindRandomDistractoresByArea() {
        System.out.println("BancoRespuestaDAOIT.findRandomDistractoresByArea()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            // Area Matemática tiene 4 respuestas, pedir max 3
            List<BancoRespuesta> resultado = cut.findRandomDistractoresByArea(ID_AREA_MATE, null, 3);
            assertNotNull(resultado);
            assertTrue(resultado.size() <= 3);
            assertFalse(resultado.isEmpty());

            return null;
        });
    }

    @Test
    void testFindRandomDistractoresByAreaConExclusion() {
        System.out.println("BancoRespuestaDAOIT.findRandomDistractoresByArea() - con exclusion");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            // Excluir ba...001 ("X = 1") de área Matemática → max 3 de los 3 restantes
            List<BancoRespuesta> resultado = cut.findRandomDistractoresByArea(
                    ID_AREA_MATE, ID_RESP_BA_001, 3);
            assertNotNull(resultado);
            assertTrue(resultado.size() <= 3);
            // Verificar que el excluido no está
            assertTrue(resultado.stream()
                    .noneMatch(r -> ID_RESP_BA_001.equals(r.getIdBancoRespuesta())));

            return null;
        });
    }

    @Test
    void testFindRandomDistractoresByAreaInvalido() {
        System.out.println("BancoRespuestaDAOIT.findRandomDistractoresByArea() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findRandomDistractoresByArea(null, null, 3));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findRandomDistractoresByArea(ID_AREA_MATE, null, 0));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findRandomDistractoresByArea(ID_AREA_MATE, null, -1));
            return null;
        });
    }

    // ===================== VALIDACIONES CREAR =====================

    @Test
    void testCrearNulo() {
        System.out.println("BancoRespuestaDAOIT.crear() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    void testCrearSinTexto() {
        System.out.println("BancoRespuestaDAOIT.crear() - texto null/blank");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            BancoRespuesta sinTexto = new BancoRespuesta();
            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinTexto));

            BancoRespuesta textoBlank = new BancoRespuesta();
            textoBlank.setTextoRespuesta("   ");
            assertThrows(IllegalArgumentException.class, () -> cut.crear(textoBlank));

            return null;
        });
    }

    @Test
    void testCrearDuplicadoLocal() {
        System.out.println("BancoRespuestaDAOIT.crear() - texto duplicado en misma area");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            AreasConocimiento areaMate = em.find(AreasConocimiento.class, ID_AREA_MATE);

            // "X = 1" ya existe en Matemática
            BancoRespuesta duplicada = new BancoRespuesta();
            duplicada.setTextoRespuesta("X = 1");
            duplicada.setAreaConocimiento(areaMate);

            assertThrows(IllegalArgumentException.class, () -> cut.crear(duplicada));
            return null;
        });
    }

    @Test
    void testCrearDuplicadoGlobal() {
        System.out.println("BancoRespuestaDAOIT.crear() - duplicado global");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            // Crear una global primero
            BancoRespuesta global1 = new BancoRespuesta();
            global1.setTextoRespuesta("Respuesta Global Unica");
            cut.crear(global1);

            // Intentar crear otra global con mismo texto
            BancoRespuesta global2 = new BancoRespuesta();
            global2.setTextoRespuesta("Respuesta Global Unica");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(global2));
            return null;
        });
    }

    // ===================== VALIDACIONES ACTUALIZAR =====================

    @Test
    void testActualizarNulo() {
        System.out.println("BancoRespuestaDAOIT.actualizar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(null));
            return null;
        });
    }

    @Test
    void testActualizarSinId() {
        System.out.println("BancoRespuestaDAOIT.actualizar() - sin ID");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            BancoRespuesta sinId = new BancoRespuesta();
            sinId.setTextoRespuesta("Sin ID");

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(sinId));
            return null;
        });
    }

    @Test
    void testActualizarTextoVacio() {
        System.out.println("BancoRespuestaDAOIT.actualizar() - texto vacio");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            BancoRespuesta respuesta = cut.leer(ID_RESP_BA_001);
            assertNotNull(respuesta);

            respuesta.setTextoRespuesta("   ");

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(respuesta));
            return null;
        });
    }

    @Test
    void testActualizarDuplicadoLocal() {
        System.out.println("BancoRespuestaDAOIT.actualizar() - texto de otra respuesta en misma area");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            BancoRespuestaDAO cut = new BancoRespuestaDAO();
            cut.em = em;

            // ba...001 es "X = 1" en Matemática → cambiar a "X = 2" (ba...002 ya existe)
            BancoRespuesta respuesta = cut.leer(ID_RESP_BA_001);
            assertNotNull(respuesta);

            respuesta.setTextoRespuesta("X = 2");

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(respuesta));
            return null;
        });
    }
}
