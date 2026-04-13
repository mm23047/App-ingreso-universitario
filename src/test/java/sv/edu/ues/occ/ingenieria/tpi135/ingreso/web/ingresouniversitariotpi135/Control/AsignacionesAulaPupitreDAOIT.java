package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;


import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;

import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AsignacionesAulaPupitreDAOIT extends AbstractBaseIT {

    public AsignacionesAulaPupitreDAOIT() {
    }

    /**
     * Obtiene datos relacionados necesarios para crear asignación (inscripción y aula)
     */
    private void obtenerYAsignarDatos(AsignacionesAulaPupitre asignacion, EntityManager em) {
        InscripcionesPrueba inscripcion = em.createQuery("Select a from InscripcionesPrueba a", InscripcionesPrueba.class)
                .setMaxResults(1)
                .getSingleResult();
        AulasExaman aula = em.createQuery("Select a from AulasExaman a", AulasExaman.class)
                .setMaxResults(1)
                .getSingleResult();
        asignacion.setIdInscripcion(inscripcion);
        asignacion.setIdAula(aula);
    }

    @Test
    public void testCount() {
        System.out.println("TEST DAOIT COUNT");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
            cut.em = em;

            int resultado = cut.count();
            assertEquals(2, resultado);
            System.out.println("RESULTADO COUNT: " + resultado);

            return null;
        });
    }

    @Test
    public void testFindRange() {
        System.out.println("TEST DAOIT FIND RANGE");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
            cut.em = em;

            List<AsignacionesAulaPupitre> resultado = cut.findRange(0, 10);
            assertEquals(2, resultado.size());
            System.out.println("RESULTADO FIND RANGE: " + resultado.size());

            return null;
        });
    }

    @Test
    public void testCrear() {
        System.out.println("TEST DAOIT CREAR");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
            cut.em = em;

            AsignacionesAulaPupitre asignacion = new AsignacionesAulaPupitre();
            asignacion.setPupitre("Pupitre 1");
            obtenerYAsignarDatos(asignacion, em);

            cut.crear(asignacion);
            assertNotNull(asignacion.getId());
            assertEquals(3, cut.count());
            System.out.println("RESULTADO CREADO: " + asignacion.getId());

            return null;
        });

        // Verificamos el rollback (la limpieza)
        ejecutarEnTransaccion(em -> {
            AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
            cut.em = em;
            assertEquals(2, cut.count());
            return null;
        });
    }


    @Test
    public void testLeer() {
        System.out.println("TEST DAOIT LEER");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
            cut.em = em;

            AsignacionesAulaPupitre asignacionExistente = cut.findRange(0, 1).get(0);
            AsignacionesAulaPupitre resultado = cut.leer(asignacionExistente.getId());
            assertNotNull(resultado);
            assertEquals(asignacionExistente.getId(), resultado.getId());

            return null;
        });
    }

    @Test
    public void testActualizar() {
        System.out.println("TEST DAOIT ACTUALIZAR");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
            cut.em = em;

            AsignacionesAulaPupitre asignacionExistente = cut.findRange(0, 1).get(0);
            assertNotNull(asignacionExistente);

            asignacionExistente.setPupitre("A-03");
            AsignacionesAulaPupitre resultadoActualizado = cut.actualizar(asignacionExistente);

            assertNotNull(resultadoActualizado);
            assertEquals("A-03", resultadoActualizado.getPupitre());
            System.out.println("RESULTADO ACTUALIZADO: " + resultadoActualizado.getPupitre());

            return null;
        });
    }

    @Test
    public void testEliminar() {
        System.out.println("TEST DAOIT ELIMINAR");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
            cut.em = em;

            AsignacionesAulaPupitre asignacionTemporal = new AsignacionesAulaPupitre();
            asignacionTemporal.setPupitre("Pupitre Temporal");
            obtenerYAsignarDatos(asignacionTemporal, em);
            cut.crear(asignacionTemporal);

            // Eliminar y verificar
            cut.eliminar(asignacionTemporal);
            AsignacionesAulaPupitre resultadoEliminado = cut.leer(asignacionTemporal.getId());
            assertNull(resultadoEliminado);
            System.out.println("RESULTADO ELIMINADO: Asignación con ID " + asignacionTemporal.getId() + " eliminada correctamente");

            return null;
        });
    }

    @Test
    public void testFindByInscripcionId() {
        System.out.println("TEST DAOIT FIND BY INSCRIPCION ID");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
            cut.em = em;

            // Obtener una inscripción existente para filtrar
            InscripcionesPrueba inscripcion = em.createQuery("Select a from InscripcionesPrueba a", InscripcionesPrueba.class)
                    .setMaxResults(1)
                    .getSingleResult();

            UUID inscripcionId = inscripcion.getId();

            // Ejecutar método de filtro
            List<AsignacionesAulaPupitre> resultado = cut.findByInscripcionId(inscripcionId);

            // Validaciones básicas
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(1, resultado.size());

            // Validación de contenido: verificar que todos pertenecen a la inscripción buscada
            assertTrue(resultado.stream()
                    .allMatch(a -> a.getIdInscripcion() != null && inscripcionId.equals(a.getIdInscripcion().getId())));

            System.out.println("RESULTADO FIND BY INSCRIPCION ID: " + resultado.size() + " registro(s) encontrado(s)");

                // Parámetro nulo debe lanzar IllegalArgumentException
                IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                    () -> cut.findByInscripcionId(null));
                assertEquals("inscripcionId must not be null", iae.getMessage());

                // Error de acceso a BD (em nulo) debe envolver en IllegalStateException
                cut.em = null;
                IllegalStateException ise = assertThrows(IllegalStateException.class,
                    () -> cut.findByInscripcionId(inscripcionId));
                assertEquals("Cannot access db", ise.getMessage());

            return null;
        });
    }

    @Test
    public void testFindByAspiranteId() {
        System.out.println("TEST DAOIT FIND BY ASPIRANTE ID");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
            cut.em = em;

            // Obtener un aspirante existente a través de su inscripción
            InscripcionesPrueba inscripcion = em.createQuery("Select a from InscripcionesPrueba a", InscripcionesPrueba.class)
                    .setMaxResults(1)
                    .getSingleResult();

            UUID aspiranteId = inscripcion.getIdAspirante().getId();

            // Ejecutar método de filtro
            List<AsignacionesAulaPupitre> resultado = cut.findByAspiranteId(aspiranteId);

            // Validaciones básicas
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(1, resultado.size());

            // Validación de contenido: verificar que todas las asignaciones pertenecen al aspirante buscado
            assertTrue(resultado.stream()
                    .allMatch(a -> a.getIdInscripcion() != null &&
                            a.getIdInscripcion().getIdAspirante() != null &&
                            aspiranteId.equals(a.getIdInscripcion().getIdAspirante().getId())));

            System.out.println("RESULTADO FIND BY ASPIRANTE ID: " + resultado.size() + " registro(s) encontrado(s)");

                // Parámetro nulo debe lanzar IllegalArgumentException
                IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                    () -> cut.findByAspiranteId(null));
                assertEquals("aspiranteId must not be null", iae.getMessage());

                // Error de acceso a BD (em nulo) debe envolver en IllegalStateException
                cut.em = null;
                IllegalStateException ise = assertThrows(IllegalStateException.class,
                    () -> cut.findByAspiranteId(aspiranteId));
                assertEquals("Cannot access db", ise.getMessage());

            return null;
        });
    }

}
