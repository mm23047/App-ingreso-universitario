package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CuposCarreraDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_PRUEBA_2026  = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_ETAPA_FINAL  = UUID.fromString("c1000000-0000-0000-0000-000000000003");
    private static final UUID ID_ETAPA_1      = UUID.fromString("c1000000-0000-0000-0000-000000000001");

    CuposCarreraDAOIT() {
    }

    @BeforeAll
    void inicializar() {
        // La configuracion de postgres y emf se realiza en AbstractBaseIT
    }

    @Test
    @Order(1)
    void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql  3 cupos: ICS=50, ISI=60, ICC=45
            assertTrue(resultado > 0);
            assertEquals(3, resultado);

            return null;
        });
    }

    @Test
    @Order(2)
    void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            List<CuposCarrera> resultado = cut.findRange(0, 10);

            // Aún no se ha insertado nada  sigue habiendo 3
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(3, resultado.size());

            return null;
        });
    }

    @Test
    @Order(3)
    void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear un cupo temporal y verificarlo dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            // MAT aún no tiene cupos asignados en el init.sql  combinación única
            PruebasAdmision prueba  = em.find(PruebasAdmision.class, ID_PRUEBA_2026);
            CatalogoCarrera carrera = em.find(CatalogoCarrera.class, "MAT");
            EtapasAdmision  etapa   = em.find(EtapasAdmision.class, ID_ETAPA_1);

            CuposCarreraId clave = new CuposCarreraId();
            clave.setIdPrueba(ID_PRUEBA_2026);
            clave.setIdCarrera("MAT");
            clave.setIdEtapa(ID_ETAPA_1);

            CuposCarrera nuevo = new CuposCarrera();
            nuevo.setIdCupoCarrera(clave);
            nuevo.setPruebaAdmision(prueba);
            nuevo.setCatalogoCarrera(carrera);
            nuevo.setEtapaAdmision(etapa);
            nuevo.setCupos(30);

            cut.crear(nuevo);

            // Dentro de la transacción el registro es visible
            assertEquals(4, cut.count());

            return null;
        });

        // Verificar que, tras el rollback implícito, la BD vuelve a 3 registros
        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            assertEquals(3, cut.count());
            return null;
        });
    }

    @Test
    @Order(4)
    void testLeer() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            // Leer el registro ICS del init.sql: prueba 2026, carrera ICS, etapa final, cupos=50
            CuposCarreraId clave = new CuposCarreraId();
            clave.setIdPrueba(ID_PRUEBA_2026);
            clave.setIdCarrera("ICS");
            clave.setIdEtapa(ID_ETAPA_FINAL);

            CuposCarrera resultado = cut.leer(clave);

            assertNotNull(resultado);
            assertEquals("ICS", resultado.getIdCupoCarrera().getIdCarrera());
            assertEquals(ID_PRUEBA_2026, resultado.getIdCupoCarrera().getIdPrueba());
            assertEquals(ID_ETAPA_FINAL, resultado.getIdCupoCarrera().getIdEtapa());
            assertEquals(50, resultado.getCupos());

            return null;
        });
    }

    @Test
    @Order(5)
    void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            // Leer ISI (cupos=60) y cambiar a 75
            CuposCarreraId clave = new CuposCarreraId();
            clave.setIdPrueba(ID_PRUEBA_2026);
            clave.setIdCarrera("ISI");
            clave.setIdEtapa(ID_ETAPA_FINAL);

            CuposCarrera cupo = cut.leer(clave);
            cupo.setCupos(75);

            CuposCarrera resultado = cut.actualizar(cupo);

            assertNotNull(resultado);
            assertEquals(75, resultado.getCupos());

            // Dentro de la misma transacción el cambio es visible
            CuposCarrera verificacion = cut.leer(clave);
            assertEquals(75, verificacion.getCupos());

            return null;
        });
    }

    @Test
    @Order(6)
    void testEliminar() {
        assertTrue(postgres.isRunning());

        // Crear y eliminar un cupo temporal dentro de una única transacción
        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            PruebasAdmision prueba  = em.find(PruebasAdmision.class, ID_PRUEBA_2026);
            CatalogoCarrera carrera = em.find(CatalogoCarrera.class, "MAT");
            EtapasAdmision  etapa   = em.find(EtapasAdmision.class, ID_ETAPA_1);

            CuposCarreraId clave = new CuposCarreraId();
            clave.setIdPrueba(ID_PRUEBA_2026);
            clave.setIdCarrera("MAT");
            clave.setIdEtapa(ID_ETAPA_1);

            CuposCarrera cupo = new CuposCarrera();
            cupo.setIdCupoCarrera(clave);
            cupo.setPruebaAdmision(prueba);
            cupo.setCatalogoCarrera(carrera);
            cupo.setEtapaAdmision(etapa);
            cupo.setCupos(30);

            // Crear
            cut.crear(cupo);
            assertEquals(4, cut.count());

            // Eliminar
            cut.eliminar(cupo);
            assertEquals(3, cut.count());

            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    @Order(7)
    void testFindByCarrera() {
        System.out.println("CuposCarreraDAOIT.findByCarrera()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            // ICS tiene 1 cupo configurado
            List<CuposCarrera> resultado = cut.findByCarrera("ICS");
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals(50, resultado.get(0).getCupos());

            // ISI tiene 1 cupo configurado
            List<CuposCarrera> resultadoISI = cut.findByCarrera("ISI");
            assertNotNull(resultadoISI);
            assertEquals(1, resultadoISI.size());
            assertEquals(60, resultadoISI.get(0).getCupos());

            return null;
        });
    }

    @Test
    @Order(8)
    void testFindByCarreraInexistente() {
        System.out.println("CuposCarreraDAOIT.findByCarrera() - carrera sin cupos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            List<CuposCarrera> resultado = cut.findByCarrera("MAT");
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    @Order(9)
    void testFindByCarreraInvalido() {
        System.out.println("CuposCarreraDAOIT.findByCarrera() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByCarrera(null));
            assertThrows(IllegalArgumentException.class, () -> cut.findByCarrera(""));
            assertThrows(IllegalArgumentException.class, () -> cut.findByCarrera("   "));
            return null;
        });
    }

    @Test
    @Order(10)
    void testFindCuposConfigurados() {
        System.out.println("CuposCarreraDAOIT.findCuposConfigurados()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            // ICS en prueba 2026, etapa final → 50 cupos
            Integer cuposICS = cut.findCuposConfigurados(ID_PRUEBA_2026, "ICS", ID_ETAPA_FINAL);
            assertEquals(50, cuposICS);

            // ISI → 60 cupos
            Integer cuposISI = cut.findCuposConfigurados(ID_PRUEBA_2026, "ISI", ID_ETAPA_FINAL);
            assertEquals(60, cuposISI);

            // ICC → 45 cupos
            Integer cuposICC = cut.findCuposConfigurados(ID_PRUEBA_2026, "ICC", ID_ETAPA_FINAL);
            assertEquals(45, cuposICC);

            return null;
        });
    }

    @Test
    @Order(11)
    void testFindCuposConfiguradosNoExiste() {
        System.out.println("CuposCarreraDAOIT.findCuposConfigurados() - combinacion inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            // MAT no tiene cupos configurados → retorna 0
            Integer resultado = cut.findCuposConfigurados(ID_PRUEBA_2026, "MAT", ID_ETAPA_FINAL);
            assertEquals(0, resultado);
            return null;
        });
    }

    @Test
    @Order(12)
    void testFindCuposConfiguradosInvalido() {
        System.out.println("CuposCarreraDAOIT.findCuposConfigurados() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findCuposConfigurados(null, "ICS", ID_ETAPA_FINAL));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findCuposConfigurados(ID_PRUEBA_2026, null, ID_ETAPA_FINAL));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findCuposConfigurados(ID_PRUEBA_2026, "   ", ID_ETAPA_FINAL));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findCuposConfigurados(ID_PRUEBA_2026, "ICS", null));
            return null;
        });
    }

    @Test
    @Order(13)
    void testDecrementarCupo() {
        System.out.println("CuposCarreraDAOIT.decrementarCupo()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            // ICC tiene 45 cupos, decrementar debe retornar true
            boolean resultado = cut.decrementarCupo(ID_PRUEBA_2026, "ICC", ID_ETAPA_FINAL);
            assertTrue(resultado);

            // Verificar que bajó a 44
            em.clear();
            Integer cuposActualizados = cut.findCuposConfigurados(ID_PRUEBA_2026, "ICC", ID_ETAPA_FINAL);
            assertEquals(44, cuposActualizados);

            return null;
        });
    }

    @Test
    @Order(14)
    void testDecrementarCupoInexistente() {
        System.out.println("CuposCarreraDAOIT.decrementarCupo() - combinacion inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            // MAT no tiene cupos → false
            boolean resultado = cut.decrementarCupo(ID_PRUEBA_2026, "MAT", ID_ETAPA_FINAL);
            assertFalse(resultado);
            return null;
        });
    }

    @Test
    @Order(15)
    void testDecrementarCupoInvalido() {
        System.out.println("CuposCarreraDAOIT.decrementarCupo() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.decrementarCupo(null, "ICS", ID_ETAPA_FINAL));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.decrementarCupo(ID_PRUEBA_2026, null, ID_ETAPA_FINAL));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.decrementarCupo(ID_PRUEBA_2026, "", ID_ETAPA_FINAL));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.decrementarCupo(ID_PRUEBA_2026, "ICS", null));
            return null;
        });
    }

    @Test
    @Order(16)
    void testLeerNoExiste() {
        System.out.println("CuposCarreraDAOIT.leer() - PK inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            CuposCarreraId claveInexistente = new CuposCarreraId();
            claveInexistente.setIdPrueba(UUID.randomUUID());
            claveInexistente.setIdCarrera("XYZ");
            claveInexistente.setIdEtapa(UUID.randomUUID());

            CuposCarrera resultado = cut.leer(claveInexistente);
            assertNull(resultado, "Debe retornar null si la PK no existe");
            return null;
        });
    }

    // ===================== VALIDACIONES CREAR =====================

    @Test
    @Order(17)
    void testCrearNulo() {
        System.out.println("CuposCarreraDAOIT.crear() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    @Order(18)
    void testCrearCuposNull() {
        System.out.println("CuposCarreraDAOIT.crear() - cupos null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            PruebasAdmision prueba = em.find(PruebasAdmision.class, ID_PRUEBA_2026);
            CatalogoCarrera carrera = em.find(CatalogoCarrera.class, "MAT");
            EtapasAdmision etapa = em.find(EtapasAdmision.class, ID_ETAPA_1);

            CuposCarreraId clave = new CuposCarreraId();
            clave.setIdPrueba(ID_PRUEBA_2026);
            clave.setIdCarrera("MAT");
            clave.setIdEtapa(ID_ETAPA_1);

            CuposCarrera sinCupos = new CuposCarrera();
            sinCupos.setIdCupoCarrera(clave);
            sinCupos.setPruebaAdmision(prueba);
            sinCupos.setCatalogoCarrera(carrera);
            sinCupos.setEtapaAdmision(etapa);
            // cupos queda null

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinCupos));
            return null;
        });
    }

    @Test
    @Order(19)
    void testCrearCuposNegativo() {
        System.out.println("CuposCarreraDAOIT.crear() - cupos negativo");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            PruebasAdmision prueba = em.find(PruebasAdmision.class, ID_PRUEBA_2026);
            CatalogoCarrera carrera = em.find(CatalogoCarrera.class, "MAT");
            EtapasAdmision etapa = em.find(EtapasAdmision.class, ID_ETAPA_1);

            CuposCarreraId clave = new CuposCarreraId();
            clave.setIdPrueba(ID_PRUEBA_2026);
            clave.setIdCarrera("MAT");
            clave.setIdEtapa(ID_ETAPA_1);

            CuposCarrera cupoNegativo = new CuposCarrera();
            cupoNegativo.setIdCupoCarrera(clave);
            cupoNegativo.setPruebaAdmision(prueba);
            cupoNegativo.setCatalogoCarrera(carrera);
            cupoNegativo.setEtapaAdmision(etapa);
            cupoNegativo.setCupos(-1);

            assertThrows(IllegalArgumentException.class, () -> cut.crear(cupoNegativo));
            return null;
        });
    }

    // ===================== VALIDACIONES ACTUALIZAR =====================

    @Test
    @Order(20)
    void testActualizarNulo() {
        System.out.println("CuposCarreraDAOIT.actualizar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(null));
            return null;
        });
    }

    @Test
    @Order(21)
    void testActualizarCuposNull() {
        System.out.println("CuposCarreraDAOIT.actualizar() - cupos null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            CuposCarreraId clave = new CuposCarreraId();
            clave.setIdPrueba(ID_PRUEBA_2026);
            clave.setIdCarrera("ICS");
            clave.setIdEtapa(ID_ETAPA_FINAL);

            CuposCarrera cupo = cut.leer(clave);
            assertNotNull(cupo);
            cupo.setCupos(null);

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(cupo));
            return null;
        });
    }

    @Test
    @Order(22)
    void testActualizarCuposNegativo() {
        System.out.println("CuposCarreraDAOIT.actualizar() - cupos negativo");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            CuposCarreraId clave = new CuposCarreraId();
            clave.setIdPrueba(ID_PRUEBA_2026);
            clave.setIdCarrera("ICS");
            clave.setIdEtapa(ID_ETAPA_FINAL);

            CuposCarrera cupo = cut.leer(clave);
            assertNotNull(cupo);
            cupo.setCupos(-5);

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(cupo));
            return null;
        });
    }
}
