package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExamenesRealizadoDAOIT {

    //ID que utilizaremos durante la prueba CRUD
    private static UUID idExamenRealizado;
    private static EntityManagerFactory emf;

    //Contenedor de Docker (Se levanta una vez para toda la clase)
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123");

    // Configuración inicial
    @BeforeAll
    static void inicializar() {
        Integer puertoPostgresql = postgres.getMappedPort(5432);
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("jakarta.persistence.jdbc.url", String.format("jdbc:postgresql://localhost:%d/ingresoTPI135", puertoPostgresql));
        propiedades.put("jakarta.persistence.jdbc.user", "postgres");
        propiedades.put("jakarta.persistence.jdbc.password", "abc123");
        emf = Persistence.createEntityManagerFactory("ingresoPUIT", propiedades);
    }

    public ExamenesRealizadoDAOIT() {}

    @Test
    @Order(1)
    public void testCount() {
        System.out.println("Inicializando TEST COUNT() del DAO ExamenesRealizado");
        assertTrue(postgres.isRunning());

        ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
        EntityManager em = emf.createEntityManager();

        cut.em=em;
        int resultado = cut.count();

        assertEquals(resultado, 2);

    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("Inicializando TEST testFindRange() del DAO ExamenesRealizado");
        ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();

        cut.em=emf.createEntityManager();
        List<ExamenesRealizado> resultado = cut.findRange(0, 2);
        assertNotNull(resultado);
    }

    @Test
    @Order(3)
    public void testCrear() {
        System.out.println("Inicializando TEST testCrear()");
        ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
        EntityManager em = emf.createEntityManager();

        cut.em=em;

        AsignacionesAulaPupitre idAsignacionesAulaPupitre = em.createQuery("SELECT AP FROM AsignacionesAulaPupitre AP", AsignacionesAulaPupitre.class).setMaxResults(1).getSingleResult();
        ClavesExaman idClaveExamen= em.createQuery("SELECT CE from ClavesExaman CE", ClavesExaman.class).setMaxResults(1).getSingleResult();
        EtapasAdmision idEtapaAdmision= em.createQuery("SELECT EA from EtapasAdmision EA", EtapasAdmision.class).setMaxResults(1).getSingleResult();

        ExamenesRealizado nuevoExamen = new ExamenesRealizado();


        nuevoExamen.setIdAsignacion(idAsignacionesAulaPupitre);
        nuevoExamen.setIdClave(idClaveExamen);
        nuevoExamen.setIdEtapa(idEtapaAdmision);
        nuevoExamen.setPuntajeFinal(new BigDecimal("52"));
        nuevoExamen.setFechaRealizacion(OffsetDateTime.now());

        cut.em.getTransaction().begin();
        cut.crear(nuevoExamen);
        cut.em.getTransaction().commit();
        idExamenRealizado=nuevoExamen.getId();
        assertNotNull(idExamenRealizado);

        assertEquals(3, cut.count());

    }

    @Test
    @Order(4)
    public void testLeer() {
        System.out.println("Inicializando TEST testLeer()");
        ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
        cut.em=emf.createEntityManager();
        ExamenesRealizado resultado = cut.leer(idExamenRealizado);

        assertNotNull(resultado,"No debe de ser null porque debe de existir en la BD");

        assertEquals(new BigDecimal(52), resultado.getPuntajeFinal());
        assertNotNull(resultado.getIdEtapa());
        System.out.println(resultado.getIdEtapa());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        System.out.println("Inicializando TEST testUpdate()");
        ExamenesRealizadoDAO cut=new ExamenesRealizadoDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        //Leemos el registro de ExamenRealizado utilizado en este moemnto
        ExamenesRealizado examenesRealizado=cut.leer(idExamenRealizado);
        assertNotNull(examenesRealizado);

        examenesRealizado.setPuntajeFinal(new BigDecimal("30"));

        //GUardar camnois
        em.getTransaction().begin();
        ExamenesRealizado resultado = cut.actualizar(examenesRealizado);
        em.getTransaction().commit();

        //Verificar cambios
        assertEquals(new BigDecimal("30"), resultado.getPuntajeFinal());
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

    }

    @Test
    @Order(6)
    public void testEliminar() {
        System.out.println("Inicializando TEST testEliminar()");
        EntityManager em=emf.createEntityManager();
        ExamenesRealizadoDAO cut=new ExamenesRealizadoDAO();
        cut.em=em;

        ExamenesRealizado examenesRealizado=cut.leer(idExamenRealizado);
        assertNotNull(examenesRealizado);

        em.getTransaction().begin();
        cut.eliminar(examenesRealizado);
        em.getTransaction().commit();

        ExamenesRealizado registroBorrado = cut.leer(idExamenRealizado);
        assertNotNull(registroBorrado,"Registro ya no debe de existir");
        assertEquals(2, cut.count());
    }


}
