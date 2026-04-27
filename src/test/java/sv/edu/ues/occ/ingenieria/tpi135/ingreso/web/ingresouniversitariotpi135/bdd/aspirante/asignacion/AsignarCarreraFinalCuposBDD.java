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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;
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

    // Variables dinámicas para las carreras y cupos
    static String idCarreraPri1;
    static String idCarreraPri2;
    static int cuposInicialesCarrera2;

    private static final UUID ID_USUARIO_SEMILLA = UUID.fromString("b1000000-0000-0000-0000-000000000001");
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

    private String generarDuiUnico() {
        long n = Math.abs(System.nanoTime());
        String s = String.format("%09d", n % 1_000_000_000L);
        return s.substring(0, 8) + "-" + s.substring(8);
    }

    private UUID crearAspiranteDePrueba() {
        UsuariosSistema usuarioRef = new UsuariosSistema();
        usuarioRef.setId(ID_USUARIO_SEMILLA);

        AspirantesDato aspirante = new AspirantesDato();
        aspirante.setIdUsuario(usuarioRef);
        aspirante.setNombres("Aspirante");
        aspirante.setApellidos("Asignacion");
        aspirante.setDui(generarDuiUnico());
        aspirante.setUsaSillaRuedas(false);

        Response respAsp = postJson("aspirantes_datos", aspirante);
        Assertions.assertEquals(201, respAsp.getStatus(), "POST aspirantes_datos debe retornar 201");
        return extraerIdDelHeader(respAsp);
    }

    @Given("se inicializa el servidor para asignación de carrera")
    public void se_inicializa_el_servidor_para_asignacion_de_carrera() {
        BaseSistemaBDD.init();
        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());
    }

    @Given("existe una inscripción con proceso en etapa de asignación")
    public void existe_una_inscripcion_con_proceso_en_etapa_de_asignacion() {
        UUID idAspirante = crearAspiranteDePrueba();

        InscripcionesPrueba inscripcion = new InscripcionesPrueba();
        AspirantesDato aspiranteRef = new AspirantesDato();
        aspiranteRef.setId(idAspirante);
        inscripcion.setIdAspirante(aspiranteRef);
        PruebasAdmision pruebaRef = new PruebasAdmision();
        pruebaRef.setId(ID_PRUEBA_2025);
        inscripcion.setIdPrueba(pruebaRef);
        inscripcion.setEstado("INSCRITO");

        Response respIns = postJson("inscripciones_prueba", inscripcion);
        Assertions.assertEquals(201, respIns.getStatus());
        idInscripcion = extraerIdDelHeader(respIns);

        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setNombre("Etapa Asignacion BDD " + generarDuiUnico());

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

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // ESCENARIO 1: CAMINO FELIZ

    @Given("existen carreras elegidas con prioridades y cupos configurados")
    public void existen_carreras_elegidas_con_prioridades_y_cupos_configurados() {
        idCarreraPri1 = "ICS";
        idCarreraPri2 = "ISI";
        cuposInicialesCarrera2 = 1;

        crearCarreraElegida(idCarreraPri1, (short) 1);
        crearCarreraElegida(idCarreraPri2, (short) 2);

        // La primera prioridad no tiene cupos
        crearCuposCarrera(idCarreraPri1, 0);
        // La segunda sí tiene
        crearCuposCarrera(idCarreraPri2, cuposInicialesCarrera2);
    }

    @Then("se obtiene estado ADMITIDO con carrera asignada por cupos y prioridad")
    public void se_obtiene_estado_admitido_con_carrera_asignada_por_cupos_y_prioridad() {
        Assertions.assertEquals(200, ultimaRespuesta.getStatus());
        Assertions.assertNotNull(ultimoResultado);
        Assertions.assertEquals("ADMITIDO", ultimoResultado.getEstado());
        Assertions.assertNotNull(ultimoResultado.getCarreraAsignada());
        // Se valida que asignó la segunda prioridad, porque la primera estaba llena
        Assertions.assertEquals(idCarreraPri2, ultimoResultado.getCarreraAsignada().getIdCarrera());
    }

    @Then("el cupo de la carrera asignada se decrementa")
    public void el_cupo_de_la_carrera_asignada_se_decrementa() {
        Response respCupos = getJson("cupos_carrera/" + ID_PRUEBA_2025 + "/" + idCarreraPri2 + "/" + idEtapaAsignacion);
        Assertions.assertEquals(200, respCupos.getStatus());
        CuposCarrera cupos = respCupos.readEntity(CuposCarrera.class);

        int cuposEsperados = cuposInicialesCarrera2 - 1;
        Assertions.assertEquals(cuposEsperados, cupos.getCupos());
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // ESCENARIO 2: CAMINO TRIZTE

    @Given("existen carreras elegidas con prioridades pero sin cupos disponibles")
    public void existen_carreras_elegidas_con_prioridades_pero_sin_cupos_disponibles() {
        idCarreraPri1 = "ICC";
        idCarreraPri2 = "MAT";

        crearCarreraElegida(idCarreraPri1, (short) 1);
        crearCarreraElegida(idCarreraPri2, (short) 2);

        // Ambas sin cupos
        crearCuposCarrera(idCarreraPri1, 0);
        crearCuposCarrera(idCarreraPri2, 0);
    }

    @Then("se obtiene estado NO_ADMITIDO y el aspirante queda sin carrera asignada")
    public void se_obtiene_estado_no_admitido_y_el_aspirante_queda_sin_carrera_asignada() {
        Assertions.assertEquals(200, ultimaRespuesta.getStatus());
        Assertions.assertNotNull(ultimoResultado);
        Assertions.assertEquals("NO_ADMITIDO", ultimoResultado.getEstado());
        Assertions.assertNull(ultimoResultado.getCarreraAsignada(), "La carrera asignada debe ser nula al ser rechazado");
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
