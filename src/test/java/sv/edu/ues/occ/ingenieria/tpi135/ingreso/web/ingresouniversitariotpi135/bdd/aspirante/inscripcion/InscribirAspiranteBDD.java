package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.inscripcion;

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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.BaseSistemaBDD;

import java.util.UUID;

/**
 * Step definitions para el scenario: Inscribir un aspirante en una prueba de admision
 */
public class InscribirAspiranteBDD {

    private static Client cliente;
    private static WebTarget target;
    private static UUID idAspiranteCreado;
    private static UUID idInscripcionCreada;

    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");

    @Dado("se tiene un servidor corriendo con la aplicacion desplegada para inscribir aspirantes")
    public void se_tiene_un_servidor_corriendo_con_la_aplicacion_desplegada_para_inscribir_aspirantes() {
        System.out.println("Iniciando entorno de sistema para inscripciones");
        BaseSistemaBDD.init();
        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());
    }

    @Y("se tiene un aspirante creado")
    public void se_tiene_un_aspirante_creado() {
        System.out.println("Creando aspirante para inscripcion");
        AspirantesDato nuevo = new AspirantesDato();
        nuevo.setNombres("Juan");
        nuevo.setApellidos("Perez");
        nuevo.setDui("98765432-1");
        nuevo.setCorreo("juan.perez@local");
        nuevo.setUsaSillaRuedas(false);

        Response respuesta = target
                .path("aspirantes_datos")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(nuevo));

        Assertions.assertEquals(201, respuesta.getStatus(), "El aspirante debe crearse exitosamente");
        Assertions.assertTrue(respuesta.getHeaders().containsKey("Location"), "Debe devolver Location header");
        
        UUID id = UUID.fromString(respuesta.getHeaderString("Location").split("aspirantes_datos/")[1]);
        Assertions.assertNotNull(id);
        idAspiranteCreado = id;
        System.out.println("Aspirante creado con ID: " + idAspiranteCreado);
    }

    @Cuando("el aspirante se inscribe en una prueba de admision {int}")
    public void el_aspirante_se_inscribe_en_una_prueba_de_admision(Integer anio) {
        System.out.println("Inscribiendo aspirante en prueba de admision " + anio);
        Assertions.assertNotNull(idAspiranteCreado, "El aspirante debe estar creado");

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

        Assertions.assertEquals(201, respuestaInscripcion.getStatus(), "La inscripcion debe crearse exitosamente");
        String locationInscripcion = respuestaInscripcion.getHeaderString("Location");
        idInscripcionCreada = UUID.fromString(locationInscripcion.split("inscripciones_prueba/")[1]);
        System.out.println("Inscripcion creada con ID: " + idInscripcionCreada);
    }

    @Entonces("la inscripcion se registra exitosamente")
    public void la_inscripcion_se_registra_exitosamente() {
        System.out.println("Verificando que la inscripcion se registro exitosamente");
        Assertions.assertNotNull(idInscripcionCreada, "La inscripcion debe haberse creado");
    }

    @Y("el estado de la inscripcion es INSCRITO")
    public void el_estado_de_la_inscripcion_es_INSCRITO() {
        System.out.println("Verificando estado de la inscripcion");
        Assertions.assertNotNull(idInscripcionCreada, "La inscripcion debe existir");

        Response respuesta = target
                .path("inscripciones_prueba/{id}")
                .resolveTemplate("id", idInscripcionCreada)
                .request(MediaType.APPLICATION_JSON)
                .get();

        Assertions.assertEquals(200, respuesta.getStatus(), "Debe consultar la inscripcion exitosamente");
        InscripcionesPrueba inscripcionConsultada = respuesta.readEntity(InscripcionesPrueba.class);
        Assertions.assertEquals("INSCRITO", inscripcionConsultada.getEstado(), "El estado debe ser INSCRITO");
        System.out.println("Estado verificado: " + inscripcionConsultada.getEstado());
    }
}
