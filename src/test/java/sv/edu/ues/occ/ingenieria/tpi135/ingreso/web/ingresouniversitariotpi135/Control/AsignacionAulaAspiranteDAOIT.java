package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Aula;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionAulaAspirante;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurnoId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExamen;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AsignacionAulaAspiranteDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_INSCRIPCION_1  = UUID.fromString("ffff1001-1001-1001-1001-000000001001");
    private static final UUID ID_INSCRIPCION_2  = UUID.fromString("ffff1002-1002-1002-1002-000000001002");
    private static final UUID ID_TURNO_1        = UUID.fromString("ffff0001-0001-0001-0001-000000000001");
    private static final UUID ID_TURNO_2        = UUID.fromString("ffff0002-0002-0002-0002-000000000002");
    private static final UUID ID_AULA_1         = UUID.fromString("ffffff11-1111-1111-1111-111111111111");
    private static final UUID ID_AULA_2         = UUID.fromString("ffffff22-2222-2222-2222-222222222222");
    private static final UUID ID_ASIGNACION_1   = UUID.fromString("fa000001-0000-0000-0000-000000000001");
    private static final UUID ID_ASIGNACION_2   = UUID.fromString("fa000001-0000-0000-0000-000000000002");

    public AsignacionAulaAspiranteDAOIT() {
    }

    @BeforeAll
    void inicializar() {
        // La configuración de postgres y emf se realiza en AbstractBaseIT
    }

    @Test
    @Order(1)
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 2 asignaciones
            assertTrue(resultado > 0);
            assertEquals(2, resultado);

            return null;
        });
    }

    @Test
    @Order(2)
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            List<AsignacionAulaAspirante> resultado = cut.findRange(0, 10);

            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size());

            return null;
        });
    }

    @Test
    @Order(3)
    public void testCrear() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, ID_INSCRIPCION_2);
            DisponibilidadAulaTurnoId dispId = new DisponibilidadAulaTurnoId();
            dispId.setIdAula(ID_AULA_2);
            dispId.setIdTurno(ID_TURNO_2);
            DisponibilidadAulaTurno disponibilidad = em.find(
                    DisponibilidadAulaTurno.class,
                    dispId
            );

            AsignacionAulaAspirante nueva = new AsignacionAulaAspirante();
            nueva.setInscripcionPrueba(inscripcion);
            nueva.setDisponibilidad(disponibilidad);

            cut.crear(nueva);

            assertEquals(3, cut.count());

            return null;
        });

        // Verificar rollback
        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            assertEquals(2, cut.count());
            return null;
        });
    }

    @Test
    @Order(4)
    public void testViolacionUniqueConstraintInscripcionTurno() {
        assertTrue(postgres.isRunning());

        // Intenta crear dos asignaciones para la misma inscripción y turno
        Throwable exception = assertThrows(
            IllegalStateException.class,
                () -> {
                    ejecutarEnTransaccion(em -> {
                        AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
                        cut.em = em;

                        InscripcionesPrueba inscripcion = em.find(
                                InscripcionesPrueba.class,
                                ID_INSCRIPCION_1
                        );
                        DisponibilidadAulaTurnoId dispId1 = new DisponibilidadAulaTurnoId();
                        dispId1.setIdAula(ID_AULA_1);
                        dispId1.setIdTurno(ID_TURNO_1);
                        DisponibilidadAulaTurno disponibilidad1 = em.find(
                                DisponibilidadAulaTurno.class,
                                dispId1
                        );
                        DisponibilidadAulaTurnoId dispId2 = new DisponibilidadAulaTurnoId();
                        dispId2.setIdAula(ID_AULA_2);
                        dispId2.setIdTurno(ID_TURNO_1);
                        DisponibilidadAulaTurno disponibilidad2 = em.find(
                                DisponibilidadAulaTurno.class,
                                dispId2
                        );

                        // Primera asignación: inscripcion_1 + turno_1 en aula_1
                        AsignacionAulaAspirante asignacion1 = new AsignacionAulaAspirante();
                        asignacion1.setInscripcionPrueba(inscripcion);
                        asignacion1.setDisponibilidad(disponibilidad1);
                        cut.crear(asignacion1);

                        // Segunda asignación: misma inscripcion_1 + mismo turno_1 (aula diferente)
                        // → VIOLACIÓN porque una persona NO puede estar en dos aulas en el mismo turno
                        AsignacionAulaAspirante asignacion2 = new AsignacionAulaAspirante();
                        asignacion2.setInscripcionPrueba(inscripcion);
                        asignacion2.setDisponibilidad(disponibilidad2);
                        cut.crear(asignacion2);

                        fail("Debería haber lanzado PersistenceException");

                        return null;
                    });
                }
        );

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("aula asignada"));
    }

    @Test
    @Order(5)
    public void testValidacionNoExcederCapacidadAula() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            Aula aula1 = em.find(Aula.class, ID_AULA_1);
            assertNotNull(aula1);
            
            // Aula 1 tiene capacidad 40
            assertEquals(40, aula1.getCapacidadFisica());

            // Contar asignaciones actuales en aula1 + turno1
            // (init.sql tiene 2 asignaciones: inscripcion_1 y inscripcion_2 ambas en aula1+turno1)
            int countActual = ((Number) em.createQuery(
                "SELECT COUNT(a) FROM AsignacionAulaAspirante a WHERE " +
                "a.disponibilidad.idDisponibilidadAulaTurno.idAula = :idAula AND " +
                "a.disponibilidad.idDisponibilidadAulaTurno.idTurno = :idTurno"
            ).setParameter("idAula", ID_AULA_1)
             .setParameter("idTurno", ID_TURNO_1)
             .getSingleResult()).intValue();

            assertTrue(countActual < 40);

            return null;
        });
    }

    // ===================== CRUD FALTANTE =====================

    @Test
    @Order(6)
    public void testLeer() {
        System.out.println("AsignacionAulaAspiranteDAOIT.leer()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            // Asignacion 1: inscripcion ffff1001, aula2+turno2
            AsignacionAulaAspirante resultado = cut.leer(ID_ASIGNACION_1);

            assertNotNull(resultado);
            assertEquals(ID_ASIGNACION_1, resultado.getIdAsignacionAulaAspirante());
            assertNotNull(resultado.getInscripcionPrueba());
            assertNotNull(resultado.getDisponibilidad());
            assertNotNull(resultado.getDisponibilidad().getAula());
            assertNotNull(resultado.getDisponibilidad().getTurnoExamen());
            return null;
        });
    }

    @Test
    @Order(7)
    public void testLeerNoExiste() {
        System.out.println("AsignacionAulaAspiranteDAOIT.leer() - ID inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            AsignacionAulaAspirante resultado = cut.leer(UUID.randomUUID());
            assertNull(resultado, "Debe retornar null si el ID no existe");
            return null;
        });
    }

    @Test
    @Order(8)
    public void testActualizar() {
        System.out.println("AsignacionAulaAspiranteDAOIT.actualizar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            // Leer asignacion existente y verificar que actualizar no falla
            AsignacionAulaAspirante asignacion = cut.leer(ID_ASIGNACION_1);
            assertNotNull(asignacion);

            AsignacionAulaAspirante resultado = cut.actualizar(asignacion);
            assertNotNull(resultado);
            assertEquals(ID_ASIGNACION_1, resultado.getIdAsignacionAulaAspirante());
            return null;
        });
    }

    @Test
    @Order(9)
    public void testEliminar() {
        System.out.println("AsignacionAulaAspiranteDAOIT.eliminar()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            // Crear temporal: inscripcion2 en aula2+turno2
            InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, ID_INSCRIPCION_2);
            DisponibilidadAulaTurnoId dispId = new DisponibilidadAulaTurnoId();
            dispId.setIdAula(ID_AULA_2);
            dispId.setIdTurno(ID_TURNO_2);
            DisponibilidadAulaTurno disponibilidad = em.find(DisponibilidadAulaTurno.class, dispId);

            AsignacionAulaAspirante temporal = new AsignacionAulaAspirante();
            temporal.setInscripcionPrueba(inscripcion);
            temporal.setDisponibilidad(disponibilidad);

            cut.crear(temporal);
            assertEquals(3, cut.count());

            cut.eliminar(temporal);
            assertEquals(2, cut.count());

            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    @Order(10)
    public void testCountByAulaAndTurno() {
        System.out.println("AsignacionAulaAspiranteDAOIT.countByAulaAndTurno()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            // Aula2 + Turno2 tiene 1 asignacion (inscripcion ffff1001)
            long count1 = cut.countByAulaAndTurno(ID_AULA_2, ID_TURNO_2);
            assertEquals(1, count1);

            // Aula1 + Turno1 tiene 1 asignacion (inscripcion ffff1002)
            long count2 = cut.countByAulaAndTurno(ID_AULA_1, ID_TURNO_1);
            assertEquals(1, count2);

            // Combinacion sin asignaciones → 0
            long count3 = cut.countByAulaAndTurno(ID_AULA_1, ID_TURNO_2);
            assertEquals(0, count3);

            return null;
        });
    }

    @Test
    @Order(11)
    public void testCountByAulaAndTurnoNulos() {
        System.out.println("AsignacionAulaAspiranteDAOIT.countByAulaAndTurno() - nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.countByAulaAndTurno(null, ID_TURNO_1));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.countByAulaAndTurno(ID_AULA_1, null));
            return null;
        });
    }

    @Test
    @Order(12)
    public void testExistsByInscripcionAndTurno() {
        System.out.println("AsignacionAulaAspiranteDAOIT.existsByInscripcionAndTurno()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            // Inscripcion1 está asignada en turno2 → true
            assertTrue(cut.existsByInscripcionAndTurno(ID_INSCRIPCION_1, ID_TURNO_2));

            // Inscripcion1 NO está en turno1 → false
            assertFalse(cut.existsByInscripcionAndTurno(ID_INSCRIPCION_1, ID_TURNO_1));

            // Inscripcion2 está asignada en turno1 → true
            assertTrue(cut.existsByInscripcionAndTurno(ID_INSCRIPCION_2, ID_TURNO_1));

            return null;
        });
    }

    @Test
    @Order(13)
    public void testExistsByInscripcionAndTurnoNulos() {
        System.out.println("AsignacionAulaAspiranteDAOIT.existsByInscripcionAndTurno() - nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByInscripcionAndTurno(null, ID_TURNO_1));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByInscripcionAndTurno(ID_INSCRIPCION_1, null));
            return null;
        });
    }

    @Test
    @Order(14)
    public void testFindByInscripcion() {
        System.out.println("AsignacionAulaAspiranteDAOIT.findByInscripcion()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            // Inscripcion ffff1001 tiene 1 asignacion
            List<AsignacionAulaAspirante> resultado = cut.findByInscripcion(ID_INSCRIPCION_1);
            assertNotNull(resultado);
            assertEquals(1, resultado.size());

            // Inscripcion ffff1002 tiene 1 asignacion
            List<AsignacionAulaAspirante> resultado2 = cut.findByInscripcion(ID_INSCRIPCION_2);
            assertNotNull(resultado2);
            assertEquals(1, resultado2.size());

            return null;
        });
    }

    @Test
    @Order(15)
    public void testFindByInscripcionInexistente() {
        System.out.println("AsignacionAulaAspiranteDAOIT.findByInscripcion() - inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            List<AsignacionAulaAspirante> resultado = cut.findByInscripcion(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    @Order(16)
    public void testFindByInscripcionNulo() {
        System.out.println("AsignacionAulaAspiranteDAOIT.findByInscripcion() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByInscripcion(null));
            return null;
        });
    }

    @Test
    @Order(17)
    public void testFindByAulaAndTurno() {
        System.out.println("AsignacionAulaAspiranteDAOIT.findByAulaAndTurno()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            // Aula1 + Turno1 → 1 asignacion (inscripcion ffff1002)
            List<AsignacionAulaAspirante> resultado = cut.findByAulaAndTurno(ID_AULA_1, ID_TURNO_1, 0, 10);
            assertNotNull(resultado);
            assertEquals(1, resultado.size());

            // Aula2 + Turno2 → 1 asignacion (inscripcion ffff1001)
            List<AsignacionAulaAspirante> resultado2 = cut.findByAulaAndTurno(ID_AULA_2, ID_TURNO_2, 0, 10);
            assertNotNull(resultado2);
            assertEquals(1, resultado2.size());

            return null;
        });
    }

    @Test
    @Order(18)
    public void testFindByAulaAndTurnoInexistente() {
        System.out.println("AsignacionAulaAspiranteDAOIT.findByAulaAndTurno() - sin asignaciones");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            // Aula1 + Turno2 no tiene asignaciones
            List<AsignacionAulaAspirante> resultado = cut.findByAulaAndTurno(ID_AULA_1, ID_TURNO_2, 0, 10);
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    @Order(19)
    public void testFindByAulaAndTurnoNulos() {
        System.out.println("AsignacionAulaAspiranteDAOIT.findByAulaAndTurno() - nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByAulaAndTurno(null, ID_TURNO_1, 0, 10));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByAulaAndTurno(ID_AULA_1, null, 0, 10));
            return null;
        });
    }

    // ===================== VALIDACIONES CREAR =====================

    @Test
    @Order(20)
    public void testCrearNulo() {
        System.out.println("AsignacionAulaAspiranteDAOIT.crear() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    @Order(21)
    public void testCrearSinInscripcion() {
        System.out.println("AsignacionAulaAspiranteDAOIT.crear() - sin inscripcion");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            DisponibilidadAulaTurnoId dispId = new DisponibilidadAulaTurnoId();
            dispId.setIdAula(ID_AULA_2);
            dispId.setIdTurno(ID_TURNO_2);
            DisponibilidadAulaTurno disponibilidad = em.find(DisponibilidadAulaTurno.class, dispId);

            AsignacionAulaAspirante sinInscripcion = new AsignacionAulaAspirante();
            sinInscripcion.setDisponibilidad(disponibilidad);

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinInscripcion));
            return null;
        });
    }

    @Test
    @Order(22)
    public void testCrearSinDisponibilidad() {
        System.out.println("AsignacionAulaAspiranteDAOIT.crear() - sin disponibilidad");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionAulaAspiranteDAO cut = new AsignacionAulaAspiranteDAO();
            cut.em = em;

            InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, ID_INSCRIPCION_2);

            AsignacionAulaAspirante sinDisponibilidad = new AsignacionAulaAspirante();
            sinDisponibilidad.setInscripcionPrueba(inscripcion);

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinDisponibilidad));
            return null;
        });
    }
}
