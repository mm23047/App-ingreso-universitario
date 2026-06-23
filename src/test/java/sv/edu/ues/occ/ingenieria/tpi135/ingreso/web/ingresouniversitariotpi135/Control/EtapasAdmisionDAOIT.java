package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EtapasAdmisionDAOIT extends AbstractBaseIT {


    EtapasAdmisionDAOIT() {
    }

    @Test
    void testCount() {
        System.out.println("TEST DAOIT COUNT");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em->{

            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em =em;

            int Resultado = cut.count();

            //Tenemos 5 registros en la base de datos (3 reales + 2 para tests IT)
            assertEquals(5, Resultado);

            return null;
        });

    }

    @Test
    void testFindRange() {
        System.out.println("TEST EtapasAdmision DAOIT FIND RANGE");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em->{

            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            List<EtapasAdmision> resultado = cut.findRange(0, 2);
            System.out.println("RESULTADO: " + resultado);
            assertNotNull(resultado);

            return null;
        });


    }


    @Test
    void testCrear() {
        System.out.println("TEST EtapasAdmision DAOIT CREAR");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em->{

            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            EtapasAdmision nuevo = new EtapasAdmision();
            nuevo.setNombre("Primer VUELTA");
            nuevo.setPuntajeMaximo(new BigDecimal(100));
            nuevo.setPuntajeMinimo(new BigDecimal(50));
            nuevo.setDescripcion("Primer etapa del examen de ADMISION");
            nuevo.setCantidadPreguntasRequeridas(1);

            cut.crear(nuevo);

            assertNotNull(nuevo.getIdEtapaAdmision());
            //Para verificar en la consola
            System.out.println(nuevo.getIdEtapaAdmision());

            return null;
        });

        //Verificamos de no ensuciar la BD
        ejecutarEnTransaccion(em->{
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            //Verificamos la cantidad tal cual el metodo de count (5 iniciales, rollback vuelve a 5)
            assertEquals(cut.count(),5);

            return null;
        });


    }

    @Test
    void testLeer() {
        System.out.println("TEST EtapasAdmision DAOIT READ");

        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em->{

            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            //Utilizamos el primer registro para hacer el test
            EtapasAdmision resultadoDesdeLaBD = cut.findRange(0,3).get(0);

            EtapasAdmision resultado = cut.leer(resultadoDesdeLaBD.getIdEtapaAdmision());

            System.out.println("RESULTADO: " + resultado);
            assertNotNull(resultado, "El ID del catalogo no puede ser nulo porque ya debe de existir");
            assertEquals(resultadoDesdeLaBD.getIdEtapaAdmision(),resultado.getIdEtapaAdmision());

            return null;
        });

    }

    @Test
    void testActualizar() {
        System.out.println("TEST EtapasAdmision DAOIT UPDATE");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em ->{

            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            //Leemos un dato de la bd
            EtapasAdmision etapaActualizada = cut.findRange(0,3).get(0);
            assertNotNull(etapaActualizada, "No se encontró la etapa de admisión con el ID proporcionado");

            //Actualizamos la descripción de la etapa
            etapaActualizada.setDescripcion("Descripción actualizada para la etapa 1");

            EtapasAdmision resultadp = cut.actualizar(etapaActualizada);

            assertNotNull(resultadp, "No se encontró la etapa de admisión después de la actualización");
            assertEquals("Descripción actualizada para la etapa 1", resultadp.getDescripcion());
            System.out.println("Etapa actualizada: " + resultadp);

            return null;
        });



    }

    @Test
    void testEliminar() {
        System.out.println("TEST EtapasAdmision DAOIT DELETE");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em->{

            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            //Creamos un dato TEMPORAL

            EtapasAdmision etapa = new  EtapasAdmision();

            etapa.setNombre("Primer VUELTA");
            etapa.setPuntajeMaximo(new BigDecimal(100));
            etapa.setPuntajeMinimo(new BigDecimal(50));
            etapa.setDescripcion("Primer etapa del examen de ADMISION");
            etapa.setCantidadPreguntasRequeridas(1);
            //GUARDAR la temporal
            cut.crear(etapa);

            //VERIFICAR un dato NUEVO (5 iniciales + 1 temporal = 6)
            assertEquals(cut.count(),6);


            cut.eliminar(etapa);

            assertEquals(cut.count(),5);
            assertNull(cut.leer(etapa.getIdEtapaAdmision()), "EL registro debbe de ser null");
            System.out.println("Etapa eliminada: " + cut.leer(etapa.getIdEtapaAdmision()));

            return null;
        });

    }

    // ===================== NAMED QUERIES =====================

    @Test
    void testFindByNombre() {
        System.out.println("EtapasAdmisionDAOIT.findByNombre()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            EtapasAdmision resultado = cut.findByNombre("Primera Etapa");

            assertNotNull(resultado);
            assertEquals("Primera Etapa", resultado.getNombre());
            assertEquals(new BigDecimal("0.00"), resultado.getPuntajeMinimo());
            assertEquals(20, resultado.getCantidadPreguntasRequeridas());
            return null;
        });
    }

    @Test
    void testFindByNombreNoExiste() {
        System.out.println("EtapasAdmisionDAOIT.findByNombre() - no existe");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            EtapasAdmision resultado = cut.findByNombre("Etapa Inexistente");
            assertNull(resultado, "Debe retornar null si el nombre no existe");
            return null;
        });
    }

    @Test
    void testFindByNombreInvalido() {
        System.out.println("EtapasAdmisionDAOIT.findByNombre() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.findByNombre(null));
            assertThrows(IllegalArgumentException.class, () -> cut.findByNombre(""));
            assertThrows(IllegalArgumentException.class, () -> cut.findByNombre("   "));
            return null;
        });
    }

    @Test
    void testFindEtapasAprobadasPorPuntaje() {
        System.out.println("EtapasAdmisionDAOIT.findEtapasAprobadasPorPuntaje()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            // Puntaje 50 → Primera Etapa (0-59.99) + Inscripcion (0-100) + Asignacion (0-100) = 3
            List<EtapasAdmision> resultado50 = cut.findEtapasAprobadasPorPuntaje(new BigDecimal("50"));
            assertNotNull(resultado50);
            assertEquals(3, resultado50.size());

            // Puntaje 70 → Segunda Etapa (60-79.99) + Inscripcion + Asignacion = 3
            List<EtapasAdmision> resultado70 = cut.findEtapasAprobadasPorPuntaje(new BigDecimal("70"));
            assertNotNull(resultado70);
            assertEquals(3, resultado70.size());

            // Puntaje 90 → Etapa Final (80-100) + Inscripcion + Asignacion = 3
            List<EtapasAdmision> resultado90 = cut.findEtapasAprobadasPorPuntaje(new BigDecimal("90"));
            assertNotNull(resultado90);
            assertEquals(3, resultado90.size());

            // Puntaje 150 → fuera de rango de todas → 0
            List<EtapasAdmision> resultadoFuera = cut.findEtapasAprobadasPorPuntaje(new BigDecimal("150"));
            assertNotNull(resultadoFuera);
            assertTrue(resultadoFuera.isEmpty());

            return null;
        });
    }

    @Test
    void testFindEtapasAprobadasPorPuntajeNulo() {
        System.out.println("EtapasAdmisionDAOIT.findEtapasAprobadasPorPuntaje() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findEtapasAprobadasPorPuntaje(null));
            return null;
        });
    }

    // ===================== VALIDACIONES CREAR =====================

    @Test
    void testCrearNulo() {
        System.out.println("EtapasAdmisionDAOIT.crear() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    void testCrearSinNombre() {
        System.out.println("EtapasAdmisionDAOIT.crear() - sin nombre");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            EtapasAdmision sinNombre = new EtapasAdmision();
            sinNombre.setCantidadPreguntasRequeridas(10);
            sinNombre.setPuntajeMinimo(new BigDecimal("0"));
            sinNombre.setPuntajeMaximo(new BigDecimal("100"));

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinNombre));
            return null;
        });
    }

    @Test
    void testCrearSinCantidadPreguntas() {
        System.out.println("EtapasAdmisionDAOIT.crear() - sin cantidad de preguntas");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            // cantidadPreguntasRequeridas null
            EtapasAdmision sinPreguntas = new EtapasAdmision();
            sinPreguntas.setNombre("Etapa sin preguntas");
            sinPreguntas.setPuntajeMinimo(new BigDecimal("0"));
            sinPreguntas.setPuntajeMaximo(new BigDecimal("100"));

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinPreguntas));

            // cantidadPreguntasRequeridas = 0
            EtapasAdmision ceroPreguntas = new EtapasAdmision();
            ceroPreguntas.setNombre("Etapa cero preguntas");
            ceroPreguntas.setCantidadPreguntasRequeridas(0);

            assertThrows(IllegalArgumentException.class, () -> cut.crear(ceroPreguntas));

            return null;
        });
    }

    @Test
    void testCrearPuntajeMinMayorQueMax() {
        System.out.println("EtapasAdmisionDAOIT.crear() - puntaje min > max");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            EtapasAdmision invalida = new EtapasAdmision();
            invalida.setNombre("Etapa puntaje invertido");
            invalida.setCantidadPreguntasRequeridas(10);
            invalida.setPuntajeMinimo(new BigDecimal("80"));
            invalida.setPuntajeMaximo(new BigDecimal("50"));

            assertThrows(IllegalArgumentException.class, () -> cut.crear(invalida));
            return null;
        });
    }

    @Test
    void testCrearNombreDuplicado() {
        System.out.println("EtapasAdmisionDAOIT.crear() - nombre duplicado");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            EtapasAdmision duplicada = new EtapasAdmision();
            duplicada.setNombre("Primera Etapa");
            duplicada.setCantidadPreguntasRequeridas(5);
            duplicada.setPuntajeMinimo(new BigDecimal("0"));
            duplicada.setPuntajeMaximo(new BigDecimal("100"));

            assertThrows(IllegalArgumentException.class, () -> cut.crear(duplicada));
            return null;
        });
    }

    // ===================== VALIDACIONES ACTUALIZAR =====================

    @Test
    void testActualizarNulo() {
        System.out.println("EtapasAdmisionDAOIT.actualizar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(null));
            return null;
        });
    }

    @Test
    void testActualizarSinId() {
        System.out.println("EtapasAdmisionDAOIT.actualizar() - sin ID");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            EtapasAdmision sinId = new EtapasAdmision();
            sinId.setNombre("Sin ID");
            sinId.setCantidadPreguntasRequeridas(5);

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(sinId));
            return null;
        });
    }

    @Test
    void testActualizarNombreDuplicado() {
        System.out.println("EtapasAdmisionDAOIT.actualizar() - nombre de otra etapa");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            EtapasAdmisionDAO cut = new EtapasAdmisionDAO();
            cut.em = em;

            // Obtener "Segunda Etapa" e intentar cambiarle el nombre a "Primera Etapa"
            EtapasAdmision segunda = cut.findByNombre("Segunda Etapa");
            assertNotNull(segunda);

            segunda.setNombre("Primera Etapa");

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(segunda));
            return null;
        });
    }
}
