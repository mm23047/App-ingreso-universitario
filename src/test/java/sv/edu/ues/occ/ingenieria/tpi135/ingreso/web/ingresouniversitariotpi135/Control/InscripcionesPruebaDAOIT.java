package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InscripcionesPruebaDAOIT extends AbstractBaseIT {

    InscripcionesPruebaDAOIT() {
    }

    @BeforeAll
    void inicializar() {
        // La configuracion de postgres y emf se realiza en AbstractBaseIT
    }

    @Test
    @Order(1)
    void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql  4 inscripciones (2 para tests base + 2 para ExamenRealizado/Asignacion)
            assertTrue(resultado > 0);
            assertEquals(4, resultado);

            return null;
        });
    }

    @Test
    @Order(2)
    void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            List<InscripcionesPrueba> resultado = cut.findRange(0, 10);

            // Aún no se ha insertado nada  sigue habiendo 4
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(4, resultado.size());

            return null;
        });
    }

    @Test
    @Order(3)
    void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear una inscripción temporal y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            // Usar aspirante y prueba existentes del init.sql que no generan conflicto
            AspirantesDato aspirante = em.find(AspirantesDato.class,
                UUID.fromString("e1000000-0000-0000-0000-000000000001"));
            PruebasAdmision prueba = em.find(PruebasAdmision.class,
                UUID.fromString("d1000000-0000-0000-0000-000000000002"));

            InscripcionesPrueba nueva = new InscripcionesPrueba();
            nueva.setAspiranteDato(aspirante);
            nueva.setPruebaAdmision(prueba);
            nueva.setEstado("PENDIENTE");

            cut.crear(nueva);

            assertNotNull(nueva.getIdInscripcionPrueba());
            assertEquals(5, cut.count());

            return null;
        });

        // Verificar que después del rollback implícito la BD queda con 4 inscripciones
        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            assertEquals(4, cut.count());
            return null;
        });
    }

    @Test
    @Order(4)
    void testLeer() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            UUID idExistente = UUID.fromString("09000000-0000-0000-0000-000000000001");
            InscripcionesPrueba resultado = cut.leer(idExistente);

            assertNotNull(resultado);
            assertEquals(idExistente, resultado.getIdInscripcionPrueba());
            assertEquals("INSCRITO", resultado.getEstado());

            return null;
        });
    }

    @Test
    @Order(5)
    void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            UUID idExistente = UUID.fromString("09000000-0000-0000-0000-000000000001");
            InscripcionesPrueba inscripcion = cut.leer(idExistente);
            inscripcion.setEstado("PROCESADO");

            InscripcionesPrueba resultado = cut.actualizar(inscripcion);

            assertNotNull(resultado);
            assertEquals("PROCESADO", resultado.getEstado());

            // Dentro de la misma transacción el cambio es visible
            InscripcionesPrueba verificacion = cut.leer(idExistente);
            assertEquals("PROCESADO", verificacion.getEstado());

            return null;
        });
    }

    @Test
    @Order(6)
    void testEliminar() {
        assertTrue(postgres.isRunning());

        // Crear y eliminar una inscripción temporal dentro de una única transacción
        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            AspirantesDato aspirante = em.find(AspirantesDato.class,
                    UUID.fromString("e1000000-0000-0000-0000-000000000001"));
            PruebasAdmision prueba = em.find(PruebasAdmision.class,
                    UUID.fromString("d1000000-0000-0000-0000-000000000002"));

            InscripcionesPrueba nueva = new InscripcionesPrueba();
            nueva.setAspiranteDato(aspirante);
            nueva.setPruebaAdmision(prueba);
            nueva.setEstado("PENDIENTE");

            cut.crear(nueva);
            assertEquals(5, cut.count());

            cut.eliminar(nueva);
            assertEquals(4, cut.count());

            return null;
        });
    }

    @Test
    @Order(7)
    void testFindByAspiranteId() {
        assertTrue(postgres.isRunning());
        System.out.println("Test FindByAspiranteId");

        ejecutarEnTransaccion(em ->{

            //Camino FELIZ!!!
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;
            UUID idAspirante = UUID.fromString("09000000-0000-0000-0000-000000000001");

            List<InscripcionesPrueba> resultado = cut.findByAspiranteId(idAspirante);
            assertNotNull(resultado);
            //FIN del camino FELIZ!!!

            //Probas un ID NULL
            IllegalArgumentException argumentException = assertThrows(IllegalArgumentException.class, () ->{
                cut.findByAspiranteId(null);
            });
            assertEquals(argumentException.getMessage(), "El identificador del aspirante es obligatorio.");

            //Probando el CATCH
            cut.em=null;
            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->{
                cut.findByAspiranteId(idAspirante);
            });
            assertEquals(illegalStateException.getMessage(), "Error de infraestructura al obtener inscripciones por aspirante.");

            return null;
        });

    }

    @Test
    @Order(8)
    void testFindByPruebaId() {
        assertTrue(postgres.isRunning());
        System.out.println("Test FindByPruebaId");

        ejecutarEnTransaccion(em->{
            //Probando el camino FELIZ!!!
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            UUID idPrueba = UUID.fromString("d1000000-0000-0000-0000-000000000002");
            List<InscripcionesPrueba>  resultado = cut.findByPruebaId(idPrueba);
            assertNotNull(resultado);
            //FIN del camino FELIZ!!!

            //Probas un ID NULL
            IllegalArgumentException argumentException = assertThrows(IllegalArgumentException.class, () ->{
                cut.findByPruebaId(null);
            });
            assertEquals(argumentException.getMessage(), "El identificador de la prueba es obligatorio.");

            //Probando el CATCH
            cut.em=null;
            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () ->{
                cut.findByPruebaId(idPrueba);
            });
            assertEquals(illegalStateException.getMessage(), "Error de infraestructura al obtener inscripciones por prueba.");


            return null;
        });
    }

    @Test
    @Order(9)
    void testExistsByAspiranteAndPrueba() {
        assertTrue(postgres.isRunning());
        System.out.println("Test ExistsByAspiranteAndPrueba");

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            // Juan Carlos (Aspirante 1)
            UUID idAspirante = UUID.fromString("e1000000-0000-0000-0000-000000000001");
            // Prueba 2026 En la que está inscrito
            UUID idPruebaInscrita = UUID.fromString("d1000000-0000-0000-0000-000000000001");
            // Prueba 2025 (En la que NO está inscrito)
            UUID idPruebaNoInscrita = UUID.fromString("d1000000-0000-0000-0000-000000000002");


            // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            // CAMINO FELIZ !!!

            // Probamos que devuelva TRUE si la combinación existe
            boolean existe = cut.existsByAspiranteAndPrueba(idAspirante, idPruebaInscrita);
            assertTrue(existe, "El aspirante SI debería estar inscrito en esta prueba");

            // Probamos que devuelva FALSE si la combinación no existe
            boolean noExiste = cut.existsByAspiranteAndPrueba(idAspirante, idPruebaNoInscrita);
            assertFalse(noExiste, "El aspirante NO debería estar inscrito en esta prueba");

            // FIN del camino FELIZ!!!

            // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
            // CAMINOS DE ERROR !!!

            // Probar ID de aspirante NULL
            IllegalArgumentException argEx1 = assertThrows(IllegalArgumentException.class, () -> {
                cut.existsByAspiranteAndPrueba(null, idPruebaInscrita);
            });
            assertEquals("Los identificadores de aspirante y prueba son estrictamente mandatorios.", argEx1.getMessage());

            // Probar ID de prueba NULL
            IllegalArgumentException argEx2 = assertThrows(IllegalArgumentException.class, () -> {
                cut.existsByAspiranteAndPrueba(idAspirante, null);
            });
            assertEquals("Los identificadores de aspirante y prueba son estrictamente mandatorios.", argEx2.getMessage());

            // Probanmos el CATCH sin conexión a la BD
            cut.em = null;
            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () -> {
                cut.existsByAspiranteAndPrueba(idAspirante, idPruebaInscrita);
            });
            assertEquals("Error al verificar la existencia previa de la inscripción.", illegalStateException.getMessage());

            return null;
        });
    }

    // ===================== CRUD FALTANTE =====================

    @Test
    @Order(10)
    void testLeerNoExiste() {
        System.out.println("InscripcionesPruebaDAOIT.leer() - ID inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            InscripcionesPrueba resultado = cut.leer(UUID.randomUUID());
            assertNull(resultado, "Debe retornar null si el ID no existe");
            return null;
        });
    }

    // ===================== NAMED QUERIES - COMPLEMENTO =====================

    @Test
    @Order(11)
    void testFindByAspiranteIdConResultados() {
        System.out.println("InscripcionesPruebaDAOIT.findByAspiranteId() - verificar conteo");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            // Aspirante e1000000 tiene 1 inscripcion en prueba d1...001
            UUID idAspirante = UUID.fromString("e1000000-0000-0000-0000-000000000001");
            List<InscripcionesPrueba> resultado = cut.findByAspiranteId(idAspirante);
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("INSCRITO", resultado.get(0).getEstado());

            // Aspirante inexistente -> vacio
            List<InscripcionesPrueba> vacio = cut.findByAspiranteId(UUID.randomUUID());
            assertNotNull(vacio);
            assertTrue(vacio.isEmpty());

            return null;
        });
    }

    @Test
    @Order(12)
    void testFindByPruebaIdConResultados() {
        System.out.println("InscripcionesPruebaDAOIT.findByPruebaId() - verificar conteo");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            // Prueba d1...001 (Prueba Test A) tiene 2 inscripciones
            UUID idPruebaA = UUID.fromString("d1000000-0000-0000-0000-000000000001");
            List<InscripcionesPrueba> resultadoA = cut.findByPruebaId(idPruebaA);
            assertNotNull(resultadoA);
            assertEquals(2, resultadoA.size());

            // Prueba dddd (Prueba Nacional UES) tiene 2 inscripciones
            UUID idPruebaNacional = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
            List<InscripcionesPrueba> resultadoN = cut.findByPruebaId(idPruebaNacional);
            assertNotNull(resultadoN);
            assertEquals(2, resultadoN.size());

            // Prueba inexistente -> vacio
            List<InscripcionesPrueba> vacio = cut.findByPruebaId(UUID.randomUUID());
            assertNotNull(vacio);
            assertTrue(vacio.isEmpty());

            return null;
        });
    }

    @Test
    @Order(13)
    void testExistsByAspiranteAndPruebaExcludingId() {
        System.out.println("InscripcionesPruebaDAOIT.existsByAspiranteAndPruebaExcludingId()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            UUID idAspirante = UUID.fromString("e1000000-0000-0000-0000-000000000001");
            UUID idPrueba = UUID.fromString("d1000000-0000-0000-0000-000000000001");
            UUID idInscripcion = UUID.fromString("09000000-0000-0000-0000-000000000001");

            // Excluyendo su propio ID -> false (no hay otro duplicado)
            assertFalse(cut.existsByAspiranteAndPruebaExcludingId(idAspirante, idPrueba, idInscripcion));

            // Excluyendo un ID diferente -> true (la inscripcion 09...001 aparece como duplicado)
            assertTrue(cut.existsByAspiranteAndPruebaExcludingId(idAspirante, idPrueba, UUID.randomUUID()));

            return null;
        });
    }

    @Test
    @Order(14)
    void testExistsByAspiranteAndPruebaExcludingIdNulos() {
        System.out.println("InscripcionesPruebaDAOIT.existsByAspiranteAndPruebaExcludingId() - nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            UUID id = UUID.randomUUID();

            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByAspiranteAndPruebaExcludingId(null, id, id));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByAspiranteAndPruebaExcludingId(id, null, id));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByAspiranteAndPruebaExcludingId(id, id, null));
            return null;
        });
    }

    @Test
    @Order(15)
    void testFindByPruebaAndEstado() {
        System.out.println("InscripcionesPruebaDAOIT.findByPruebaAndEstado()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            // Prueba d1...001 con estado INSCRITO -> 2 resultados
            UUID idPrueba = UUID.fromString("d1000000-0000-0000-0000-000000000001");
            List<InscripcionesPrueba> resultado = cut.findByPruebaAndEstado(idPrueba, "INSCRITO");
            assertNotNull(resultado);
            assertEquals(2, resultado.size());

            return null;
        });
    }

    @Test
    @Order(16)
    void testFindByPruebaAndEstadoSinResultados() {
        System.out.println("InscripcionesPruebaDAOIT.findByPruebaAndEstado() - estado sin match");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            UUID idPrueba = UUID.fromString("d1000000-0000-0000-0000-000000000001");
            List<InscripcionesPrueba> resultado = cut.findByPruebaAndEstado(idPrueba, "RECHAZADO");
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());

            return null;
        });
    }

    @Test
    @Order(17)
    void testFindByPruebaAndEstadoInvalido() {
        System.out.println("InscripcionesPruebaDAOIT.findByPruebaAndEstado() - parametros invalidos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            UUID idPrueba = UUID.fromString("d1000000-0000-0000-0000-000000000001");

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByPruebaAndEstado(null, "INSCRITO"));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByPruebaAndEstado(idPrueba, null));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByPruebaAndEstado(idPrueba, ""));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByPruebaAndEstado(idPrueba, "   "));
            return null;
        });
    }

    // ===================== VALIDACIONES CREAR =====================

    @Test
    @Order(18)
    void testCrearNulo() {
        System.out.println("InscripcionesPruebaDAOIT.crear() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.crear(null));
            return null;
        });
    }

    @Test
    @Order(19)
    void testCrearSinAspirante() {
        System.out.println("InscripcionesPruebaDAOIT.crear() - sin aspirante");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            PruebasAdmision prueba = em.find(PruebasAdmision.class,
                    UUID.fromString("d1000000-0000-0000-0000-000000000002"));

            InscripcionesPrueba sinAspirante = new InscripcionesPrueba();
            sinAspirante.setPruebaAdmision(prueba);
            sinAspirante.setEstado("INSCRITO");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinAspirante));
            return null;
        });
    }

    @Test
    @Order(20)
    void testCrearSinPrueba() {
        System.out.println("InscripcionesPruebaDAOIT.crear() - sin prueba");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            AspirantesDato aspirante = em.find(AspirantesDato.class,
                    UUID.fromString("e1000000-0000-0000-0000-000000000001"));

            InscripcionesPrueba sinPrueba = new InscripcionesPrueba();
            sinPrueba.setAspiranteDato(aspirante);
            sinPrueba.setEstado("INSCRITO");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(sinPrueba));
            return null;
        });
    }

    @Test
    @Order(21)
    void testCrearDuplicadoAspirantePrueba() {
        System.out.println("InscripcionesPruebaDAOIT.crear() - duplicado aspirante+prueba");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            // e1000000 ya esta inscrito en d1...001 -> duplicado
            AspirantesDato aspirante = em.find(AspirantesDato.class,
                    UUID.fromString("e1000000-0000-0000-0000-000000000001"));
            PruebasAdmision prueba = em.find(PruebasAdmision.class,
                    UUID.fromString("d1000000-0000-0000-0000-000000000001"));

            InscripcionesPrueba duplicada = new InscripcionesPrueba();
            duplicada.setAspiranteDato(aspirante);
            duplicada.setPruebaAdmision(prueba);
            duplicada.setEstado("INSCRITO");

            assertThrows(IllegalArgumentException.class, () -> cut.crear(duplicada));
            return null;
        });
    }

    // ===================== VALIDACIONES ACTUALIZAR =====================

    @Test
    @Order(22)
    void testActualizarNulo() {
        System.out.println("InscripcionesPruebaDAOIT.actualizar() - entidad nula");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(null));
            return null;
        });
    }

    @Test
    @Order(23)
    void testActualizarSinId() {
        System.out.println("InscripcionesPruebaDAOIT.actualizar() - sin ID");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
            cut.em = em;

            AspirantesDato aspirante = em.find(AspirantesDato.class,
                    UUID.fromString("e1000000-0000-0000-0000-000000000001"));
            PruebasAdmision prueba = em.find(PruebasAdmision.class,
                    UUID.fromString("d1000000-0000-0000-0000-000000000002"));

            InscripcionesPrueba sinId = new InscripcionesPrueba();
            sinId.setAspiranteDato(aspirante);
            sinId.setPruebaAdmision(prueba);
            sinId.setEstado("INSCRITO");

            assertThrows(IllegalArgumentException.class, () -> cut.actualizar(sinId));
            return null;
        });
    }
}
