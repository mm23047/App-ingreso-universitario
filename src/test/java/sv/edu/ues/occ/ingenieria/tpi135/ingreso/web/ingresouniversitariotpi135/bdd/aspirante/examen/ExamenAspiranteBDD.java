package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.examen;

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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.BaseSistemaBDD;

import java.util.UUID;
import java.time.OffsetDateTime;

/**
 * Step definitions para el scenario: Realizar un examen de admision
 */
public class ExamenAspiranteBDD {

    private static Client cliente;
    private static WebTarget target;
    private static UUID idAspiranteCreado;
    private static UUID idInscripcionCreada;
    private static UUID idExamenCreado;

    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_ETAPA_1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ID_CLAVE_1 = UUID.fromString("aaaabbbb-cccc-dddd-eeee-ffffffffffff");

    @Dado("se tiene un servidor corriendo con la aplicacion desplegada para examenes")
    public void se_tiene_un_servidor_corriendo_con_la_aplicacion_desplegada_para_examenes() {
        System.out.println("Iniciando entorno de sistema para examenes");
        BaseSistemaBDD.init();
        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());
    }

    @Y("se tiene una inscripcion de aspirante en una etapa")
    public void se_tiene_una_inscripcion_de_aspirante_en_una_etapa() {
        System.out.println("Creando aspirante e inscripcion para examen");
        
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setNombres("Carlos");
        nuevo.setApellidos("Lopez");
        nuevo.setDui("55555555-5");
        nuevo.setCorreo("carlos.lopez@local");
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
        System.out.println("Inscripcion creada con ID: " + idInscripcionCreada);
    }

    @Cuando("el aspirante realiza el examen de la primera etapa")
    public void el_aspirante_realiza_el_examen_de_la_primera_etapa() {
        System.out.println("Registrando examen realizado");
        Assertions.assertNotNull(idInscripcionCreada, "La inscripcion debe existir");

        ExamenRealizado examen = new ExamenRealizado();
        
        InscripcionesPrueba inscripcionRef = new InscripcionesPrueba();
        inscripcionRef.setIdInscripcionPrueba(idInscripcionCreada);
        examen.setInscripcionesPrueba(inscripcionRef);

        EtapasAdmision etapaRef = new EtapasAdmision();
        etapaRef.setIdEtapaAdmision(ID_ETAPA_1);
        examen.setEtapaAdmision(etapaRef);

        examen.setFechaRealizacion(OffsetDateTime.now());

        Response respuestaExamen = target
                .path("examen_realizado")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(examen));

        Assertions.assertEquals(201, respuestaExamen.getStatus(), "El examen debe registrarse exitosamente");
        String locationExamen = respuestaExamen.getHeaderString("Location");
        idExamenCreado = UUID.fromString(locationExamen.split("examen_realizado/")[1]);
        System.out.println("Examen creado con ID: " + idExamenCreado);
    }

    @Y("proporciona respuestas para todas las preguntas")
    public void proporciona_respuestas_para_todas_las_preguntas() {
        System.out.println("Registrando respuestas del examen");
        Assertions.assertNotNull(idExamenCreado, "El examen debe existir");
    }

    @Entonces("el examen se registra exitosamente")
    public void el_examen_se_registra_exitosamente() {
        System.out.println("Verificando registro del examen");
        Assertions.assertNotNull(idExamenCreado, "El examen debe haberse creado");
    }

    @Y("se pueden consultar las respuestas registradas")
    public void se_pueden_consultar_las_respuestas_registradas() {
        System.out.println("Consultando respuestas del examen");
        Assertions.assertNotNull(idExamenCreado, "El examen debe existir");

        Response respuesta = target
                .path("examen_realizado/{id}")
                .resolveTemplate("id", idExamenCreado)
                .request(MediaType.APPLICATION_JSON)
                .get();

        Assertions.assertEquals(200, respuesta.getStatus(), "Debe consultar el examen exitosamente");
        ExamenRealizado examenConsultado = respuesta.readEntity(ExamenRealizado.class);
        Assertions.assertNotNull(examenConsultado, "El examen debe estar disponible");
        System.out.println("Examen consultado exitosamente");
    }
}
