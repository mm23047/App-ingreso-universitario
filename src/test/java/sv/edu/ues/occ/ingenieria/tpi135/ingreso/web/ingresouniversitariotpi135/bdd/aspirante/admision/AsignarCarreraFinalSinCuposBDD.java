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

public class AsignarCarreraFinalSinCuposBDD {

    static Client cliente;
    static WebTarget target;

    static UUID idAspirante;
    static UUID idInscripcion;
    static UUID idEtapa;
    static UUID idProceso;

    private static final UUID ID_USUARIO = UUID.fromString("b1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");

    private static final String ID_CARRERA_1 = "ICS";
    private static final String ID_CARRERA_2 = "ISI";

    private String generarDuiUnico() {
        long n = Math.abs(System.nanoTime());
        String s = String.format("%09d", n % 1_000_000_000L);
        return s.substring(0, 8) + "-" + s.substring(8);
    }

    private UUID extraerIdDelHeader(Response respuesta, String endPoint) {
        String ubicacion = respuesta.getHeaderString("Location");
        Assertions.assertNotNull(ubicacion);
        return UUID.fromString(ubicacion.split(endPoint + "/")[1]);
    }

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

    private Response hacerGet(String endPoint, UUID id) {
        return target.path(endPoint + "/{id}")
                .resolveTemplate("id", id)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    private Response hacerGetCupos(UUID idPrueba, String idCarrera, UUID idEtapaLocal) {
        return target.path("cupos_carrera/{idPrueba}/{idCarrera}/{idEtapa}")
                .resolveTemplate("idPrueba", idPrueba)
                .resolveTemplate("idCarrera", idCarrera)
                .resolveTemplate("idEtapa", idEtapaLocal)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    @Given("se inicializa el servidor para asignacion de carrera sin cupos")
    public void se_inicializa_el_servidor_para_asignacion_de_carrera_sin_cupos() {
        BaseSistemaBDD.init();
        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());
    }

    @Given("se crea un aspirante para asignacion sin cupos")
    public void se_crea_un_aspirante_para_asignacion_sin_cupos() {
        AspirantesDato aspirante = new AspirantesDato();
        aspirante.setNombres("Aspirante");
        aspirante.setApellidos("SinCupos");
        aspirante.setDui(generarDuiUnico());
        aspirante.setUsaSillaRuedas(false);

        UsuariosSistema usuario = new UsuariosSistema();
        usuario.setId(ID_USUARIO);
        aspirante.setIdUsuario(usuario);

        Response resp = hacerPost("aspirantes_datos", aspirante);
        Assertions.assertEquals(201, resp.getStatus());
        idAspirante = extraerIdDelHeader(resp, "aspirantes_datos");
    }

    @Given("se crea una inscripcion a prueba para asignacion sin cupos")
    public void se_crea_una_inscripcion_a_prueba_para_asignacion_sin_cupos() {
        InscripcionesPrueba insc = new InscripcionesPrueba();
        AspirantesDato refAsp = new AspirantesDato();
        refAsp.setId(idAspirante);
        PruebasAdmision refPrueba = new PruebasAdmision();
        refPrueba.setId(ID_PRUEBA_SEMILLA);

        insc.setIdAspirante(refAsp);
        insc.setIdPrueba(refPrueba);
        insc.setEstado("INSCRITO");

        Response resp = hacerPost("inscripciones_prueba", insc);
        Assertions.assertEquals(201, resp.getStatus());
        idInscripcion = extraerIdDelHeader(resp, "inscripciones_prueba");
    }

    @Given("se asocian carreras elegidas sin cupos")
    public void se_asocian_carreras_elegidas_sin_cupos() {
        crearCarreraElegida(ID_CARRERA_1, (short) 1);
        crearCarreraElegida(ID_CARRERA_2, (short) 2);
    }

    @Given("se define una etapa y cupos en cero")
    public void se_define_una_etapa_y_cupos_en_cero() {
        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setNombre("Etapa Sin Cupos BDD");

        Response respEtapa = hacerPost("etapas_admision", etapa);
        Assertions.assertEquals(201, respEtapa.getStatus());
        idEtapa = extraerIdDelHeader(respEtapa, "etapas_admision");

        crearCuposCarrera(ID_CARRERA_1, 0);
        crearCuposCarrera(ID_CARRERA_2, 0);
    }

    @Given("se crea el proceso de admision sin cupos")
    public void se_crea_el_proceso_de_admision_sin_cupos() {
        ProcesoAdmisionAspirante proceso = new ProcesoAdmisionAspirante();

        InscripcionesPrueba refInsc = new InscripcionesPrueba();
        refInsc.setId(idInscripcion);
        proceso.setInscripcionesPrueba(refInsc);

        EtapasAdmision refEtapa = new EtapasAdmision();
        refEtapa.setId(idEtapa);
        proceso.setIdEtapaActual(refEtapa);

        proceso.setEstado("EN_PROCESO");

        Response resp = hacerPost("proceso_admision_aspirante", proceso);
        Assertions.assertEquals(201, resp.getStatus());
        idProceso = extraerIdDelHeader(resp, "proceso_admision_aspirante");
    }

    @When("ejecuto el endpoint de asignacion de carrera final sin cupos")
    public void ejecuto_el_endpoint_de_asignacion_de_carrera_final_sin_cupos() {
        Response resp = hacerPostVacio("proceso_admision_aspirante/" + idProceso + "/asignar-carrera");
        Assertions.assertEquals(200, resp.getStatus());
    }

    @Then("el sistema cambia el estado a NO_ADMITIDO y no asigna carrera")
    public void el_sistema_cambia_el_estado_a_no_admitido_y_no_asigna_carrera() {
        Response resp = hacerGet("proceso_admision_aspirante", idProceso);
        Assertions.assertEquals(200, resp.getStatus());

        ProcesoAdmisionAspirante proc = resp.readEntity(ProcesoAdmisionAspirante.class);
        Assertions.assertNotNull(proc);
        Assertions.assertEquals("NO_ADMITIDO", proc.getEstado());
        Assertions.assertNull(proc.getCarreraAsignada());
    }

    @Then("los cupos se mantienen en cero")
    public void los_cupos_se_mantienen_en_cero() {
        Response respA = hacerGetCupos(ID_PRUEBA_SEMILLA, ID_CARRERA_1, idEtapa);
        Assertions.assertEquals(200, respA.getStatus());
        CuposCarrera cuposA = respA.readEntity(CuposCarrera.class);
        Assertions.assertEquals(0, cuposA.getCupos());

        Response respB = hacerGetCupos(ID_PRUEBA_SEMILLA, ID_CARRERA_2, idEtapa);
        Assertions.assertEquals(200, respB.getStatus());
        CuposCarrera cuposB = respB.readEntity(CuposCarrera.class);
        Assertions.assertEquals(0, cuposB.getCupos());
    }

    private void crearCarreraElegida(String idCarrera, short prioridad) {
        CarrerasElegidaId pk = new CarrerasElegidaId();
        pk.setIdInscripcion(idInscripcion);
        pk.setIdCarrera(idCarrera);

        CarrerasElegida ce = new CarrerasElegida();
        ce.setId(pk);

        InscripcionesPrueba insRef = new InscripcionesPrueba();
        insRef.setId(idInscripcion);
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
