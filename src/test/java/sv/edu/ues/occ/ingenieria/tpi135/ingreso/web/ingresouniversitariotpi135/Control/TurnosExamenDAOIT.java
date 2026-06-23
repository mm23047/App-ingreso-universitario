package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExamen;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
public class TurnosExamenDAOIT extends AbstractBaseIT {

    private static final UUID ID_PRUEBA_NACIONAL = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final LocalDate FECHA_TURNOS = LocalDate.of(2026, 7, 15);

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
            nuevoTurno.setPruebaAdmision(pruebasAdmision);
            nuevoTurno.setFecha(LocalDate.now());
            // En lugar de LocalTime.now(), definimos horas estáticas de la mañana
            nuevoTurno.setHoraInicio(LocalTime.of(8, 0));  // 08:00 AM
            nuevoTurno.setHoraFin(LocalTime.of(10, 0));    // 10:00 AM
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
            turnoTemporal.setPruebaAdmision(pruebasAdmision);
            turnoTemporal.setFecha(LocalDate.now());
            turnoTemporal.setHoraInicio(LocalTime.of(14, 0)); // 02:00 PM
            turnoTemporal.setHoraFin(LocalTime.of(16, 0));    // 04:00 PM

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

    // ===================== CRUD FALTANTE =====================

    @Test
    public void testLeerNoExiste() {
        System.out.println("TurnosExamenDAOIT.leer() - ID inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            TurnosExamen resultado = cut.leer(UUID.randomUUID());
            assertNull(resultado, "Debe retornar null si el ID no existe");
            return null;
        });
    }

    // ===================== NAMED QUERIES =====================

    @Test
    public void testFindByPrueba() {
        System.out.println("TurnosExamenDAOIT.findByPrueba()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            // Prueba Nacional UES tiene 2 turnos
            List<TurnosExamen> resultado = cut.findByPrueba(ID_PRUEBA_NACIONAL);
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            // Ordenados por fecha + horaInicio: Mañana primero, Tarde después
            assertEquals("Turno Mañana", resultado.get(0).getNombreTurno());
            assertEquals("Turno Tarde", resultado.get(1).getNombreTurno());

            return null;
        });
    }

    @Test
    public void testFindByPruebaInexistente() {
        System.out.println("TurnosExamenDAOIT.findByPrueba() - inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            List<TurnosExamen> resultado = cut.findByPrueba(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    public void testFindByPruebaNulo() {
        System.out.println("TurnosExamenDAOIT.findByPrueba() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByPrueba(null));
            return null;
        });
    }

    @Test
    public void testFindByFecha() {
        System.out.println("TurnosExamenDAOIT.findByFecha()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            // 2026-07-15 tiene 2 turnos, ordenados por horaInicio
            List<TurnosExamen> resultado = cut.findByFecha(FECHA_TURNOS);
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            assertEquals(LocalTime.of(8, 0), resultado.get(0).getHoraInicio());
            assertEquals(LocalTime.of(13, 0), resultado.get(1).getHoraInicio());

            return null;
        });
    }

    @Test
    public void testFindByFechaSinResultados() {
        System.out.println("TurnosExamenDAOIT.findByFecha() - fecha sin turnos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            List<TurnosExamen> resultado = cut.findByFecha(LocalDate.of(2099, 1, 1));
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    public void testFindByFechaNula() {
        System.out.println("TurnosExamenDAOIT.findByFecha() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByFecha(null));
            return null;
        });
    }

    @Test
    public void testExisteTraslape() {
        System.out.println("TurnosExamenDAOIT.existeTraslape() - hay traslape");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            // 09:00-10:00 se cruza con Turno Mañana (08:00-11:00)
            boolean traslape = cut.existeTraslape(
                    ID_PRUEBA_NACIONAL, FECHA_TURNOS,
                    LocalTime.of(9, 0), LocalTime.of(10, 0),
                    null);
            assertTrue(traslape);

            // 07:00-09:00 se cruza con Turno Mañana (08:00 < 09:00 AND 11:00 > 07:00)
            boolean traslape2 = cut.existeTraslape(
                    ID_PRUEBA_NACIONAL, FECHA_TURNOS,
                    LocalTime.of(7, 0), LocalTime.of(9, 0),
                    null);
            assertTrue(traslape2);

            return null;
        });
    }

    @Test
    public void testExisteTraslapeNoHay() {
        System.out.println("TurnosExamenDAOIT.existeTraslape() - sin traslape");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            // 17:00-18:00 no se cruza con ningún turno (Mañana 08-11, Tarde 13-16)
            boolean sinTraslape = cut.existeTraslape(
                    ID_PRUEBA_NACIONAL, FECHA_TURNOS,
                    LocalTime.of(17, 0), LocalTime.of(18, 0),
                    null);
            assertFalse(sinTraslape);

            // 11:00-13:00 tampoco (gap entre mañana y tarde)
            boolean sinTraslape2 = cut.existeTraslape(
                    ID_PRUEBA_NACIONAL, FECHA_TURNOS,
                    LocalTime.of(11, 0), LocalTime.of(13, 0),
                    null);
            assertFalse(sinTraslape2);

            // Fecha diferente → sin traslape
            boolean otraFecha = cut.existeTraslape(
                    ID_PRUEBA_NACIONAL, LocalDate.of(2099, 1, 1),
                    LocalTime.of(9, 0), LocalTime.of(10, 0),
                    null);
            assertFalse(otraFecha);

            return null;
        });
    }

    @Test
    public void testExisteTraslapeNulos() {
        System.out.println("TurnosExamenDAOIT.existeTraslape() - parametros nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            LocalTime h1 = LocalTime.of(9, 0);
            LocalTime h2 = LocalTime.of(10, 0);

            assertThrows(IllegalArgumentException.class,
                    () -> cut.existeTraslape(null, FECHA_TURNOS, h1, h2, null));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existeTraslape(ID_PRUEBA_NACIONAL, null, h1, h2, null));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existeTraslape(ID_PRUEBA_NACIONAL, FECHA_TURNOS, null, h2, null));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existeTraslape(ID_PRUEBA_NACIONAL, FECHA_TURNOS, h1, null, null));
            return null;
        });
    }

    @Test
    public void testFindTurnoActivoParaAspiranteNulos() {
        System.out.println("TurnosExamenDAOIT.findTurnoActivoParaAspirante() - nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findTurnoActivoParaAspirante(null, FECHA_TURNOS, LocalTime.of(9, 0)));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findTurnoActivoParaAspirante(UUID.randomUUID(), null, LocalTime.of(9, 0)));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findTurnoActivoParaAspirante(UUID.randomUUID(), FECHA_TURNOS, null));
            return null;
        });
    }

    @Test
    public void testFindTurnoActivoParaAspiranteNoExiste() {
        System.out.println("TurnosExamenDAOIT.findTurnoActivoParaAspirante() - sin turno activo");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            TurnosExamenDAO cut = new TurnosExamenDAO();
            cut.em = em;

            // Aspirante inexistente → null
            TurnosExamen resultado = cut.findTurnoActivoParaAspirante(
                    UUID.randomUUID(), FECHA_TURNOS, LocalTime.of(9, 0));
            assertNull(resultado, "Debe retornar null si el aspirante no tiene turno activo");
            return null;
        });
    }
}
