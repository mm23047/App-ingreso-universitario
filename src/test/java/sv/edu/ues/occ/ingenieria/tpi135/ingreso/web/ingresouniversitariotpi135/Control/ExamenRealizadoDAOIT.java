package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExamenRealizadoDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_INSCRIPCION_1  = UUID.fromString("ffff1001-1001-1001-1001-000000001001");
    private static final UUID ID_INSCRIPCION_2  = UUID.fromString("ffff1002-1002-1002-1002-000000001002");
    private static final UUID ID_ETAPA_1        = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ID_ETAPA_2        = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID ID_ETAPA_3        = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID ID_CLAVE_1        = UUID.fromString("aaaabbbb-cccc-dddd-eeee-ffffffffffff");

    public ExamenRealizadoDAOIT() {
    }

    @BeforeAll
    void inicializar() {
        // La configuración de postgres y emf se realiza en AbstractBaseIT
    }

    @Test
    @Order(1)
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql → 2 exámenes realizados
            assertTrue(resultado > 0);
            assertEquals(2, resultado);

            return null;
        });
    }

    @Test
    @Order(2)
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            List<ExamenRealizado> resultado = cut.findRange(0, 10);

            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size());

            return null;
        });
    }

    @Test
    @Order(3)
    public void testCrear() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            InscripcionesPrueba inscripcion = em.find(InscripcionesPrueba.class, ID_INSCRIPCION_2);
            EtapasAdmision etapa = em.find(EtapasAdmision.class, ID_ETAPA_1);
            ClavesExamen clave = em.find(ClavesExamen.class, ID_CLAVE_1);

            ExamenRealizado nuevo = new ExamenRealizado();
            nuevo.setInscripcionesPrueba(inscripcion);
            nuevo.setEtapaAdmision(etapa);
            nuevo.setClaveExamen(clave);

            cut.crear(nuevo);

            assertEquals(3, cut.count());

            return null;
        });

        // Verificar rollback
        ejecutarEnTransaccion(em -> {
            ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
            cut.em = em;

            assertEquals(2, cut.count());
            return null;
        });
    }

    @Test
    @Order(4)
    public void testViolacionUniqueConstraintInscripcionEtapa() {
        assertTrue(postgres.isRunning());

        // Intenta crear dos exámenes para la misma inscripción y etapa
        Throwable exception = assertThrows(
            IllegalStateException.class,
                () -> {
                    ejecutarEnTransaccion(em -> {
                        ExamenRealizadoDAO cut = new ExamenRealizadoDAO();
                        cut.em = em;

                        InscripcionesPrueba inscripcion = em.find(
                                InscripcionesPrueba.class,
                                ID_INSCRIPCION_1
                        );
                        EtapasAdmision etapa = em.find(
                                EtapasAdmision.class,
                                ID_ETAPA_2
                        );
                        ClavesExamen clave = em.find(ClavesExamen.class, ID_CLAVE_1);

                        // Primer examen: inscripcion_1 + etapa_2
                        ExamenRealizado examen1 = new ExamenRealizado();
                        examen1.setInscripcionesPrueba(inscripcion);
                        examen1.setEtapaAdmision(etapa);
                        examen1.setClaveExamen(clave);
                        cut.crear(examen1);

                        // Segundo examen: misma inscripcion_1 + misma etapa_2 → VIOLACIÓN
                        ExamenRealizado examen2 = new ExamenRealizado();
                        examen2.setInscripcionesPrueba(inscripcion);
                        examen2.setEtapaAdmision(etapa);
                        examen2.setClaveExamen(clave);
                        cut.crear(examen2);

                        fail("Debería haber lanzado IllegalStateException");

                        return null;
                    });
                }
        );

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Error al ingresar el registro"));
    }
}
