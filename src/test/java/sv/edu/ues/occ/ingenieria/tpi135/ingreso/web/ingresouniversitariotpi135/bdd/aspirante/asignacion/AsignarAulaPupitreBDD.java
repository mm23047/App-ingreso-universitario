package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.asignacion;

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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class AsignarAulaPupitreBDD {

    static Client cliente;
    static WebTarget target;

    // IDs de entidades creadas durante el escenario
    static UUID idAspirante;
    static UUID idInscripcion;
    static UUID idTurno;
    static UUID idAula;
    static UUID idAsignacion;

    // IDs semilla reutilizadas de datos existentes
    private static final UUID ID_USUARIO = UUID.fromString("b1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final String ID_CARRERA_SEMILLA = "ICS";

    // ===== Métodos auxiliares para reducir duplicación =====

    /**
     * Extrae UUID del header Location de una respuesta POST
     */
    private UUID extraerIdDelHeader(Response respuesta, String endpoint) {
        String location = respuesta.getHeaderString("Location");
        return UUID.fromString(location.split(endpoint + "/")[1]);
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

    // ===== Steps de Cucumber =====

    @Given("se tiene un servidor corriendo con la aplicación desplegada")
    public void se_tiene_un_servidor_corriendo_con_la_aplicacion_desplegada() {
        System.out.println("Iniciando infraestructura singleton de pruebas BDD");
        BaseSistemaBDD.init();

        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());
    }

    @Given("existe un aspirante inscrito en una prueba de admisión y turno específico")
    public void existe_un_aspirante_inscrito_en_una_prueba_de_admision_y_turno_especifico() {
        System.out.println("Preparando aspirante inscrito en prueba y turno");

        // ===== 1. Crear aspirante =====
        AspirantesDato nuevoAspirante = new AspirantesDato();
        nuevoAspirante.setNombres("Carlos");
        nuevoAspirante.setApellidos("López");
        nuevoAspirante.setDui("98765432-1");
        UsuariosSistema usuario = new UsuariosSistema();
        usuario.setId(ID_USUARIO);
        nuevoAspirante.setIdUsuario(usuario);
        nuevoAspirante.setUsaSillaRuedas(false);

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
        inscripcion.setEstado("INSCRITO");

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
    }

    @Given("existe un aula de examen con pupitres disponibles")
    public void existe_un_aula_de_examen_con_pupitres_disponibles() {
        System.out.println("Preparando aula de examen con pupitres");

        // ===== 1. Crear turno para la prueba semilla =====
        TurnosExaman nuevoTurno = new TurnosExaman();
        nuevoTurno.setNombreTurno("Turno Matutino Asignación");
        PruebasAdmision pruebaRef = new PruebasAdmision();
        pruebaRef.setId(ID_PRUEBA_SEMILLA);
        nuevoTurno.setIdPrueba(pruebaRef);
        nuevoTurno.setFecha(LocalDate.now());
        nuevoTurno.setHoraInicio(LocalTime.of(8, 0));
        nuevoTurno.setHoraFin(LocalTime.of(10, 0));

        Response respuestaTurno = hacerPost("turnos_examen", nuevoTurno);
        Assertions.assertEquals(201, respuestaTurno.getStatus());
        idTurno = extraerIdDelHeader(respuestaTurno, "turnos_examen");
        System.out.println("Turno creado: " + idTurno);

        // ===== 2. Crear aula de examen para ese turno =====
        AulasExaman nuevoAula = new AulasExaman();
        TurnosExaman turnoRef = new TurnosExaman();
        turnoRef.setId(idTurno);
        nuevoAula.setIdTurno(turnoRef);
        nuevoAula.setIdAulaApi("AULA-ASIGNACION-001");
        nuevoAula.setCapacidad(40);
        nuevoAula.setCuposOcupados(0);
        nuevoAula.setAccesibleSillaRuedas(false);

        Response respuestaAula = hacerPost("aulas_examen", nuevoAula);
        Assertions.assertEquals(201, respuestaAula.getStatus());
        idAula = extraerIdDelHeader(respuestaAula, "aulas_examen");
        System.out.println("Aula creada: " + idAula);
    }

    @When("asigno un aula y un pupitre al aspirante para esa prueba")
    public void asigno_un_aula_y_un_pupitre_al_aspirante_para_esa_prueba() {
        System.out.println("Realizando asignación de aula y pupitre");

        AsignacionesAulaPupitre asignacion = new AsignacionesAulaPupitre();
        InscripcionesPrueba inscripcionRef = new InscripcionesPrueba();
        inscripcionRef.setId(idInscripcion);
        asignacion.setIdInscripcion(inscripcionRef);
        AulasExaman aulaRef = new AulasExaman();
        aulaRef.setId(idAula);
        asignacion.setIdAula(aulaRef);
        asignacion.setPupitre("Pupitre A-101");

        Response respuestaAsignacion = hacerPost("asignaciones_aula_pupitre", asignacion);
        Assertions.assertEquals(201, respuestaAsignacion.getStatus());
        idAsignacion = extraerIdDelHeader(respuestaAsignacion, "asignaciones_aula_pupitre");
        System.out.println("Asignación creada: " + idAsignacion);
    }

    @Then("se registra la asignación de aula y pupitre para el aspirante")
    public void se_registra_la_asignacion_de_aula_y_pupitre_para_el_aspirante() {
        System.out.println("Verificando que se registró la asignación");
        Assertions.assertNotNull(idAsignacion);
        AsignacionesAulaPupitre asignacion = obtenerAsignacion(idAsignacion);
        System.out.println("Asignación verificada: ID=" + asignacion.getId());
    }

    @Then("puedo consultar la asignación de aula y pupitre del aspirante")
    public void puedo_consultar_la_asignacion_de_aula_y_pupitre_del_aspirante() {
        System.out.println("Consultando asignación de aula y pupitre");
        Assertions.assertNotNull(idAsignacion);
        AsignacionesAulaPupitre asignacion = obtenerAsignacion(idAsignacion);
        Assertions.assertNotNull(asignacion.getIdInscripcion());
        Assertions.assertNotNull(asignacion.getIdAula());
        System.out.println("Asignación consultada exitosamente");
    }

    @Then("la asignación muestra el aula y el pupitre asignados correctamente")
    public void la_asignacion_muestra_el_aula_y_el_pupitre_asignados_correctamente() {
        System.out.println("Verificando que aula y pupitre son correctos");
        AsignacionesAulaPupitre asignacion = obtenerAsignacion(idAsignacion);

        // Validar inscripción
        Assertions.assertNotNull(asignacion.getIdInscripcion());
        Assertions.assertEquals(idInscripcion, asignacion.getIdInscripcion().getId());

        // Validar aula
        Assertions.assertNotNull(asignacion.getIdAula());
        Assertions.assertEquals(idAula, asignacion.getIdAula().getId());

        // Validar pupitre
        Assertions.assertNotNull(asignacion.getPupitre());
        Assertions.assertEquals("Pupitre A-101", asignacion.getPupitre());

        System.out.println("Verificación exitosa: Aula=" + asignacion.getIdAula().getId() +
                ", Pupitre=" + asignacion.getPupitre() +
                ", Inscripción=" + asignacion.getIdInscripcion().getId());
    }
}
