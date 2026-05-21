package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExamen;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class TurnosExamenDAOIT extends AbstractBaseIT {


    public TurnosExamenDAOIT() {
    }

    @Test
    public void testCount() {
        System.out.println("TEST TurnosExaman DAOIT COUNT");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            int resultado = cut.count();

            //Hay 2 registros semilla en la bd
            assertEquals(2, resultado);

            return null;
        });
    }

    @Test
    public void testFindRange() {
        System.out.println("TEST TurnosExaman DAOIT FIND RANGE");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            List<TurnosExamen> resultado = cut.findRange(0, 5);

            assertNotNull(resultado);
            assertEquals(2, resultado.size());

            return null;
        });
    }

    @Test
    public void testCrear() {
        System.out.println("TEST TurnosExaman DAOIT CREAR");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            // Obtenemos una prueba de admisión existente para asignarla al turno
            PruebasAdmision pruebasAdmision = em.createQuery("SELECT p FROM PruebasAdmision p", PruebasAdmision.class)
                    .setMaxResults(1)
                    .getSingleResult();
            assertNotNull(pruebasAdmision);

            TurnosExamen nuevoTurno = new TurnosExamen();
            nuevoTurno.setNombreTurno("Turno Matutino");
            nuevoTurno.setIdPrueba(pruebasAdmision);
            nuevoTurno.setFecha(LocalDate.now());
            nuevoTurno.setHoraInicio(LocalTime.now());
            nuevoTurno.setHoraFin(LocalTime.now().plusHours(2));

            // Creamos el nuevo turno
            cut.crear(nuevoTurno);

            assertNotNull(nuevoTurno.getIdTurnoExamen());
            // Sube a 3 en esta transacción
            assertEquals(3, cut.count());

            return null;
        });

        // Verificamos el rollback
        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;
            // Vuelve a 2
            assertEquals(2, cut.count());
            return null;
        });
    }

    @Test
    public void testLeer() {
        System.out.println("TEST TurnosExaman DAOIT LEER");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            // Obtenemos un turno que ya exista en la Bd
            TurnosExamen turnoExistente = cut.findRange(0, 1).get(0);

            TurnosExamen resultado = cut.leer(turnoExistente.getIdTurnoExamen());

            assertNotNull(resultado, "El ID del turno de examen no puede ser nulo");
            assertEquals(turnoExistente.getIdTurnoExamen(), resultado.getIdTurnoExamen());
            assertEquals(turnoExistente.getNombreTurno(), resultado.getNombreTurno());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        System.out.println("TEST TurnosExaman DAOIT ACTUALIZAR");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            // Leemos un turno de la Bd
            TurnosExamen turnoExistente = cut.findRange(0, 1).get(0);
            assertNotNull(turnoExistente, "El turno de examen a actualizar no existe");

            // Modificamos
            turnoExistente.setNombreTurno("Turno Vespertino Modificado");

            TurnosExamen turnoActualizado = cut.actualizar(turnoExistente);

            assertNotNull(turnoActualizado);
            assertEquals("Turno Vespertino Modificado", turnoActualizado.getNombreTurno());

            return null;
        });
    }

    @Test
    public void testEliminar() {
        System.out.println("TEST TurnosExaman DAOIT ELIMINAR");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            // Necesitamos una Prueba de Admision para poder crear el turno temporal
            PruebasAdmision pruebasAdmision = em.createQuery("SELECT p FROM PruebasAdmision p", PruebasAdmision.class)
                    .setMaxResults(1)
                    .getSingleResult();

            // Creamos el dato temporal
            TurnosExamen turnoTemporal = new TurnosExamen();
            turnoTemporal.setNombreTurno("Turno Temporal a Eliminar");
            turnoTemporal.setIdPrueba(pruebasAdmision);
            turnoTemporal.setFecha(LocalDate.now());
            turnoTemporal.setHoraInicio(LocalTime.now());
            turnoTemporal.setHoraFin(LocalTime.now().plusHours(2));

            cut.crear(turnoTemporal);

            // Verificamos que se creó correctamente
            assertEquals(3, cut.count());

            // Lo eliminamos
            cut.eliminar(turnoTemporal);

            //Verificamos que bajó a 2 y que la base devuelve null al buscarlo
            assertEquals(2, cut.count());
            assertNull(cut.leer(turnoTemporal.getIdTurnoExamen()), "El turno debería retornar null al haber sido borrado");

            System.out.println("Dato eliminado: "+ cut.leer(turnoTemporal.getIdTurnoExamen()));
            return null;
        });
    }
}
