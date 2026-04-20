package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.admision;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.BaseSistemaBDD;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Step Definitions para el flujo BDD: Asignar Carrera Final según Cupos y Etapa
 * 
 * Patrones aplicados:
 * 1. BaseSistemaBDD.init() en @Given inicial
 * 2. Construir payloads con entidades JPA + referencias con solo id
 * 3. POST/PUT/GET reales contra endpoints HTTP
 * 4. Validar status + Location
 * 5. GET posterior para confirmar persistencia
 * 6. IDs semilla para catálogos/FKs necesarias
 */
public class AsignarCarreraFinalBDD {

    // ============================================
    // VARIABLES DE CLIENTE HTTP
    // ============================================
    static Client cliente;
    static WebTarget target;

    // ============================================
    // VARIABLES DE ESTADO DEL ESCENARIO
    // ============================================
    static UUID idAspirante;
    static UUID idInscripcion;
    static UUID idTurno;
    static UUID idEtapa;
    static String idCarreraSemilla;
    static UUID idProcesoAdmision;

    // ============================================
    // IDs SEMILLA (del script de inicialización)
    // ============================================
    private static final UUID ID_USUARIO = UUID.fromString("b1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final String ID_CARRERA_SEMILLA = "ICS";

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    /**
     * Extrae el UUID del header Location devuelto por POST (201 Created)
     * 
     * @param respuesta Respuesta HTTP del servidor
     * @param endPoint  Nombre del recurso (ej: "aspirantes_datos")
     * @return UUID generado por la BD
     */
    private UUID extraerIdDelHeader(Response respuesta, String endPoint) {
        String ubicacion = respuesta.getHeaderString("Location");
        if (ubicacion == null || ubicacion.isEmpty()) {
            throw new IllegalStateException("Header Location no encontrado en respuesta POST");
        }
        return UUID.fromString(ubicacion.split(endPoint + "/")[1]);
    }

    /**
     * Envía un POST a un endpoint con un payload JSON
     * 
     * @param endPoint Ruta del recurso
     * @param entidad  Objeto a enviar
     * @return Respuesta HTTP
     */
    private Response hacerPost(String endPoint, Object entidad) {
        return target.path(endPoint)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(entidad));
    }

    /**
     * Realiza un GET para recuperar un registro por su ID
     * 
     * @param endPoint Ruta del recurso
     * @param id       UUID del registro
     * @return Respuesta HTTP
     */
    private Response hacerGet(String endPoint, UUID id) {
        return target.path(endPoint + "/{id}")
                .resolveTemplate("id", id)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    /**
     * Realiza un GET para recuperar un CuposCarrera (PK compuesta)
     * 
     * @param idPrueba   UUID de la prueba
     * @param idCarrera  String del código de carrera
     * @param idEtapa    UUID de la etapa
     * @return Respuesta HTTP
     */
    private Response hacerGetCuposCarrera(UUID idPrueba, String idCarrera, UUID idEtapa) {
        return target.path("cupos_carrera/{idPrueba}/{idCarrera}/{idEtapa}")
                .resolveTemplate("idPrueba", idPrueba)
                .resolveTemplate("idCarrera", idCarrera)
                .resolveTemplate("idEtapa", idEtapa)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    // ============================================
    // STEPS: GIVEN
    // ============================================

    @Given("se tiene un servidor corriendo para ejecutar la asignacion de carrera final")
    public void se_tiene_un_servidor_corriendo_para_ejecutar_la_asignacion_de_carrera_final() {
        System.out.println("=== [PASO 1] Iniciando infraestructura BDD para Asignar Carrera Final ===");
        BaseSistemaBDD.init();

        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());

        System.out.println("✓ Servidor disponible en: " + BaseSistemaBDD.getBaseUrl());
    }

    @Given("existe un aspirante con perfil creado")
    public void existe_un_aspirante_con_perfil_creado() {
        System.out.println("=== [PASO 2] Creando aspirante ===");

        AspirantesDato nuevoAspirante = new AspirantesDato();
        nuevoAspirante.setNombres("Carlos");
        nuevoAspirante.setApellidos("Rodriguez");
        nuevoAspirante.setDui("87654321-9");
        nuevoAspirante.setUsaSillaRuedas(false);

        // Resolver FK: usuario sistema
        UsuariosSistema usuario = new UsuariosSistema();
        usuario.setId(ID_USUARIO);
        nuevoAspirante.setIdUsuario(usuario);

        Response respuesta = hacerPost("aspirantes_datos", nuevoAspirante);
        Assertions.assertEquals(201, respuesta.getStatus(), "POST aspirante debería devolver 201");

        idAspirante = extraerIdDelHeader(respuesta, "aspirantes_datos");
        System.out.println("✓ Aspirante creado con ID: " + idAspirante);
    }

    @Given("existe una inscripcion a prueba con carrera elegida asociada")
    public void existe_una_inscripcion_a_prueba_con_carrera_elegida_asociada() {
        System.out.println("=== [PASO 3] Creando turno, inscripción y asignando carrera ===");

        // --- PASO 3a: Crear turno ---
        TurnosExaman nuevoTurno = new TurnosExaman();
        nuevoTurno.setNombreTurno("Turno Vespertino");
        nuevoTurno.setFecha(LocalDate.now().plusDays(15));
        nuevoTurno.setHoraInicio(LocalTime.of(14, 0));
        nuevoTurno.setHoraFin(LocalTime.of(16, 0));

        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setId(ID_PRUEBA_SEMILLA);
        nuevoTurno.setIdPrueba(prueba);

        Response respuestaTurno = hacerPost("turnos_examen", nuevoTurno);
        Assertions.assertEquals(201, respuestaTurno.getStatus(), "POST turno debería devolver 201");

        idTurno = extraerIdDelHeader(respuestaTurno, "turnos_examen");
        System.out.println("  ✓ Turno creado: " + idTurno);

        // --- PASO 3b: Crear inscripción ---
        InscripcionesPrueba inscripcion = new InscripcionesPrueba();

        AspirantesDato aspiranteRef = new AspirantesDato();
        aspiranteRef.setId(idAspirante);
        inscripcion.setIdAspirante(aspiranteRef);

        PruebasAdmision pruebaRef = new PruebasAdmision();
        pruebaRef.setId(ID_PRUEBA_SEMILLA);
        inscripcion.setIdPrueba(pruebaRef);

        inscripcion.setEstado("INSCRITO");

        Response respuestaInscripcion = hacerPost("inscripciones_prueba", inscripcion);
        Assertions.assertEquals(201, respuestaInscripcion.getStatus(), "POST inscripción debería devolver 201");

        idInscripcion = extraerIdDelHeader(respuestaInscripcion, "inscripciones_prueba");
        System.out.println("  ✓ Inscripción creada: " + idInscripcion);

        // --- PASO 3c: Asociar carrera elegida ---
        CarrerasElegidaId carrerasElegidaId = new CarrerasElegidaId();
        carrerasElegidaId.setIdInscripcion(idInscripcion);
        carrerasElegidaId.setIdCarrera(ID_CARRERA_SEMILLA);

        CarrerasElegida carrerasElegida = new CarrerasElegida();
        carrerasElegida.setId(carrerasElegidaId);

        InscripcionesPrueba inscripcionRefCarrera = new InscripcionesPrueba();
        inscripcionRefCarrera.setId(idInscripcion);
        carrerasElegida.setIdInscripcion(inscripcionRefCarrera);

        CatalogoCarrera catalogoCarrera = new CatalogoCarrera();
        catalogoCarrera.setIdCarrera(ID_CARRERA_SEMILLA);
        carrerasElegida.setIdCarrera(catalogoCarrera);

        carrerasElegida.setPrioridad((short) 1);

        Response respuestaCarrera = hacerPost("carreras_elegidas", carrerasElegida);
        Assertions.assertEquals(201, respuestaCarrera.getStatus(), "POST carrera elegida debería devolver 201");

        idCarreraSemilla = ID_CARRERA_SEMILLA;
        System.out.println("  ✓ Carrera elegida asociada: " + idCarreraSemilla);
    }

    @Given("existe una etapa de admision vigente con cupos disponibles para una carrera")
    public void existe_una_etapa_de_admision_vigente_con_cupos_disponibles_para_una_carrera() {
        System.out.println("=== [PASO 4] Creando etapa de admisión y cupos ===");

        // --- PASO 4a: Crear etapa ---
        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setNombre("Etapa Regular 2026");
        etapa.setDescripcion("Primera etapa de admisión del ciclo 2026");

        Response respuestaEtapa = hacerPost("etapas_admision", etapa);
        Assertions.assertEquals(201, respuestaEtapa.getStatus(), "POST etapa debería devolver 201");

        idEtapa = extraerIdDelHeader(respuestaEtapa, "etapas_admision");
        System.out.println("  ✓ Etapa de admisión creada: " + idEtapa);

        // Validar que la etapa se creó correctamente
        Response respuestaValidacion = hacerGet("etapas_admision", idEtapa);
        Assertions.assertEquals(200, respuestaValidacion.getStatus(), "GET etapas_admision debería devolver 200");
        System.out.println("  ✓ Etapa validada en BD");

        // --- PASO 4b: Crear cupo para la carrera en esta etapa ---
        CuposCarreraId cuposId = new CuposCarreraId();
        cuposId.setIdPrueba(ID_PRUEBA_SEMILLA);
        cuposId.setIdCarrera(ID_CARRERA_SEMILLA);
        cuposId.setIdEtapa(idEtapa);

        CuposCarrera cupos = new CuposCarrera();
        cupos.setId(cuposId);

        PruebasAdmision pruebaRef = new PruebasAdmision();
        pruebaRef.setId(ID_PRUEBA_SEMILLA);
        cupos.setIdPrueba(pruebaRef);

        CatalogoCarrera carreraRef = new CatalogoCarrera();
        carreraRef.setIdCarrera(ID_CARRERA_SEMILLA);
        cupos.setIdCarrera(carreraRef);

        EtapasAdmision etapaRef = new EtapasAdmision();
        etapaRef.setId(idEtapa);
        cupos.setIdEtapa(etapaRef);

        cupos.setCupos(50); // 50 cupos disponibles

        Response respuestaCupos = hacerPost("cupos_carrera", cupos);
        Assertions.assertEquals(201, respuestaCupos.getStatus(), "POST cupos debería devolver 201");

        System.out.println("  ✓ Cupos creados: 50 cupos para carrera " + ID_CARRERA_SEMILLA);
    }

    // ============================================
    // STEPS: WHEN
    // ============================================

    @When("asigno la carrera final al aspirante verificando cupos y etapa")
    public void asigno_la_carrera_final_al_aspirante_verificando_cupos_y_etapa() {
        System.out.println("=== [PASO 5] Asignando carrera final (creando ProcesoAdmisionAspirante) ===");

        // Crear el proceso de admisión del aspirante
        ProcesoAdmisionAspirante procesoAdmision = new ProcesoAdmisionAspirante();

        // FK: InscripcionesPrueba (la ID será la misma de la inscripción)
        // IMPORTANTE: NO setear el id directamente. Es un @MapsId que se genera automáticamente
        InscripcionesPrueba inscripcionRef = new InscripcionesPrueba();
        inscripcionRef.setId(idInscripcion);
        procesoAdmision.setInscripcionesPrueba(inscripcionRef);

        // FK: EtapasAdmision
        EtapasAdmision etapaRef = new EtapasAdmision();
        etapaRef.setId(idEtapa);
        procesoAdmision.setIdEtapaActual(etapaRef);

        // Estado del proceso
        procesoAdmision.setEstado("ADMITIDO");

        // Carrera asignada
        CatalogoCarrera carreraAsignada = new CatalogoCarrera();
        carreraAsignada.setIdCarrera(ID_CARRERA_SEMILLA);
        procesoAdmision.setCarreraAsignada(carreraAsignada);

        Response respuesta = hacerPost("proceso_admision_aspirante", procesoAdmision);
        Assertions.assertEquals(201, respuesta.getStatus(), "POST proceso admisión debería devolver 201");

        idProcesoAdmision = idInscripcion; // El id es la inscripción misma
        System.out.println("✓ Proceso de admisión creado con ID: " + idProcesoAdmision);
    }

    // ============================================
    // STEPS: THEN
    // ============================================

    @Then("se crea el proceso admision aspirante con carrera asignada")
    public void se_crea_el_proceso_admision_aspirante_con_carrera_asignada() {
        System.out.println("=== [PASO 6] Validando creación del proceso de admisión ===");

        Assertions.assertNotNull(idProcesoAdmision, "ID del proceso de admisión no debería ser null");
        System.out.println("✓ Proceso de admisión confirmado con ID: " + idProcesoAdmision);
    }

    @Then("puedo consultar el proceso de admision del aspirante")
    public void puedo_consultar_el_proceso_de_admision_del_aspirante() {
        System.out.println("=== [PASO 7] GET: Consultando proceso de admisión ===");

        Response respuesta = hacerGet("proceso_admision_aspirante", idProcesoAdmision);
        Assertions.assertEquals(200, respuesta.getStatus(), "GET proceso_admision_aspirante debería devolver 200");

        ProcesoAdmisionAspirante procesoRecuperado = respuesta.readEntity(ProcesoAdmisionAspirante.class);
        Assertions.assertNotNull(procesoRecuperado, "Proceso de admisión recuperado no debería ser null");
        Assertions.assertNotNull(procesoRecuperado.getCarreraAsignada(), "Carrera asignada no debería ser null");

        System.out.println("✓ Proceso recuperado con éxito");
        System.out.println("  - ID Proceso: " + procesoRecuperado.getId());
        System.out.println("  - Estado: " + procesoRecuperado.getEstado());
        System.out.println("  - Carrera Asignada: " + procesoRecuperado.getCarreraAsignada().getIdCarrera());
    }

    @Then("la carrera asignada coincide con la carrera elegida dentro del cupo disponible")
    public void la_carrera_asignada_coincide_con_la_carrera_elegida_dentro_del_cupo_disponible() {
        System.out.println("=== [PASO 8] Validando coherencia: carrera elegida vs. asignada ===");

        // Verificar que la carrera asignada coincide con la elegida
        Response respuestaProceso = hacerGet("proceso_admision_aspirante", idProcesoAdmision);
        ProcesoAdmisionAspirante proceso = respuestaProceso.readEntity(ProcesoAdmisionAspirante.class);

        Assertions.assertEquals(
                idCarreraSemilla,
                proceso.getCarreraAsignada().getIdCarrera(),
                "La carrera asignada debería coincidir con la elegida"
        );

        // Verificar que el cupo existe y tiene disponibilidad
        Response respuestaCupos = hacerGetCuposCarrera(ID_PRUEBA_SEMILLA, ID_CARRERA_SEMILLA, idEtapa);
        Assertions.assertEquals(200, respuestaCupos.getStatus(), "GET cupos_carrera debería devolver 200");

        CuposCarrera cuposRecuperado = respuestaCupos.readEntity(CuposCarrera.class);
        Assertions.assertNotNull(cuposRecuperado.getCupos(), "Cupos no debería ser null");
        Assertions.assertTrue(
                cuposRecuperado.getCupos() > 0,
                "Debería haber cupos disponibles"
        );

        System.out.println("✓ Coherencia validada:");
        System.out.println("  - Carrera elegida: " + idCarreraSemilla);
        System.out.println("  - Carrera asignada: " + proceso.getCarreraAsignada().getIdCarrera());
        System.out.println("  - Cupos disponibles: " + cuposRecuperado.getCupos());
    }
}
