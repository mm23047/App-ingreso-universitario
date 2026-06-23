package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ClavesExamenDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_PRUEBA_1  = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_ETAPA_1   = UUID.fromString("c1000000-0000-0000-0000-000000000001");
    private static final UUID ID_CLAVE_A   = UUID.fromString("08000000-0000-0000-0000-000000000001");
    private static final UUID ID_CLAVE_B   = UUID.fromString("aaaabbbb-cccc-dddd-eeee-ffffffffffff");

    @Test
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 2 claves de examen (Clave A, Clave B)
            assertTrue(resultado > 0);
            assertEquals(2, resultado);

            return null;
        });
    }

    @Test
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            List<ClavesExamen> resultado = cut.findRange(0, 10);

            // BD recién iniciada con init.sql → 2 claves de examen
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size());

            return null;
        });
    }

    @Test
    public void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            // Asociar la nueva clave a la prueba de admisión 2026 (d1...001)
            PruebasAdmision prueba = em.find(PruebasAdmision.class, ID_PRUEBA_1);
            EtapasAdmision etapa   = em.find(EtapasAdmision.class, ID_ETAPA_1);
            assertNotNull(prueba);
            assertNotNull(etapa);

            ClavesExamen nueva = new ClavesExamen();
            nueva.setPruebaAdmision(prueba);
            nueva.setEtapaAdmision(etapa);
            nueva.setNombreClave("Clave C");

            cut.crear(nueva);

            // Validación dentro de la transacción
            assertEquals(3, cut.count());

            return null;
        });

        // Verificar rollback: vuelve a 2
        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            assertEquals(2, cut.count());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            // Obtener la primera clave del init.sql
            ClavesExamen clave = cut.findRange(0, 1).get(0);
            assertNotNull(clave);

            // Modificar dentro de la transacción
            clave.setNombreClave("Clave Actualizada");

            ClavesExamen resultado = cut.actualizar(clave);

            assertNotNull(resultado);
            assertEquals("Clave Actualizada", resultado.getNombreClave());

            return null;
        });
    }

    @Test
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            // Crear una nueva clave para eliminarla
            PruebasAdmision prueba = em.find(PruebasAdmision.class, ID_PRUEBA_1);
            EtapasAdmision etapa   = em.find(EtapasAdmision.class, ID_ETAPA_1);
            assertNotNull(prueba);
            assertNotNull(etapa);

            ClavesExamen nueva = new ClavesExamen();
            nueva.setPruebaAdmision(prueba);
            nueva.setEtapaAdmision(etapa);
            nueva.setNombreClave("Clave para eliminar");

            cut.crear(nueva);
            assertEquals(3, cut.count());

            // Eliminar la clave recién creada
            cut.eliminar(nueva);
            assertEquals(2, cut.count());

            return null;
        });
    }

    // ===================== CRUD FALTANTE =====================

    @Test
    public void testLeer() {
        System.out.println("ClavesExamenDAOIT.leer()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            ClavesExamen resultado = cut.leer(ID_CLAVE_A);

            assertNotNull(resultado, "La clave A debe existir en BD");
            assertEquals(ID_CLAVE_A, resultado.getIdClaveExaman());
            assertEquals("Clave A", resultado.getNombreClave());
            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    public void testFindByPrueba() {
        System.out.println("ClavesExamenDAOIT.findByPrueba()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            List<ClavesExamen> resultado = cut.findByPrueba(ID_PRUEBA_1);

            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            return null;
        });
    }

    @Test
    public void testFindByPruebaInexistente() {
        System.out.println("ClavesExamenDAOIT.findByPrueba() - prueba inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            List<ClavesExamen> resultado = cut.findByPrueba(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    public void testFindByPruebaNulo() {
        System.out.println("ClavesExamenDAOIT.findByPrueba() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByPrueba(null));
            return null;
        });
    }

    @Test
    public void testCountByPrueba() {
        System.out.println("ClavesExamenDAOIT.countByPrueba()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            long resultado = cut.countByPrueba(ID_PRUEBA_1);
            assertEquals(2, resultado);

            // Prueba inexistente → 0
            long vacio = cut.countByPrueba(UUID.randomUUID());
            assertEquals(0, vacio);

            return null;
        });
    }

    @Test
    public void testCountByPruebaNulo() {
        System.out.println("ClavesExamenDAOIT.countByPrueba() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.countByPrueba(null));
            return null;
        });
    }

    @Test
    public void testExistsByPruebaAndNombre() {
        System.out.println("ClavesExamenDAOIT.existsByPruebaAndNombre()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            assertTrue(cut.existsByPruebaAndNombre(ID_PRUEBA_1, "Clave A"));
            assertTrue(cut.existsByPruebaAndNombre(ID_PRUEBA_1, "Clave B"));
            assertFalse(cut.existsByPruebaAndNombre(ID_PRUEBA_1, "Clave Inexistente"));
            return null;
        });
    }

    @Test
    public void testExistsByPruebaAndNombreInvalido() {
        System.out.println("ClavesExamenDAOIT.existsByPruebaAndNombre() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByPruebaAndNombre(null, "Clave A"));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByPruebaAndNombre(ID_PRUEBA_1, null));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByPruebaAndNombre(ID_PRUEBA_1, "   "));
            return null;
        });
    }

    @Test
    public void testFindByIdWithEtapa() {
        System.out.println("ClavesExamenDAOIT.findByIdWithEtapa()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            ClavesExamen resultado = cut.findByIdWithEtapa(ID_CLAVE_A);

            assertNotNull(resultado);
            assertEquals("Clave A", resultado.getNombreClave());
            assertNotNull(resultado.getEtapaAdmision(), "La etapa debe estar cargada");
            assertEquals(ID_ETAPA_1, resultado.getEtapaAdmision().getIdEtapaAdmision());
            return null;
        });
    }

    @Test
    public void testFindByIdWithEtapaNoExiste() {
        System.out.println("ClavesExamenDAOIT.findByIdWithEtapa() - ID inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            ClavesExamen resultado = cut.findByIdWithEtapa(UUID.randomUUID());
            assertNull(resultado, "Debe retornar null si el ID no existe");
            return null;
        });
    }

    // ===================== VALIDACIONES CREAR =====================

    @Test
    public void testCrearNulo() {
        System.out.println("ClavesExamenDAOIT.crear() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    public void testCrearSinNombre() {
        System.out.println("ClavesExamenDAOIT.crear() - sin nombre");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            PruebasAdmision prueba = em.find(PruebasAdmision.class, ID_PRUEBA_1);
            EtapasAdmision etapa = em.find(EtapasAdmision.class, ID_ETAPA_1);

            ClavesExamen sinNombre = new ClavesExamen();
            sinNombre.setPruebaAdmision(prueba);
            sinNombre.setEtapaAdmision(etapa);
            // nombreClave queda null

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinNombre));
            return null;
        });
    }

    @Test
    public void testCrearSinEtapa() {
        System.out.println("ClavesExamenDAOIT.crear() - sin etapa");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            PruebasAdmision prueba = em.find(PruebasAdmision.class, ID_PRUEBA_1);

            ClavesExamen sinEtapa = new ClavesExamen();
            sinEtapa.setPruebaAdmision(prueba);
            sinEtapa.setNombreClave("Clave sin etapa");
            // etapaAdmision queda null

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinEtapa));
            return null;
        });
    }

    @Test
    public void testCrearNombreDuplicadoEnPrueba() {
        System.out.println("ClavesExamenDAOIT.crear() - nombre duplicado en misma prueba");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            PruebasAdmision prueba = em.find(PruebasAdmision.class, ID_PRUEBA_1);
            EtapasAdmision etapa = em.find(EtapasAdmision.class, ID_ETAPA_1);

            // "Clave A" ya existe en la prueba
            ClavesExamen duplicada = new ClavesExamen();
            duplicada.setPruebaAdmision(prueba);
            duplicada.setEtapaAdmision(etapa);
            duplicada.setNombreClave("Clave A");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(duplicada));
            return null;
        });
    }

    // ===================== VALIDACIONES ACTUALIZAR =====================

    @Test
    public void testActualizarNulo() {
        System.out.println("ClavesExamenDAOIT.actualizar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(null));
            return null;
        });
    }

    @Test
    public void testActualizarSinId() {
        System.out.println("ClavesExamenDAOIT.actualizar() - sin ID");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            PruebasAdmision prueba = em.find(PruebasAdmision.class, ID_PRUEBA_1);

            ClavesExamen sinId = new ClavesExamen();
            sinId.setPruebaAdmision(prueba);
            sinId.setNombreClave("Sin ID");

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(sinId));
            return null;
        });
    }

    @Test
    public void testActualizarNombreDuplicadoEnPrueba() {
        System.out.println("ClavesExamenDAOIT.actualizar() - nombre de otra clave en misma prueba");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ClavesExamanDAO cut = new ClavesExamanDAO();
            cut.em = em;

            // Obtener Clave B e intentar renombrarla a "Clave A"
            ClavesExamen claveB = cut.leer(ID_CLAVE_B);
            assertNotNull(claveB);

            claveB.setNombreClave("Clave A");

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(claveB));
            return null;
        });
    }
}
