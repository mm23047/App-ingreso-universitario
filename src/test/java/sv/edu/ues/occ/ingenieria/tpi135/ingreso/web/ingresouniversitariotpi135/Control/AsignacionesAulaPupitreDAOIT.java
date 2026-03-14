package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AsignacionesAulaPupitreDAOIT extends AbstractBaseIT {
    //ID a utilizar durante el CRUD
    private static UUID idAsginacionesAulaPupitre;

    public AsignacionesAulaPupitreDAOIT() {
    }

    @Test
    @Order(1)
    public void testCount() {
        System.out.println("TEST DAOIT COUNT");
        assertTrue(postgres.isRunning());

        AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();
        assertEquals(2, resultado);
        System.out.println("RESULTADO COUNT: " + resultado);

    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("TEST DAOIT FIND RANGE");

        AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
        cut.em = emf.createEntityManager();

        List<AsignacionesAulaPupitre> resultado = cut.findRange(0, 10);
        assertEquals(2, resultado.size());
        System.out.println("RESULTADO FIND RANGE: " + resultado.size());
    }

    @Test
    @Order(3)
    public void testCrear() {
        System.out.println("TEST DAOIT CREAR");

        AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

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
        em.getTransaction().begin();
        cut.crear(asignacion);
        em.getTransaction().commit();

        //Verificar que se creó el registro

        assertNotNull(asignacion.getId());
        idAsginacionesAulaPupitre = asignacion.getId();
        System.out.println("RESULTADO CREADO: " + idAsginacionesAulaPupitre);

    }


    @Test
    @Order(4)
    public void testLeer() {
        System.out.println("TEST DAOIT LEER");

        //Crear el DAO y asignar el EntityManager
        AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
        cut.em = emf.createEntityManager();

        //Leer el registro creado en el test anterior
        AsignacionesAulaPupitre resultado = cut.leer(idAsginacionesAulaPupitre);
        assertNotNull(resultado);

        //Verificar que los datos leídos sean correctos
        System.out.println("RESULTADO LEER: " + resultado);
        assertEquals("Pupitre 1", resultado.getPupitre());

    }

    @Test
    @Order(5)
    public void testActualizar() {
        System.out.println("TEST DAOIT ACTUALIZAR");

        //Crear el DAO y asignar el EntityManager
        AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        //Leer el registro existente
        AsignacionesAulaPupitre asignacionExistente = cut.leer(idAsginacionesAulaPupitre);
        assertNotNull(asignacionExistente);

        //Modificar los datos
        asignacionExistente.setPupitre("A-03");

        //Persistir los cambios
        em.getTransaction().begin();
        cut.actualizar(asignacionExistente);
        em.getTransaction().commit();

        //Leer nuevamente para verificar la actualización
        AsignacionesAulaPupitre resultadoActualizado = cut.leer(idAsginacionesAulaPupitre);
        assertNotNull(resultadoActualizado);

        //Verificar que los datos actualizados sean correctos
        if (resultadoActualizado != null) {

            System.out.println("RESULTADO ACTUALIZADO: " + resultadoActualizado);
            assertEquals("A-03", resultadoActualizado.getPupitre());
        } else {
            System.out.println("No se encontró la asignación con ID: " + idAsginacionesAulaPupitre);
        }

    }

    @Test
    @Order(6)
    public void testEliminar() {
        System.out.println("TEST DAOIT ELIMINAR");

        //Crear el DAO y asignar el EntityManager
        AsignacionesAulaPupitreDAO cut = new AsignacionesAulaPupitreDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        //Leer el registro existente
        AsignacionesAulaPupitre asignacionExistente = cut.leer(idAsginacionesAulaPupitre);
        assertNotNull(asignacionExistente);

        //Eliminar el registro
        em.getTransaction().begin();
        cut.eliminar(asignacionExistente);
        em.getTransaction().commit();

        //Verificar que el registro se eliminó correctamente
        AsignacionesAulaPupitre resultadoEliminado = cut.leer(idAsginacionesAulaPupitre);
        assertNull(resultadoEliminado);
        if (resultadoEliminado == null) {
            System.out.println("RESULTADO ELIMINADO: " + resultadoEliminado);
            System.out.println("Asignación eliminada correctamente, no se encontró en la base de datos.");
        } else {
            System.out.println("No se pudo eliminar la asignación con ID: " + idAsginacionesAulaPupitre);

        }


    }


}
