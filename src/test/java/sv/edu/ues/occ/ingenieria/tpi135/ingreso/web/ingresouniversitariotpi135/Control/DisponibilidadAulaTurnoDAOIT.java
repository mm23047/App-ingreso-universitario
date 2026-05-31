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
public class DisponibilidadAulaTurnoDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_AULA_1   = UUID.fromString("ffffff11-1111-1111-1111-111111111111");
    private static final UUID ID_AULA_2   = UUID.fromString("ffffff22-2222-2222-2222-222222222222");
    private static final UUID ID_TURNO_1  = UUID.fromString("ffff0001-0001-0001-0001-000000000001");
    private static final UUID ID_TURNO_2  = UUID.fromString("ffff0002-0002-0002-0002-000000000002");

    public DisponibilidadAulaTurnoDAOIT() {
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
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 2 disponibilidades
            // (aula1+turno1, aula2+turno2)
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
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            List<DisponibilidadAulaTurno> resultado = cut.findRange(0, 10);

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

            assertEquals(3, cut.count());

            return null;
        });

        // Verificar rollback
        ejecutarEnTransaccion(em -> {
            DisponibilidadAulaTurnoDAO cut = new DisponibilidadAulaTurnoDAO();
            cut.em = em;

            assertEquals(2, cut.count());
            return null;
        });
    }

    @Test
    @Order(4)
    public void testViolacionPrimaryKeyComputestaBulkInsert() {
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
}
