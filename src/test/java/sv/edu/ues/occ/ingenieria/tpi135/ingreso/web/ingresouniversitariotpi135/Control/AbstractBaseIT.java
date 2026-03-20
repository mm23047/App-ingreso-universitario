package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeAll;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractBaseIT {

    protected static EntityManagerFactory emf;

    protected static SharedPostgresContainer postgres = SharedPostgresContainer.getInstance();

    @BeforeAll
    static void inicializarConfiguracionDocker() {
        Integer puertoPostgresql = postgres.getMappedPort(5432);

        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("jakarta.persistence.jdbc.url",
                String.format("jdbc:postgresql://localhost:%d/ingresoTPI135", puertoPostgresql));
        propiedades.put("jakarta.persistence.jdbc.user", "postgres");
        propiedades.put("jakarta.persistence.jdbc.password", "abc123");

        emf = Persistence.createEntityManagerFactory("ingresoPUIT", propiedades);
    }

    protected <T> T ejecutarEnTransaccion(Function<EntityManager, T> funcion) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            T resultado = funcion.apply(em);

            em.getTransaction().rollback();

            return resultado;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}