package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ProcesoAdmisionAspirante;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProcesoAdmisionAspiranteDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_PROCESO_1   = UUID.fromString("09000000-0000-0000-0000-000000000001");
    private static final UUID ID_ASPIRANTE_1 = UUID.fromString("e1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_2025 = UUID.fromString("d1000000-0000-0000-0000-000000000002");
    private static final UUID ID_ETAPA_1     = UUID.fromString("c1000000-0000-0000-0000-000000000001");

    public ProcesoAdmisionAspiranteDAOIT() {
    }

    @BeforeAll
    void inicializar() {
        // La configuracion de postgres y emf se realiza en AbstractBaseIT
    }

    @Test
    @Order(1)
    public void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ProcesoAdmisionAspiranteDAO cut = new ProcesoAdmisionAspiranteDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql  2 registros: ambas inscripciones EN_PROCESO en etapa1
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
            ProcesoAdmisionAspiranteDAO cut = new ProcesoAdmisionAspiranteDAO();
            cut.em = em;

            List<ProcesoAdmisionAspirante> resultado = cut.findRange(0, 10);

            // Aún no se ha insertado nada  sigue habiendo 2
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

        // Crear un proceso de admisión temporal y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            ProcesoAdmisionAspiranteDAO cut = new ProcesoAdmisionAspiranteDAO();
            cut.em = em;

            // Las dos inscripciones del init.sql ya tienen proceso asignado.
            // Se crea una nueva InscripcionesPrueba (aspirante1 + prueba2025) para tener un PK libre.
            AspirantesDato aspirante = em.find(AspirantesDato.class, ID_ASPIRANTE_1);
            PruebasAdmision prueba   = em.find(PruebasAdmision.class, ID_PRUEBA_2025);
            EtapasAdmision etapa1    = em.find(EtapasAdmision.class, ID_ETAPA_1);

            InscripcionesPrueba nuevaIn = new InscripcionesPrueba();
            nuevaIn.setIdAspirante(aspirante);
            nuevaIn.setIdPrueba(prueba);
            nuevaIn.setEstado("INSCRITO");

            em.persist(nuevaIn);
            em.flush(); // UUID auto-generado disponible después del flush

            ProcesoAdmisionAspirante nuevoProceso = new ProcesoAdmisionAspirante();
            nuevoProceso.setInscripcionesPrueba(nuevaIn);
            nuevoProceso.setId(nuevaIn.getId()); // @MapsId: el PK debe coincidir
            nuevoProceso.setIdEtapaActual(etapa1);
            nuevoProceso.setEstado("EN_PROCESO");

            cut.crear(nuevoProceso);

            assertNotNull(nuevoProceso.getId());
            assertEquals(3, cut.count());

            return null;
        });

        // Verificar que después del rollback implícito la BD queda con 2 registros
        ejecutarEnTransaccion(em -> {
            ProcesoAdmisionAspiranteDAO cut = new ProcesoAdmisionAspiranteDAO();
            cut.em = em;

            assertEquals(2, cut.count());
            return null;
        });
    }

    @Test
    @Order(4)
    public void testLeer() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ProcesoAdmisionAspiranteDAO cut = new ProcesoAdmisionAspiranteDAO();
            cut.em = em;

            // Leer primer registro del init.sql: inscripcion1, etapa1, estado EN_PROCESO
            ProcesoAdmisionAspirante resultado = cut.leer(ID_PROCESO_1);

            assertNotNull(resultado);
            assertEquals("EN_PROCESO", resultado.getEstado());
            assertEquals(ID_ETAPA_1, resultado.getIdEtapaActual().getId());

            return null;
        });
    }

    @Test
    @Order(5)
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ProcesoAdmisionAspiranteDAO cut = new ProcesoAdmisionAspiranteDAO();
            cut.em = em;

            // Cambiar estado de EN_PROCESO a ADMITIDO
            ProcesoAdmisionAspirante proceso = cut.leer(ID_PROCESO_1);
            assertNotNull(proceso);
            assertEquals("EN_PROCESO", proceso.getEstado());

            proceso.setEstado("ADMITIDO");

            ProcesoAdmisionAspirante actualizado = cut.actualizar(proceso);

            assertNotNull(actualizado);
            assertEquals("ADMITIDO", actualizado.getEstado());

            return null;
        });
    }

    @Test
    @Order(6)
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        // Crear y eliminar un proceso de admisión temporal dentro de una única transacción
        ejecutarEnTransaccion(em -> {
            ProcesoAdmisionAspiranteDAO cut = new ProcesoAdmisionAspiranteDAO();
            cut.em = em;

            AspirantesDato aspirante = em.find(AspirantesDato.class, ID_ASPIRANTE_1);
            PruebasAdmision prueba   = em.find(PruebasAdmision.class, ID_PRUEBA_2025);
            EtapasAdmision etapa1    = em.find(EtapasAdmision.class, ID_ETAPA_1);

            InscripcionesPrueba nuevaIn = new InscripcionesPrueba();
            nuevaIn.setIdAspirante(aspirante);
            nuevaIn.setIdPrueba(prueba);
            nuevaIn.setEstado("INSCRITO");

            em.persist(nuevaIn);
            em.flush();

            ProcesoAdmisionAspirante nuevoProceso = new ProcesoAdmisionAspirante();
            nuevoProceso.setInscripcionesPrueba(nuevaIn);
            nuevoProceso.setId(nuevaIn.getId());
            nuevoProceso.setIdEtapaActual(etapa1);
            nuevoProceso.setEstado("EN_PROCESO");

            cut.crear(nuevoProceso);
            assertEquals(3, cut.count());

            cut.eliminar(nuevoProceso);
            assertEquals(2, cut.count());

            return null;
        });
    }
}
