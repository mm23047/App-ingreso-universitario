package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PruebasAdmisionDAOIT extends AbstractBaseIT {

    //ID utilizado para el CRUD
    private static UUID idPruebasAdmisionActual;

    public PruebasAdmisionDAOIT() {
    }

    @Test
    @Order(1)
    public void testCount() {
        System.out.println("TEST DAOIT COUNT");
        assertTrue(postgres.isRunning());

        PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
        cut.em = emf.createEntityManager();

        int Resultado = cut.count();

        //Tenemos 2 registros en la base de datos
        assertEquals(2, Resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("TEST PruebasAdmision DAOIT FIND RANGE");

        PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
        cut.em = emf.createEntityManager();

        List<PruebasAdmision> resultado = cut.findRange(0, 2);
        System.out.println("RESULTADO: " + resultado);
        assertNotNull(resultado);
    }

    @Test
    @Order(3)
    public void testCrear() {
        System.out.println("TEST PruebasAdmision DAOIT CREAR");
        PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        PruebasAdmision nuevo = new PruebasAdmision();
        nuevo.setNombrePrueba("PRUEBA DE ADMISION 2025");
        nuevo.setAnio(2025);
        nuevo.setActiva(false);

        em.getTransaction().begin();
        cut.crear(nuevo);
        em.getTransaction().commit();

        assertNotNull(nuevo.getId());
        idPruebasAdmisionActual = nuevo.getId();
        System.out.println(nuevo.getId());
    }

    @Test
    @Order(4)
    public void testLeer() {

        System.out.println("TEST PruebasAdmision DAOIT LEER");
        PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
        cut.em = emf.createEntityManager();

        PruebasAdmision resultado = cut.leer(idPruebasAdmisionActual);

        System.out.println("RESULTADO: " + resultado.toString());
        assertNotNull(resultado, "El ID de la prueba de admisión no puede ser nulo porque ya debe de existir");
        assertEquals("PRUEBA DE ADMISION 2025", resultado.getNombrePrueba());

    }

    @Test
    @Order(5)
    public void testActualizar() {
        System.out.println("TEST PruebasAdmision DAOIT UPDATE");
        PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        PruebasAdmision pruebaActualizada = cut.leer(idPruebasAdmisionActual);
        assertNotNull(pruebaActualizada, "No se encontró la prueba de admisión con el ID proporcionado para actualizar");

        pruebaActualizada.setNombrePrueba("PRUEBA DE ADMISION 2026");
        pruebaActualizada.setActiva(true);
        pruebaActualizada.setAnio(2026);

        em.getTransaction().begin();
        PruebasAdmision resultado = cut.actualizar(pruebaActualizada);
        em.getTransaction().commit();

        assertNotNull(resultado, "No se encontró la prueba de admisión después de la actualización");
        assertTrue(resultado.getActiva(), "La prueba de admisión debería estar activa después de la actualización");
        assertEquals(2026, resultado.getAnio(), "El año de la prueba de admisión debería ser 2026 después de la actualización");
        System.out.println("Prueba actualizada: " + resultado);
    }


    @Test
    @Order(6)
    public void testEliminar() {
        System.out.println("TEST PruebasAdmision DAOIT ELIMINAR");
        PruebasAdmisionDAO cut = new PruebasAdmisionDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        PruebasAdmision pruebaAEliminar = cut.leer(idPruebasAdmisionActual);
        assertNotNull(pruebaAEliminar, "No se encontró la prueba de admisión con el ID proporcionado para eliminar");

        em.getTransaction().begin();
        cut.eliminar(pruebaAEliminar);
        em.getTransaction().commit();

        PruebasAdmision pruebaEliminada = cut.leer(idPruebasAdmisionActual);
        assertNull(pruebaEliminada, "La prueba de admisión debería haber sido eliminada");
        if (pruebaEliminada == null) {
            System.out.println(pruebaEliminada + ": Prueba eliminada correctamente, no se encontró en la base de datos.");
        }
    }


}
