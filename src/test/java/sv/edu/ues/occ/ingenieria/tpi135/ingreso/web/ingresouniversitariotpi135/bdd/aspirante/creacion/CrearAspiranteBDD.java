package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.creacion;

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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.BaseSistemaBDD;

import java.util.UUID;

public class CrearAspiranteBDD {


    static Client cliente;
    static WebTarget target;
      
      
        static AspirantesDato nuevo;
        static UUID idAspiranteCreado;
        static UUID idInscripcionCreada;
        private static final UUID ID_USUARIO = UUID.fromString("b1000000-0000-0000-0000-000000000001");
        private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");
        private static final String ID_CARRERA_SEMILLA = "ICS";


    @Given("se tiene un servidor corriendo con la aplicacion desplegada para crear aspirantes")
    public void se_tiene_un_servidor_corriendo_con_la_aplicacion_desplegada_para_crear_aspirantes() {
        System.out.println("Arrancando entorno de sistema (singleton BaseSistemaBDD)");
        BaseSistemaBDD.init();

        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());

    }

    @When("puedo crear un aspirante")
    public void puedo_crear_un_aspirante() {
        System.out.println("crear aspirante");
        nuevo = new AspirantesDato();
        nuevo.setNombres("Jose");
        nuevo.setApellidos("Morales");
        nuevo.setDui("12345678-9");
        // Legacy UsuariosSistema removed: set correo directly on aspirante
        nuevo.setCorreo("usuario.test@local");
        nuevo.setUsaSillaRuedas(false);
        int esperado = 201;
        Response respuesta = target
                .path("aspirantes_datos")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(nuevo));
        Assertions.assertEquals(esperado,respuesta.getStatus());
        Assertions.assertTrue(respuesta.getHeaders().containsKey("Location"));
        UUID id = UUID.fromString(respuesta.getHeaderString("Location").split("aspirantes_datos/")[1]);
            Assertions.assertNotNull(id);
            idAspiranteCreado = id;
    }


    @When("puedo asociarle a una opcion de carrera, por ejemplo {word}")
    public void puedo_asociarle_a_una_opcion_de_carrera_por_ejemplo_i30515(String codigoCarrera) {
            Assertions.assertNotNull(codigoCarrera);
            Assertions.assertNotNull(idAspiranteCreado);

            InscripcionesPrueba inscripcion = new InscripcionesPrueba();
            AspirantesDato aspiranteRef = new AspirantesDato();
            aspiranteRef.setId(idAspiranteCreado);
            inscripcion.setIdAspirante(aspiranteRef);
            PruebasAdmision pruebaRef = new PruebasAdmision();
            pruebaRef.setId(ID_PRUEBA_SEMILLA);
            inscripcion.setIdPrueba(pruebaRef);
            inscripcion.setEstado("INSCRITO");

            Response respuestaInscripcion = target
                .path("inscripciones_prueba")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(inscripcion));
            Assertions.assertEquals(201, respuestaInscripcion.getStatus());
            String locationInscripcion = respuestaInscripcion.getHeaderString("Location");
            idInscripcionCreada = UUID.fromString(locationInscripcion.split("inscripciones_prueba/")[1]);

            CarrerasElegidaId pk = new CarrerasElegidaId();
            pk.setIdInscripcion(idInscripcionCreada);
            pk.setIdCarrera(ID_CARRERA_SEMILLA);

            CarrerasElegida carreraElegida = new CarrerasElegida();
            carreraElegida.setId(pk);

            InscripcionesPrueba inscripcionRef = new InscripcionesPrueba();
            inscripcionRef.setId(idInscripcionCreada);
            carreraElegida.setIdInscripcion(inscripcionRef);

            CatalogoCarrera carreraRef = new CatalogoCarrera();
            carreraRef.setIdCarrera(ID_CARRERA_SEMILLA);
            carreraElegida.setIdCarrera(carreraRef);
            carreraElegida.setPrioridad((short) 1);

            Response respuestaCarrera = target
                .path("carreras_elegidas")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(carreraElegida));
            Assertions.assertEquals(201, respuestaCarrera.getStatus());

    }
    @Then("puedo consular el perfil del aspirante recien creado")
    public void puedo_consular_el_perfil_del_aspirante_recien_creado() {
        System.out.println("consultar perfil aspirante");
        Response respuesta = target
                .path("aspirantes_datos/{id}")
                .resolveTemplate("id", idAspiranteCreado)
                .request(MediaType.APPLICATION_JSON)
                .get();
        Assertions.assertEquals(200, respuesta.getStatus());
        AspirantesDato encontrado = respuesta.readEntity(AspirantesDato.class);
        Assertions.assertEquals(idAspiranteCreado, encontrado.getId());

    }

    @Then("verificar la opcion de carrera a la que fue asociado")
    public void verificar_la_opcion_de_carrera_a_la_que_fue_asociado() {
        System.out.println("verificar opcion de carrera");
        Assertions.assertNotNull(idInscripcionCreada);

        Response respuesta = target
                .path("carreras_elegidas/{idInscripcion}/{idCarrera}")
                .resolveTemplate("idInscripcion", idInscripcionCreada)
                .resolveTemplate("idCarrera", ID_CARRERA_SEMILLA)
                .request(MediaType.APPLICATION_JSON)
                .get();

        Assertions.assertEquals(200, respuesta.getStatus());
        CarrerasElegida encontrada = respuesta.readEntity(CarrerasElegida.class);
        Assertions.assertEquals(idInscripcionCreada, encontrada.getId().getIdInscripcion());
        Assertions.assertEquals(ID_CARRERA_SEMILLA, encontrada.getId().getIdCarrera());
        Assertions.assertEquals(Short.valueOf((short) 1), encontrada.getPrioridad());
    }
}
