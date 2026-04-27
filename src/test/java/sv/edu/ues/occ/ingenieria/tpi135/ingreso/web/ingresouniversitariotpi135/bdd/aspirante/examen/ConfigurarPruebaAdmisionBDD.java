package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.examen;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.BaseSistemaBDD;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigurarPruebaAdmisionBDD {
    static Client cliente;
    static WebTarget target;

    static UUID idArea;
    static UUID idPrueba;
    static UUID idPregunta;
    static UUID idClave;

    static Response ultimaRespuesta;

    private UUID extraerIdDelHeader(Response respuesta) {
        String location = respuesta.getHeaderString("Location");
        assertNotNull(location, "El header Location no debe ser nulo");
        String id = location.substring(location.lastIndexOf('/') + 1);
        return UUID.fromString(id);
    }

    @Given("el servidor esta inicializado para la administracion academica")
    public void el_servidor_esta_inicializado_para_la_administracion_academica() {
        System.out.println("\n==========================================================================");
        System.out.println("Inicializando la administración académica y conectando al cliente...");
        System.out.println("==========================================================================\n");
        BaseSistemaBDD.init();
        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());
    }

    @Given("existe un area de conocimiento llamada {string}")
    public void existe_un_area_de_conocimiento_llamada(String nombreArea) {

        AreasConocimiento area = new AreasConocimiento();
        area.setNombreArea(nombreArea);

        Response resp = postEntity("areas_conocimiento", area);

        assertEquals(201, resp.getStatus(), "Debe crear el área");
        idArea = extraerIdDelHeader(resp);
        System.out.println("Área creada con ID: " + idArea + "\n");
    }

    @When("el administrador registra la pregunta {string} para el area de {string}")
    public void el_administrador_registra_la_pregunta_para_el_area_de(String textoPregunta, String nombreArea) {

        AreasConocimiento refArea = new AreasConocimiento();
        refArea.setId(idArea);

        //BANCO DE PREGUNTAS
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setEnunciado(textoPregunta);
        pregunta.setIdArea(refArea);

        Response resp = postEntity("banco_preguntas", pregunta);
        assertEquals(201, resp.getStatus(), "Debe crear la pregunta");
        idPregunta = extraerIdDelHeader(resp);
        System.out.println("Pregunta creada con ID: " + idPregunta + "\n");
    }

    @When("asigna {string} como la respuesta correcta")
    public void asigna_como_la_respuesta_correcta(String opcion) {

        crearOpcionRespuesta(opcion, true);
    }

    @When("asigna {string}, {string} y {string} como las respuestas trampa")
    public void asigna_y_como_las_respuestas_trampa(String op1, String op2, String op3) {
        crearOpcionRespuesta(op1, false);
        crearOpcionRespuesta(op2, false);
        crearOpcionRespuesta(op3, false);
    }

    @When("crea una nueva configuracion de examen llamada {string} para la prueba actual")
    public void crea_una_nueva_configuracion_de_examen_llamada_para_la_prueba_actual(String nombreClave) {

        // 1. Crear Prueba Global
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setNombrePrueba("Prueba Admision BDD");
        prueba.setAnio(2025);

        Response respP = postEntity("pruebas_admision", prueba);
        assertEquals(201, respP.getStatus(), "Debe crear la prueba global");
        idPrueba = extraerIdDelHeader(respP);
        System.out.println("Prueba Global creada con ID: " + idPrueba);

        System.out.println("Creando la versión del examen (Clave): " + nombreClave);
        // 2. Crear Clave Examen
        PruebasAdmision refPrueba = new PruebasAdmision();
        refPrueba.setId(idPrueba);

        ClavesExaman clave = new ClavesExaman();
        clave.setNombreClave(nombreClave);
        clave.setIdPrueba(refPrueba);

        Response respC = postEntity("claves_examen", clave);
        assertEquals(201, respC.getStatus(), "Debe crear la clave");
        idClave = extraerIdDelHeader(respC);
    }

    @When("asocia esta pregunta a la {string}")
    public void asocia_esta_pregunta_a_la(String nombreClave) {
        //Creamos la Llave Primaria Compuesta
        PreguntasPorClaveId pk = new PreguntasPorClaveId();
        pk.setIdClave(idClave);
        pk.setIdPregunta(idPregunta);

        // Entidades de referencia
        ClavesExaman clave = new ClavesExaman();
        clave.setId(idClave);

        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setId(idPregunta);

        //Armamos la entidad principal
        PreguntasPorClave relacion = new PreguntasPorClave();
        relacion.setId(pk);
        relacion.setIdClave(clave);
        relacion.setIdPregunta(pregunta);

        //Se lo pasas directamente al método auxiliar
        ultimaRespuesta = postEntity("preguntas_por_clave", relacion);
    }

    @Then("el sistema guarda la pregunta y sus opciones en el banco de preguntas exitosamente")
    public void el_sistema_guarda_la_pregunta_y_sus_opciones_en_el_banco_de_preguntas_exitosamente() {
        assertEquals(201, ultimaRespuesta.getStatus(), "La relación Clave-Pregunta debe registrarse con 201");
        System.out.println("Asociación registrada correctamente en BD.\n");
    }

    @Then("al consultar la {string} mediante HTTP, el sistema devuelve la estructura del examen lista para los aspirantes")
    public void al_consultar_la_mediante_http_el_sistema_devuelve_la_estructura_del_examen_lista_para_los_aspirantes(String nombreClave) {
        System.out.println("Realizando petición GET para validar que el examen existe...");

        Response resp = target.path("preguntas_por_clave/" + idClave + "/" + idPregunta)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, resp.getStatus(), "Debe poder leer la relación Clave-Pregunta recién creada");

        String cuerpo = resp.readEntity(String.class);
        System.out.println("XXXXXXXXXXXXXXXXXXXXXX");
        System.out.println("JSON recuperado del servidor: \n" + cuerpo + "\n");
        System.out.println("XXXXXXXXXXXXXXXXXXXXXX");
        assertTrue(cuerpo.contains(idClave.toString()), "El JSON debe contener el ID de la clave");
        assertTrue(cuerpo.contains(idPregunta.toString()), "El JSON debe contener el ID de la pregunta");

    }

    // Método auxiliar para enviar entidades Java y convertirlas a JSON automáticamente
    private Response postEntity(String endpoint, Object entidad) {
        return target.path(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(entidad));
    }

    private void crearOpcionRespuesta(String textoOpcion, boolean esCorrecta) {
        BancoPregunta refPregunta = new BancoPregunta();
        refPregunta.setId(idPregunta);

        OpcionesRespuesta opcion = new OpcionesRespuesta();
        opcion.setTextoOpcion(textoOpcion);
        opcion.setEsCorrecta(esCorrecta);
        opcion.setIdPregunta(refPregunta);

        Response resp = postEntity("opciones_respuesta", opcion);
        assertEquals(201, resp.getStatus(), "Debe insertar la opción " + textoOpcion);
        System.out.println("Opción guardada: '" + textoOpcion + "' | Correcta: " + esCorrecta);
    }
}