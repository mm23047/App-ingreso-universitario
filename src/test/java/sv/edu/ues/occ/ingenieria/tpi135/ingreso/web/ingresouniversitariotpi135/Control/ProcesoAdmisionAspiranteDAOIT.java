package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId;
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
            nuevaIn.setAspiranteDato(aspirante);
            nuevaIn.setPruebaAdmision(prueba);
            nuevaIn.setEstado("INSCRITO");

            em.persist(nuevaIn);
            em.flush(); // UUID auto-generado disponible después del flush

            ProcesoAdmisionAspirante nuevoProceso = new ProcesoAdmisionAspirante();
            nuevoProceso.setInscripcionesPrueba(nuevaIn);
            nuevoProceso.setIdProcesoAdmisionAspirante(nuevaIn.getIdInscripcionPrueba()); // @MapsId: el PK debe coincidir
            nuevoProceso.setIdEtapaActual(etapa1);
            nuevoProceso.setEstado("EN_PROCESO");

            cut.crear(nuevoProceso);

            assertNotNull(nuevoProceso.getIdProcesoAdmisionAspirante());
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
            assertEquals(ID_ETAPA_1, resultado.getIdEtapaActual().getIdEtapaAdmision());

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
            nuevaIn.setAspiranteDato(aspirante);
            nuevaIn.setPruebaAdmision(prueba);
            nuevaIn.setEstado("INSCRITO");

            em.persist(nuevaIn);
            em.flush();

            ProcesoAdmisionAspirante nuevoProceso = new ProcesoAdmisionAspirante();
            nuevoProceso.setInscripcionesPrueba(nuevaIn);
            nuevoProceso.setIdProcesoAdmisionAspirante(nuevaIn.getIdInscripcionPrueba());
            nuevoProceso.setIdEtapaActual(etapa1);
            nuevoProceso.setEstado("EN_PROCESO");

            cut.crear(nuevoProceso);
            assertEquals(3, cut.count());

            cut.eliminar(nuevoProceso);
            assertEquals(2, cut.count());

            return null;
        });
    }

    @Test
    @Order(7)
    public void testAsignarCarreraFinal_PrioridadYCupos_DebeAsignarPrimeraConCupoYDecrementar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ProcesoAdmisionAspiranteDAO cut = new ProcesoAdmisionAspiranteDAO();
            cut.em = em;

            AspirantesDato aspirante = em.find(AspirantesDato.class, ID_ASPIRANTE_1);
            PruebasAdmision prueba = em.find(PruebasAdmision.class, ID_PRUEBA_2025);
            assertNotNull(aspirante);
            assertNotNull(prueba);

            EtapasAdmision etapaAsignacion = new EtapasAdmision();
            etapaAsignacion.setNombre("Etapa Asignacion Carrera IT");
            em.persist(etapaAsignacion);
            em.flush();

            InscripcionesPrueba inscripcion = new InscripcionesPrueba();
            inscripcion.setAspiranteDato(aspirante);
            inscripcion.setPruebaAdmision(prueba);
            inscripcion.setEstado("INSCRITO");
            em.persist(inscripcion);
            em.flush();

            ProcesoAdmisionAspirante proceso = new ProcesoAdmisionAspirante();
            proceso.setInscripcionesPrueba(inscripcion);
            proceso.setIdProcesoAdmisionAspirante(inscripcion.getIdInscripcionPrueba());
            proceso.setIdEtapaActual(etapaAsignacion);
            proceso.setEstado("EN_PROCESO");
            em.persist(proceso);

            CatalogoCarrera carreraICS = em.find(CatalogoCarrera.class, "ICS");
            CatalogoCarrera carreraISI = em.find(CatalogoCarrera.class, "ISI");
            assertNotNull(carreraICS);
            assertNotNull(carreraISI);

            CarrerasElegidaId pk1 = new CarrerasElegidaId();
            pk1.setIdInscripcion(inscripcion.getIdInscripcionPrueba());
            pk1.setIdCarrera("ICS");
            CarrerasElegida elegida1 = new CarrerasElegida();
            elegida1.setIdCarreraElegida(pk1);
            elegida1.setInscripcionesPrueba(inscripcion);
            elegida1.setCatalogoCarrera(carreraICS);
            elegida1.setPrioridad((short) 1);
            em.persist(elegida1);

            CarrerasElegidaId pk2 = new CarrerasElegidaId();
            pk2.setIdInscripcion(inscripcion.getIdInscripcionPrueba());
            pk2.setIdCarrera("ISI");
            CarrerasElegida elegida2 = new CarrerasElegida();
            elegida2.setIdCarreraElegida(pk2);
            elegida2.setInscripcionesPrueba(inscripcion);
            elegida2.setCatalogoCarrera(carreraISI);
            elegida2.setPrioridad((short) 2);
            em.persist(elegida2);

            CuposCarreraId cuposICSId = new CuposCarreraId();
            cuposICSId.setIdPrueba(prueba.getIdPruebaAdmision());
            cuposICSId.setIdCarrera("ICS");
            cuposICSId.setIdEtapa(etapaAsignacion.getIdEtapaAdmision());
            CuposCarrera cuposICS = new CuposCarrera();
            cuposICS.setIdCupoCarrera(cuposICSId);
            cuposICS.setPruebaAdmision(prueba);
            cuposICS.setCatalogoCarrera(carreraICS);
            cuposICS.setEtapaAdmision(etapaAsignacion);
            cuposICS.setCupos(0);
            em.persist(cuposICS);

            CuposCarreraId cuposISIId = new CuposCarreraId();
            cuposISIId.setIdPrueba(prueba.getIdPruebaAdmision());
            cuposISIId.setIdCarrera("ISI");
            cuposISIId.setIdEtapa(etapaAsignacion.getIdEtapaAdmision());
            CuposCarrera cuposISI = new CuposCarrera();
            cuposISI.setIdCupoCarrera(cuposISIId);
            cuposISI.setPruebaAdmision(prueba);
            cuposISI.setCatalogoCarrera(carreraISI);
            cuposISI.setEtapaAdmision(etapaAsignacion);
            cuposISI.setCupos(2);
            em.persist(cuposISI);

            ProcesoAdmisionAspirante resultado = cut.asignarCarreraFinal(inscripcion.getIdInscripcionPrueba());

            assertNotNull(resultado);
            assertEquals(inscripcion.getIdInscripcionPrueba(), resultado.getIdProcesoAdmisionAspirante());
            assertEquals("ADMITIDO", resultado.getEstado());
            assertNotNull(resultado.getCarreraAsignada());
            assertEquals("ISI", resultado.getCarreraAsignada().getIdCarrera());

            ProcesoAdmisionAspirante desdeBd = em.find(ProcesoAdmisionAspirante.class, inscripcion.getIdInscripcionPrueba());
            assertNotNull(desdeBd);
            assertEquals("ADMITIDO", desdeBd.getEstado());
            assertNotNull(desdeBd.getCarreraAsignada());
            assertEquals("ISI", desdeBd.getCarreraAsignada().getIdCarrera());

            CuposCarrera cuposISIDesdeBd = em.find(CuposCarrera.class, cuposISIId);
            assertNotNull(cuposISIDesdeBd);
            assertEquals(1, cuposISIDesdeBd.getCupos());

            return null;
        });
    }

    @Test
    @Order(8)
    public void testAsignarCarreraFinal_SinCuposDisponibles_DebeMarcarNoAdmitido() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            ProcesoAdmisionAspiranteDAO cut = new ProcesoAdmisionAspiranteDAO();
            cut.em = em;

            AspirantesDato aspirante = em.find(AspirantesDato.class, ID_ASPIRANTE_1);
            PruebasAdmision prueba = em.find(PruebasAdmision.class, ID_PRUEBA_2025);
            assertNotNull(aspirante);
            assertNotNull(prueba);

            EtapasAdmision etapaAsignacion = new EtapasAdmision();
            etapaAsignacion.setNombre("Etapa Asignacion Carrera IT - Sin cupos");
            em.persist(etapaAsignacion);
            em.flush();

            InscripcionesPrueba inscripcion = new InscripcionesPrueba();
            inscripcion.setAspiranteDato(aspirante);
            inscripcion.setPruebaAdmision(prueba);
            inscripcion.setEstado("INSCRITO");
            em.persist(inscripcion);
            em.flush();

            ProcesoAdmisionAspirante proceso = new ProcesoAdmisionAspirante();
            proceso.setInscripcionesPrueba(inscripcion);
            proceso.setIdProcesoAdmisionAspirante(inscripcion.getIdInscripcionPrueba());
            proceso.setIdEtapaActual(etapaAsignacion);
            proceso.setEstado("EN_PROCESO");
            em.persist(proceso);

            CatalogoCarrera carreraICS = em.find(CatalogoCarrera.class, "ICS");
            CatalogoCarrera carreraISI = em.find(CatalogoCarrera.class, "ISI");
            assertNotNull(carreraICS);
            assertNotNull(carreraISI);

            // Prioridad 1: ICS (tendrá cupos=0)
            CarrerasElegidaId pk1 = new CarrerasElegidaId();
            pk1.setIdInscripcion(inscripcion.getIdInscripcionPrueba());
            pk1.setIdCarrera("ICS");
            CarrerasElegida elegida1 = new CarrerasElegida();
            elegida1.setIdCarreraElegida(pk1);
            elegida1.setInscripcionesPrueba(inscripcion);
            elegida1.setCatalogoCarrera(carreraICS);
            elegida1.setPrioridad((short) 1);
            em.persist(elegida1);

            // Prioridad 2: ISI (no tendrá registro de cupos, buscarCupos devolverá null)
            CarrerasElegidaId pk2 = new CarrerasElegidaId();
            pk2.setIdInscripcion(inscripcion.getIdInscripcionPrueba());
            pk2.setIdCarrera("ISI");
            CarrerasElegida elegida2 = new CarrerasElegida();
            elegida2.setIdCarreraElegida(pk2);
            elegida2.setInscripcionesPrueba(inscripcion);
            elegida2.setCatalogoCarrera(carreraISI);
            elegida2.setPrioridad((short) 2);
            em.persist(elegida2);

            // Cupos para ICS en 0
            CuposCarreraId cuposICSId = new CuposCarreraId();
            cuposICSId.setIdPrueba(prueba.getIdPruebaAdmision());
            cuposICSId.setIdCarrera("ICS");
            cuposICSId.setIdEtapa(etapaAsignacion.getIdEtapaAdmision());
            CuposCarrera cuposICS = new CuposCarrera();
            cuposICS.setIdCupoCarrera(cuposICSId);
            cuposICS.setPruebaAdmision(prueba);
            cuposICS.setCatalogoCarrera(carreraICS);
            cuposICS.setEtapaAdmision(etapaAsignacion);
            cuposICS.setCupos(0);
            em.persist(cuposICS);

            ProcesoAdmisionAspirante resultado = cut.asignarCarreraFinal(inscripcion.getIdInscripcionPrueba());

            assertNotNull(resultado);
            assertEquals(inscripcion.getIdInscripcionPrueba(), resultado.getIdProcesoAdmisionAspirante());
            assertEquals("NO_ADMITIDO", resultado.getEstado());
            assertNull(resultado.getCarreraAsignada());

            ProcesoAdmisionAspirante desdeBd = em.find(ProcesoAdmisionAspirante.class, inscripcion.getIdInscripcionPrueba());
            assertNotNull(desdeBd);
            assertEquals("NO_ADMITIDO", desdeBd.getEstado());
            assertNull(desdeBd.getCarreraAsignada());

            // Cupos ICS se mantiene en 0 porque no se consume
            CuposCarrera cuposICSDesdeBd = em.find(CuposCarrera.class, cuposICSId);
            assertNotNull(cuposICSDesdeBd);
            assertEquals(0, cuposICSDesdeBd.getCupos());

            return null;
        });
    }
}
