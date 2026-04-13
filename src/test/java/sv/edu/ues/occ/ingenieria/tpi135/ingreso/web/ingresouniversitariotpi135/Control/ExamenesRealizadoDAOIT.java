package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ExamenesRealizadoDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_INSCRIPCION_1  = UUID.fromString("09000000-0000-0000-0000-000000000001");
    private static final UUID ID_AULA_2         = UUID.fromString("0a000000-0000-0000-0000-000000000002");
    private static final UUID ID_CLAVE_2        = UUID.fromString("08000000-0000-0000-0000-000000000002");
    private static final UUID ID_ETAPA_2        = UUID.fromString("c1000000-0000-0000-0000-000000000002");

    @Test
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 2 exámenes realizados
            assertTrue(resultado > 0);
            assertEquals(2, resultado);

            return null;
        });
    }

    @Test
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
            cut.em = em;

            List<ExamenesRealizado> resultado = cut.findRange(0, 10);

            // BD recién iniciada con init.sql → 2 exámenes
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size());

            return null;
        });
    }

    @Test
    public void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
            cut.em = em;

            // Crear una nueva asignación de aula como prerequisito
            // (inscripcion 1 + aula 2 con pupitre diferente)
            InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, ID_INSCRIPCION_1);
            AulasExaman aula = em.find(AulasExaman.class, ID_AULA_2);
            assertNotNull(inscripcion);
            assertNotNull(aula);

            AsignacionesAulaPupitre nuevaAsignacion = new AsignacionesAulaPupitre();
            nuevaAsignacion.setIdInscripcion(inscripcion);
            nuevaAsignacion.setIdAula(aula);
            nuevaAsignacion.setPupitre("Z-99");

            // Cargar las FKs restantes del examen: clave 2 y etapa 2
            ClavesExaman clave = em.find(ClavesExaman.class, ID_CLAVE_2);
            EtapasAdmision etapa = em.find(EtapasAdmision.class, ID_ETAPA_2);
            assertNotNull(clave);
            assertNotNull(etapa);

            ExamenesRealizado nuevo = new ExamenesRealizado();
            nuevo.setIdAsignacion(nuevaAsignacion);
            nuevo.setIdClave(clave);
            nuevo.setIdEtapa(etapa);
            // puntajeFinal y fechaRealizacion son opcionales → se dejan en null

            em.persist(nuevaAsignacion);   // persistir prerequisito primero
            cut.crear(nuevo);

            // Validación dentro de la transacción
            assertEquals(3, cut.count());

            return null;
        });

        // Verificar rollback: vuelve a 2
        ejecutarEnTransaccion(em -> {
            ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
            cut.em = em;

            assertEquals(2, cut.count());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
            cut.em = em;

            // Obtener el primer examen del init.sql
            ExamenesRealizado examen = cut.findRange(0, 1).get(0);
            assertNotNull(examen);

            // Modificar dentro de la transacción: asignar puntaje
            examen.setPuntajeFinal(new BigDecimal("8.50"));

            ExamenesRealizado resultado = cut.actualizar(examen);

            assertNotNull(resultado);
            assertEquals(new BigDecimal("8.50"), resultado.getPuntajeFinal());

            return null;
        });
    }

    @Test
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
            cut.em = em;

            // Crear una nueva asignación y examen para eliminar
            InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, ID_INSCRIPCION_1);
            AulasExaman aula = em.find(AulasExaman.class, ID_AULA_2);
            assertNotNull(inscripcion);
            assertNotNull(aula);

            AsignacionesAulaPupitre nuevaAsignacion = new AsignacionesAulaPupitre();
            nuevaAsignacion.setIdInscripcion(inscripcion);
            nuevaAsignacion.setIdAula(aula);
            nuevaAsignacion.setPupitre("Z-88");

            ClavesExaman clave = em.find(ClavesExaman.class, ID_CLAVE_2);
            EtapasAdmision etapa = em.find(EtapasAdmision.class, ID_ETAPA_2);
            assertNotNull(clave);
            assertNotNull(etapa);

            ExamenesRealizado nuevo = new ExamenesRealizado();
            nuevo.setIdAsignacion(nuevaAsignacion);
            nuevo.setIdClave(clave);
            nuevo.setIdEtapa(etapa);

            em.persist(nuevaAsignacion);
            cut.crear(nuevo);
            assertEquals(3, cut.count());

            // Eliminar el examen y su asignación
            cut.eliminar(nuevo);
            em.remove(nuevaAsignacion);
            assertEquals(2, cut.count());

            return null;
        });
    }

            @Test
            public void testFindByAspiranteId() {
            assertTrue(postgres.isRunning());

            ejecutarEnTransaccion(em -> {
                ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
                cut.em = em;

                // Obtener un examen existente y derivar el aspirante asociado
                ExamenesRealizado existente = em.createQuery(
                    "SELECT e FROM ExamenesRealizado e JOIN e.idAsignacion a JOIN a.idInscripcion i",
                    ExamenesRealizado.class)
                    .setMaxResults(1)
                    .getSingleResult();

                UUID aspiranteId = existente.getIdAsignacion()
                    .getIdInscripcion()
                    .getIdAspirante()
                    .getId();

                // Camino feliz
                List<ExamenesRealizado> resultado = cut.findByAspiranteId(aspiranteId);
                assertNotNull(resultado);
                assertFalse(resultado.isEmpty());
                assertTrue(resultado.stream().allMatch(e -> e.getIdAsignacion() != null
                    && e.getIdAsignacion().getIdInscripcion() != null
                    && e.getIdAsignacion().getIdInscripcion().getIdAspirante() != null
                    && aspiranteId.equals(e.getIdAsignacion().getIdInscripcion().getIdAspirante().getId())));

                // Parámetro nulo
                IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                    () -> cut.findByAspiranteId(null));
                assertEquals("aspiranteId must not be null", iae.getMessage());

                // Error de acceso a BD (em nulo)
                cut.em = null;
                IllegalStateException ise = assertThrows(IllegalStateException.class,
                    () -> cut.findByAspiranteId(aspiranteId));
                assertEquals("Cannot access db", ise.getMessage());

                return null;
            });
            }

            @Test
            public void testFindByPruebaId() {
            assertTrue(postgres.isRunning());

            ejecutarEnTransaccion(em -> {
                ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
                cut.em = em;

                // Obtener un examen existente y derivar la prueba asociada
                ExamenesRealizado existente = em.createQuery(
                    "SELECT e FROM ExamenesRealizado e JOIN e.idClave c",
                    ExamenesRealizado.class)
                    .setMaxResults(1)
                    .getSingleResult();

                UUID pruebaId = existente.getIdClave()
                    .getIdPrueba()
                    .getId();

                // Camino feliz
                List<ExamenesRealizado> resultado = cut.findByPruebaId(pruebaId);
                assertNotNull(resultado);
                assertFalse(resultado.isEmpty());
                assertTrue(resultado.stream().allMatch(e -> e.getIdClave() != null
                    && e.getIdClave().getIdPrueba() != null
                    && pruebaId.equals(e.getIdClave().getIdPrueba().getId())));

                // Parámetro nulo
                IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                    () -> cut.findByPruebaId(null));
                assertEquals("pruebaId must not be null", iae.getMessage());

                // Error de acceso a BD (em nulo)
                cut.em = null;
                IllegalStateException ise = assertThrows(IllegalStateException.class,
                    () -> cut.findByPruebaId(pruebaId));
                assertEquals("Cannot access db", ise.getMessage());

                return null;
            });
            }
}
