package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BancoPreguntaDAOIT extends AbstractBaseIT {

    //ID a utilizar para las pruebas
    private static UUID ID_PRUEBA;

    public BancoPreguntaDAOIT() {
    }

    @Test
    @Order(1)
    public void testCount() {
        System.out.println("TEST DAOIT COUNT");
        assertTrue(postgres.isRunning());

        BancoPreguntaDAO cut = new BancoPreguntaDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();
        System.out.println("RESULTADO COUNT: " + resultado);
        assertEquals(4, resultado);

    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("TEST DAOIT FIND RANGE");

        BancoPreguntaDAO cut = new BancoPreguntaDAO();
        cut.em = emf.createEntityManager();

        List<BancoPregunta> resultado = cut.findRange(0, 10);
        System.out.println("RESULTADO FIND RANGE: " + resultado);
        assertEquals(4, resultado.size());
    }

    @Test
    @Order(3)
    public void testCrear() {
        System.out.println("TEST DAOIT CREAR");

        BancoPreguntaDAO cut = new BancoPreguntaDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        AreasConocimiento area = em.createQuery("SELECT a FROM AreasConocimiento a", AreasConocimiento.class).setMaxResults(1).getSingleResult();
        assertNotNull(area);


        BancoPregunta nuevoBancoPregunta = new BancoPregunta();

        nuevoBancoPregunta.setEnunciado("¿Cuál es la capital de Francia?");
        nuevoBancoPregunta.setIdArea(area);

        em.getTransaction().begin();
        cut.crear(nuevoBancoPregunta);
        em.getTransaction().commit();

        assertNotNull(nuevoBancoPregunta.getId());
        ID_PRUEBA = nuevoBancoPregunta.getId();
        System.out.println("RESULTADO CREADO: " + ID_PRUEBA);

    }


    @Test
    @Order(4)
    public void testLeer() {
        System.out.println("TEST DAOIT LEER");

        BancoPreguntaDAO cut = new BancoPreguntaDAO();
        cut.em = emf.createEntityManager();

        BancoPregunta resultado = cut.leer(ID_PRUEBA);
        assertNotNull(resultado);
        System.out.println("RESULTADO LEER: " + resultado.getEnunciado());
        assertEquals("¿Cuál es la capital de Francia?", resultado.getEnunciado());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        System.out.println("TEST DAOIT ACTUALIZAR");

        BancoPreguntaDAO cut = new BancoPreguntaDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        BancoPregunta bancoPreguntaExistente = cut.leer(ID_PRUEBA);
        assertNotNull(bancoPreguntaExistente);

        bancoPreguntaExistente.setEnunciado("¿Cuál es la capital de España?");

        em.getTransaction().begin();
        cut.actualizar(bancoPreguntaExistente);
        em.getTransaction().commit();

        BancoPregunta resultado = cut.leer(ID_PRUEBA);
        assertNotNull(resultado);
        if (resultado != null) {
            System.out.println("RESULTADO ACTUALIZADO: " + resultado.getEnunciado());
        } else {
            System.out.println("No se encontró el banco de preguntas con ID: " + ID_PRUEBA);
        }

        assertEquals("¿Cuál es la capital de España?", resultado.getEnunciado());

    }

    @Test
    @Order(6)
    public void testEliminar() {
        System.out.println("TEST DAOIT ELIMINAR");
        BancoPreguntaDAO cut = new BancoPreguntaDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        BancoPregunta bancoPreguntaExistente = cut.leer(ID_PRUEBA);
        assertNotNull(bancoPreguntaExistente);

        em.getTransaction().begin();
        cut.eliminar(bancoPreguntaExistente);
        em.getTransaction().commit();

        BancoPregunta resultado = cut.leer(ID_PRUEBA);
        assertNull(resultado);
        if (resultado == null) {
            System.out.println("Banco de preguntas eliminado correctamente, no se encontró en la base de datos.");
        } else {
            System.out.println("Error: El banco de preguntas aún existe en la base de datos después de intentar eliminarlo.");
        }
        System.out.println("RESULTADO ELIMINADO: " + resultado);
    }
}
