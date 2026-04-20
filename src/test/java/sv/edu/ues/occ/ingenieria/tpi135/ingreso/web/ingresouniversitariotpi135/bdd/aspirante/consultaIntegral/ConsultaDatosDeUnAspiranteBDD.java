package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.consultaIntegral;

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
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
public class ConsultaDatosDeUnAspiranteBDD {

    static Client client;
    static WebTarget target;

    // IDs de entidades creadas durante el escenario
    static UUID idAspirante;
    static UUID idInscripcion;
    static UUID idTurno;
    static UUID idAula;
    static UUID idAsignacion;

    // Variables para GUARDAR las respuestas de los GET
    private AspirantesDato aspiranteRecuperado;
    private InscripcionesPrueba inscripcionRecuperada;
    private CarrerasElegida carreraRecuperada;
    private AsignacionesAulaPupitre asignacionRecuperada;

    // Valores esperados creados durante el escenario
    private String nombresEsperados;
    private String apellidosEsperados;
    private String duiEsperado;
    private boolean usaSillaRuedasEsperado;
    private String estadoInscripcionEsperado;
    private String pupitreEsperado;

    // IDs semilla reutilizadas de datos existentes
    private static final UUID ID_USUARIO = UUID.fromString("b1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final String ID_CARRERA_SEMILLA = "ICS";

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Metodos auxiliares

    /**
     * Extrae UUID del header Location de una respuesta POST
     */
    private UUID extraerIdDelHeader(Response respuesta, String endpoint) {
        String location = respuesta.getHeaderString("Location");
        Assertions.assertNotNull(location, "Location no debe ser null");
        String[] parts = location.split(endpoint + "/");
        Assertions.assertTrue(parts.length >= 2, "Location no contiene el endpoint esperado: " + location);
        return UUID.fromString(parts[1]);
    }

    /**
     * Realiza POST a un endpoint y retorna la respuesta
     */
    private Response hacerPost(String endpoint, Object entidad) {
        return target
                .path(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(entidad));
    }

    /**
     * Realiza GET a un endpoint por ID y retorna la respuesta
     */
    private Response hacerGet(String endpoint, UUID id) {
        return target
                .path(endpoint + "/{id}")
                .resolveTemplate("id", id)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    /**
     * Obtiene una asignación y valida que exista
     */
    private AsignacionesAulaPupitre obtenerAsignacion(UUID id) {
        Response respuesta = hacerGet("asignaciones_aula_pupitre", id);
        Assertions.assertEquals(200, respuesta.getStatus());
        AsignacionesAulaPupitre asignacion = respuesta.readEntity(AsignacionesAulaPupitre.class);
        Assertions.assertNotNull(asignacion);
        Assertions.assertEquals(id, asignacion.getId());
        return asignacion;
    }

    @Given("se tiene un servidor corriendo con la aplicacion desplegada para consultar datos de un aspirante")
    public void se_tiene_un_servidor_corriendo_con_la_aplicacion_desplegada_para_consultar_datos_de_un_aspirante(){

        BaseSistemaBDD.init();

        client= BaseSistemaBDD.getClient();
        target = client.target(BaseSistemaBDD.getBaseUrl());

    }

    @Given("se crea el expediente de un aspirante con inscripcion, carrera y aula asignada")
    public void se_crea_el_expediente_de_un_aspirante_con_inscripcion_carrera_y_aula_asignada(){

        // ===== 1. Crear aspirante =====
        nombresEsperados = "Carlos";
        apellidosEsperados = "López";
        duiEsperado = "98765432-1";
        usaSillaRuedasEsperado = false;

        AspirantesDato nuevoAspirante = new AspirantesDato();
        nuevoAspirante.setNombres(nombresEsperados);
        nuevoAspirante.setApellidos(apellidosEsperados);
        nuevoAspirante.setDui(duiEsperado);
        UsuariosSistema usuario = new UsuariosSistema();
        usuario.setId(ID_USUARIO);
        nuevoAspirante.setIdUsuario(usuario);
        nuevoAspirante.setUsaSillaRuedas(usaSillaRuedasEsperado);

        Response respuestaAspirante = hacerPost("aspirantes_datos", nuevoAspirante);
        Assertions.assertEquals(201, respuestaAspirante.getStatus());
        idAspirante = extraerIdDelHeader(respuestaAspirante, "aspirantes_datos");
        System.out.println("Aspirante creado: " + idAspirante);

        // ===== 2. Crear inscripción a la prueba =====
        InscripcionesPrueba inscripcion = new InscripcionesPrueba();
        AspirantesDato aspiranteRef = new AspirantesDato();
        aspiranteRef.setId(idAspirante);
        inscripcion.setIdAspirante(aspiranteRef);
        PruebasAdmision pruebaRef = new PruebasAdmision();
        pruebaRef.setId(ID_PRUEBA_SEMILLA);
        inscripcion.setIdPrueba(pruebaRef);
        estadoInscripcionEsperado = "INSCRITO";
        inscripcion.setEstado(estadoInscripcionEsperado);

        Response respuestaInscripcion = hacerPost("inscripciones_prueba", inscripcion);
        Assertions.assertEquals(201, respuestaInscripcion.getStatus());
        idInscripcion = extraerIdDelHeader(respuestaInscripcion, "inscripciones_prueba");
        System.out.println("Inscripción creada: " + idInscripcion);

        // ===== 3. Crear relación carrera elegida =====
        CarrerasElegidaId pk = new CarrerasElegidaId();
        pk.setIdInscripcion(idInscripcion);
        pk.setIdCarrera(ID_CARRERA_SEMILLA);
        CarrerasElegida carreraElegida = new CarrerasElegida();
        carreraElegida.setId(pk);
        InscripcionesPrueba inscripcionRef = new InscripcionesPrueba();
        inscripcionRef.setId(idInscripcion);
        carreraElegida.setIdInscripcion(inscripcionRef);
        CatalogoCarrera carreraRef = new CatalogoCarrera();
        carreraRef.setIdCarrera(ID_CARRERA_SEMILLA);
        carreraElegida.setIdCarrera(carreraRef);
        carreraElegida.setPrioridad((short) 1);

        Response respuestaCarrera = hacerPost("carreras_elegidas", carreraElegida);
        Assertions.assertEquals(201, respuestaCarrera.getStatus());
        System.out.println("Carrera elegida asociada: " + ID_CARRERA_SEMILLA);

        // 4. Crear turno para la prueba
        TurnosExaman nuevoTurno = new TurnosExaman();
        nuevoTurno.setNombreTurno("Turno Matutino Consulta");
        PruebasAdmision pruebaTurnoRef = new PruebasAdmision();
        pruebaTurnoRef.setId(ID_PRUEBA_SEMILLA);
        nuevoTurno.setIdPrueba(pruebaTurnoRef);
        nuevoTurno.setFecha(java.time.LocalDate.now());
        nuevoTurno.setHoraInicio(java.time.LocalTime.of(8, 0));
        nuevoTurno.setHoraFin(java.time.LocalTime.of(10, 0));

        Response respuestaTurno = hacerPost("turnos_examen", nuevoTurno);
        Assertions.assertEquals(201, respuestaTurno.getStatus());
        idTurno = extraerIdDelHeader(respuestaTurno, "turnos_examen");
        System.out.println("Turno creado: " + idTurno);

        // 5. Crear aula de examen para ese turno
        AulasExaman nuevoAula = new AulasExaman();
        TurnosExaman turnoRef = new TurnosExaman();
        turnoRef.setId(idTurno);
        nuevoAula.setIdTurno(turnoRef);
        nuevoAula.setIdAulaApi("AULA-CONSULTA-001");
        nuevoAula.setCapacidad(40);
        nuevoAula.setCuposOcupados(0);
        nuevoAula.setAccesibleSillaRuedas(false);

        Response respuestaAula = hacerPost("aulas_examen", nuevoAula);
        Assertions.assertEquals(201, respuestaAula.getStatus());
        idAula = extraerIdDelHeader(respuestaAula, "aulas_examen");
        System.out.println("Aula creada: " + idAula);

        // ===== 6. Asignar aula y pupitre a la inscripción =====
        AsignacionesAulaPupitre asignacion = new AsignacionesAulaPupitre();
        InscripcionesPrueba inscripcionRefA_P = new InscripcionesPrueba();
        inscripcionRefA_P.setId(idInscripcion);
        asignacion.setIdInscripcion(inscripcionRefA_P);

        AulasExaman aulaRef = new AulasExaman();
        aulaRef.setId(idAula);
        asignacion.setIdAula(aulaRef);

        pupitreEsperado = "Pupitre A-101";
        asignacion.setPupitre(pupitreEsperado);

        Response respuestaAsignacion = hacerPost("asignaciones_aula_pupitre", asignacion);
        Assertions.assertEquals(201, respuestaAsignacion.getStatus());
        idAsignacion = extraerIdDelHeader(respuestaAsignacion, "asignaciones_aula_pupitre");
        System.out.println("Asignación creada: " + idAsignacion);

    }

    @When("consulto la informacion ejecutiva del aspirante mediante HTTP")
    public void consulto_la_informacion_ejecutiva_del_aspirante_mediante_HTTP(){

        //Extraemos la informacion del aspirante creado
        Response response = hacerGet("aspirantes_datos", idAspirante);
        assertEquals(200, response.getStatus());
        aspiranteRecuperado = response.readEntity(AspirantesDato.class);

        //Verificamos la inscripcion del aspirante
        Response resInscripcion = hacerGet("inscripciones_prueba", idInscripcion);
        Assertions.assertEquals(200, resInscripcion.getStatus());
        inscripcionRecuperada = resInscripcion.readEntity(InscripcionesPrueba.class);

        //Verficamos la asignacion de AULA-PUPITRE del alumno
        Response resAsignacion = hacerGet("asignaciones_aula_pupitre",idAsignacion);
        Assertions.assertEquals(200, resAsignacion.getStatus());
        asignacionRecuperada =  resAsignacion.readEntity(AsignacionesAulaPupitre.class);

        Response resCarrera = target
                .path("carreras_elegidas/{idInscripcion}/{idCarrera}")
                .resolveTemplate("idInscripcion", idInscripcion)
                .resolveTemplate("idCarrera", ID_CARRERA_SEMILLA)
                .request(MediaType.APPLICATION_JSON)
                .get();
        Assertions.assertEquals(200, resCarrera.getStatus());
        carreraRecuperada = resCarrera.readEntity(CarrerasElegida.class);
    }

    @Then("el sistema responde exitosamente")
    public void el_sistema_responde_exitosamente(){
        //Verifivamos que los datos no sean nulos
        assertNotNull(aspiranteRecuperado);
        assertNotNull(inscripcionRecuperada);
        assertNotNull(asignacionRecuperada);
        assertNotNull(carreraRecuperada);
    }

    @Then("la informacion personal devuelta coincide con los datos originales")
    public void la_informacion_personal_devuelta_coincide_con_los_datos_originales(){
        assertEquals(nombresEsperados, aspiranteRecuperado.getNombres());
        assertEquals(apellidosEsperados, aspiranteRecuperado.getApellidos());
        assertEquals(duiEsperado, aspiranteRecuperado.getDui());
        assertEquals(Boolean.valueOf(usaSillaRuedasEsperado), aspiranteRecuperado.getUsaSillaRuedas());
    }

    @Then("los datos de su inscripcion y carrera elegida son consistentes")
    public void los_datos_de_su_inscripcion_y_carrera_elegida_son_consistentes(){
        assertEquals(estadoInscripcionEsperado, inscripcionRecuperada.getEstado());
        assertEquals(idAspirante, inscripcionRecuperada.getIdAspirante().getId());
        assertNotNull(inscripcionRecuperada.getIdPrueba());
        assertEquals(ID_PRUEBA_SEMILLA, inscripcionRecuperada.getIdPrueba().getId());

        assertNotNull(carreraRecuperada);
        assertNotNull(carreraRecuperada.getId(), "CarrerasElegida.id (PK embebida) no debe ser null");
        assertEquals(idInscripcion, carreraRecuperada.getId().getIdInscripcion());
        assertEquals(ID_CARRERA_SEMILLA, carreraRecuperada.getId().getIdCarrera());
        assertEquals(Short.valueOf((short) 1), carreraRecuperada.getPrioridad());
    }


    @Then("la asignacion de aula y pupitre corresponde a la inscripcion")
    public void la_asignacion_de_aula_y_pupitre_corresponde_a_la_inscripcion(){
        assertEquals(idInscripcion, asignacionRecuperada.getIdInscripcion().getId());
        assertNotNull(asignacionRecuperada.getIdAula());
        assertEquals(idAula, asignacionRecuperada.getIdAula().getId());
        assertEquals(pupitreEsperado, asignacionRecuperada.getPupitre());
    }

}
