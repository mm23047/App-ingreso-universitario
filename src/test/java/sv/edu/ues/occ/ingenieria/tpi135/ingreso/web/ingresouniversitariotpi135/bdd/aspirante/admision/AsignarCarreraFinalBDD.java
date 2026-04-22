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
    static UUID idEtapa;
    static UUID idProcesoAdmision;

    // Control del escenario (dos carreras y cupos)
    private static final String ID_CARRERA_PRIORIDAD_1 = "ICS";
    private static final String ID_CARRERA_PRIORIDAD_2 = "ISI";
    private static final int CUPOS_CARRERA_PRIORIDAD_1 = 0;
    private static final int CUPOS_CARRERA_PRIORIDAD_2 = 5;

    // ============================================
    // IDs SEMILLA (del script de inicialización)
    // ============================================
    private static final UUID ID_USUARIO = UUID.fromString("b1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");

    private static String generarDuiUnico() {
        long n = Math.abs(System.currentTimeMillis());
        String s = String.format("%09d", n % 1_000_000_000L);
        return s.substring(0, 8) + "-" + s.substring(8);
    }

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

    private Response hacerPostVacio(String endPoint) {
        return target.path(endPoint)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json("{}"));
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

    @Given("se inicializa el servidor para asignacion automatica de carrera")
    public void se_inicializa_el_servidor_para_asignacion_automatica_de_carrera() {
        System.out.println("=== [ASIGNACION CARRERA] Iniciando infraestructura BDD ===");
        BaseSistemaBDD.init();

        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());

        System.out.println("✓ Servidor disponible en: " + BaseSistemaBDD.getBaseUrl());
    }

    @Given("se crea un aspirante unico")
    public void se_crea_un_aspirante_unico() {
        AspirantesDato aspirante = new AspirantesDato();
        aspirante.setId(null);
        aspirante.setNombres("Aspirante");
        aspirante.setApellidos("Asignacion");
        aspirante.setDui(generarDuiUnico());
        aspirante.setUsaSillaRuedas(false);

        UsuariosSistema userRef = new UsuariosSistema();
        userRef.setId(ID_USUARIO);
        aspirante.setIdUsuario(userRef);

        Response r = hacerPost("aspirantes_datos", aspirante);
        Assertions.assertEquals(201, r.getStatus());
        idAspirante = extraerIdDelHeader(r, "aspirantes_datos");
        Assertions.assertNotNull(idAspirante);
    }

    @Given("se crea una inscripcion a prueba para el aspirante")
    public void se_crea_una_inscripcion_a_prueba_para_el_aspirante() {
        InscripcionesPrueba insc = new InscripcionesPrueba();
        insc.setId(null);

        AspirantesDato refAsp = new AspirantesDato();
        refAsp.setId(idAspirante);

        PruebasAdmision refPrueba = new PruebasAdmision();
        refPrueba.setId(ID_PRUEBA_SEMILLA);

        insc.setIdAspirante(refAsp);
        insc.setIdPrueba(refPrueba);
        insc.setEstado("INSCRITO");

        Response r = hacerPost("inscripciones_prueba", insc);
        Assertions.assertEquals(201, r.getStatus());
        idInscripcion = extraerIdDelHeader(r, "inscripciones_prueba");
        Assertions.assertNotNull(idInscripcion);
    }

    @Given("se asocian carreras elegidas con prioridad")
    public void se_asocian_carreras_elegidas_con_prioridad() {
        crearCarreraElegida(idInscripcion, ID_CARRERA_PRIORIDAD_1, (short) 1);
        crearCarreraElegida(idInscripcion, ID_CARRERA_PRIORIDAD_2, (short) 2);
    }

    @Given("se define una etapa y cupos por carrera")
    public void se_define_una_etapa_y_cupos_por_carrera() {
        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setNombre("Etapa Asignacion Carrera (BDD)");

        Response respEtapa = hacerPost("etapas_admision", etapa);
        Assertions.assertEquals(201, respEtapa.getStatus());
        idEtapa = extraerIdDelHeader(respEtapa, "etapas_admision");
        Assertions.assertNotNull(idEtapa);

        crearCuposCarrera(ID_CARRERA_PRIORIDAD_1, CUPOS_CARRERA_PRIORIDAD_1);
        crearCuposCarrera(ID_CARRERA_PRIORIDAD_2, CUPOS_CARRERA_PRIORIDAD_2);

        // Verificación previa para poder afirmar decremento real
        Response respCupoB = hacerGetCuposCarrera(ID_PRUEBA_SEMILLA, ID_CARRERA_PRIORIDAD_2, idEtapa);
        Assertions.assertEquals(200, respCupoB.getStatus());
        CuposCarrera cupoB = respCupoB.readEntity(CuposCarrera.class);
        Assertions.assertEquals(CUPOS_CARRERA_PRIORIDAD_2, cupoB.getCupos());
    }

    @Given("se crea el proceso de admision en estado EN_PROCESO sin carrera asignada")
    public void se_crea_el_proceso_de_admision_en_estado_en_proceso_sin_carrera_asignada() {
        ProcesoAdmisionAspirante proc = new ProcesoAdmisionAspirante();

        InscripcionesPrueba refInsc = new InscripcionesPrueba();
        refInsc.setId(idInscripcion);
        proc.setInscripcionesPrueba(refInsc);

        EtapasAdmision refEtapa = new EtapasAdmision();
        refEtapa.setId(idEtapa);
        proc.setIdEtapaActual(refEtapa);

        proc.setEstado("EN_PROCESO");

        Response r = hacerPost("proceso_admision_aspirante", proc);
        Assertions.assertEquals(201, r.getStatus());
        idProcesoAdmision = extraerIdDelHeader(r, "proceso_admision_aspirante");
        Assertions.assertNotNull(idProcesoAdmision);

        Response respGet = hacerGet("proceso_admision_aspirante", idProcesoAdmision);
        Assertions.assertEquals(200, respGet.getStatus());
        ProcesoAdmisionAspirante persisted = respGet.readEntity(ProcesoAdmisionAspirante.class);
        Assertions.assertEquals("EN_PROCESO", persisted.getEstado());
        Assertions.assertNull(persisted.getCarreraAsignada(), "La carrera no debe predefinirse; debe decidirla el sistema");
    }

    // ============================================
    // STEPS: WHEN
    // ============================================

    @When("ejecuto el endpoint de asignacion de carrera final")
    public void ejecuto_el_endpoint_de_asignacion_de_carrera_final() {
        Response r = hacerPostVacio("proceso_admision_aspirante/" + idProcesoAdmision + "/asignar-carrera");
        Assertions.assertEquals(200, r.getStatus());
    }

    // ============================================
    // STEPS: THEN
    // ============================================

    @Then("el sistema cambia el estado a ADMITIDO y asigna la carrera disponible por prioridad")
    public void el_sistema_cambia_el_estado_a_admitido_y_asigna_la_carrera_disponible_por_prioridad() {
        Response resp = hacerGet("proceso_admision_aspirante", idProcesoAdmision);
        Assertions.assertEquals(200, resp.getStatus());

        ProcesoAdmisionAspirante procResp = resp.readEntity(ProcesoAdmisionAspirante.class);
        Assertions.assertNotNull(procResp);
        Assertions.assertEquals("ADMITIDO", procResp.getEstado());

        Assertions.assertNotNull(procResp.getCarreraAsignada(), "Debe existir carrera asignada");
        Assertions.assertEquals(ID_CARRERA_PRIORIDAD_2, procResp.getCarreraAsignada().getIdCarrera(),
                "Debe asignar la carrera con cupo disponible respetando prioridad (prioridad 1 sin cupo)"
        );
    }

    @Then("el sistema decrementa el cupo de la carrera asignada")
    public void el_sistema_decrementa_el_cupo_de_la_carrera_asignada() {
        Response respCupos = hacerGetCuposCarrera(ID_PRUEBA_SEMILLA, ID_CARRERA_PRIORIDAD_2, idEtapa);
        Assertions.assertEquals(200, respCupos.getStatus());
        CuposCarrera cupoResp = respCupos.readEntity(CuposCarrera.class);
        Assertions.assertNotNull(cupoResp);
        Assertions.assertEquals(CUPOS_CARRERA_PRIORIDAD_2 - 1, cupoResp.getCupos(),
                "El cupo debe decrementarse exactamente en 1 al asignar la carrera"
        );
    }

    private void crearCarreraElegida(UUID idInscripcionLocal, String idCarrera, short prioridad) {
        CarrerasElegidaId pk = new CarrerasElegidaId();
        pk.setIdInscripcion(idInscripcionLocal);
        pk.setIdCarrera(idCarrera);

        CarrerasElegida ce = new CarrerasElegida();
        ce.setId(pk);

        InscripcionesPrueba insRef = new InscripcionesPrueba();
        insRef.setId(idInscripcionLocal);
        ce.setIdInscripcion(insRef);

        CatalogoCarrera carreraRef = new CatalogoCarrera();
        carreraRef.setIdCarrera(idCarrera);
        ce.setIdCarrera(carreraRef);
        ce.setPrioridad(prioridad);

        Response resp = hacerPost("carreras_elegidas", ce);
        Assertions.assertEquals(201, resp.getStatus());
    }

    private void crearCuposCarrera(String idCarrera, int cupos) {
        CuposCarreraId pk = new CuposCarreraId();
        pk.setIdPrueba(ID_PRUEBA_SEMILLA);
        pk.setIdCarrera(idCarrera);
        pk.setIdEtapa(idEtapa);

        CuposCarrera entidad = new CuposCarrera();
        entidad.setId(pk);

        PruebasAdmision pruebaRef = new PruebasAdmision();
        pruebaRef.setId(ID_PRUEBA_SEMILLA);
        entidad.setIdPrueba(pruebaRef);

        CatalogoCarrera carreraRef = new CatalogoCarrera();
        carreraRef.setIdCarrera(idCarrera);
        entidad.setIdCarrera(carreraRef);

        EtapasAdmision etapaRef = new EtapasAdmision();
        etapaRef.setId(idEtapa);
        entidad.setIdEtapa(etapaRef);

        entidad.setCupos(cupos);

        Response resp = hacerPost("cupos_carrera", entidad);
        Assertions.assertEquals(201, resp.getStatus());
    }
}
