package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EtapasAdmisionDAOIT extends AbstractBaseIT {

    private static UUID idEtapasAdmisionActual;

    public EtapasAdmisionDAOIT() {
    }

    @Test
    @Order(1)
    public void testCount() {
        System.out.println("TEST DAOIT COUNT");
        assertTrue(postgres.isRunning());

        EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
        cut.em = emf.createEntityManager();

        int Resultado = cut.count();

        //Tenemos 3 registros en la base de datos
        assertEquals(3, Resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("TEST EtapasAdmision DAOIT FIND RANGE");

        EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
        cut.em = emf.createEntityManager();

        List<EtapasAdmision> resultado = cut.findRange(0, 2);
        System.out.println("RESULTADO: " + resultado);
        assertNotNull(resultado);
    }


    @Test
    @Order(3)
    public void testCrear() {
        System.out.println("TEST EtapasAdmision DAOIT CREAR");
        EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        EtapasAdmision nuevo = new EtapasAdmision();
        nuevo.setNombre("Primer VUELTA");
        nuevo.setPuntajeMaximo(new BigDecimal(100));
        nuevo.setPuntajeMinimo(new BigDecimal(50));
        nuevo.setDescripcion("Primer etapa del examen de ADMISION");

        em.getTransaction().begin();
        cut.crear(nuevo);
        em.getTransaction().commit();

        assertNotNull(nuevo.getId());
        idEtapasAdmisionActual = nuevo.getId();
        System.out.println(nuevo.getId());
    }

    @Test
    @Order(4)
    public void testLeer() {
        System.out.println("TEST EtapasAdmision DAOIT READ");
        EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
        cut.em = emf.createEntityManager();

        EtapasAdmision resultado = cut.leer(idEtapasAdmisionActual);

        System.out.println("RESULTADO: " + resultado);
        assertNotNull(resultado, "El ID del catalogo no puede ser nulo porque ya debe de existir");
        assertEquals("Primer VUELTA", resultado.getNombre());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        System.out.println("TEST EtapasAdmision DAOIT UPDATE");
        EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        EtapasAdmision etapaActualizada = cut.leer(idEtapasAdmisionActual);
        assertNotNull(etapaActualizada, "No se encontró la etapa de admisión con el ID proporcionado");

        //Actualizamos la descripción de la etapa
        etapaActualizada.setDescripcion("Descripción actualizada para la etapa 1");

        em.getTransaction().begin();
        EtapasAdmision resultadp = cut.actualizar(etapaActualizada);
        em.getTransaction().commit();

        assertNotNull(resultadp, "No se encontró la etapa de admisión después de la actualización");
        assertEquals("Descripción actualizada para la etapa 1", resultadp.getDescripcion());
        System.out.println("Etapa actualizada: " + resultadp);


    }

    @Test
    @Order(6)
    public void testEliminar() {
        System.out.println("TEST EtapasAdmision DAOIT DELETE");
        EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        EtapasAdmision etapa = cut.leer(idEtapasAdmisionActual);
        assertNotNull(etapa, "No se encontró la etapa de admisión con el ID proporcionado");

        em.getTransaction().begin();
        cut.eliminar(etapa);
        em.getTransaction().commit();

        EtapasAdmision etapaEliminada = cut.leer(idEtapasAdmisionActual);
        assertNull(etapaEliminada, "Se encontró la etapa de admisión después de la eliminación");
        System.out.println("Etapa eliminada: " + etapaEliminada);
    }
}
