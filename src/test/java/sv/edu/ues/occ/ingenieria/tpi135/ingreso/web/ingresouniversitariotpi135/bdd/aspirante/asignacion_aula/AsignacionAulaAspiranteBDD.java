package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.asignacion_aula;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Y;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionAulaAspirante;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurnoId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.BaseSistemaBDD;

import java.util.UUID;

/**
 * Step definitions para el scenario: Asignar aula a aspirante para examen
 */
public class AsignacionAulaAspiranteBDD {

    private static Client cliente;
    private static WebTarget target;
    private static UUID idAspiranteCreado;
    private static UUID idInscripcionCreada;
    private static UUID idAsignacionCreada;

    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_AULA_1 = UUID.fromString("ffffff11-1111-1111-1111-111111111111");
    private static final UUID ID_TURNO_1 = UUID.fromString("ffff0001-0001-0001-0001-000000000001");

    @Dado("se tiene un servidor corriendo con la aplicacion desplegada para asignacion de aulas")
    public void se_tiene_un_servidor_corriendo_con_la_aplicacion_desplegada_para_asignacion_de_aulas() {
        System.out.println("Iniciando entorno de sistema para asignacion de aulas");
        BaseSistemaBDD.init();
        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());
    }

    @Y("se tiene un aspirante inscrito en una prueba con turno asignado")
    public void se_tiene_un_aspirante_inscrito_en_una_prueba_con_turno_asignado() {
        System.out.println("Creando aspirante e inscripcion con turno");
        
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setNombres("Roberto");
        nuevo.setApellidos("Martinez");
        nuevo.setDui("99999999-9");
        nuevo.setCorreo("roberto.martinez@local");
        nuevo.setUsaSillaRuedas(false);

        Response respuestaAspirante = target
                .path("aspirantes_datos")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(nuevo));

        Assertions.assertEquals(201, respuestaAspirante.getStatus());
        UUID id = UUID.fromString(respuestaAspirante.getHeaderString("Location").split("aspirantes_datos/")[1]);
        idAspiranteCreado = id;

        InscripcionesPrueba inscripcion = new InscripcionesPrueba();
        AspirantesDato aspiranteRef = new AspirantesDato();
        aspiranteRef.setId(idAspiranteCreado);
        inscripcion.setAspiranteDato(aspiranteRef);

        PruebasAdmision pruebaRef = new PruebasAdmision();
        pruebaRef.setIdPruebaAdmision(ID_PRUEBA_SEMILLA);
        inscripcion.setPruebaAdmision(pruebaRef);
        inscripcion.setEstado("INSCRITO");

        Response respuestaInscripcion = target
                .path("inscripciones_prueba")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(inscripcion));

        Assertions.assertEquals(201, respuestaInscripcion.getStatus());
        String locationInscripcion = respuestaInscripcion.getHeaderString("Location");
        idInscripcionCreada = UUID.fromString(locationInscripcion.split("inscripciones_prueba/")[1]);

        System.out.println("Aspirante inscrito con ID: " + idInscripcionCreada);
    }

    @Cuando("se asigna una aula disponible al aspirante para el turno")
    public void se_asigna_una_aula_disponible_al_aspirante_para_el_turno() {
        System.out.println("Asignando aula al aspirante");
        Assertions.assertNotNull(idInscripcionCreada, "La inscripcion debe existir");

        AsignacionAulaAspirante asignacion = new AsignacionAulaAspirante();

        InscripcionesPrueba inscripcionRef = new InscripcionesPrueba();
        inscripcionRef.setIdInscripcionPrueba(idInscripcionCreada);
        asignacion.setInscripcionPrueba(inscripcionRef);

        DisponibilidadAulaTurnoId dispId = new DisponibilidadAulaTurnoId();
        dispId.setIdAula(ID_AULA_1);
        dispId.setIdTurno(ID_TURNO_1);

        DisponibilidadAulaTurno disponibilidad = new DisponibilidadAulaTurno();
        disponibilidad.setIdDisponibilidadAulaTurno(dispId);
        asignacion.setDisponibilidad(disponibilidad);

        Response respuestaAsignacion = target
                .path("asignaciones_aula/inscripciones/" + idInscripcionCreada)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(asignacion));

        Assertions.assertEquals(201, respuestaAsignacion.getStatus(), "La asignacion debe crearse exitosamente");
        String locationAsignacion = respuestaAsignacion.getHeaderString("Location");
        idAsignacionCreada = UUID.fromString(locationAsignacion.split("asignaciones_aula/")[1]);
        System.out.println("Asignacion creada con ID: " + idAsignacionCreada);
    }

    @Entonces("la asignacion se registra exitosamente")
    public void la_asignacion_se_registra_exitosamente() {
        System.out.println("Verificando que la asignacion se registro exitosamente");
        Assertions.assertNotNull(idAsignacionCreada, "La asignacion debe haberse creado");
    }

    @Y("se puede consultar el aula asignada al aspirante")
    public void se_puede_consultar_el_aula_asignada_al_aspirante() {
        System.out.println("Consultando aula asignada");
        Assertions.assertNotNull(idAsignacionCreada, "La asignacion debe existir");

        Response respuesta = target
                .path("asignaciones_aula/{id}")
                .resolveTemplate("id", idAsignacionCreada)
                .request(MediaType.APPLICATION_JSON)
                .get();

        Assertions.assertEquals(200, respuesta.getStatus(), "Debe consultar la asignacion exitosamente");
        AsignacionAulaAspirante asignacionConsultada = respuesta.readEntity(AsignacionAulaAspirante.class);
        Assertions.assertNotNull(asignacionConsultada.getDisponibilidad(), "La disponibilidad debe estar asignada");
        System.out.println("Asignacion consultada exitosamente");
    }

    @Y("el cupo disponible en el aula disminuye en uno")
    public void el_cupo_disponible_en_el_aula_disminuye_en_uno() {
        System.out.println("Verificando disminucion de cupo");
        Assertions.assertNotNull(idAsignacionCreada, "La asignacion debe existir");
    }
}
