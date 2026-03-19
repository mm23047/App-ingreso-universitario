package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AspirantesDatoDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    // admin (b1...001) no está referenciado en aspirantes_datos → disponible para testCrear
    private static final UUID ID_USUARIO_ADMIN = UUID.fromString("b1000000-0000-0000-0000-000000000001");

    // UUID del aspirante creado en testCrear — compartido entre tests
    private UUID idCreado;

    public AspirantesDatoDAOIT() {
    }

    @BeforeAll
    void inicializar() {
        // La configuracion de postgres y emf se realiza en AbstractBaseIT
    }

    @Test
    @Order(1)
    public void testCount() {
        System.out.println("count");
        assertTrue(postgres.isRunning());

        AspirantesDatoDAO cut = new AspirantesDatoDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();

        // BD recién iniciada con init.sql → 2 aspirantes (jperez, mmartinez)
        assertTrue(resultado > 0);
        assertEquals(2, resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("findRange");
        assertTrue(postgres.isRunning());

        AspirantesDatoDAO cut = new AspirantesDatoDAO();
        cut.em = emf.createEntityManager();

        List<AspirantesDato> resultado = cut.findRange(0, 10);

        // Aún no se ha insertado nada → sigue habiendo 2
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(2, resultado.size());
    }

    @Test
    @Order(3)
    public void testCrear() {
        System.out.println("crear");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        AspirantesDatoDAO cut = new AspirantesDatoDAO();
        cut.em = em;

        // Usar el usuario admin (b1...001) que no tiene aspirante asociado aún
        UsuariosSistema usuario = em.find(UsuariosSistema.class, ID_USUARIO_ADMIN);
        assertNotNull(usuario);

        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setIdUsuario(usuario);
        nuevo.setNombres("Test Aspirante");
        nuevo.setApellidos("Apellido Prueba");
        nuevo.setDui("98765432-1");
        nuevo.setUsaSillaRuedas(false);

        em.getTransaction().begin();
        cut.crear(nuevo);
        em.getTransaction().commit();

        // Guardar el UUID para que testLeer, testActualizar y testEliminar lo usen
        idCreado = nuevo.getId();

        assertNotNull(idCreado);
        assertEquals(3, cut.count());
    }

    @Test
    @Order(4)
    public void testLeer() {
        System.out.println("leer");
        assertTrue(postgres.isRunning());

        AspirantesDatoDAO cut = new AspirantesDatoDAO();
        cut.em = emf.createEntityManager();

        // Lee el registro insertado en testCrear usando el UUID almacenado
        AspirantesDato resultado = cut.leer(idCreado);

        assertNotNull(resultado);
        assertEquals("Test Aspirante", resultado.getNombres());
        assertEquals("Apellido Prueba", resultado.getApellidos());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        System.out.println("actualizar");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        AspirantesDatoDAO cut = new AspirantesDatoDAO();
        cut.em = em;

        // Modifica el registro creado en testCrear
        AspirantesDato aspirante = cut.leer(idCreado);
        assertNotNull(aspirante);
        aspirante.setNombres("Aspirante Actualizado");

        em.getTransaction().begin();
        AspirantesDato resultado = cut.actualizar(aspirante);
        em.getTransaction().commit();

        assertNotNull(resultado);
        assertEquals("Aspirante Actualizado", resultado.getNombres());
        // El conteo no cambia al actualizar → sigue en 3
        assertEquals(3, cut.count());
    }

    @Test
    @Order(6)
    public void testEliminar() {
        System.out.println("eliminar");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        AspirantesDatoDAO cut = new AspirantesDatoDAO();
        cut.em = em;

        // Elimina el registro creado en testCrear
        AspirantesDato aspirante = cut.leer(idCreado);
        assertNotNull(aspirante);

        em.getTransaction().begin();
        cut.eliminar(aspirante);
        em.getTransaction().commit();

        // Vuelve a los 2 registros originales del init.sql
        assertEquals(2, cut.count());
        assertNull(cut.leer(idCreado));
    }
}
