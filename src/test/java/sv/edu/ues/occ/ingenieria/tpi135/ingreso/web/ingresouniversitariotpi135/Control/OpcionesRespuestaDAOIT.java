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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OpcionesRespuestaDAOIT {

    private static EntityManagerFactory emf;
    // ID que vamos a utilizar durante la prueba CRUD
    private static UUID idOpcionRespuesta;

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

    public OpcionesRespuestaDAOIT() {
    }
    
    @Test
    @Order(1)
    public void testCount() {
        System.out.println("Inicializando TEST COUNT() del DAO OpcionesRespuesta");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = em;

        int resultado = cut.count();
        assertEquals(resultado, 10);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("Inicializando TEST testFindRange() del DAO OpcionesRespuesta");
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;
        List<OpcionesRespuesta> resultado = cut.findRange(0, 10);
        assertNotNull(resultado);
        System.out.println("Opciones de respuesta encontradas: " + resultado.size());
        assertTrue(resultado.size() > 0);

    }

    @Test
    @Order(3)
    public void testCreate() {
        System.out.println("Inicializando TEST testCreate() del DAO OpcionesRespuesta");
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        EntityManager em = emf.createEntityManager();
        
        cut.em = em;

        BancoPregunta idBancoPreguntaParaGuardar = em.createQuery("SELECT bp FROM BancoPregunta bp", BancoPregunta.class)
                .setMaxResults(1)
                .getSingleResult();

        OpcionesRespuesta nuevaOpcion = new OpcionesRespuesta();

        nuevaOpcion.setIdPregunta(idBancoPreguntaParaGuardar);
        nuevaOpcion.setTextoOpcion("4");
        nuevaOpcion.setEsCorrecta(true);

        cut.em.getTransaction().begin();
        cut.crear(nuevaOpcion);
        cut.em.getTransaction().commit();

        idOpcionRespuesta = nuevaOpcion.getId();
        assertNotNull(idOpcionRespuesta);

        assertEquals(11, cut.count());
        System.out.println("Cantidad de datos en la BD: "+cut.count());

    }

    @Test
    @Order(4)
    public void testLeer() {
        System.out.println("Inicializando TEST testLeer() del DAO OpcionesRespuesta");
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        OpcionesRespuesta resultado = cut.leer(idOpcionRespuesta);
        assertNotNull(resultado, "No debe de ser null porque debe de existir en la BD");
        
        assertEquals(true, resultado.getEsCorrecta());
        System.out.println("Opcion de respuesta encontrada: " + resultado.getEsCorrecta()+" EL texto de Opcion es: "+resultado.getTextoOpcion());
        assertNotNull(resultado.getIdPregunta(), "La pregunta asociada a la opcion de respuesta no debe de ser null");

    }

    @Test
    @Order(5)
    public void testUpdate() {
        System.out.println("Inicializando TEST testUpdate() del DAO OpcionesRespuesta");
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        OpcionesRespuesta opcionExistente = cut.leer(idOpcionRespuesta);
        assertNotNull(opcionExistente, "No debe de ser null porque debe de existir en la BD");

        // Modificamos los campos que queremos actualizar 
        opcionExistente.setTextoOpcion("5");
        opcionExistente.setEsCorrecta(false);

        // Guardamos los cambios
        cut.em.getTransaction().begin();
        cut.actualizar(opcionExistente);
        cut.em.getTransaction().commit();

        // Verificamos que los cambios se hayan guardado correctamente
        OpcionesRespuesta opcionActualizada = cut.leer(idOpcionRespuesta);
        assertNotNull(opcionActualizada, "No debe de ser null porque debe de existir en la BD");
        assertEquals("5", opcionActualizada.getTextoOpcion());
        assertEquals(false, opcionActualizada.getEsCorrecta());
        System.out.println("Opcion de respuesta actualizada: " + opcionActualizada.getTextoOpcion()+" Es correcta? "+opcionActualizada.getEsCorrecta());

    }
    
    @Test
    @Order(6)
    public void testEliminar() {
        System.out.println("Inicializando TEST testEliminar() del DAO OpcionesRespuesta");
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        OpcionesRespuesta opcionExistente = cut.leer(idOpcionRespuesta);
        assertNotNull(opcionExistente, "No debe de ser null porque debe de existir en la BD");

        // Eliminamos el registro
        cut.em.getTransaction().begin();
        cut.eliminar(opcionExistente);
        cut.em.getTransaction().commit();

        // Verificamos que el registro haya sido eliminado
        OpcionesRespuesta opcionEliminada = cut.leer(idOpcionRespuesta);
        assertNull(opcionEliminada, "Debe de ser null porque el registro fue eliminado");
        if(idOpcionRespuesta!=null){
            System.out.println("Opcion de respuesta eliminada correctamente");}
    }



}