package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TurnosExamanDAOIT extends AbstractBaseIT {

    //ID utilizado para el CRUD
    private static UUID idTurnosExamanActual;

    public TurnosExamanDAOIT() {
    }

    @Test
    @Order(1)
    public void testCount() {
        System.out.println("TEST DAOIT COUNT");
        assertTrue(postgres.isRunning());

        TurnosExamanDAO cut = new TurnosExamanDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();
        assertEquals(2, resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("TEST Turnos de examenes DAOIT FIND RANGE");


        TurnosExamanDAO cut = new TurnosExamanDAO();
        cut.em = emf.createEntityManager();

        List<TurnosExaman> resultado = cut.findRange(0, 5);
        assertNotNull(resultado);
        System.out.println("RESULTADO: " + resultado.size());
        assertEquals(2, resultado.size());

    }

    @Test
    @Order(3)
    public void testCrear() {
        System.out.println("TEST Turnos de examenes DAOIT CREAR");
        TurnosExamanDAO cut = new TurnosExamanDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        PruebasAdmision pruebasAdmision = em.createQuery("SELECT p FROM PruebasAdmision p", PruebasAdmision.class)
                .setMaxResults(1)
                .getSingleResult();
        assertNotNull(pruebasAdmision);

        TurnosExaman nuevoTurno = new TurnosExaman();
        nuevoTurno.setNombreTurno("Turno de Matutino");
        nuevoTurno.setIdPrueba(pruebasAdmision);

        nuevoTurno.setFecha(LocalDate.now());
        nuevoTurno.setHoraInicio(LocalTime.now());
        nuevoTurno.setHoraFin(LocalTime.now().plusHours(2));

        //guardamos el nuevo turno
        em.getTransaction().begin();
        cut.crear(nuevoTurno);
        em.getTransaction().commit();

        assertNotNull(nuevoTurno.getId());
        idTurnosExamanActual = nuevoTurno.getId();
        System.out.println("ID del nuevo turno creado: " + idTurnosExamanActual);

    }

    @Test
    @Order(4)
    public void testLeer() {
        System.out.println("TEST Turnos de examenes DAOIT LEER");
        TurnosExamanDAO cut = new TurnosExamanDAO();
        cut.em = emf.createEntityManager();

        TurnosExaman resultado = cut.leer(idTurnosExamanActual);

        System.out.println("RESULTADO: " + resultado.toString());
        assertNotNull(resultado, "El ID del turno de examen no puede ser nulo porque ya debe de existir");
        assertEquals("Turno de Matutino", resultado.getNombreTurno());

    }

    @Test
    @Order(5)
    public void testActualizar() {
        System.out.println("TEST Turnos de examenes DAOIT ACTUALIZAR");
        TurnosExamanDAO cut = new TurnosExamanDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        TurnosExaman turnoExistente = cut.leer(idTurnosExamanActual);
        assertNotNull(turnoExistente, "El turno de examen a actualizar no existe");

        turnoExistente.setNombreTurno("Turno de Vespertino");

        em.getTransaction().begin();
        cut.actualizar(turnoExistente);
        em.getTransaction().commit();

        TurnosExaman turnoActualizado = cut.leer(idTurnosExamanActual);
        assertNotNull(turnoActualizado, "El turno de examen actualizado no puede ser nulo");
        if (turnoActualizado != null) {
            assertEquals("Turno de Vespertino", turnoActualizado.getNombreTurno());
            System.out.println("Turno de examen actualizado correctamente: " + turnoActualizado.getNombreTurno());
        }
    }

    @Test
    @Order(6)
    public void testEliminar() {
        System.out.println("TEST Turnos de examenes DAOIT ELIMINAR");
        TurnosExamanDAO cut = new TurnosExamanDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        TurnosExaman turnoExistente = cut.leer(idTurnosExamanActual);
        assertNotNull(turnoExistente, "El turno de examen a eliminar no existe");

        em.getTransaction().begin();
        cut.eliminar(turnoExistente);
        em.getTransaction().commit();

        TurnosExaman turnoEliminado = cut.leer(idTurnosExamanActual);
        assertNull(turnoEliminado, "El turno de examen debería haber sido eliminado y no debe existir");
        if (turnoEliminado == null) {
            System.out.println("Turno de examen eliminado correctamente, no se encontró en la base de datos.");
        } else {
            System.out.println("Error: El turno de examen aún existe después de intentar eliminarlo.");
        }
    }
}
