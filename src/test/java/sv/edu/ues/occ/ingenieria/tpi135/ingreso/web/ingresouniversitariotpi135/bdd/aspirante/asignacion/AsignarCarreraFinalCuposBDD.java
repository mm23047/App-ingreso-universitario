package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.asignacion;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ProcesoAdmisionAspirante;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.BaseSistemaBDD;

import java.util.UUID;

/**
 * Step Definitions para el feature BDD de asignación final de carrera (por cupos/prioridad).
 *
 * Nota: existe otro step definition con nombre similar en otro paquete.
 * Este se nombra distinto para evitar ambigüedad en el escaneo de Cucumber.
 */
public class AsignarCarreraFinalCuposBDD {

    static Client cliente;
    static WebTarget target;

    static UUID idInscripcion;
    static UUID idEtapaAsignacion;

    static Response ultimaRespuesta;
    static ProcesoAdmisionAspirante ultimoResultado;

    private static final UUID ID_ASPIRANTE_SEMILLA = UUID.fromString("e1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_2025 = UUID.fromString("d1000000-0000-0000-0000-000000000002");

    private UUID extraerIdDelHeader(Response respuesta) {
        String location = respuesta.getHeaderString("Location");
        Assertions.assertNotNull(location);
        String id = location.substring(location.lastIndexOf('/') + 1);
        return UUID.fromString(id);
    }

    private Response postJson(String endpoint, Object entidad) {
        return target
                .path(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(entidad));
    }

    private Response getJson(String endpoint) {
        return target
                .path(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    @Given("se inicializa el servidor para asignación de carrera")
    public void se_inicializa_el_servidor_para_asignacion_de_carrera() {
        BaseSistemaBDD.init();
        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());
    }

    @Given("existe una inscripción con proceso en etapa de asignación")
    public void existe_una_inscripcion_con_proceso_en_etapa_de_asignacion() {
        InscripcionesPrueba inscripcion = new InscripcionesPrueba();
        AspirantesDato aspiranteRef = new AspirantesDato();
        aspiranteRef.setId(ID_ASPIRANTE_SEMILLA);
        inscripcion.setIdAspirante(aspiranteRef);
        PruebasAdmision pruebaRef = new PruebasAdmision();
        pruebaRef.setId(ID_PRUEBA_2025);
        inscripcion.setIdPrueba(pruebaRef);
        inscripcion.setEstado("INSCRITO");

        Response respIns = postJson("inscripciones_prueba", inscripcion);
        Assertions.assertEquals(201, respIns.getStatus());
        idInscripcion = extraerIdDelHeader(respIns);

        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setNombre("Etapa Asignacion Carrera BDD");

        Response respEtapa = postJson("etapas_admision", etapa);
        Assertions.assertEquals(201, respEtapa.getStatus());
        idEtapaAsignacion = extraerIdDelHeader(respEtapa);

        ProcesoAdmisionAspirante proceso = new ProcesoAdmisionAspirante();
        InscripcionesPrueba insRef = new InscripcionesPrueba();
        insRef.setId(idInscripcion);
        proceso.setInscripcionesPrueba(insRef);
        EtapasAdmision etapaRef = new EtapasAdmision();
        etapaRef.setId(idEtapaAsignacion);
        proceso.setIdEtapaActual(etapaRef);
        proceso.setEstado("EN_PROCESO");

        Response respProceso = postJson("proceso_admision_aspirante", proceso);
        Assertions.assertEquals(201, respProceso.getStatus());
    }

    @Given("existen carreras elegidas con prioridades y cupos configurados")
    public void existen_carreras_elegidas_con_prioridades_y_cupos_configurados() {
        crearCarreraElegida("ICS", (short) 1);
        crearCarreraElegida("ISI", (short) 2);

        crearCuposCarrera("ICS", 0);
        crearCuposCarrera("ISI", 1);
    }

    @When("ejecuto la asignación final de carrera para la inscripción")
    public void ejecuto_la_asignacion_final_de_carrera_para_la_inscripcion() {
        ultimaRespuesta = target
                .path("proceso_admision_aspirante/{id}/asignar-carrera")
                .resolveTemplate("id", idInscripcion)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json("{}"));

        Assertions.assertNotNull(ultimaRespuesta);
        ultimoResultado = ultimaRespuesta.readEntity(ProcesoAdmisionAspirante.class);
    }

    @Then("se obtiene estado ADMITIDO con carrera asignada por cupos y prioridad")
    public void se_obtiene_estado_admitido_con_carrera_asignada_por_cupos_y_prioridad() {
        Assertions.assertEquals(200, ultimaRespuesta.getStatus());
        Assertions.assertNotNull(ultimoResultado);
        Assertions.assertEquals(idInscripcion, ultimoResultado.getId());
        Assertions.assertEquals("ADMITIDO", ultimoResultado.getEstado());
        Assertions.assertNotNull(ultimoResultado.getCarreraAsignada());
        Assertions.assertEquals("ISI", ultimoResultado.getCarreraAsignada().getIdCarrera());
    }

    @Then("el cupo de la carrera asignada se decrementa")
    public void el_cupo_de_la_carrera_asignada_se_decrementa() {
        Response respCupos = getJson("cupos_carrera/" + ID_PRUEBA_2025 + "/ISI/" + idEtapaAsignacion);
        Assertions.assertEquals(200, respCupos.getStatus());
        CuposCarrera cupos = respCupos.readEntity(CuposCarrera.class);
        Assertions.assertNotNull(cupos);
        Assertions.assertEquals(0, cupos.getCupos());
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

        Response resp = postJson("carreras_elegidas", ce);
        Assertions.assertEquals(201, resp.getStatus());
    }

    private void crearCuposCarrera(String idCarrera, int cupos) {
        CuposCarreraId pk = new CuposCarreraId();
        pk.setIdPrueba(ID_PRUEBA_2025);
        pk.setIdCarrera(idCarrera);
        pk.setIdEtapa(idEtapaAsignacion);

        CuposCarrera entidad = new CuposCarrera();
        entidad.setId(pk);

        PruebasAdmision pruebaRef = new PruebasAdmision();
        pruebaRef.setId(ID_PRUEBA_2025);
        entidad.setIdPrueba(pruebaRef);

        CatalogoCarrera carreraRef = new CatalogoCarrera();
        carreraRef.setIdCarrera(idCarrera);
        entidad.setIdCarrera(carreraRef);

        EtapasAdmision etapaRef = new EtapasAdmision();
        etapaRef.setId(idEtapaAsignacion);
        entidad.setIdEtapa(etapaRef);

        entidad.setCupos(cupos);

        Response resp = postJson("cupos_carrera", entidad);
        Assertions.assertEquals(201, resp.getStatus());
    }
}
