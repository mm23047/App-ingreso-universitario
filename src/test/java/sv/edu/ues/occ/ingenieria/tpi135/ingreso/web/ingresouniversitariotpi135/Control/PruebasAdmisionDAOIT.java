package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PruebasAdmisionDAOIT extends AbstractBaseIT {


    PruebasAdmisionDAOIT() {
    }

    @Test
    void testCount() {
        System.out.println("TEST PruebasAdmision DAOIT COUNT");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            int resultado = cut.count();

            // Tenemos 3 registros iniciales en la BD
            assertEquals(3, resultado);
            return null;
        });
    }

    @Test
    void testFindRange() {
        System.out.println("TEST PruebasAdmision DAOIT FIND RANGE");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            List<PruebasAdmision> resultado = cut.findRange(0, 2);

            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size()); // findRange(0,2) devuelve max 2 aunque haya 3

            return null;
        });
    }

    @Test
    void testCrear() {
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

            // Verificamos que se sumó una prueba en esta transacción (3 iniciales + 1 = 4)
            assertEquals(4, cut.count());
            assertNotNull(nuevo.getIdPruebaAdmision());
            //Para verificar por consola
            System.out.println("NUmeor de registros actaules: "+cut.count());
            return null;
        });
        //Verificar el rollback: no debemos ensuciar la BD (vuelve a 3)
        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;
            assertEquals(3, cut.count());
            return null;
        });
    }

    @Test
    void testLeer() {
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
    void testActualizar() {
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
    void testEliminar() {
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

            // Verificamos que se creó (3 iniciales + 1 = 4)
            assertEquals(4, cut.count());

            //Eliminamos
            cut.eliminar(pruebaAEliminar);

            // Verificamos que bajó de nuevo a 3 y que el ID ya no existe
            assertEquals(3, cut.count());
            assertNull(cut.leer(pruebaAEliminar.getIdPruebaAdmision()), "La prueba debería haber sido eliminada y retornar null");
            //Verificamos en consola
            System.out.println("Dato eliminado: "+ cut.leer(pruebaAEliminar.getIdPruebaAdmision()));
            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    void testFindActivas() {
        System.out.println("PruebasAdmisionDAOIT.findActivas()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            // Las 3 pruebas del init.sql tienen activa=true
            List<PruebasAdmision> resultado = cut.findActivas();
            assertNotNull(resultado);
            assertEquals(3, resultado.size());
            resultado.forEach(p -> assertTrue(p.getActiva()));
            return null;
        });
    }

    @Test
    void testFindByNombreAndAnio() {
        System.out.println("PruebasAdmisionDAOIT.findByNombreAndAnio()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            PruebasAdmision resultado = cut.findByNombreAndAnio("Prueba Nacional UES", 2026);

            assertNotNull(resultado);
            assertEquals("Prueba Nacional UES", resultado.getNombrePrueba());
            assertEquals(2026, resultado.getAnio());
            assertTrue(resultado.getActiva());
            return null;
        });
    }

    @Test
    void testFindByNombreAndAnioNoExiste() {
        System.out.println("PruebasAdmisionDAOIT.findByNombreAndAnio() - no existe");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            // Nombre correcto pero año incorrecto
            PruebasAdmision resultado = cut.findByNombreAndAnio("Prueba Nacional UES", 9999);
            assertNull(resultado, "Debe retornar null si la combinacion no existe");

            // Nombre incorrecto
            PruebasAdmision resultado2 = cut.findByNombreAndAnio("Inexistente", 2026);
            assertNull(resultado2);

            return null;
        });
    }

    @Test
    void testFindByNombreAndAnioInvalido() {
        System.out.println("PruebasAdmisionDAOIT.findByNombreAndAnio() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByNombreAndAnio(null, 2026));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByNombreAndAnio("", 2026));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByNombreAndAnio("   ", 2026));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByNombreAndAnio("Prueba Nacional UES", null));
            return null;
        });
    }

    @Test
    void testFindAllOrdenado() {
        System.out.println("PruebasAdmisionDAOIT.findAllOrdenado()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            // Orden: anio DESC → 2026, 2024, 2023
            List<PruebasAdmision> resultado = cut.findAllOrdenado(0, 10);
            assertNotNull(resultado);
            assertEquals(3, resultado.size());
            assertEquals(2026, resultado.get(0).getAnio());
            assertEquals(2024, resultado.get(1).getAnio());
            assertEquals(2023, resultado.get(2).getAnio());

            // Paginacion: solo 2 primeros
            List<PruebasAdmision> paginado = cut.findAllOrdenado(0, 2);
            assertEquals(2, paginado.size());
            assertEquals(2026, paginado.get(0).getAnio());

            return null;
        });
    }

    @Test
    void testBuscarPorTermino() {
        System.out.println("PruebasAdmisionDAOIT.buscarPorTermino()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            // "Test" coincide con "Prueba Test A" y "Prueba Test B"
            List<PruebasAdmision> resultado = cut.buscarPorTermino("Test", 0, 10);
            assertNotNull(resultado);
            assertEquals(2, resultado.size());

            // "Nacional" coincide solo con "Prueba Nacional UES"
            List<PruebasAdmision> resultadoNacional = cut.buscarPorTermino("Nacional", 0, 10);
            assertNotNull(resultadoNacional);
            assertEquals(1, resultadoNacional.size());
            assertEquals("Prueba Nacional UES", resultadoNacional.get(0).getNombrePrueba());

            return null;
        });
    }

    @Test
    void testBuscarPorTerminoSinResultados() {
        System.out.println("PruebasAdmisionDAOIT.buscarPorTermino() - sin coincidencia");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            List<PruebasAdmision> resultado = cut.buscarPorTermino("XYZNOEXISTE", 0, 10);
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    // ===================== REGLA DE NEGOCIO =====================

    @Test
    void testSetPruebaActivaExclusiva() {
        System.out.println("PruebasAdmisionDAOIT.setPruebaActivaExclusiva()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            UUID idPruebaNacional = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

            // Activar exclusivamente la Prueba Nacional UES
            cut.setPruebaActivaExclusiva(idPruebaNacional);
            em.flush();
            em.clear();

            // Verificar que solo la prueba objetivo queda activa
            PruebasAdmision pruebaActiva = em.find(PruebasAdmision.class, idPruebaNacional);
            assertTrue(pruebaActiva.getActiva());

            // Las demas deben estar desactivadas
            PruebasAdmision testA = em.find(PruebasAdmision.class,
                    UUID.fromString("d1000000-0000-0000-0000-000000000001"));
            assertFalse(testA.getActiva());

            PruebasAdmision testB = em.find(PruebasAdmision.class,
                    UUID.fromString("d1000000-0000-0000-0000-000000000002"));
            assertFalse(testB.getActiva());

            return null;
        });
    }

    @Test
    void testSetPruebaActivaExclusivaNulo() {
        System.out.println("PruebasAdmisionDAOIT.setPruebaActivaExclusiva() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.setPruebaActivaExclusiva(null));
            return null;
        });
    }

    @Test
    void testSetPruebaActivaExclusivaInexistente() {
        System.out.println("PruebasAdmisionDAOIT.setPruebaActivaExclusiva() - ID inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.setPruebaActivaExclusiva(UUID.randomUUID()));
            return null;
        });
    }
}
