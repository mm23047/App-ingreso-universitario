package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarrerasElegidaDAOIT extends AbstractBaseIT {

    // ID de la carrera elegida creada en testCrear — compartido entre tests
    private CarrerasElegidaId idCreado;

    public CarrerasElegidaDAOIT() {
    }

    @BeforeAll
    void inicializar() {
        // La configuracion de postgres y emf se realiza en AbstractBaseIT
    }

    @Test
    @Order(1)
    public void testCount() {
        assertTrue(postgres.isRunning());

        CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();

        // BD recién iniciada con init.sql  4 carreras elegidas
        assertTrue(resultado > 0);
        assertEquals(4, resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
        cut.em = emf.createEntityManager();

        List<CarrerasElegida> resultado = cut.findRange(0, 10);

        // Aún no se ha insertado nada  sigue habiendo 4
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(4, resultado.size());
    }

    @Test
    @Order(3)
    public void testCrear() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
        cut.em = em;

        // Usar una combinacion inscripcion+carrera que no existe en el init.sql
        // inscripcion 001 ya tiene ICS y ISI → usamos MAT (existe en catalogo)
        UUID idInscripcion = UUID.fromString("09000000-0000-0000-0000-000000000001");
        InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, idInscripcion);
        CatalogoCarrera carrera = em.find(CatalogoCarrera.class, "MAT");

        CarrerasElegidaId clave = new CarrerasElegidaId();
        clave.setIdInscripcion(idInscripcion);
        clave.setIdCarrera("MAT");

        CarrerasElegida nueva = new CarrerasElegida();
        nueva.setId(clave);
        nueva.setIdInscripcion(inscripcion);
        nueva.setIdCarrera(carrera);
        nueva.setPrioridad((short) 3);

        em.getTransaction().begin();
        cut.crear(nueva);
        em.getTransaction().commit();

        // Guardar el ID para que testLeer, testActualizar y testEliminar lo usen
        idCreado = nueva.getId();

        assertEquals(5, cut.count());
    }

    @Test
    @Order(4)
    public void testLeer() {
        assertTrue(postgres.isRunning());

        CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
        cut.em = emf.createEntityManager();

        // Leer la primera carrera elegida del init.sql: inscripcion 001 → ICS, prioridad 1
        CarrerasElegidaId clave = new CarrerasElegidaId();
        clave.setIdInscripcion(UUID.fromString("09000000-0000-0000-0000-000000000001"));
        clave.setIdCarrera("ICS");

        CarrerasElegida resultado = cut.leer(clave);

        assertNotNull(resultado);
        assertEquals("ICS", resultado.getId().getIdCarrera());
        assertEquals(UUID.fromString("09000000-0000-0000-0000-000000000001"),
                resultado.getId().getIdInscripcion());
        assertEquals((short) 1, resultado.getPrioridad());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
        cut.em = em;

        // Leer inscripcion 001 → ISI, prioridad 2 → cambiar prioridad a 5
        CarrerasElegidaId clave = new CarrerasElegidaId();
        clave.setIdInscripcion(UUID.fromString("09000000-0000-0000-0000-000000000001"));
        clave.setIdCarrera("ISI");

        CarrerasElegida elegida = cut.leer(clave);
        elegida.setPrioridad((short) 5);

        em.getTransaction().begin();
        CarrerasElegida resultado = cut.actualizar(elegida);
        em.getTransaction().commit();

        assertNotNull(resultado);
        assertEquals((short) 5, resultado.getPrioridad());

        // Limpiar cache de primer nivel y verificar que el cambio persiste en BD
        em.clear();
        CarrerasElegida verificacion = cut.leer(clave);
        assertEquals((short) 5, verificacion.getPrioridad());
    }

    @Test
    @Order(6)
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        CarrerasElegidaDAO cut = new CarrerasElegidaDAO();
        cut.em = em;

        // Eliminar la carrera elegida creada en testCrear
        CarrerasElegida elegida = cut.leer(idCreado);
        assertNotNull(elegida);

        em.getTransaction().begin();
        cut.eliminar(elegida);
        em.getTransaction().commit();

        // Vuelve a los 4 registros originales del init.sql
        assertEquals(4, cut.count());
        assertNull(cut.leer(idCreado));
    }
}
