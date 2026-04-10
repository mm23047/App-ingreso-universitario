package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeAll;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// Clase base para las pruebas de integración que usan JPA y PostgreSQL,
// apoyándose en un contenedor PostgreSQL compartido (Singleton).
public abstract class AbstractBaseIT {

    // Fábrica de EntityManager reutilizada por todas las pruebas de integración.
    protected static EntityManagerFactory emf;

    // Contenedor PostgreSQL compartido (Singleton) para toda la suite de pruebas.
    protected static SharedPostgresContainer postgres = SharedPostgresContainer.getInstance();

    @BeforeAll
    static void inicializarConfiguracionDocker() {
        Integer puertoPostgresql = postgres.getMappedPort(5432);

        // Propiedades dinámicas para apuntar a la base de datos del contenedor.
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("jakarta.persistence.jdbc.url",
                String.format("jdbc:postgresql://localhost:%d/ingresoTPI135", puertoPostgresql));
        propiedades.put("jakarta.persistence.jdbc.user", "postgres");
        propiedades.put("jakarta.persistence.jdbc.password", "abc123");

        // Crear la fábrica de EntityManager para la unidad de persistencia de pruebas.
        emf = Persistence.createEntityManagerFactory("ingresoPUIT", propiedades);
    }

    protected <T> T ejecutarEnTransaccion(Function<EntityManager, T> funcion) {
        EntityManager em = emf.createEntityManager();
        try {
            // Iniciar transacción para la operación de prueba.
            em.getTransaction().begin();

            // Ejecutar la lógica de prueba recibida.
            T resultado = funcion.apply(em);

            // Revertir siempre los cambios para no dejar efectos en la base.
            em.getTransaction().rollback();

            return resultado;
        } catch (Exception e) {
            // Asegurar rollback si ocurrió algún error durante la transacción.
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            // Cerrar siempre el EntityManager al finalizar.
            em.close();
        }
    }
}