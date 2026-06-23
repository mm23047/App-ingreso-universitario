package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Aula;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AulaDAOIT extends AbstractBaseIT {

    // ===================== CRUD HEREDADO =====================

    @Test
    public void testCount() {
        System.out.println("AulaDAOIT.count()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            int resultado = cut.count();
            // La BD tiene 2 aulas (AULA-A101 y AULA-B202)
            assertEquals(2, resultado);
            return null;
        });
    }

    @Test
    public void testFindRange() {
        System.out.println("AulaDAOIT.findRange()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            List<Aula> resultado = cut.findRange(0, 10);
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size());
            System.out.println("Resultado findRange: " + resultado);
            return null;
        });
    }

    @Test
    public void testCrear() {
        System.out.println("AulaDAOIT.crear()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            Aula nueva = new Aula();
            nueva.setCodigoAulaApi("AULA-C303");
            nueva.setCapacidadFisica(50);
            nueva.setAccesibleSillaRuedas(true);
            nueva.setNombreSede("Sede Oriente");
            nueva.setDepartamento("San Miguel");
            nueva.setMunicipio("San Miguel");

            cut.crear(nueva);

            assertNotNull(nueva.getIdAula());
            assertEquals(3, cut.count());
            System.out.println("Aula creada con ID: " + nueva.getIdAula());
            return null;
        });

        // Verificar rollback: debe volver a 2
        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;
            assertEquals(2, cut.count());
            return null;
        });
    }

    @Test
    public void testLeer() {
        System.out.println("AulaDAOIT.leer()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            Aula aulaExistente = cut.findRange(0, 1).get(0);

            Aula resultado = cut.leer(aulaExistente.getIdAula());

            assertNotNull(resultado, "El aula leida no puede ser nula porque ya existe en BD");
            assertEquals(aulaExistente.getIdAula(), resultado.getIdAula());
            assertEquals(aulaExistente.getCodigoAulaApi(), resultado.getCodigoAulaApi());
            return null;
        });
    }

    @Test
    public void testActualizar() {
        System.out.println("AulaDAOIT.actualizar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            Aula aula = cut.findRange(0, 1).get(0);
            assertNotNull(aula);

            aula.setNombreSede("Sede Central Modificada");
            aula.setCapacidadFisica(100);

            Aula resultado = cut.actualizar(aula);

            assertEquals("Sede Central Modificada", resultado.getNombreSede());
            assertEquals(100, resultado.getCapacidadFisica());
            return null;
        });
    }

    @Test
    public void testEliminar() {
        System.out.println("AulaDAOIT.eliminar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            // Crear aula temporal para no afectar FK existentes
            Aula temporal = new Aula();
            temporal.setCodigoAulaApi("AULA-TEMP-DEL");
            temporal.setCapacidadFisica(20);
            temporal.setAccesibleSillaRuedas(false);
            temporal.setNombreSede("Sede Temporal");
            temporal.setDepartamento("Temporal");
            cut.crear(temporal);

            assertEquals(3, cut.count());

            cut.eliminar(temporal);

            assertEquals(2, cut.count());
            assertNull(cut.leer(temporal.getIdAula()), "El aula temporal ya no debe existir");
            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    public void testFindByCodigoAulaApi() {
        System.out.println("AulaDAOIT.findByCodigoAulaApi()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            Aula resultado = cut.findByCodigoAulaApi("AULA-A101");

            assertNotNull(resultado);
            assertEquals("AULA-A101", resultado.getCodigoAulaApi());
            assertEquals(40, resultado.getCapacidadFisica());
            assertEquals("Sede Central", resultado.getNombreSede());
            return null;
        });
    }

    @Test
    public void testFindByCodigoAulaApiNoExiste() {
        System.out.println("AulaDAOIT.findByCodigoAulaApi() - no existe");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            Aula resultado = cut.findByCodigoAulaApi("CODIGO-INEXISTENTE");

            assertNull(resultado, "Debe retornar null si el codigo no existe");
            return null;
        });
    }

    @Test
    public void testFindAulasAccesibles() {
        System.out.println("AulaDAOIT.findAulasAccesibles()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            // Solo AULA-A101 es accesible (accesible_silla_ruedas = true)
            List<Aula> resultado = cut.findAulasAccesibles();

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertTrue(resultado.get(0).getAccesibleSillaRuedas());
            assertEquals("AULA-A101", resultado.get(0).getCodigoAulaApi());
            return null;
        });
    }

    @Test
    public void testFindByCapacidadMinima() {
        System.out.println("AulaDAOIT.findByCapacidadMinima()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            // capacidad >= 35 debe retornar ambas aulas (35 y 40)
            List<Aula> resultado = cut.findByCapacidadMinima(35);
            assertNotNull(resultado);
            assertEquals(2, resultado.size());

            // capacidad >= 40 debe retornar solo AULA-A101 (40)
            List<Aula> resultadoAlto = cut.findByCapacidadMinima(40);
            assertNotNull(resultadoAlto);
            assertEquals(1, resultadoAlto.size());
            assertEquals("AULA-A101", resultadoAlto.get(0).getCodigoAulaApi());

            // capacidad >= 100 no debe retornar ninguna
            List<Aula> resultadoVacio = cut.findByCapacidadMinima(100);
            assertNotNull(resultadoVacio);
            assertTrue(resultadoVacio.isEmpty());

            return null;
        });
    }

    // ===================== VALIDACIONES DE NEGOCIO =====================

    @Test
    public void testCrearAulaNula() {
        System.out.println("AulaDAOIT.crear() - aula nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    public void testCrearSinCodigoApi() {
        System.out.println("AulaDAOIT.crear() - sin codigo API");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            Aula aula = new Aula();
            aula.setCapacidadFisica(30);
            aula.setNombreSede("Sede");
            aula.setDepartamento("Depto");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(aula));
            return null;
        });
    }

    @Test
    public void testCrearSinCapacidad() {
        System.out.println("AulaDAOIT.crear() - sin capacidad");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            Aula aula = new Aula();
            aula.setCodigoAulaApi("AULA-FAIL");
            aula.setNombreSede("Sede");
            aula.setDepartamento("Depto");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(aula));
            return null;
        });
    }

    @Test
    public void testCrearConCapacidadCero() {
        System.out.println("AulaDAOIT.crear() - capacidad cero");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            Aula aula = new Aula();
            aula.setCodigoAulaApi("AULA-FAIL");
            aula.setCapacidadFisica(0);
            aula.setNombreSede("Sede");
            aula.setDepartamento("Depto");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(aula));
            return null;
        });
    }

    @Test
    public void testCrearSinNombreSede() {
        System.out.println("AulaDAOIT.crear() - sin nombre sede");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            Aula aula = new Aula();
            aula.setCodigoAulaApi("AULA-FAIL");
            aula.setCapacidadFisica(30);
            aula.setDepartamento("Depto");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(aula));
            return null;
        });
    }

    @Test
    public void testCrearSinDepartamento() {
        System.out.println("AulaDAOIT.crear() - sin departamento");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            Aula aula = new Aula();
            aula.setCodigoAulaApi("AULA-FAIL");
            aula.setCapacidadFisica(30);
            aula.setNombreSede("Sede");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(aula));
            return null;
        });
    }

    @Test
    public void testCrearCodigoDuplicado() {
        System.out.println("AulaDAOIT.crear() - codigo duplicado");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            // AULA-A101 ya existe en la BD
            Aula duplicada = new Aula();
            duplicada.setCodigoAulaApi("AULA-A101");
            duplicada.setCapacidadFisica(25);
            duplicada.setAccesibleSillaRuedas(false);
            duplicada.setNombreSede("Otra Sede");
            duplicada.setDepartamento("Otro Depto");

            assertThrows(IllegalStateException.class, () -> cut.crear(duplicada));
            return null;
        });
    }

    @Test
    public void testActualizarNulo() {
        System.out.println("AulaDAOIT.actualizar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(null));
            return null;
        });
    }

    @Test
    public void testActualizarSinId() {
        System.out.println("AulaDAOIT.actualizar() - sin ID");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            Aula sinId = new Aula();
            sinId.setCodigoAulaApi("AULA-SIN-ID");
            sinId.setCapacidadFisica(30);
            sinId.setNombreSede("Sede");
            sinId.setDepartamento("Depto");

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(sinId));
            return null;
        });
    }

    @Test
    public void testActualizarCodigoDuplicado() {
        System.out.println("AulaDAOIT.actualizar() - codigo duplicado de otra aula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            // Obtener AULA-B202 e intentar cambiarle el codigo a AULA-A101
            Aula aula = cut.findByCodigoAulaApi("AULA-B202");
            assertNotNull(aula);

            aula.setCodigoAulaApi("AULA-A101");

            assertThrows(IllegalStateException.class, () -> cut.actualizar(aula));
            return null;
        });
    }

    @Test
    public void testFindByCapacidadMinimaInvalida() {
        System.out.println("AulaDAOIT.findByCapacidadMinima() - parametro invalido");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByCapacidadMinima(null));
            assertThrows(IllegalArgumentException.class, () -> cut.findByCapacidadMinima(0));
            assertThrows(IllegalArgumentException.class, () -> cut.findByCapacidadMinima(-5));
            return null;
        });
    }

    @Test
    public void testFindByCodigoAulaApiInvalido() {
        System.out.println("AulaDAOIT.findByCodigoAulaApi() - parametro invalido");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AulaDAO cut = new AulaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByCodigoAulaApi(null));
            assertThrows(IllegalArgumentException.class, () -> cut.findByCodigoAulaApi(""));
            assertThrows(IllegalArgumentException.class, () -> cut.findByCodigoAulaApi("   "));
            return null;
        });
    }
}
