package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.examen;

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

import java.math.BigDecimal;
import java.util.UUID;

public class ResolverExamenPuntajeBDD {

    static Client cliente;
    static WebTarget target;

    private static final UUID ID_EXAMEN_SEMILLA = UUID.fromString("0d000000-0000-0000-0000-000000000002");
    private static final UUID ID_INSCRIPCION_SEMILLA = UUID.fromString("09000000-0000-0000-0000-000000000002");

    static ExamenesRealizado examenAntes;
    static ExamenesRealizado examenCalificado;

    private Response hacerGet(String endpoint, UUID id) {
        return target.path(endpoint + "/{id}")
                .resolveTemplate("id", id)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    @Given("se tiene un servidor corriendo con la aplicacion desplegada para resolver examen")
    public void se_tiene_un_servidor_corriendo_con_la_aplicacion_desplegada_para_resolver_examen() {
        BaseSistemaBDD.init();
        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());
    }

    @Given("existe un examen realizado con respuestas registradas")
    public void existe_un_examen_realizado_con_respuestas_registradas() {
        Response resp = hacerGet("examenes_realizados", ID_EXAMEN_SEMILLA);
        Assertions.assertEquals(200, resp.getStatus());
        examenAntes = resp.readEntity(ExamenesRealizado.class);
        Assertions.assertNotNull(examenAntes);
        Assertions.assertEquals(ID_EXAMEN_SEMILLA, examenAntes.getId());
        Assertions.assertNotNull(examenAntes.getPuntajeFinal());
        Assertions.assertNotNull(examenAntes.getIdClave());
        Assertions.assertNotNull(examenAntes.getIdEtapa());
        Assertions.assertNotNull(examenAntes.getIdAsignacion());
    }

    @When("solicito la calificacion del examen")
    public void solicito_la_calificacion_del_examen() {
        Response resp = target.path("examenes_realizados/{id}/calificar")
                .resolveTemplate("id", ID_EXAMEN_SEMILLA)
                .request(MediaType.APPLICATION_JSON)
            .post(Entity.text(""));

        Assertions.assertEquals(200, resp.getStatus());
        examenCalificado = resp.readEntity(ExamenesRealizado.class);
        Assertions.assertNotNull(examenCalificado);
        Assertions.assertEquals(ID_EXAMEN_SEMILLA, examenCalificado.getId());
    }

    @Then("el examen queda con puntaje final recalculado")
    public void el_examen_queda_con_puntaje_final_recalculado() {
        Assertions.assertNotNull(examenAntes);
        Assertions.assertNotNull(examenCalificado);
        Assertions.assertNotNull(examenCalificado.getPuntajeFinal());
        Assertions.assertEquals(0, new BigDecimal("5.00").compareTo(examenCalificado.getPuntajeFinal()));
        Assertions.assertTrue(examenCalificado.getPuntajeFinal().compareTo(examenAntes.getPuntajeFinal()) != 0,
                "El puntaje debe cambiar tras recalcularse");
    }

    @Then("la inscripcion asociada al examen queda en estado CALIFICADO")
    public void la_inscripcion_asociada_al_examen_queda_en_estado_calificado() {
        Response resp = hacerGet("inscripciones_prueba", ID_INSCRIPCION_SEMILLA);
        Assertions.assertEquals(200, resp.getStatus());
        InscripcionesPrueba inscripcion = resp.readEntity(InscripcionesPrueba.class);
        Assertions.assertNotNull(inscripcion);
        Assertions.assertEquals("CALIFICADO", inscripcion.getEstado());
    }

    @Then("el examen calificado es consultable por su id")
    public void el_examen_calificado_es_consultable_por_su_id() {
        Response resp = hacerGet("examenes_realizados", ID_EXAMEN_SEMILLA);
        Assertions.assertEquals(200, resp.getStatus());
        ExamenesRealizado examenPersistido = resp.readEntity(ExamenesRealizado.class);
        Assertions.assertNotNull(examenPersistido);
        Assertions.assertNotNull(examenPersistido.getPuntajeFinal());
        Assertions.assertEquals(0, new BigDecimal("5.00").compareTo(examenPersistido.getPuntajeFinal()));

        Response filtro = target.path("respuestas_examen")
                .queryParam("examenId", ID_EXAMEN_SEMILLA.toString())
                .request(MediaType.APPLICATION_JSON)
                .get();
        Assertions.assertEquals(200, filtro.getStatus());
        RespuestasExaman[] respuestas = filtro.readEntity(RespuestasExaman[].class);
        Assertions.assertNotNull(respuestas);
        Assertions.assertTrue(respuestas.length > 0);
    }
}
