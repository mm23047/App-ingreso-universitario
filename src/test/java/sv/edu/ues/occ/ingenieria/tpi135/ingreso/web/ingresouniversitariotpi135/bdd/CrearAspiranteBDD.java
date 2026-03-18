package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.MountableFile;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AspirantesDato;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.io.support.ClassicRequestBuilder.post;

public class CrearAspiranteBDD {


     static Client cliente;
    static WebTarget target;

    static  Network red = Network.newNetwork();

    static MountableFile war = MountableFile.forHostPath(
            Paths.get("target/IngresoUniversitarioTPI135-1.0-SNAPSHOT.war").toAbsolutePath());

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123")
            .withNetwork(red)
            .withNetworkAliases("db")
            .withExposedPorts(5432);

    @Container
    protected static final GenericContainer<?> liberty = new GenericContainer<>("ingresouniversitariotpi135-base:26.0.0.2")
            .withNetwork(red)
            .withEnv("PGSERVER", "db")
            .withEnv("PGPORT", "5432")
            .withEnv("PGDBNAME", "ingresoTPI135")
            .withEnv("PGUSER", "postgres")
            .withEnv("PGPASSWORD", "abc123")
            .dependsOn(postgres)
            .withCopyFileToContainer(war, "/opt/wlp/usr/servers/tpi135_2026/dropins/ingreso.war")
            .withExposedPorts(9080);
      
      
        static AspirantesDato nuevo;
        static UUID idAspiranteCreado;
        static UUID idInscripcionCreada;
        private static final UUID ID_USUARIO = UUID.fromString("b1000000-0000-0000-0000-000000000001");
        private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");
        private static final String ID_CARRERA_SEMILLA = "ICS";


    @Given("se tiene un servidor corriendo con la aplicacion desplegada")
    public void se_tiene_un_servidor_corriendo_con_la_aplicacion_desplegada() {
        System.out.println("Arrancando openliberty");
        Startables.deepStart(List.of(postgres, liberty)).join();
        Assertions.assertTrue(liberty.isRunning());
        cliente = ClientBuilder.newClient();
        target = cliente.target(String.format("http://localhost:%d/ingreso/resources/v1", liberty.getMappedPort(9080)));

    }

    @When("puedo crear un aspirante")
    public void puedo_crear_un_aspirante() {
        System.out.println("crear aspirante");
        nuevo = new AspirantesDato();
        nuevo.setNombres("Jose");
        nuevo.setApellidos("Morales");
        nuevo.setDui("12345678-9");
        UsuariosSistema usuario = new UsuariosSistema();
        usuario.setId(ID_USUARIO);
        nuevo.setIdUsuario(usuario);
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
