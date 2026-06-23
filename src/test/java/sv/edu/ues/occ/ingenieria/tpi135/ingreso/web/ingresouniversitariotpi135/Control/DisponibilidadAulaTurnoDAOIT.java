package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Aula;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurnoId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExamen;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DisponibilidadAulaTurnoDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_AULA_1   = UUID.fromString("ffffff11-1111-1111-1111-111111111111");
    private static final UUID ID_AULA_2   = UUID.fromString("ffffff22-2222-2222-2222-222222222222");
    private static final UUID ID_TURNO_1  = UUID.fromString("ffff0001-0001-0001-0001-000000000001");
    private static final UUID ID_TURNO_2  = UUID.fromString("ffff0002-0002-0002-0002-000000000002");

    DisponibilidadAulaTurnoDAOIT() {
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
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 3 disponibilidades
            // (aula1+turno1, aula2+turno1, aula2+turno2)
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
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            List<DisponibilidadAulaTurno> resultado = cut.findRange(0, 10);

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

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            Aula aula = em.find(Aula.class, ID_AULA_1);
            TurnosExamen turno = em.find(TurnosExamen.class, ID_TURNO_2);

            DisponibilidadAulaTurnoId dispId = new DisponibilidadAulaTurnoId();
            dispId.setIdAula(ID_AULA_1);
            dispId.setIdTurno(ID_TURNO_2);

            DisponibilidadAulaTurno nueva = new DisponibilidadAulaTurno();
            nueva.setIdDisponibilidadAulaTurno(dispId);
            nueva.setAula(aula);
            nueva.setTurnoExamen(turno);

            cut.crear(nueva);

            assertEquals(4, cut.count());

            return null;
        });

        // Verificar rollback
        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            assertEquals(3, cut.count());
            return null;
        });
    }

    @Test
    @Order(4)
    void testViolacionPrimaryKeyComputestaBulkInsert() {
        assertTrue(postgres.isRunning());

        // Intenta insertar duplicado usando EntityManager directo para capturar exception
        Throwable exception = assertThrows(
                jakarta.persistence.PersistenceException.class,
                () -> {
                    ejecutarEnTransaccion(em -> {
                        Aula aula = em.find(Aula.class, ID_AULA_1);
                        TurnosExamen turno = em.find(TurnosExamen.class, ID_TURNO_1);

                        // Primer registro: aula1 + turno1 (ya existe en init.sql)
                        DisponibilidadAulaTurnoId id1 = new DisponibilidadAulaTurnoId();
                        id1.setIdAula(ID_AULA_1);
                        id1.setIdTurno(ID_TURNO_1);

                        DisponibilidadAulaTurno disponibilidad1 = new DisponibilidadAulaTurno();
                        disponibilidad1.setIdDisponibilidadAulaTurno(id1);
                        disponibilidad1.setAula(aula);
                        disponibilidad1.setTurnoExamen(turno);

                        // Insertar directamente con EntityManager
                        em.persist(disponibilidad1);
                        em.flush();

                        // Segundo registro: aula1 + turno1 (DUPLICADO) → VIOLACIÓN
                        DisponibilidadAulaTurnoId id2 = new DisponibilidadAulaTurnoId();
                        id2.setIdAula(ID_AULA_1);
                        id2.setIdTurno(ID_TURNO_1);

                        DisponibilidadAulaTurno disponibilidad2 = new DisponibilidadAulaTurno();
                        disponibilidad2.setIdDisponibilidadAulaTurno(id2);
                        disponibilidad2.setAula(aula);
                        disponibilidad2.setTurnoExamen(turno);

                        em.persist(disponibilidad2);
                        em.flush(); // Fuerza la ejecución del INSERT en la BD

                        fail("Debería haber lanzado PersistenceException por clave primaria duplicada");

                        return null;
                    });
                }
        );

        assertNotNull(exception);
        assertTrue(
                exception.getMessage().contains("duplicate key") ||
                exception.getMessage().contains("PRIMARY KEY")
        );
    }

    // ===================== CRUD FALTANTE =====================

    @Test
    @Order(5)
    void testLeer() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.leer()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            DisponibilidadAulaTurnoId id = new DisponibilidadAulaTurnoId();
            id.setIdAula(ID_AULA_1);
            id.setIdTurno(ID_TURNO_1);

            DisponibilidadAulaTurno resultado = cut.leer(id);

            assertNotNull(resultado);
            assertEquals(ID_AULA_1, resultado.getAula().getIdAula());
            assertEquals(ID_TURNO_1, resultado.getTurnoExamen().getIdTurnoExamen());
            return null;
        });
    }

    @Test
    @Order(6)
    void testLeerNoExiste() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.leer() - PK inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            DisponibilidadAulaTurnoId idInexistente = new DisponibilidadAulaTurnoId();
            idInexistente.setIdAula(UUID.randomUUID());
            idInexistente.setIdTurno(UUID.randomUUID());

            DisponibilidadAulaTurno resultado = cut.leer(idInexistente);
            assertNull(resultado, "Debe retornar null si la PK no existe");
            return null;
        });
    }

    @Test
    @Order(7)
    void testLeerTipoInvalido() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.leer() - tipo de ID invalido");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.leer(UUID.randomUUID()));
            assertThrows(IllegalArgumentException.class, () -> cut.leer("id-invalido"));
            return null;
        });
    }

    @Test
    @Order(8)
    void testEliminar() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.eliminar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            // Crear temporal: Aula1 + Turno2 (no existe en init.sql)
            Aula aula = em.find(Aula.class, ID_AULA_1);
            TurnosExamen turno = em.find(TurnosExamen.class, ID_TURNO_2);

            DisponibilidadAulaTurnoId dispId = new DisponibilidadAulaTurnoId();
            dispId.setIdAula(ID_AULA_1);
            dispId.setIdTurno(ID_TURNO_2);

            DisponibilidadAulaTurno temporal = new DisponibilidadAulaTurno();
            temporal.setIdDisponibilidadAulaTurno(dispId);
            temporal.setAula(aula);
            temporal.setTurnoExamen(turno);

            cut.crear(temporal);
            assertEquals(4, cut.count());

            cut.eliminar(temporal);
            assertEquals(3, cut.count());

            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    @Order(9)
    void testExistsByAulaAndTurno() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.existsByAulaAndTurno()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            // Aula1 + Turno1 existe → true
            assertTrue(cut.existsByAulaAndTurno(ID_AULA_1, ID_TURNO_1));
            // Aula2 + Turno2 existe → true
            assertTrue(cut.existsByAulaAndTurno(ID_AULA_2, ID_TURNO_2));
            // Aula1 + Turno2 NO existe → false
            assertFalse(cut.existsByAulaAndTurno(ID_AULA_1, ID_TURNO_2));

            return null;
        });
    }

    @Test
    @Order(10)
    void testExistsByAulaAndTurnoNulos() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.existsByAulaAndTurno() - parametros nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByAulaAndTurno(null, ID_TURNO_1));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByAulaAndTurno(ID_AULA_1, null));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByAulaAndTurno(null, null));
            return null;
        });
    }

    @Test
    @Order(11)
    void testFindByTurno() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.findByTurno()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            // Turno1 tiene 2 disponibilidades (Aula1+Turno1, Aula2+Turno1)
            List<DisponibilidadAulaTurno> resultadoT1 = cut.findByTurno(ID_TURNO_1);
            assertNotNull(resultadoT1);
            assertEquals(2, resultadoT1.size());

            // Turno2 tiene 1 disponibilidad (Aula2+Turno2)
            List<DisponibilidadAulaTurno> resultadoT2 = cut.findByTurno(ID_TURNO_2);
            assertNotNull(resultadoT2);
            assertEquals(1, resultadoT2.size());

            return null;
        });
    }

    @Test
    @Order(12)
    void testFindByTurnoInexistente() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.findByTurno() - turno inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            List<DisponibilidadAulaTurno> resultado = cut.findByTurno(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    @Order(13)
    void testFindByTurnoNulo() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.findByTurno() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByTurno(null));
            return null;
        });
    }

    // ===================== CRITERIA API: findFiltrado =====================

    @Test
    @Order(14)
    void testFindFiltradoSinFiltros() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.findFiltrado() - sin filtros");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            // Sin filtros → retorna las 3 disponibilidades
            List<DisponibilidadAulaTurno> resultado = cut.findFiltrado(null, null, 0, 10);
            assertNotNull(resultado);
            assertEquals(3, resultado.size());
            return null;
        });
    }

    @Test
    @Order(15)
    void testFindFiltradoPorAula() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.findFiltrado() - por aula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            // Aula1 tiene 1 disponibilidad (Turno1)
            List<DisponibilidadAulaTurno> resultadoA1 = cut.findFiltrado(ID_AULA_1, null, 0, 10);
            assertNotNull(resultadoA1);
            assertEquals(1, resultadoA1.size());

            // Aula2 tiene 2 disponibilidades (Turno1, Turno2)
            List<DisponibilidadAulaTurno> resultadoA2 = cut.findFiltrado(ID_AULA_2, null, 0, 10);
            assertNotNull(resultadoA2);
            assertEquals(2, resultadoA2.size());

            return null;
        });
    }

    @Test
    @Order(16)
    void testFindFiltradoPorTurno() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.findFiltrado() - por turno");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            // Turno1 tiene 2 disponibilidades
            List<DisponibilidadAulaTurno> resultadoT1 = cut.findFiltrado(null, ID_TURNO_1, 0, 10);
            assertNotNull(resultadoT1);
            assertEquals(2, resultadoT1.size());

            // Turno2 tiene 1 disponibilidad
            List<DisponibilidadAulaTurno> resultadoT2 = cut.findFiltrado(null, ID_TURNO_2, 0, 10);
            assertNotNull(resultadoT2);
            assertEquals(1, resultadoT2.size());

            return null;
        });
    }

    @Test
    @Order(17)
    void testFindFiltradoPorAulaYTurno() {
        System.out.println("DisponibilidadAulaTurnoDAOIT.findFiltrado() - por aula y turno");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            // Aula2 + Turno1 → 1 resultado exacto
            List<DisponibilidadAulaTurno> resultado = cut.findFiltrado(ID_AULA_2, ID_TURNO_1, 0, 10);
            assertNotNull(resultado);
            assertEquals(1, resultado.size());

            // Aula1 + Turno2 → 0 (no existe)
            List<DisponibilidadAulaTurno> vacio = cut.findFiltrado(ID_AULA_1, ID_TURNO_2, 0, 10);
            assertNotNull(vacio);
            assertTrue(vacio.isEmpty());

            return null;
        });
    }
}
