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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CatalogoCarreraDAOIT {

    //Creamos el ID tipo String
    private static String idCatalogo;
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

    public CatalogoCarreraDAOIT() {
    }

    @Test
    @Order(1)
    public void testCount(){
        System.out.println("CatalogoCarreraDAOIT.count()");
        assertTrue(postgres.isRunning());

        CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
        cut.em=emf.createEntityManager();

        int resultado = cut.count();

        assertTrue(resultado>=0);

    }

    @Test
    @Order(2)
    public void testFindRange(){
        System.out.println("CatalogoCarreraDAOIT.findRange()");
        CatalogoCarreraDAO cut= new CatalogoCarreraDAO();

        cut.em=emf.createEntityManager();
        List<CatalogoCarrera> resultado = cut.findRange(0,5);
        assertNotNull(resultado);
    }

    @Test
    @Order(3)
    public void testCrear(){

        System.out.println("CatalogoCarreraDAOIT.create()");
        EntityManager em = emf.createEntityManager();
        CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
        cut.em=em;
        CatalogoCarrera nuevaCarrera = new CatalogoCarrera();

        //EL ID se debe de generar manualmente para poder saber de donde proviene la carrera
        nuevaCarrera.setIdCarrera("INGSO-98");
        nuevaCarrera.setNombre("Ingenieria en Sistemas Informaticos") ;

        //Abrimos transaccion entre ... para ...
        cut.em.getTransaction().begin();
        cut.crear(nuevaCarrera);
        cut.em.getTransaction().commit();

        //Guardamos el ID para las siguientes pruebsa
        idCatalogo = nuevaCarrera.getIdCarrera();

        assertNotNull(idCatalogo);

    }
    @Test
    @Order(4)
    public void testLeer(){
        System.out.println("CatalogoCarreraDAOIT.leer()");
        CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
        cut.em = emf.createEntityManager();

        CatalogoCarrera resultado = cut.leer(idCatalogo);

        assertNotNull(resultado, "El ID del catalogo no puede ser nulo porque ya debe de existir");
        assertEquals("INGSO-98",resultado.getIdCarrera() );
        assertEquals("Ingenieria en Sistemas Informaticos", resultado.getNombre() );

    }
    @Test
    @Order(5)
    public void testActualizar(){
        System.out.println("CatalogoCarreraDAOIT.actualizar()");
        EntityManager em = emf.createEntityManager();
        CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
        cut.em=em;

        //Leemos la carrera que existe actualmente
        CatalogoCarrera catalogoCarrera = cut.leer(idCatalogo);
        assertNotNull(catalogoCarrera);

        //Cambiamos el nombre
        catalogoCarrera.setNombre("Ingenieria en Sistemas Informaticos - 2025");

        //Guardamos cambios
        em.getTransaction().begin();
        CatalogoCarrera resultado=cut.actualizar(catalogoCarrera);
        em.getTransaction().commit();

        //Verificamos cambios
        assertEquals("Ingenieria en Sistemas Informaticos - 2025", resultado.getNombre());

    }

    @Test
    @Order(6)
    public void testEliminar(){
        System.out.println("CatalogoCarreraDAOIT.eliminar()");
        EntityManager em = emf.createEntityManager();
        CatalogoCarreraDAO cut = new CatalogoCarreraDAO();
        cut.em=em;

        CatalogoCarrera carrera = cut.leer(idCatalogo);
        assertNotNull(carrera);

        em.getTransaction().begin();
        cut.eliminar(carrera);
        em.getTransaction().commit();

        //Verficar que ya no existe el registro
        assertNull(cut.leer(idCatalogo), "El registro no debe de existir y su retorno debe ser NULO");


    }

}
