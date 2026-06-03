package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.expediente;

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
 * Step definitions para el scenario: Consultar expediente de aspirante
 */
public class ExpedienteAspiranteBDD {

    private static Client cliente;
    private static WebTarget target;
    private static UUID idAspiranteCreado;
    private static UUID idInscripcionCreada;
    private static UUID idExamenCreado;

    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_ETAPA_1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Dado("se tiene un servidor corriendo con la aplicacion desplegada para expedientes")
    public void se_tiene_un_servidor_corriendo_con_la_aplicacion_desplegada_para_expedientes() {
        System.out.println("Iniciando entorno de sistema para expedientes");
        BaseSistemaBDD.init();
        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());
    }

    @Y("se tiene un aspirante con examenes realizados")
    public void se_tiene_un_aspirante_con_examenes_realizados() {
        System.out.println("Creando aspirante con inscripcion y examen");
        
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setNombres("Maria");
        nuevo.setApellidos("Garcia");
        nuevo.setDui("77777777-7");
        nuevo.setCorreo("maria.garcia@local");
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

        Assertions.assertEquals(201, respuestaExamen.getStatus());
        String locationExamen = respuestaExamen.getHeaderString("Location");
        idExamenCreado = UUID.fromString(locationExamen.split("examen_realizado/")[1]);
        System.out.println("Aspirante con examen creado: " + idAspiranteCreado);
    }

    @Cuando("consulto el expediente del aspirante")
    public void consulto_el_expediente_del_aspirante() {
        System.out.println("Consultando expediente del aspirante");
        Assertions.assertNotNull(idAspiranteCreado, "El aspirante debe existir");
    }

    @Entonces("se visualizan los datos personales")
    public void se_visualizan_los_datos_personales() {
        System.out.println("Verificando datos personales del aspirante");
        
        Response respuesta = target
                .path("aspirantes_datos/{id}")
                .resolveTemplate("id", idAspiranteCreado)
                .request(MediaType.APPLICATION_JSON)
                .get();

        Assertions.assertEquals(200, respuesta.getStatus(), "Debe consultar los datos personales");
        AspirantesDato aspiranteConsultado = respuesta.readEntity(AspirantesDato.class);
        Assertions.assertNotNull(aspiranteConsultado.getNombres(), "Los nombres deben existir");
        Assertions.assertNotNull(aspiranteConsultado.getApellidos(), "Los apellidos deben existir");
        System.out.println("Datos personales verificados: " + aspiranteConsultado.getNombres() + " " + aspiranteConsultado.getApellidos());
    }

    @Y("se visualizan las inscripciones en pruebas")
    public void se_visualizan_las_inscripciones_en_pruebas() {
        System.out.println("Verificando inscripciones en pruebas");
        
        Response respuesta = target
                .path("inscripciones_prueba/{id}")
                .resolveTemplate("id", idInscripcionCreada)
                .request(MediaType.APPLICATION_JSON)
                .get();

        Assertions.assertEquals(200, respuesta.getStatus(), "Debe consultar la inscripcion");
        InscripcionesPrueba inscripcionConsultada = respuesta.readEntity(InscripcionesPrueba.class);
        Assertions.assertNotNull(inscripcionConsultada.getEstado(), "El estado de inscripcion debe existir");
        System.out.println("Inscripcion verificada con estado: " + inscripcionConsultada.getEstado());
    }

    @Y("se visualizan los examenes realizados y sus respuestas")
    public void se_visualizan_los_examenes_realizados_y_sus_respuestas() {
        System.out.println("Verificando examenes realizados");
        
        Response respuesta = target
                .path("examen_realizado/{id}")
                .resolveTemplate("id", idExamenCreado)
                .request(MediaType.APPLICATION_JSON)
                .get();

        Assertions.assertEquals(200, respuesta.getStatus(), "Debe consultar el examen realizado");
        ExamenRealizado examenConsultado = respuesta.readEntity(ExamenRealizado.class);
        Assertions.assertNotNull(examenConsultado.getFechaRealizacion(), "La fecha del examen debe existir");
        System.out.println("Examen realizado verificado con fecha: " + examenConsultado.getFechaRealizacion());
    }
}
