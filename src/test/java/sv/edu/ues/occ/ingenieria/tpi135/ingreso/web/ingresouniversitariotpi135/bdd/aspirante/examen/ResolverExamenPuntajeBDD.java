package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.examen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class ResolverExamenPuntajeBDD {

    static Client cliente;
    static WebTarget target;

    // Seeds (solo cuando es necesario para enlazar FKs existentes)
    private static final UUID ID_EXAMEN_SEMILLA = UUID.fromString("0d000000-0000-0000-0000-000000000001");
    private static final UUID ID_ETAPA_SEMILLA = UUID.fromString("c1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_SEMILLA = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_OPCION_SEMILLA = UUID.fromString("0b000000-0000-0000-0000-000000000002");

    static ExamenesRealizado examenBase;

    static UUID idRespuestaCreada;
    static RespuestasExaman respuestaCreada;
    static OpcionesRespuesta opcionSeleccionada;

    private UUID extraerIdDelHeader(Response respuesta, String endpoint) {
        String location = respuesta.getHeaderString("Location");
        Assertions.assertNotNull(location, "Location no debe ser null");
        String[] parts = location.split(endpoint + "/");
        Assertions.assertTrue(parts.length >= 2, "Location no contiene el endpoint esperado: " + location);
        return UUID.fromString(parts[1]);
    }

    private Response hacerPost(String endpoint, Object entidad) {
        return target.path(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(entidad));
    }

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

    @Given("existe un examen realizado disponible")
    public void existe_un_examen_realizado_disponible() {
        Response resp = hacerGet("examenes_realizados", ID_EXAMEN_SEMILLA);
        Assertions.assertEquals(200, resp.getStatus());
        examenBase = resp.readEntity(ExamenesRealizado.class);
        Assertions.assertNotNull(examenBase);
        Assertions.assertEquals(ID_EXAMEN_SEMILLA, examenBase.getId());

        // El recurso es de solo lectura; validamos que ya exista puntaje y etapa
        Assertions.assertNotNull(examenBase.getPuntajeFinal(), "El examen semilla debe tener puntajeFinal para poder validarlo");
        Assertions.assertNotNull(examenBase.getIdEtapa(), "El examen debe tener etapa");
        Assertions.assertNotNull(examenBase.getIdClave(), "El examen debe tener clave");
        Assertions.assertNotNull(examenBase.getIdAsignacion(), "El examen debe tener asignacion");
    }

    @When("registro una respuesta de examen para una pregunta existente")
    public void registro_una_respuesta_de_examen_para_una_pregunta_existente() {
        RespuestasExaman nueva = new RespuestasExaman();

        ExamenesRealizado examenRef = new ExamenesRealizado();
        examenRef.setId(ID_EXAMEN_SEMILLA);
        nueva.setIdExamen(examenRef);

        BancoPregunta preguntaRef = new BancoPregunta();
        preguntaRef.setId(ID_PREGUNTA_SEMILLA);
        nueva.setIdPregunta(preguntaRef);

        OpcionesRespuesta opcionRef = new OpcionesRespuesta();
        opcionRef.setId(ID_OPCION_SEMILLA);
        nueva.setIdOpcionSeleccionada(opcionRef);

        Response resp = hacerPost("respuestas_examen", nueva);
        Assertions.assertEquals(201, resp.getStatus());
        Assertions.assertTrue(resp.getHeaders().containsKey("Location"));

        idRespuestaCreada = extraerIdDelHeader(resp, "respuestas_examen");
        Assertions.assertNotNull(idRespuestaCreada);

        Response respGet = hacerGet("respuestas_examen", idRespuestaCreada);
        Assertions.assertEquals(200, respGet.getStatus());
        respuestaCreada = respGet.readEntity(RespuestasExaman.class);
        Assertions.assertNotNull(respuestaCreada);
        Assertions.assertEquals(idRespuestaCreada, respuestaCreada.getId());

        if (respuestaCreada.getIdExamen() != null && respuestaCreada.getIdExamen().getId() != null) {
            Assertions.assertEquals(ID_EXAMEN_SEMILLA, respuestaCreada.getIdExamen().getId());
        }
        if (respuestaCreada.getIdPregunta() != null && respuestaCreada.getIdPregunta().getId() != null) {
            Assertions.assertEquals(ID_PREGUNTA_SEMILLA, respuestaCreada.getIdPregunta().getId());
        }
        if (respuestaCreada.getIdOpcionSeleccionada() != null && respuestaCreada.getIdOpcionSeleccionada().getId() != null) {
            Assertions.assertEquals(ID_OPCION_SEMILLA, respuestaCreada.getIdOpcionSeleccionada().getId());
        }
    }

    @Then("la respuesta queda persistida y consultable por examen")
    public void la_respuesta_queda_persistida_y_consultable_por_examen() {
        Assertions.assertNotNull(idRespuestaCreada);

        Response respList = target.path("respuestas_examen")
                .queryParam("examenId", ID_EXAMEN_SEMILLA.toString())
                .request(MediaType.APPLICATION_JSON)
                .get();
        Assertions.assertEquals(200, respList.getStatus());

        RespuestasExaman[] list = respList.readEntity(RespuestasExaman[].class);
        Assertions.assertNotNull(list);

        boolean encontrada = Arrays.stream(list)
                .filter(Objects::nonNull)
                .map(RespuestasExaman::getId)
                .anyMatch(idRespuestaCreada::equals);
        Assertions.assertTrue(encontrada, "La respuesta creada debe aparecer al filtrar por examenId");
    }

    @Then("la opcion seleccionada muestra detalle y si es correcta")
    public void la_opcion_seleccionada_muestra_detalle_y_si_es_correcta() {
        Response resp = hacerGet("opciones_respuesta", ID_OPCION_SEMILLA);
        Assertions.assertEquals(200, resp.getStatus());

        // Validar por JSON crudo para no depender del MessageBodyReader seleccionado por Jersey
        String json = resp.readEntity(String.class);
        Assertions.assertNotNull(json);
        Assertions.assertFalse(json.isBlank());

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            String debugJson = "JSON=" + json;
            Assertions.assertTrue(root.hasNonNull("id"));
            Assertions.assertEquals(ID_OPCION_SEMILLA.toString(), root.get("id").asText());

            Assertions.assertTrue(root.has("textoOpcion"), "textoOpcion debe existir. " + debugJson);
            Assertions.assertTrue(root.hasNonNull("textoOpcion"), "textoOpcion no debe ser null. " + debugJson);
            Assertions.assertEquals("4", root.get("textoOpcion").asText());

            Assertions.assertTrue(root.has("esCorrecta"), "esCorrecta debe existir. " + debugJson);
            Assertions.assertTrue(root.hasNonNull("esCorrecta"), "esCorrecta no debe ser null. " + debugJson);
            Assertions.assertTrue(root.get("esCorrecta").asBoolean(), "La opción semilla seleccionada debe ser correcta");
        } catch (Exception ex) {
            Assertions.fail("No se pudo parsear JSON de opciones_respuesta: " + ex.getMessage());
        }

        Response respFiltrado = target.path("opciones_respuesta")
                .queryParam("preguntaId", ID_PREGUNTA_SEMILLA.toString())
                .request(MediaType.APPLICATION_JSON)
                .get();
        Assertions.assertEquals(200, respFiltrado.getStatus());

        String jsonLista = respFiltrado.readEntity(String.class);
        Assertions.assertNotNull(jsonLista);
        Assertions.assertFalse(jsonLista.isBlank());

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode lista = mapper.readTree(jsonLista);
            Assertions.assertTrue(lista.isArray(), "La respuesta del filtro debe ser un arreglo JSON");

            boolean encontrada = false;
            for (JsonNode item : lista) {
                if (item != null && item.hasNonNull("id") && ID_OPCION_SEMILLA.toString().equals(item.get("id").asText())) {
                    encontrada = true;
                    break;
                }
            }
            Assertions.assertTrue(encontrada, "La opción seleccionada debe aparecer al filtrar por preguntaId");
        } catch (Exception ex) {
            Assertions.fail("No se pudo parsear JSON de opciones_respuesta (lista filtrada): " + ex.getMessage());
        }
    }

    @Then("el examen realizado muestra puntaje final consistente con la etapa")
    public void el_examen_realizado_muestra_puntaje_final_consistente_con_la_etapa() {
        Response respExamen = hacerGet("examenes_realizados", ID_EXAMEN_SEMILLA);
        Assertions.assertEquals(200, respExamen.getStatus());
        ExamenesRealizado examen = respExamen.readEntity(ExamenesRealizado.class);
        Assertions.assertNotNull(examen);
        Assertions.assertNotNull(examen.getPuntajeFinal(), "puntajeFinal no debe ser null");

        // Validar puntaje contra la etapa semilla (evita depender de serialización de relaciones LAZY)
        Response respEtapa = hacerGet("etapas_admision", ID_ETAPA_SEMILLA);
        Assertions.assertEquals(200, respEtapa.getStatus());
        EtapasAdmision etapa = respEtapa.readEntity(EtapasAdmision.class);
        Assertions.assertNotNull(etapa);

        BigDecimal puntaje = examen.getPuntajeFinal();
        Assertions.assertEquals(0, puntaje.compareTo(new BigDecimal("8.50")), "puntajeFinal debe coincidir con el valor semilla esperado");

        if (etapa.getPuntajeMinimo() != null) {
            Assertions.assertTrue(puntaje.compareTo(etapa.getPuntajeMinimo()) >= 0,
                    "puntajeFinal debe ser >= puntajeMinimo de la etapa");
        }
        if (etapa.getPuntajeMaximo() != null) {
            Assertions.assertTrue(puntaje.compareTo(etapa.getPuntajeMaximo()) <= 0,
                    "puntajeFinal debe ser <= puntajeMaximo de la etapa");
        }
    }

    @Then("el filtro por examenId invalido es rechazado")
    public void el_filtro_por_examen_id_invalido_es_rechazado() {
        Response resp = target.path("respuestas_examen")
                .queryParam("examenId", "no-es-uuid")
                .request(MediaType.APPLICATION_JSON)
                .get();
        Assertions.assertEquals(422, resp.getStatus());
    }
}
