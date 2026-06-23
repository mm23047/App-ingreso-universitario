package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AspirantesDatoDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql

    @Test
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 4 aspirantes (los originales + los de otros tests)
            assertTrue(resultado > 0);
            assertEquals(4, resultado);  

            return null;
        });
    }

    @Test
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            List<AspirantesDato> resultado = cut.findRange(0, 10);

            // BD recién iniciada → 4 aspirantes
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(4, resultado.size()); 

            return null;
        });
    }

    @Test
    public void testFindByDui() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            AspirantesDato resultado = cut.findByDui("01234567-8");

            assertNotNull(resultado);
            assertEquals("01234567-8", resultado.getDui());

            return null;
        });
    }

    @Test
    public void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            AspirantesDato nuevo = new AspirantesDato();
            nuevo.setNombres("Test Aspirante");
            nuevo.setApellidos("Apellido Prueba");
            nuevo.setDui("98765432-1");
            nuevo.setCorreo("test.aspirante@example.com");
            nuevo.setFechaNacimiento(LocalDate.of(1990,1,1));
            nuevo.setUsaSillaRuedas(false);

            cut.crear(nuevo);

            // Validación dentro de la transacción (4 + 1 = 5)
            assertEquals(5, cut.count()); 

            return null;
        });

        // Verificar rollback: vuelve a 4 (no a 2)
        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            assertEquals(4, cut.count());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            // Obtener el primer aspirante del init.sql
            AspirantesDato aspirante = cut.findRange(0, 1).get(0);
            assertNotNull(aspirante);

            // Modificar dentro de la transacción
            aspirante.setNombres("Aspirante Actualizado");

            AspirantesDato resultado = cut.actualizar(aspirante);

            assertNotNull(resultado);
            assertEquals("Aspirante Actualizado", resultado.getNombres());

            return null;
        });
    }

    @Test
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            // Crear un nuevo aspirante para eliminarlo
            AspirantesDato nuevo = new AspirantesDato();
            nuevo.setNombres("Aspirante para eliminar");
            nuevo.setApellidos("Apellido");
            nuevo.setDui("87654321-0");
            nuevo.setCorreo("elim@example.com");
            nuevo.setFechaNacimiento(LocalDate.of(1992,2,2));
            nuevo.setUsaSillaRuedas(false);

            cut.crear(nuevo);
            // 4 (iniciales) + 1 = 5
            assertEquals(5, cut.count()); 

            // Eliminar el aspirante recién creado
            cut.eliminar(nuevo);
            // Vuelve a 4 (no a 2)
            assertEquals(4, cut.count());

            return null;
        });
    }

    // ===================== CRUD FALTANTE =====================

    @Test
    public void testLeer() {
        System.out.println("AspirantesDatoDAOIT.leer()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            UUID idCarlos = UUID.fromString("e1111111-1111-1111-1111-111111111111");
            AspirantesDato resultado = cut.leer(idCarlos);

            assertNotNull(resultado);
            assertEquals(idCarlos, resultado.getId());
            assertEquals("01234567-8", resultado.getDui());
            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    public void testFindByDuiNoExiste() {
        System.out.println("AspirantesDatoDAOIT.findByDui() - no existe");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            AspirantesDato resultado = cut.findByDui("00000000-0");
            assertNull(resultado, "Debe retornar null si el DUI no existe");
            return null;
        });
    }

    @Test
    public void testFindByDuiInvalido() {
        System.out.println("AspirantesDatoDAOIT.findByDui() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByDui(null));
            assertThrows(IllegalArgumentException.class, () -> cut.findByDui(""));
            assertThrows(IllegalArgumentException.class, () -> cut.findByDui("   "));
            return null;
        });
    }

    @Test
    public void testFindByCorreo() {
        System.out.println("AspirantesDatoDAOIT.findByCorreo()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            AspirantesDato resultado = cut.findByCorreo("maria.castillo@gmail.com");
            assertNotNull(resultado);
            assertEquals("02234567-8", resultado.getDui());
            return null;
        });
    }

    @Test
    public void testFindByCorreoNoExiste() {
        System.out.println("AspirantesDatoDAOIT.findByCorreo() - no existe");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            AspirantesDato resultado = cut.findByCorreo("noexiste@test.com");
            assertNull(resultado, "Debe retornar null si el correo no existe");
            return null;
        });
    }

    @Test
    public void testFindByCorreoInvalido() {
        System.out.println("AspirantesDatoDAOIT.findByCorreo() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByCorreo(null));
            assertThrows(IllegalArgumentException.class, () -> cut.findByCorreo(""));
            assertThrows(IllegalArgumentException.class, () -> cut.findByCorreo("   "));
            return null;
        });
    }

    @Test
    public void testFindByRequiereSillaRuedas() {
        System.out.println("AspirantesDatoDAOIT.findByRequiereSillaRuedas()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            // Solo Maria Fernanda usa silla de ruedas
            List<AspirantesDato> resultado = cut.findByRequiereSillaRuedas();
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertTrue(resultado.get(0).getUsaSillaRuedas());
            assertEquals("02234567-8", resultado.get(0).getDui());
            return null;
        });
    }

    // ===================== VALIDACIONES CREAR =====================

    @Test
    public void testCrearNulo() {
        System.out.println("AspirantesDatoDAOIT.crear() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    public void testCrearDuiDuplicado() {
        System.out.println("AspirantesDatoDAOIT.crear() - DUI duplicado");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            AspirantesDato duplicado = new AspirantesDato();
            duplicado.setNombres("Nuevo");
            duplicado.setApellidos("Aspirante");
            duplicado.setDui("01234567-8"); // DUI de Carlos, ya existe
            duplicado.setCorreo("nuevo.unico@test.com");
            duplicado.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            duplicado.setUsaSillaRuedas(false);

            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                    () -> cut.crear(duplicado));
            assertEquals(ReglaNegocioException.Tipo.DUI_DUPLICADO, ex.getTipo());
            return null;
        });
    }

    @Test
    public void testCrearCorreoDuplicado() {
        System.out.println("AspirantesDatoDAOIT.crear() - correo duplicado");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            AspirantesDato duplicado = new AspirantesDato();
            duplicado.setNombres("Otro");
            duplicado.setApellidos("Aspirante");
            duplicado.setDui("11111111-1"); // DUI unico
            duplicado.setCorreo("carlos.ramirez@gmail.com"); // Correo de Carlos, ya existe
            duplicado.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            duplicado.setUsaSillaRuedas(false);

            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                    () -> cut.crear(duplicado));
            assertEquals(ReglaNegocioException.Tipo.CORREO_DUPLICADO, ex.getTipo());
            return null;
        });
    }

    @Test
    public void testCrearMenorDeEdad() {
        System.out.println("AspirantesDatoDAOIT.crear() - menor de 18");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            AspirantesDato menor = new AspirantesDato();
            menor.setNombres("Menor");
            menor.setApellidos("De Edad");
            menor.setDui("55555555-5");
            menor.setCorreo("menor@test.com");
            menor.setFechaNacimiento(LocalDate.of(2015, 1, 1)); // 11 años en 2026
            menor.setUsaSillaRuedas(false);

            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                    () -> cut.crear(menor));
            assertEquals(ReglaNegocioException.Tipo.EDAD_MINIMA, ex.getTipo());
            return null;
        });
    }

    // ===================== VALIDACIONES ACTUALIZAR =====================

    @Test
    public void testActualizarNulo() {
        System.out.println("AspirantesDatoDAOIT.actualizar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(null));
            return null;
        });
    }

    @Test
    public void testActualizarSinId() {
        System.out.println("AspirantesDatoDAOIT.actualizar() - sin ID");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            AspirantesDato sinId = new AspirantesDato();
            sinId.setNombres("Sin ID");
            sinId.setApellidos("Test");
            sinId.setDui("44444444-4");
            sinId.setCorreo("sinid@test.com");
            sinId.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            sinId.setUsaSillaRuedas(false);

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(sinId));
            return null;
        });
    }

    @Test
    public void testActualizarDuiDuplicado() {
        System.out.println("AspirantesDatoDAOIT.actualizar() - DUI de otro aspirante");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            // Obtener Maria e intentar cambiarle el DUI al de Carlos
            AspirantesDato maria = cut.findByDui("02234567-8");
            assertNotNull(maria);

            maria.setDui("01234567-8"); // DUI de Carlos

            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                    () -> cut.actualizar(maria));
            assertEquals(ReglaNegocioException.Tipo.DUI_DUPLICADO, ex.getTipo());
            return null;
        });
    }

    @Test
    public void testActualizarCorreoDuplicado() {
        System.out.println("AspirantesDatoDAOIT.actualizar() - correo de otro aspirante");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AspirantesDatoDAO cut = new AspirantesDatoDAO();
            cut.em = em;

            // Obtener Maria e intentar cambiarle el correo al de Carlos
            AspirantesDato maria = cut.findByDui("02234567-8");
            assertNotNull(maria);

            maria.setCorreo("carlos.ramirez@gmail.com"); // Correo de Carlos

            ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                    () -> cut.actualizar(maria));
            assertEquals(ReglaNegocioException.Tipo.CORREO_DUPLICADO, ex.getTipo());
            return null;
        });
    }
}