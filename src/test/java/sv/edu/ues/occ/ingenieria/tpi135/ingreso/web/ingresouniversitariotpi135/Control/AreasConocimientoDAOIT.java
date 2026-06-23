package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AreasConocimientoDAOIT extends AbstractBaseIT {

    @Test
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            int resultado = cut.count();

            assertTrue(resultado > 0);
            assertEquals(3, resultado);

            return null;
        });
    }

    @Test
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            List<AreasConocimiento> resultado = cut.findRange(0, 10);

            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(3, resultado.size());

            return null;
        });
    }

    @Test
    public void testCrear() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            AreasConocimiento nuevo = new AreasConocimiento();
            nuevo.setNombreArea("registro prueba");

            cut.crear(nuevo);

            // Validación dentro de la transacción
            assertEquals(4, cut.count());

            return null;
        });

        // ✅ ÚNICA verificación de rollback (correcto)
        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            assertEquals(3, cut.count());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            AreasConocimiento area = cut.findRange(0, 1).get(0);

            area.setNombreArea("modificado");

            AreasConocimiento actualizado = cut.actualizar(area);

            assertEquals("modificado", actualizado.getNombreArea());

            return null;
        });
    }

   @Test
public void testEliminar() {
    assertTrue(postgres.isRunning());

    ejecutarEnTransaccion(em -> {
        AreasConocimientoDAO cut = new AreasConocimientoDAO();
        cut.em = em;

        // Crear dato aislado
        AreasConocimiento area = new AreasConocimiento();
        area.setNombreArea("temporal");

        cut.crear(area);

        assertEquals(4, cut.count());

        // Eliminar
        cut.eliminar(area);

        assertEquals(3, cut.count());

        return null;
    });
}

    // ===================== CRUD FALTANTE =====================

    @Test
    public void testLeer() {
        System.out.println("AreasConocimientoDAOIT.leer()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            AreasConocimiento areaExistente = cut.findRange(0, 1).get(0);

            AreasConocimiento resultado = cut.leer(areaExistente.getIdAreaConocimiento());

            assertNotNull(resultado, "El area leida no puede ser nula porque ya existe en BD");
            assertEquals(areaExistente.getIdAreaConocimiento(), resultado.getIdAreaConocimiento());
            assertEquals(areaExistente.getNombreArea(), resultado.getNombreArea());
            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    public void testExistePorNombre() {
        System.out.println("AreasConocimientoDAOIT.existePorNombre()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            assertTrue(cut.existePorNombre("Matemática"));
            assertTrue(cut.existePorNombre("Lenguaje"));
            assertFalse(cut.existePorNombre("Area Inexistente"));
            return null;
        });
    }

    @Test
    public void testExistePorNombreInvalido() {
        System.out.println("AreasConocimientoDAOIT.existePorNombre() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            assertFalse(cut.existePorNombre(null));
            assertFalse(cut.existePorNombre(""));
            assertFalse(cut.existePorNombre("   "));
            return null;
        });
    }

    @Test
    public void testBuscarPorNombreSimilar() {
        System.out.println("AreasConocimientoDAOIT.buscarPorNombreSimilar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            // "Mat" debe encontrar "Matemática"
            List<AreasConocimiento> resultado = cut.buscarPorNombreSimilar("Mat");
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("Matemática", resultado.get(0).getNombreArea());

            // "a" debe encontrar las 3 areas (Matemática, Lenguaje, Ciencias Naturales)
            List<AreasConocimiento> todos = cut.buscarPorNombreSimilar("a");
            assertNotNull(todos);
            assertEquals(3, todos.size());

            // Patron sin coincidencia
            List<AreasConocimiento> vacio = cut.buscarPorNombreSimilar("XYZ");
            assertNotNull(vacio);
            assertTrue(vacio.isEmpty());

            return null;
        });
    }

    @Test
    public void testBuscarPorNombreSimilarInvalido() {
        System.out.println("AreasConocimientoDAOIT.buscarPorNombreSimilar() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            List<AreasConocimiento> resultNull = cut.buscarPorNombreSimilar(null);
            assertNotNull(resultNull);
            assertTrue(resultNull.isEmpty());

            List<AreasConocimiento> resultVacio = cut.buscarPorNombreSimilar("");
            assertNotNull(resultVacio);
            assertTrue(resultVacio.isEmpty());

            List<AreasConocimiento> resultBlancos = cut.buscarPorNombreSimilar("   ");
            assertNotNull(resultBlancos);
            assertTrue(resultBlancos.isEmpty());

            return null;
        });
    }

    @Test
    public void testVerificarDependencias() {
        System.out.println("AreasConocimientoDAOIT.verificarDependencias()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            // Matemática tiene 2 temas → true
            UUID idMatematica = UUID.fromString("11111111-1111-1111-1111-111111111111");
            assertTrue(cut.verificarDependencias(idMatematica));

            // Ciencias Naturales no tiene temas → false
            UUID idCiencias = UUID.fromString("33333333-3333-3333-3333-333333333333");
            assertFalse(cut.verificarDependencias(idCiencias));

            return null;
        });
    }

    @Test
    public void testFindAreasConPreguntasDisponibles() {
        System.out.println("AreasConocimientoDAOIT.findAreasConPreguntasDisponibles()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            // Matemática y Lenguaje tienen temas con preguntas, Ciencias Naturales no
            List<AreasConocimiento> resultado = cut.findAreasConPreguntasDisponibles();

            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            // Ordenado ASC por nombre: Lenguaje, Matemática
            assertEquals("Lenguaje", resultado.get(0).getNombreArea());
            assertEquals("Matemática", resultado.get(1).getNombreArea());
            return null;
        });
    }

    // ===================== VALIDACIONES CREAR =====================

    @Test
    public void testCrearNulo() {
        System.out.println("AreasConocimientoDAOIT.crear() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    public void testCrearSinNombre() {
        System.out.println("AreasConocimientoDAOIT.crear() - sin nombre");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            AreasConocimiento sinNombre = new AreasConocimiento();

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinNombre));
            return null;
        });
    }

    @Test
    public void testCrearNombreDuplicado() {
        System.out.println("AreasConocimientoDAOIT.crear() - nombre duplicado");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            AreasConocimiento duplicada = new AreasConocimiento();
            duplicada.setNombreArea("Matemática");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(duplicada));
            return null;
        });
    }

    // ===================== VALIDACIONES ACTUALIZAR =====================

    @Test
    public void testActualizarNulo() {
        System.out.println("AreasConocimientoDAOIT.actualizar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(null));
            return null;
        });
    }

    @Test
    public void testActualizarSinId() {
        System.out.println("AreasConocimientoDAOIT.actualizar() - sin ID");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            AreasConocimiento sinId = new AreasConocimiento();
            sinId.setNombreArea("Sin ID");

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(sinId));
            return null;
        });
    }

    @Test
    public void testActualizarNombreDuplicado() {
        System.out.println("AreasConocimientoDAOIT.actualizar() - nombre de otra area");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            // Obtener Lenguaje e intentar cambiarle el nombre a "Matemática"
            List<AreasConocimiento> areas = cut.findRange(0, 10);
            AreasConocimiento lenguaje = areas.stream()
                    .filter(a -> "Lenguaje".equals(a.getNombreArea()))
                    .findFirst().orElseThrow();

            lenguaje.setNombreArea("Matemática");

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(lenguaje));
            return null;
        });
    }

    // ===================== VALIDACIONES ELIMINAR =====================

    @Test
    public void testEliminarNulo() {
        System.out.println("AreasConocimientoDAOIT.eliminar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.eliminar(null));
            return null;
        });
    }

    @Test
    public void testEliminarConDependencias() {
        System.out.println("AreasConocimientoDAOIT.eliminar() - area con temas asociados");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AreasConocimientoDAO cut = new AreasConocimientoDAO();
            cut.em = em;

            // Matemática tiene temas → no se puede eliminar
            AreasConocimiento matematica = cut.leer(
                    UUID.fromString("11111111-1111-1111-1111-111111111111"));
            assertNotNull(matematica);

            assertThrows(IllegalStateException.class, () -> cut.eliminar(matematica));
            return null;
        });
    }
}