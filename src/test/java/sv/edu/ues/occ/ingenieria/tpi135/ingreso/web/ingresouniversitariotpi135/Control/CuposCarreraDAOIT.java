package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CuposCarreraDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_PRUEBA_2026  = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_ETAPA_FINAL  = UUID.fromString("c1000000-0000-0000-0000-000000000003");
    private static final UUID ID_ETAPA_1      = UUID.fromString("c1000000-0000-0000-0000-000000000001");

    public CuposCarreraDAOIT() {
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
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql  3 cupos: ICS=50, ISI=60, ICC=45
            assertTrue(resultado > 0);
            assertEquals(3, resultado);

            return null;
        });
    }

    @Test
    @Order(2)
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            List<CuposCarrera> resultado = cut.findRange(0, 10);

            // Aún no se ha insertado nada  sigue habiendo 3
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(3, resultado.size());

            return null;
        });
    }

    @Test
    @Order(3)
    public void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear un cupo temporal y verificarlo dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            // MAT aún no tiene cupos asignados en el init.sql  combinación única
            PruebasAdmision prueba  = em.find(PruebasAdmision.class, ID_PRUEBA_2026);
            CatalogoCarrera carrera = em.find(CatalogoCarrera.class, "MAT");
            EtapasAdmision  etapa   = em.find(EtapasAdmision.class, ID_ETAPA_1);

            CuposCarreraId clave = new CuposCarreraId();
            clave.setIdPrueba(ID_PRUEBA_2026);
            clave.setIdCarrera("MAT");
            clave.setIdEtapa(ID_ETAPA_1);

            CuposCarrera nuevo = new CuposCarrera();
            nuevo.setIdCupoCarrera(clave);
            nuevo.setPruebaAdmision(prueba);
            nuevo.setCatalogoCarrera(carrera);
            nuevo.setEtapaAdmision(etapa);
            nuevo.setCupos(30);

            cut.crear(nuevo);

            // Dentro de la transacción el registro es visible
            assertEquals(4, cut.count());

            return null;
        });

        // Verificar que, tras el rollback implícito, la BD vuelve a 3 registros
        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            assertEquals(3, cut.count());
            return null;
        });
    }

    @Test
    @Order(4)
    public void testLeer() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            // Leer el registro ICS del init.sql: prueba 2026, carrera ICS, etapa final, cupos=50
            CuposCarreraId clave = new CuposCarreraId();
            clave.setIdPrueba(ID_PRUEBA_2026);
            clave.setIdCarrera("ICS");
            clave.setIdEtapa(ID_ETAPA_FINAL);

            CuposCarrera resultado = cut.leer(clave);

            assertNotNull(resultado);
            assertEquals("ICS", resultado.getIdCupoCarrera().getIdCarrera());
            assertEquals(ID_PRUEBA_2026, resultado.getIdCupoCarrera().getIdPrueba());
            assertEquals(ID_ETAPA_FINAL, resultado.getIdCupoCarrera().getIdEtapa());
            assertEquals(50, resultado.getCupos());

            return null;
        });
    }

    @Test
    @Order(5)
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            // Leer ISI (cupos=60) y cambiar a 75
            CuposCarreraId clave = new CuposCarreraId();
            clave.setIdPrueba(ID_PRUEBA_2026);
            clave.setIdCarrera("ISI");
            clave.setIdEtapa(ID_ETAPA_FINAL);

            CuposCarrera cupo = cut.leer(clave);
            cupo.setCupos(75);

            CuposCarrera resultado = cut.actualizar(cupo);

            assertNotNull(resultado);
            assertEquals(75, resultado.getCupos());

            // Dentro de la misma transacción el cambio es visible
            CuposCarrera verificacion = cut.leer(clave);
            assertEquals(75, verificacion.getCupos());

            return null;
        });
    }

    @Test
    @Order(6)
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        // Crear y eliminar un cupo temporal dentro de una única transacción
        ejecutarEnTransaccion(em -> {
            CuposCarreraDAO cut = new CuposCarreraDAO();
            cut.em = em;

            PruebasAdmision prueba  = em.find(PruebasAdmision.class, ID_PRUEBA_2026);
            CatalogoCarrera carrera = em.find(CatalogoCarrera.class, "MAT");
            EtapasAdmision  etapa   = em.find(EtapasAdmision.class, ID_ETAPA_1);

            CuposCarreraId clave = new CuposCarreraId();
            clave.setIdPrueba(ID_PRUEBA_2026);
            clave.setIdCarrera("MAT");
            clave.setIdEtapa(ID_ETAPA_1);

            CuposCarrera cupo = new CuposCarrera();
            cupo.setIdCupoCarrera(clave);
            cupo.setPruebaAdmision(prueba);
            cupo.setCatalogoCarrera(carrera);
            cupo.setEtapaAdmision(etapa);
            cupo.setCupos(30);

            // Crear
            cut.crear(cupo);
            assertEquals(4, cut.count());

            // Eliminar
            cut.eliminar(cupo);
            assertEquals(3, cut.count());

            return null;
        });
    }
}
