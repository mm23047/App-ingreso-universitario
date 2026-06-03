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
}
