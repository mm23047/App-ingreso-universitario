package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InscripcionesPruebaDAOIT extends AbstractBaseIT {

    // UUID de la inscripción creada en testCrear — compartido entre tests
    private UUID idCreado;

    public InscripcionesPruebaDAOIT() {
    }

    @BeforeAll
    void inicializar() {
        // La configuracion de postgres y emf se realiza en AbstractBaseIT
    }

    @Test
    @Order(1)
    public void testCount() {
        assertTrue(postgres.isRunning());

        InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();

        // BD recién iniciada con init.sql  2 inscripciones
        assertTrue(resultado > 0);
        assertEquals(2, resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
        cut.em = emf.createEntityManager();

        List<InscripcionesPrueba> resultado = cut.findRange(0, 10);

        // Aún no se ha insertado nada  sigue habiendo 2
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(2, resultado.size());
    }

    @Test
    @Order(3)
    public void testCrear() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
        cut.em = em;

        // Usar aspirante y prueba existentes del init.sql que no generan conflicto
        AspirantesDato aspirante = em.find(AspirantesDato.class,
                UUID.fromString("e1000000-0000-0000-0000-000000000001"));
        PruebasAdmision prueba = em.find(PruebasAdmision.class,
                UUID.fromString("d1000000-0000-0000-0000-000000000002"));

        InscripcionesPrueba nueva = new InscripcionesPrueba();
        nueva.setIdAspirante(aspirante);
        nueva.setIdPrueba(prueba);
        nueva.setEstado("PENDIENTE");

        em.getTransaction().begin();
        cut.crear(nueva);
        em.getTransaction().commit();

        // Guardar el UUID para que testLeer, testActualizar y testEliminar lo usen
        idCreado = nueva.getId();

        assertNotNull(idCreado);
        assertEquals(3, cut.count());
    }

    @Test
    @Order(4)
    public void testLeer() {
        assertTrue(postgres.isRunning());

        InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
        cut.em = emf.createEntityManager();

        UUID idExistente = UUID.fromString("09000000-0000-0000-0000-000000000001");
        InscripcionesPrueba resultado = cut.leer(idExistente);

        assertNotNull(resultado);
        assertEquals(idExistente, resultado.getId());
        assertEquals("INSCRITO", resultado.getEstado());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
        cut.em = em;

        UUID idExistente = UUID.fromString("09000000-0000-0000-0000-000000000001");
        InscripcionesPrueba inscripcion = cut.leer(idExistente);
        inscripcion.setEstado("PROCESADO");

        em.getTransaction().begin();
        InscripcionesPrueba resultado = cut.actualizar(inscripcion);
        em.getTransaction().commit();

        assertNotNull(resultado);
        assertEquals("PROCESADO", resultado.getEstado());

        // Limpiar cache de primer nivel para forzar consulta real a BD
        em.clear();
        InscripcionesPrueba verificacion = cut.leer(idExistente);
        assertEquals("PROCESADO", verificacion.getEstado());
    }

    @Test
    @Order(6)
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
        cut.em = em;

        // Eliminar la inscripción creada en testCrear (sin hijos en otras tablas)
        InscripcionesPrueba inscripcion = cut.leer(idCreado);
        assertNotNull(inscripcion);

        em.getTransaction().begin();
        cut.eliminar(inscripcion);
        em.getTransaction().commit();

        // Vuelve a los 2 registros originales del init.sql
        assertEquals(2, cut.count());
        assertNull(cut.leer(idCreado));
    }
}
