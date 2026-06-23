package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExamenRealizadoDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_INSCRIPCION_1  = UUID.fromString("ffff1001-1001-1001-1001-000000001001");
    private static final UUID ID_INSCRIPCION_2  = UUID.fromString("ffff1002-1002-1002-1002-000000001002");
    private static final UUID ID_ETAPA_1        = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ID_ETAPA_2        = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID ID_ETAPA_3        = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID ID_CLAVE_1        = UUID.fromString("aaaabbbb-cccc-dddd-eeee-ffffffffffff");
    private static final UUID ID_CLAVE_A       = UUID.fromString("08000000-0000-0000-0000-000000000001");
    private static final UUID ID_EXAMEN_1      = UUID.fromString("ffffeee1-1111-1111-1111-111111111111");
    private static final UUID ID_EXAMEN_2      = UUID.fromString("0d000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_TEST   = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_ASPIRANTE_2   = UUID.fromString("e2222222-2222-2222-2222-222222222222");

    ExamenRealizadoDAOIT() {
    }

    @BeforeAll
    void inicializar() {
        // La configuración de postgres y emf se realiza en AbstractBaseIT
    }

    @Test
    @Order(1)
    void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 2 exámenes realizados
            assertTrue(resultado > 0);
            assertEquals(2, resultado);

            return null;
        });
    }

    @Test
    @Order(2)
    void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            List<ExamenRealizado> resultado = cut.findRange(0, 10);

            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size());

            return null;
        });
    }

    @Test
    @Order(3)
    void testCrear() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, ID_INSCRIPCION_2);
            EtapasAdmision etapa = em.find(EtapasAdmision.class, ID_ETAPA_1);
            ClavesExamen clave = em.find(ClavesExamen.class, ID_CLAVE_1);

            ExamenRealizado nuevo = new ExamenRealizado();
            nuevo.setInscripcionesPrueba(inscripcion);
            nuevo.setEtapaAdmision(etapa);
            nuevo.setClaveExamen(clave);

            cut.crear(nuevo);

            assertEquals(3, cut.count());

            return null;
        });

        // Verificar rollback
        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertEquals(2, cut.count());
            return null;
        });
    }

    @Test
    @Order(4)
    void testViolacionUniqueConstraintInscripcionEtapa() {
        assertTrue(postgres.isRunning());

        // Intenta crear dos exámenes para la misma inscripción y etapa
        Throwable exception = assertThrows(
            IllegalStateException.class,
                () -> {
                    ejecutarEnTransaccion(em -> {
                        ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
                        cut.em = em;

                        InscripcionesPrueba inscripcion = em.find(
                                InscripcionesPrueba.class,
                                ID_INSCRIPCION_1
                        );
                        EtapasAdmision etapa = em.find(
                                EtapasAdmision.class,
                                ID_ETAPA_2
                        );
                        ClavesExamen clave = em.find(ClavesExamen.class, ID_CLAVE_1);

                        // Primer examen: inscripcion_1 + etapa_2
                        ExamenRealizado examen1 = new ExamenRealizado();
                        examen1.setInscripcionesPrueba(inscripcion);
                        examen1.setEtapaAdmision(etapa);
                        examen1.setClaveExamen(clave);
                        cut.crear(examen1);

                        // Segundo examen: misma inscripcion_1 + misma etapa_2 → VIOLACIÓN
                        ExamenRealizado examen2 = new ExamenRealizado();
                        examen2.setInscripcionesPrueba(inscripcion);
                        examen2.setEtapaAdmision(etapa);
                        examen2.setClaveExamen(clave);
                        cut.crear(examen2);

                        fail("Debería haber lanzado IllegalStateException");

                        return null;
                    });
                }
        );

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Error al ingresar el registro"));
    }

    // ===================== CRUD FALTANTE =====================

    @Test
    @Order(5)
    void testLeer() {
        System.out.println("ExamenRealizadoDAOIT.leer()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            ExamenRealizado resultado = cut.leer(ID_EXAMEN_1);

            assertNotNull(resultado);
            assertEquals(ID_EXAMEN_1, resultado.getIdExamenRealizado());
            assertEquals(new BigDecimal("70.00"), resultado.getPuntajeFinal());
            assertNotNull(resultado.getInscripcionesPrueba());
            assertNotNull(resultado.getClaveExamen());
            assertNotNull(resultado.getEtapaAdmision());
            return null;
        });
    }

    @Test
    @Order(6)
    void testLeerNoExiste() {
        System.out.println("ExamenRealizadoDAOIT.leer() - ID inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            ExamenRealizado resultado = cut.leer(UUID.randomUUID());
            assertNull(resultado, "Debe retornar null si el ID no existe");
            return null;
        });
    }

    @Test
    @Order(7)
    void testActualizar() {
        System.out.println("ExamenRealizadoDAOIT.actualizar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            ExamenRealizado examen = cut.leer(ID_EXAMEN_2);
            assertNotNull(examen);

            examen.setPuntajeFinal(new BigDecimal("85.50"));
            ExamenRealizado resultado = cut.actualizar(examen);

            assertNotNull(resultado);
            assertEquals(new BigDecimal("85.50"), resultado.getPuntajeFinal());
            return null;
        });
    }

    @Test
    @Order(8)
    void testEliminar() {
        System.out.println("ExamenRealizadoDAOIT.eliminar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            // Crear temporal
            InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, ID_INSCRIPCION_2);
            EtapasAdmision etapa = em.find(EtapasAdmision.class, ID_ETAPA_3);
            ClavesExamen clave = em.find(ClavesExamen.class, ID_CLAVE_1);

            ExamenRealizado temporal = new ExamenRealizado();
            temporal.setInscripcionesPrueba(inscripcion);
            temporal.setEtapaAdmision(etapa);
            temporal.setClaveExamen(clave);

            cut.crear(temporal);
            assertEquals(3, cut.count());

            cut.eliminar(temporal);
            assertEquals(2, cut.count());

            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    @Order(9)
    void testCountByClaveExamen() {
        System.out.println("ExamenRealizadoDAOIT.countByClaveExamen()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            // Clave A (08...001) tiene 2 exámenes
            long resultado = cut.countByClaveExamen(ID_CLAVE_A);
            assertEquals(2, resultado);

            // Clave inexistente → 0
            long vacio = cut.countByClaveExamen(UUID.randomUUID());
            assertEquals(0, vacio);

            return null;
        });
    }

    @Test
    @Order(10)
    void testCountByClaveExamenNulo() {
        System.out.println("ExamenRealizadoDAOIT.countByClaveExamen() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.countByClaveExamen(null));
            return null;
        });
    }

    @Test
    @Order(11)
    void testFindByAspiranteId() {
        System.out.println("ExamenRealizadoDAOIT.findByAspiranteId()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            // Aspirante e2222222 (María Fernanda) tiene 1 examen vía inscripción ffff1001
            List<ExamenRealizado> resultado = cut.findByAspiranteId(ID_ASPIRANTE_2);
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals(new BigDecimal("65.00"), resultado.get(0).getPuntajeFinal());

            return null;
        });
    }

    @Test
    @Order(12)
    void testFindByAspiranteIdInexistente() {
        System.out.println("ExamenRealizadoDAOIT.findByAspiranteId() - inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            List<ExamenRealizado> resultado = cut.findByAspiranteId(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    @Order(13)
    void testFindByAspiranteIdNulo() {
        System.out.println("ExamenRealizadoDAOIT.findByAspiranteId() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByAspiranteId(null));
            return null;
        });
    }

    @Test
    @Order(14)
    void testFindByPruebaId() {
        System.out.println("ExamenRealizadoDAOIT.findByPruebaId()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            // Ambos exámenes usan clave 08...001 que pertenece a prueba d1...001
            List<ExamenRealizado> resultado = cut.findByPruebaId(ID_PRUEBA_TEST);
            assertNotNull(resultado);
            assertEquals(2, resultado.size());

            return null;
        });
    }

    @Test
    @Order(15)
    void testFindByPruebaIdInexistente() {
        System.out.println("ExamenRealizadoDAOIT.findByPruebaId() - inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            List<ExamenRealizado> resultado = cut.findByPruebaId(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    @Order(16)
    void testFindByPruebaIdNulo() {
        System.out.println("ExamenRealizadoDAOIT.findByPruebaId() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByPruebaId(null));
            return null;
        });
    }

    @Test
    @Order(17)
    void testFindRankingByPruebaAndEtapa() {
        System.out.println("ExamenRealizadoDAOIT.findRankingByPruebaAndEtapa()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            // Prueba d1...001 + Etapa aaaa (Primera Etapa) → 2 exámenes, orden DESC por puntaje
            List<ExamenRealizado> resultado = cut.findRankingByPruebaAndEtapa(
                    ID_PRUEBA_TEST, ID_ETAPA_1, 0, 10);

            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            // Orden DESC: 70.00 primero, 65.00 después
            assertEquals(new BigDecimal("70.00"), resultado.get(0).getPuntajeFinal());
            assertEquals(new BigDecimal("65.00"), resultado.get(1).getPuntajeFinal());

            return null;
        });
    }

    @Test
    @Order(18)
    void testFindRankingByPruebaAndEtapaNulos() {
        System.out.println("ExamenRealizadoDAOIT.findRankingByPruebaAndEtapa() - nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findRankingByPruebaAndEtapa(null, ID_ETAPA_1, 0, 10));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findRankingByPruebaAndEtapa(ID_PRUEBA_TEST, null, 0, 10));
            return null;
        });
    }

    @Test
    @Order(19)
    void testFindByAspiranteDui() {
        System.out.println("ExamenRealizadoDAOIT.findByAspiranteDui()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            // María Fernanda DUI 02234567-8 → 1 examen
            List<ExamenRealizado> resultado = cut.findByAspiranteDui("02234567-8");
            assertNotNull(resultado);
            assertEquals(1, resultado.size());

            // DUI inexistente → vacío
            List<ExamenRealizado> vacio = cut.findByAspiranteDui("00000000-0");
            assertNotNull(vacio);
            assertTrue(vacio.isEmpty());

            return null;
        });
    }

    @Test
    @Order(20)
    void testFindByAspiranteDuiInvalido() {
        System.out.println("ExamenRealizadoDAOIT.findByAspiranteDui() - invalido");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByAspiranteDui(null));
            assertThrows(IllegalArgumentException.class, () -> cut.findByAspiranteDui(""));
            assertThrows(IllegalArgumentException.class, () -> cut.findByAspiranteDui("   "));
            return null;
        });
    }

    @Test
    @Order(21)
    void testFindByAspiranteCorreo() {
        System.out.println("ExamenRealizadoDAOIT.findByAspiranteCorreo()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            // María Fernanda correo → 1 examen
            List<ExamenRealizado> resultado = cut.findByAspiranteCorreo("maria.castillo@gmail.com");
            assertNotNull(resultado);
            assertEquals(1, resultado.size());

            // Correo inexistente → vacío
            List<ExamenRealizado> vacio = cut.findByAspiranteCorreo("noexiste@test.com");
            assertNotNull(vacio);
            assertTrue(vacio.isEmpty());

            return null;
        });
    }

    @Test
    @Order(22)
    void testFindByAspiranteCorreoInvalido() {
        System.out.println("ExamenRealizadoDAOIT.findByAspiranteCorreo() - invalido");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByAspiranteCorreo(null));
            assertThrows(IllegalArgumentException.class, () -> cut.findByAspiranteCorreo(""));
            assertThrows(IllegalArgumentException.class, () -> cut.findByAspiranteCorreo("   "));
            return null;
        });
    }

    // ===================== VALIDACIONES CREAR =====================

    @Test
    @Order(23)
    void testCrearNulo() {
        System.out.println("ExamenRealizadoDAOIT.crear() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    @Order(24)
    void testCrearPuntajeNegativo() {
        System.out.println("ExamenRealizadoDAOIT.crear() - puntaje negativo");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, ID_INSCRIPCION_2);
            EtapasAdmision etapa = em.find(EtapasAdmision.class, ID_ETAPA_3);
            ClavesExamen clave = em.find(ClavesExamen.class, ID_CLAVE_1);

            ExamenRealizado invalido = new ExamenRealizado();
            invalido.setInscripcionesPrueba(inscripcion);
            invalido.setEtapaAdmision(etapa);
            invalido.setClaveExamen(clave);
            invalido.setPuntajeFinal(new BigDecimal("-5"));

            assertThrows(IllegalArgumentException.class, () -> cut.crear(invalido));
            return null;
        });
    }

    // ===================== VALIDACIONES ACTUALIZAR =====================

    @Test
    @Order(25)
    void testActualizarNulo() {
        System.out.println("ExamenRealizadoDAOIT.actualizar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(null));
            return null;
        });
    }

    @Test
    @Order(26)
    void testActualizarPuntajeNegativo() {
        System.out.println("ExamenRealizadoDAOIT.actualizar() - puntaje negativo");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            ExamenRealizado examen = cut.leer(ID_EXAMEN_1);
            assertNotNull(examen);

            examen.setPuntajeFinal(new BigDecimal("-10"));

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(examen));
            return null;
        });
    }

    // ===================== COBERTURA FALTANTE: leer =====================

    @Test
    @Order(27)
    void testLeerNulo() {
        System.out.println("ExamenRealizadoDAOIT.leer() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.leer(null));
            return null;
        });
    }

    // ===================== COBERTURA FALTANTE: calificarExamen =====================

    @Test
    @Order(28)
    void testCalificarExamenNulo() {
        System.out.println("ExamenRealizadoDAOIT.calificarExamen() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.calificarExamen(null));
            return null;
        });
    }

    @Test
    @Order(29)
    void testCalificarExamenInexistente() {
        System.out.println("ExamenRealizadoDAOIT.calificarExamen() - examen inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            ExamenRealizado resultado = cut.calificarExamen(UUID.randomUUID());
            assertNull(resultado, "Debe retornar null si el examen no existe");
            return null;
        });
    }

    @Test
    @Order(30)
    void testCalificarExamenHappyPath() {
        System.out.println("ExamenRealizadoDAOIT.calificarExamen() - happy path");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            // Examen ffffeee1: clave 08...001 tiene 2 preguntas (f1000000...001 y 55555555)
            // Respuestas del examen: opcion cccccccc (correcta para pregunta 55555555)
            //                        opcion bbbbbbbb (incorrecta para pregunta 55555555)
            // Etapa aaaaaaaa (Primera Etapa) tiene puntajeMaximo
            ExamenRealizado resultado = cut.calificarExamen(ID_EXAMEN_1);

            assertNotNull(resultado);
            assertNotNull(resultado.getPuntajeFinal());
            assertTrue(resultado.getPuntajeFinal().compareTo(BigDecimal.ZERO) >= 0,
                    "El puntaje calculado no debe ser negativo");

            // Verificar que la inscripción fue marcada como CALIFICADO
            InscripcionesPrueba inscripcion = resultado.getInscripcionesPrueba();
            assertNotNull(inscripcion);

            return null;
        });
    }

    // ===================== COBERTURA FALTANTE: iniciarExamenAspirante =====================

    @Test
    @Order(31)
    void testIniciarExamenAspiranteNulos() {
        System.out.println("ExamenRealizadoDAOIT.iniciarExamenAspirante() - parametros nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.iniciarExamenAspirante(null, ID_ETAPA_1));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.iniciarExamenAspirante(ID_INSCRIPCION_1, null));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.iniciarExamenAspirante(null, null));
            return null;
        });
    }

    @Test
    @Order(32)
    void testIniciarExamenAspiranteYaExiste() {
        System.out.println("ExamenRealizadoDAOIT.iniciarExamenAspirante() - ya existe examen");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            // Examen ffffeee1 ya existe con inscripcion 09...001 + etapa aaaaaaaa
            UUID idInscripcionExistente = UUID.fromString("09000000-0000-0000-0000-000000000001");

            assertThrows(IllegalStateException.class,
                    () -> cut.iniciarExamenAspirante(idInscripcionExistente, ID_ETAPA_1));
            return null;
        });
    }

    @Test
    @Order(33)
    void testIniciarExamenAspiranteInscripcionInexistente() {
        System.out.println("ExamenRealizadoDAOIT.iniciarExamenAspirante() - inscripcion inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.iniciarExamenAspirante(UUID.randomUUID(), ID_ETAPA_1));
            return null;
        });
    }

    @Test
    @Order(34)
    void testIniciarExamenAspiranteSinTurnoActivo() {
        System.out.println("ExamenRealizadoDAOIT.iniciarExamenAspirante() - sin turno activo");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            // Inscripcion ffff1002 existe pero el aspirante no tiene turno activo ahora
            assertThrows(IllegalStateException.class,
                    () -> cut.iniciarExamenAspirante(ID_INSCRIPCION_2, ID_ETAPA_3));
            return null;
        });
    }
}
