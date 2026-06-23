package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TemaDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_ALGEBRA        = UUID.fromString("f0000001-0000-0000-0000-000000000001");
    private static final UUID ID_ECUACIONES     = UUID.fromString("f0000002-0000-0000-0000-000000000002");
    private static final UUID ID_COMPRENSION    = UUID.fromString("f0000003-0000-0000-0000-000000000003");
    private static final UUID ID_AREA_MATE      = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ID_AREA_LENGUAJE  = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ID_AREA_CIENCIAS  = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ID_PRUEBA_TEST    = UUID.fromString("d1000000-0000-0000-0000-000000000001");

    // ===================== CRUD =====================

    @Test
    void testCount() {
        System.out.println("TemaDAOIT.count()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            int resultado = cut.count();
            assertEquals(3, resultado);
            return null;
        });
    }

    @Test
    void testFindRange() {
        System.out.println("TemaDAOIT.findRange()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            List<Tema> resultado = cut.findRange(0, 10);
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(3, resultado.size());
            return null;
        });
    }

    @Test
    void testCrear() {
        System.out.println("TemaDAOIT.crear()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            AreasConocimiento areaCiencias = em.find(AreasConocimiento.class, ID_AREA_CIENCIAS);

            Tema nuevo = new Tema();
            nuevo.setNombreTema("Biología Celular");
            nuevo.setAreaConocimiento(areaCiencias);

            cut.crear(nuevo);

            assertNotNull(nuevo.getIdTema());
            assertEquals(4, cut.count());
            return null;
        });

        // Verificar rollback
        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;
            assertEquals(3, cut.count());
            return null;
        });
    }

    @Test
    void testCrearConPadre() {
        System.out.println("TemaDAOIT.crear() - con tema padre");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            AreasConocimiento areaMate = em.find(AreasConocimiento.class, ID_AREA_MATE);
            Tema algebra = em.find(Tema.class, ID_ALGEBRA);

            Tema subtema = new Tema();
            subtema.setNombreTema("Álgebra Lineal Avanzada");
            subtema.setAreaConocimiento(areaMate);
            subtema.setIdTemaPadre(algebra);

            cut.crear(subtema);

            assertNotNull(subtema.getIdTema());
            assertEquals(4, cut.count());
            return null;
        });
    }

    @Test
    void testLeer() {
        System.out.println("TemaDAOIT.leer()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            Tema resultado = cut.leer(ID_ALGEBRA);

            assertNotNull(resultado);
            assertEquals(ID_ALGEBRA, resultado.getIdTema());
            assertEquals("Álgebra", resultado.getNombreTema());
            assertNotNull(resultado.getAreaConocimiento());
            assertEquals(ID_AREA_MATE, resultado.getAreaConocimiento().getIdAreaConocimiento());
            assertNull(resultado.getIdTemaPadre(), "Álgebra es raíz, no tiene padre");
            return null;
        });
    }

    @Test
    void testLeerConPadre() {
        System.out.println("TemaDAOIT.leer() - tema con padre");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            Tema resultado = cut.leer(ID_ECUACIONES);

            assertNotNull(resultado);
            assertEquals("Ecuaciones Lineales", resultado.getNombreTema());
            assertNotNull(resultado.getIdTemaPadre());
            assertEquals(ID_ALGEBRA, resultado.getIdTemaPadre().getIdTema());
            return null;
        });
    }

    @Test
    void testLeerNoExiste() {
        System.out.println("TemaDAOIT.leer() - ID inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            Tema resultado = cut.leer(UUID.randomUUID());
            assertNull(resultado, "Debe retornar null si el ID no existe");
            return null;
        });
    }

    @Test
    void testActualizar() {
        System.out.println("TemaDAOIT.actualizar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            Tema tema = cut.leer(ID_COMPRENSION);
            assertNotNull(tema);

            tema.setNombreTema("Comprensión Lectora Modificada");

            Tema resultado = cut.actualizar(tema);

            assertNotNull(resultado);
            assertEquals("Comprensión Lectora Modificada", resultado.getNombreTema());
            return null;
        });
    }

    @Test
    void testEliminar() {
        System.out.println("TemaDAOIT.eliminar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            // Crear temporal en área Ciencias Naturales (sin FK dependientes)
            AreasConocimiento areaCiencias = em.find(AreasConocimiento.class, ID_AREA_CIENCIAS);

            Tema temporal = new Tema();
            temporal.setNombreTema("Tema Temporal Eliminar");
            temporal.setAreaConocimiento(areaCiencias);

            cut.crear(temporal);
            assertEquals(4, cut.count());

            cut.eliminar(temporal);
            assertEquals(3, cut.count());

            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    void testFindByNombreTema() {
        System.out.println("TemaDAOIT.findByNombreTema()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            Tema resultado = cut.findByNombreTema("Álgebra");

            assertNotNull(resultado);
            assertEquals(ID_ALGEBRA, resultado.getIdTema());
            assertEquals("Álgebra", resultado.getNombreTema());
            assertNotNull(resultado.getAreaConocimiento());
            return null;
        });
    }

    @Test
    void testFindByNombreTemaNoExiste() {
        System.out.println("TemaDAOIT.findByNombreTema() - no existe");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            Tema resultado = cut.findByNombreTema("Tema Inexistente");
            assertNull(resultado, "Debe retornar null si el nombre no existe");
            return null;
        });
    }

    @Test
    void testFindByNombreTemaInvalido() {
        System.out.println("TemaDAOIT.findByNombreTema() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByNombreTema(null));
            assertThrows(IllegalArgumentException.class, () -> cut.findByNombreTema(""));
            assertThrows(IllegalArgumentException.class, () -> cut.findByNombreTema("   "));
            return null;
        });
    }

    @Test
    void testFindByArea() {
        System.out.println("TemaDAOIT.findByArea()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            // Área Matemática tiene 2 temas: Álgebra, Ecuaciones Lineales
            List<Tema> resultadoMate = cut.findByArea(ID_AREA_MATE);
            assertNotNull(resultadoMate);
            assertEquals(2, resultadoMate.size());

            // Área Lenguaje tiene 1 tema: Comprensión Lectora
            List<Tema> resultadoLeng = cut.findByArea(ID_AREA_LENGUAJE);
            assertNotNull(resultadoLeng);
            assertEquals(1, resultadoLeng.size());
            assertEquals("Comprensión Lectora", resultadoLeng.get(0).getNombreTema());

            // Área Ciencias Naturales no tiene temas
            List<Tema> resultadoCiencias = cut.findByArea(ID_AREA_CIENCIAS);
            assertNotNull(resultadoCiencias);
            assertTrue(resultadoCiencias.isEmpty());

            return null;
        });
    }

    @Test
    void testFindByAreaInexistente() {
        System.out.println("TemaDAOIT.findByArea() - area inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            List<Tema> resultado = cut.findByArea(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    void testFindByAreaNulo() {
        System.out.println("TemaDAOIT.findByArea() - null retorna lista vacia");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            List<Tema> resultado = cut.findByArea(null);
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    void testFindRaicesByArea() {
        System.out.println("TemaDAOIT.findRaicesByArea()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            // Matemática: raíz = Álgebra (Ecuaciones Lineales tiene padre)
            List<Tema> raicesMate = cut.findRaicesByArea(ID_AREA_MATE);
            assertNotNull(raicesMate);
            assertEquals(1, raicesMate.size());
            assertEquals("Álgebra", raicesMate.get(0).getNombreTema());

            // Lenguaje: raíz = Comprensión Lectora
            List<Tema> raicesLeng = cut.findRaicesByArea(ID_AREA_LENGUAJE);
            assertNotNull(raicesLeng);
            assertEquals(1, raicesLeng.size());

            // Ciencias: sin temas → vacío
            List<Tema> raicesCiencias = cut.findRaicesByArea(ID_AREA_CIENCIAS);
            assertNotNull(raicesCiencias);
            assertTrue(raicesCiencias.isEmpty());

            return null;
        });
    }

    @Test
    void testFindRaicesByAreaNulo() {
        System.out.println("TemaDAOIT.findRaicesByArea() - null retorna lista vacia");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            List<Tema> resultado = cut.findRaicesByArea(null);
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    void testFindByTemaPadre() {
        System.out.println("TemaDAOIT.findByTemaPadre()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            // Álgebra tiene 1 hijo: Ecuaciones Lineales
            List<Tema> hijosAlgebra = cut.findByTemaPadre(ID_ALGEBRA);
            assertNotNull(hijosAlgebra);
            assertEquals(1, hijosAlgebra.size());
            assertEquals("Ecuaciones Lineales", hijosAlgebra.get(0).getNombreTema());

            // Ecuaciones Lineales no tiene hijos
            List<Tema> hijosEcuaciones = cut.findByTemaPadre(ID_ECUACIONES);
            assertNotNull(hijosEcuaciones);
            assertTrue(hijosEcuaciones.isEmpty());

            // Comprensión Lectora no tiene hijos
            List<Tema> hijosComprension = cut.findByTemaPadre(ID_COMPRENSION);
            assertNotNull(hijosComprension);
            assertTrue(hijosComprension.isEmpty());

            return null;
        });
    }

    @Test
    void testFindByTemaPadreNulo() {
        System.out.println("TemaDAOIT.findByTemaPadre() - null retorna lista vacia");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            List<Tema> resultado = cut.findByTemaPadre(null);
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    void testFindByPrueba() {
        System.out.println("TemaDAOIT.findByPrueba()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            // Prueba d1...001 tiene claves con preguntas en Álgebra, Ecuaciones Lineales y Comprensión Lectora
            List<Tema> resultado = cut.findByPrueba(ID_PRUEBA_TEST);
            assertNotNull(resultado);
            assertEquals(3, resultado.size());

            return null;
        });
    }

    @Test
    void testFindByPruebaInexistente() {
        System.out.println("TemaDAOIT.findByPrueba() - prueba inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            List<Tema> resultado = cut.findByPrueba(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    void testFindByPruebaNulo() {
        System.out.println("TemaDAOIT.findByPrueba() - null retorna lista vacia");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            List<Tema> resultado = cut.findByPrueba(null);
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    // ===================== VALIDACIONES CREAR =====================

    @Test
    void testCrearNulo() {
        System.out.println("TemaDAOIT.crear() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    void testCrearAutoReferencia() {
        System.out.println("TemaDAOIT.crear() - tema como su propio padre");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            Tema existente = cut.leer(ID_ALGEBRA);
            assertNotNull(existente);

            // Intentar asignarse a sí mismo como padre
            existente.setIdTemaPadre(existente);

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(existente));
            return null;
        });
    }

    // ===================== VALIDACIONES ACTUALIZAR =====================

    @Test
    void testActualizarNulo() {
        System.out.println("TemaDAOIT.actualizar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TemaDAO cut = new TemaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(null));
            return null;
        });
    }
}
