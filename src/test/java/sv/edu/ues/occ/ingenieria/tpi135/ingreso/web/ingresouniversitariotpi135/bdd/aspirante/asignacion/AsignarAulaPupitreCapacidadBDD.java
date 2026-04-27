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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.BaseSistemaBDD;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class AsignarAulaPupitreCapacidadBDD {

    static Client cliente;
    static WebTarget target;

    static UUID idInscripcion1;
    static UUID idInscripcion2;
    static UUID idAula;
    static UUID idTurno;

    static Response ultimaRespuesta;

    private static final UUID ID_USUARIO = UUID.fromString("b1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");

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

    private UUID crearInscripcionAspirante() {
        AspirantesDato aspirante = new AspirantesDato();
        aspirante.setNombres("Aspirante");
        aspirante.setApellidos("Aula");
        aspirante.setDui(generarDuiUnico());
        aspirante.setUsaSillaRuedas(false);

        UsuariosSistema usuario = new UsuariosSistema();
        usuario.setId(ID_USUARIO);
        aspirante.setIdUsuario(usuario);

        Response respAsp = hacerPost("aspirantes_datos", aspirante);
        Assertions.assertEquals(201, respAsp.getStatus());
        UUID idAspirante = extraerIdDelHeader(respAsp, "aspirantes_datos");

        InscripcionesPrueba inscripcion = new InscripcionesPrueba();
        AspirantesDato refAsp = new AspirantesDato();
        refAsp.setId(idAspirante);
        inscripcion.setIdAspirante(refAsp);
        PruebasAdmision pruebaRef = new PruebasAdmision();
        pruebaRef.setId(ID_PRUEBA_SEMILLA);
        inscripcion.setIdPrueba(pruebaRef);
        inscripcion.setEstado("INSCRITO");

        Response respIns = hacerPost("inscripciones_prueba", inscripcion);
        Assertions.assertEquals(201, respIns.getStatus());
        return extraerIdDelHeader(respIns, "inscripciones_prueba");
    }

    @Given("se inicializa el servidor para asignacion de aula con capacidad limitada")
    public void se_inicializa_el_servidor_para_asignacion_de_aula_con_capacidad_limitada() {
        BaseSistemaBDD.init();
        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());
    }

    @Given("existen dos aspirantes inscritos en la prueba")
    public void existen_dos_aspirantes_inscritos_en_la_prueba() {
        idInscripcion1 = crearInscripcionAspirante();
        idInscripcion2 = crearInscripcionAspirante();
    }

    @Given("existe un aula de examen con capacidad limitada")
    public void existe_un_aula_de_examen_con_capacidad_limitada() {
        TurnosExaman turno = new TurnosExaman();
        turno.setNombreTurno("Turno Capacidad");
        PruebasAdmision pruebaRef = new PruebasAdmision();
        pruebaRef.setId(ID_PRUEBA_SEMILLA);
        turno.setIdPrueba(pruebaRef);
        turno.setFecha(LocalDate.now());
        turno.setHoraInicio(LocalTime.of(9, 0));
        turno.setHoraFin(LocalTime.of(11, 0));

        Response respTurno = hacerPost("turnos_examen", turno);
        Assertions.assertEquals(201, respTurno.getStatus());
        idTurno = extraerIdDelHeader(respTurno, "turnos_examen");

        AulasExaman aula = new AulasExaman();
        TurnosExaman turnoRef = new TurnosExaman();
        turnoRef.setId(idTurno);
        aula.setIdTurno(turnoRef);
        aula.setIdAulaApi("AULA-CAP-001");
        aula.setCapacidad(1);
        aula.setCuposOcupados(0);
        aula.setAccesibleSillaRuedas(false);

        Response respAula = hacerPost("aulas_examen", aula);
        Assertions.assertEquals(201, respAula.getStatus());
        idAula = extraerIdDelHeader(respAula, "aulas_examen");
    }

    @When("asigno el aula al primer aspirante")
    public void asigno_el_aula_al_primer_aspirante() {
        AsignacionesAulaPupitre asignacion = new AsignacionesAulaPupitre();
        InscripcionesPrueba insRef = new InscripcionesPrueba();
        insRef.setId(idInscripcion1);
        asignacion.setIdInscripcion(insRef);
        AulasExaman aulaRef = new AulasExaman();
        aulaRef.setId(idAula);
        asignacion.setIdAula(aulaRef);
        asignacion.setPupitre("P-01");

        Response resp = hacerPost("asignaciones_aula_pupitre", asignacion);
        Assertions.assertEquals(201, resp.getStatus());
    }

    @When("intento asignar el mismo aula al segundo aspirante")
    public void intento_asignar_el_mismo_aula_al_segundo_aspirante() {
        AsignacionesAulaPupitre asignacion = new AsignacionesAulaPupitre();
        InscripcionesPrueba insRef = new InscripcionesPrueba();
        insRef.setId(idInscripcion2);
        asignacion.setIdInscripcion(insRef);
        AulasExaman aulaRef = new AulasExaman();
        aulaRef.setId(idAula);
        asignacion.setIdAula(aulaRef);
        asignacion.setPupitre("P-02");

        ultimaRespuesta = hacerPost("asignaciones_aula_pupitre", asignacion);
    }

    @Then("el sistema rechaza la asignacion por capacidad")
    public void el_sistema_rechaza_la_asignacion_por_capacidad() {
        Assertions.assertNotNull(ultimaRespuesta);
        Assertions.assertEquals(409, ultimaRespuesta.getStatus());
        String motivo = ultimaRespuesta.getHeaderString("Conflict-reason");
        Assertions.assertEquals("El aula ya alcanzo su capacidad maxima", motivo);
    }
}
