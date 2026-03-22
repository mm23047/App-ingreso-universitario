package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;


import org.junit.jupiter.api.Test;

import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AsignacionesAulaPupitreDAOIT extends AbstractBaseIT {


    public AsignacionesAulaPupitreDAOIT() {
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

            //Necesitamos estos registros para poder guardar una nueva asignacion de pupitre
            InscripcionesPrueba registroInscripcionesPrueba = em.createQuery("Select a from InscripcionesPrueba a", InscripcionesPrueba.class)
                    .setMaxResults(1)
                    .getSingleResult();
            AulasExaman registroAulasExaman = em.createQuery("Select a from AulasExaman a", AulasExaman.class)
                    .setMaxResults(1)
                    .getSingleResult();

            //Crear la entidad a insertar
            AsignacionesAulaPupitre asignacion = new AsignacionesAulaPupitre();

            //Insertar datos necesarios para las relaciones
            asignacion.setIdInscripcion(registroInscripcionesPrueba);
            asignacion.setIdAula(registroAulasExaman);
            asignacion.setPupitre("Pupitre 1");

            //Persistir la entidad
            cut.crear(asignacion);

            //Verificar que se creó el registro
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

            //Leer un registro que ya exista en la BD
            AsignacionesAulaPupitre asignacionExistente = cut.findRange(0, 1).get(0);

            AsignacionesAulaPupitre resultado = cut.leer(asignacionExistente.getId());
            assertNotNull(resultado);

            //Verificar que los datos leídos sean correctos
            System.out.println("RESULTADO LEER: " + resultado.getPupitre());
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

            //Leer el registro existente
            AsignacionesAulaPupitre asignacionExistente = cut.findRange(0, 1).get(0);
            assertNotNull(asignacionExistente);

            //Modificar los datos
            asignacionExistente.setPupitre("A-03");

            //Persistir los cambios
            AsignacionesAulaPupitre resultadoActualizado = cut.actualizar(asignacionExistente);
            assertNotNull(resultadoActualizado);

            //Verificar que los datos actualizados sean correctos
            if (resultadoActualizado != null) {
                System.out.println("RESULTADO ACTUALIZADO: " + resultadoActualizado.getPupitre());
                assertEquals("A-03", resultadoActualizado.getPupitre());
            } else {
                System.out.println("No se encontró la asignación tras actualizarla");
            }

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

            // Necesitamos las relaciones para crear el temporal
            InscripcionesPrueba registroInscripcionesPrueba = em.createQuery("Select a from InscripcionesPrueba a", InscripcionesPrueba.class)
                    .setMaxResults(1)
                    .getSingleResult();
            AulasExaman registroAulasExaman = em.createQuery("Select a from AulasExaman a", AulasExaman.class)
                    .setMaxResults(1)
                    .getSingleResult();

            // Crear dato temporal
            AsignacionesAulaPupitre asignacionTemporal = new AsignacionesAulaPupitre();
            asignacionTemporal.setIdInscripcion(registroInscripcionesPrueba);
            asignacionTemporal.setIdAula(registroAulasExaman);
            asignacionTemporal.setPupitre("Pupitre Temporal");
            cut.crear(asignacionTemporal);

            // Eliminar el registro
            cut.eliminar(asignacionTemporal);

            // Verificar que el registro se eliminó correctamente
            AsignacionesAulaPupitre resultadoEliminado = cut.leer(asignacionTemporal.getId());
            assertNull(resultadoEliminado);

            if (resultadoEliminado == null) {
                System.out.println("RESULTADO ELIMINADO: " + resultadoEliminado);
                System.out.println("Asignación eliminada correctamente, no se encontró en la base de datos.");
            } else {
                System.out.println("No se pudo eliminar la asignación con ID: " + asignacionTemporal.getId());
            }

            return null;
        });
    }


}
