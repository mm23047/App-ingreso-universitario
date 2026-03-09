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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExamenesRealizadoDAOIT {

    // UUIDs del init.sql
    private static final UUID ID_INSCRIPCION_1  = UUID.fromString("09000000-0000-0000-0000-000000000001");
    private static final UUID ID_AULA_2         = UUID.fromString("0a000000-0000-0000-0000-000000000002");
    private static final UUID ID_CLAVE_2        = UUID.fromString("08000000-0000-0000-0000-000000000002");
    private static final UUID ID_ETAPA_2        = UUID.fromString("c1000000-0000-0000-0000-000000000002");

    // UUID del examen creado en testCrear — compartido entre tests
    private static UUID idCreado;

    // UUID de la asignación auxiliar creada en testCrear para el prerequisito
    private static UUID idAsignacionAuxiliar;

    // EMF compartido — inicializado una sola vez en @BeforeAll
    private static EntityManagerFactory emf;

    // static → un solo contenedor levantado una vez para toda la clase
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123");

    public ExamenesRealizadoDAOIT() {
    }

    @BeforeAll
    static void inicializar() {
        Integer puertoPostgresql = postgres.getMappedPort(5432);
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("jakarta.persistence.jdbc.url", String.format("jdbc:postgresql://localhost:%d/ingresoTPI135", puertoPostgresql));
        propiedades.put("jakarta.persistence.jdbc.user", "postgres");
        propiedades.put("jakarta.persistence.jdbc.password", "abc123");
        emf = Persistence.createEntityManagerFactory("ingresoPUIT", propiedades);
    }

    @Test
    @Order(1)
    public void testCount() {
        System.out.println("count");
        assertTrue(postgres.isRunning());

        ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();

        // BD recién iniciada con init.sql → 2 exámenes realizados
        assertTrue(resultado > 0);
        assertEquals(2, resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("findRange");
        assertTrue(postgres.isRunning());

        ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
        cut.em = emf.createEntityManager();

        List<ExamenesRealizado> resultado = cut.findRange(0, 10);

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
        ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
        cut.em = em;

        // Crear una nueva asignación de aula como prerequisito
        // (inscripcion 1 + aula 2 con pupitre diferente; no hay UNIQUE constraint en la BD)
        InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, ID_INSCRIPCION_1);
        AulasExaman aula = em.find(AulasExaman.class, ID_AULA_2);
        assertNotNull(inscripcion);
        assertNotNull(aula);

        AsignacionesAulaPupitre nuevaAsignacion = new AsignacionesAulaPupitre();
        nuevaAsignacion.setIdInscripcion(inscripcion);
        nuevaAsignacion.setIdAula(aula);
        nuevaAsignacion.setPupitre("Z-99");

        // Cargar las FKs restantes del examen: clave B y etapa 2
        ClavesExaman clave = em.find(ClavesExaman.class, ID_CLAVE_2);
        EtapasAdmision etapa = em.find(EtapasAdmision.class, ID_ETAPA_2);
        assertNotNull(clave);
        assertNotNull(etapa);

        ExamenesRealizado nuevo = new ExamenesRealizado();
        nuevo.setIdAsignacion(nuevaAsignacion);
        nuevo.setIdClave(clave);
        nuevo.setIdEtapa(etapa);
        // puntajeFinal y fechaRealizacion son opcionales → se dejan en null

        em.getTransaction().begin();
        em.persist(nuevaAsignacion);   // persistir prerequisito primero
        cut.crear(nuevo);
        em.getTransaction().commit();

        idCreado = nuevo.getId();
        idAsignacionAuxiliar = nuevaAsignacion.getId();

        assertNotNull(idCreado);
        assertEquals(3, cut.count());
    }

    @Test
    @Order(4)
    public void testLeer() {
        System.out.println("leer");
        assertTrue(postgres.isRunning());

        ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
        cut.em = emf.createEntityManager();

        // Lee el registro insertado en testCrear usando el UUID almacenado
        ExamenesRealizado resultado = cut.leer(idCreado);

        assertNotNull(resultado);
        assertEquals(ID_CLAVE_2, resultado.getIdClave().getId());
        assertEquals(ID_ETAPA_2, resultado.getIdEtapa().getId());
        assertNull(resultado.getPuntajeFinal());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        System.out.println("actualizar");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
        cut.em = em;

        // Asignar un puntaje final al examen creado en testCrear
        ExamenesRealizado examen = cut.leer(idCreado);
        assertNotNull(examen);
        examen.setPuntajeFinal(new BigDecimal("9.00"));

        em.getTransaction().begin();
        ExamenesRealizado resultado = cut.actualizar(examen);
        em.getTransaction().commit();

        assertNotNull(resultado);
        assertEquals(new BigDecimal("9.00"), resultado.getPuntajeFinal());
        // El conteo no cambia al actualizar → sigue en 3
        assertEquals(3, cut.count());
    }

    @Test
    @Order(6)
    public void testEliminar() {
        System.out.println("eliminar");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        ExamenesRealizadoDAO cut = new ExamenesRealizadoDAO();
        cut.em = em;

        // Elimina el examen creado en testCrear
        ExamenesRealizado examen = cut.leer(idCreado);
        assertNotNull(examen);

        em.getTransaction().begin();
        cut.eliminar(examen);
        // Limpiar también la asignación auxiliar creada en testCrear
        AsignacionesAulaPupitre asignacion = em.find(AsignacionesAulaPupitre.class, idAsignacionAuxiliar);
        if (asignacion != null) {
            em.remove(asignacion);
        }
        em.getTransaction().commit();

        // Vuelve a los 2 registros originales del init.sql
        assertEquals(2, cut.count());
        assertNull(cut.leer(idCreado));
    }
}
