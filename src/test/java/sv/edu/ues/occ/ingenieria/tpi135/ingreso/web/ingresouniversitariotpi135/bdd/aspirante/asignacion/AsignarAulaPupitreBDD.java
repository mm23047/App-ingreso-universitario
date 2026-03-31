package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.asignacion;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.BaseSistemaST;

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

    @Dado("se tiene un servidor corriendo con la aplicación desplegada")
    public void se_tiene_un_servidor_corriendo_con_la_aplicacion_desplegada() {
        System.out.println("Iniciando infraestructura singleton de pruebas BDD");
        BaseSistemaST.init();

        cliente = BaseSistemaST.getClient();
        target = cliente.target(BaseSistemaST.getBaseUrl());
    }

    @Dado("existe un aspirante inscrito en una prueba de admisión y turno específico")
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

        Response respuestaAspirante = target
                .path("aspirantes_datos")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(nuevoAspirante));

        Assertions.assertEquals(201, respuestaAspirante.getStatus());
        Assertions.assertTrue(respuestaAspirante.getHeaders().containsKey("Location"));

        idAspirante = UUID.fromString(
                respuestaAspirante.getHeaderString("Location").split("aspirantes_datos/")[1]
        );
        Assertions.assertNotNull(idAspirante);
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

        Response respuestaInscripcion = target
                .path("inscripciones_prueba")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(inscripcion));

        Assertions.assertEquals(201, respuestaInscripcion.getStatus());

        idInscripcion = UUID.fromString(
                respuestaInscripcion.getHeaderString("Location").split("inscripciones_prueba/")[1]
        );
        Assertions.assertNotNull(idInscripcion);
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

        Response respuestaCarrera = target
                .path("carreras_elegidas")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(carreraElegida));

        Assertions.assertEquals(201, respuestaCarrera.getStatus());
        System.out.println("Carrera elegida asociada: " + ID_CARRERA_SEMILLA);
    }

    @Dado("existe un aula de examen con pupitres disponibles")
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

        Response respuestaTurno = target
                .path("turnos_examen")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(nuevoTurno));

        Assertions.assertEquals(201, respuestaTurno.getStatus());

        idTurno = UUID.fromString(
                respuestaTurno.getHeaderString("Location").split("turnos_examen/")[1]
        );
        Assertions.assertNotNull(idTurno);
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

        Response respuestaAula = target
                .path("aulas_examen")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(nuevoAula));

        Assertions.assertEquals(201, respuestaAula.getStatus());

        idAula = UUID.fromString(
                respuestaAula.getHeaderString("Location").split("aulas_examen/")[1]
        );
        Assertions.assertNotNull(idAula);
        System.out.println("Aula creada: " + idAula);
    }

    @Cuando("asigno un aula y un pupitre al aspirante para esa prueba")
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

        Response respuestaAsignacion = target
                .path("asignaciones_aula_pupitre")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(asignacion));

        Assertions.assertEquals(201, respuestaAsignacion.getStatus());

        idAsignacion = UUID.fromString(
                respuestaAsignacion.getHeaderString("Location").split("asignaciones_aula_pupitre/")[1]
        );
        Assertions.assertNotNull(idAsignacion);
        System.out.println("Asignación creada: " + idAsignacion);
    }

    @Entonces("se registra la asignación de aula y pupitre para el aspirante")
    public void se_registra_la_asignacion_de_aula_y_pupitre_para_el_aspirante() {
        System.out.println("Verificando que se registró la asignación");

        Assertions.assertNotNull(idAsignacion);

        Response respuesta = target
                .path("asignaciones_aula_pupitre/{id}")
                .resolveTemplate("id", idAsignacion)
                .request(MediaType.APPLICATION_JSON)
                .get();

        Assertions.assertEquals(200, respuesta.getStatus());

        AsignacionesAulaPupitre asignacionRegistrada = respuesta.readEntity(AsignacionesAulaPupitre.class);
        Assertions.assertNotNull(asignacionRegistrada);
        Assertions.assertEquals(idAsignacion, asignacionRegistrada.getId());

        System.out.println("Asignación verificada: ID=" + asignacionRegistrada.getId());
    }

    @Entonces("puedo consultar la asignación de aula y pupitre del aspirante")
    public void puedo_consultar_la_asignacion_de_aula_y_pupitre_del_aspirante() {
        System.out.println("Consultando asignation de aula y pupitre");

        Assertions.assertNotNull(idAsignacion);

        Response respuesta = target
                .path("asignaciones_aula_pupitre/{id}")
                .resolveTemplate("id", idAsignacion)
                .request(MediaType.APPLICATION_JSON)
                .get();

        Assertions.assertEquals(200, respuesta.getStatus());

        AsignacionesAulaPupitre asignacionConsultada = respuesta.readEntity(AsignacionesAulaPupitre.class);
        Assertions.assertNotNull(asignacionConsultada);
        Assertions.assertEquals(idAsignacion, asignacionConsultada.getId());
        Assertions.assertNotNull(asignacionConsultada.getIdInscripcion());
        Assertions.assertNotNull(asignacionConsultada.getIdAula());

        System.out.println("Asignación consultada exitosamente");
    }

    @Entonces("la asignación muestra el aula y el pupitre asignados correctamente")
    public void la_asignacion_muestra_el_aula_y_el_pupitre_asignados_correctamente() {
        System.out.println("Verificando que aula y pupitre son correctos");

        Response respuesta = target
                .path("asignaciones_aula_pupitre/{id}")
                .resolveTemplate("id", idAsignacion)
                .request(MediaType.APPLICATION_JSON)
                .get();

        Assertions.assertEquals(200, respuesta.getStatus());

        AsignacionesAulaPupitre asignacion = respuesta.readEntity(AsignacionesAulaPupitre.class);

        // Verificar que la asignación tiene la inscripción correcta
        Assertions.assertNotNull(asignacion.getIdInscripcion());
        Assertions.assertEquals(idInscripcion, asignacion.getIdInscripcion().getId());

        // Verificar que la asignación tiene el aula correcta
        Assertions.assertNotNull(asignacion.getIdAula());
        Assertions.assertEquals(idAula, asignacion.getIdAula().getId());

        // Verificar que el pupitre asignado es correcto
        Assertions.assertNotNull(asignacion.getPupitre());
        Assertions.assertEquals("Pupitre A-101", asignacion.getPupitre());

        System.out.println("Verificación exitosa: Aula=" + asignacion.getIdAula().getId() +
                ", Pupitre=" + asignacion.getPupitre() +
                ", Inscripción=" + asignacion.getIdInscripcion().getId());
    }
}
