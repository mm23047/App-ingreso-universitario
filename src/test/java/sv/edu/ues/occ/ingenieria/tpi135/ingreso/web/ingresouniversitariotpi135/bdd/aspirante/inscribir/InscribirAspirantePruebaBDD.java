package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.aspirante.inscribir;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.BaseSistemaBDD;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class InscribirAspirantePruebaBDD {

    /**
     * Variables utilizadas durante la prueba
     * cliente = Para poder jacer llamadas HTTP
     * target = La URL por donde se debe de hacer las llamadas
     */
    static Client cliente;
    static WebTarget target;

    //Variable para saber si hay duplicidad de registros
    static Response respuestaDuplicada;

    /**
     * Variables de estado del escenario para guardar los IDS generados en la prueba
     */
    static UUID idAspirante;
    static UUID idTurno;
    static UUID idInscripcion;

    // Datos del turno creado (para asserts coherentes)
    static String nombreTurno;
    static LocalDate fechaTurno;
    static LocalTime horaInicioTurno;
    static LocalTime horaFinTurno;

    // Semillas estáticas
    private static final UUID ID_USUARIO = UUID.fromString("b1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final String ID_CARRERA_SEMILLA = "ICS";

    // METODOS AUXILIARES
    // XXXXXXXXXXXXXXXXXXXX

    /**
     * Leer la respuesta(UUID) que nos da el server despues de crear un recurso
     * @param respuesta Respuesta HTTP devuelta por el servidor despues de hacer POST
     * @param endPoint EL nombre del recurso en la URL
     * @return El UUID generado por nuestra BD para el nuevo registro
     */
    private UUID extraerIdDelHeader (Response respuesta, String endPoint) {

        String Ubicacion = respuesta.getHeaderString("Location");

        return UUID.fromString(Ubicacion.split(endPoint+"/")[1]);
    }

    /**
     * Envia datos hacia nuestra API
     * @param endPoint La ruta especifica del recurso "Inscripciones prueba"
     * @param entidad El objeto java que contiene los datos a guardar
     * @return Respuesta HTTP del server, deberia de ser 201
     */
    private Response hacerPost(String endPoint, Object entidad){
        return target.path(endPoint).request(MediaType.APPLICATION_JSON).post(Entity.json(entidad));
    }

    /**
     * Para pedir informacion sobre un registro que ya debe de existir
     * @param endPoint La ruta especifica de nuestro recurso
     * @param id EL UUID del registro que queremos buscar en la BD
     * @return La repuesta HTTP del server en formato JSON
     */
    private Response hacerGet(String endPoint, UUID id){
        return target.path(endPoint+"/{id}").resolveTemplate("id", id).request(MediaType.APPLICATION_JSON).get();
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //LOs pasos de cucumber

    /**
     *
     */
    @Given("se tiene un servidor corriendo con la aplicacion desplegada")
    public void se_tiene_un_servidor_corriendo_con_la_aplicacion_desplegada(){
        System.out.println("Iniciando infraestructura (SINGLETON BaseSistemaBDD) para Inscribir Aspirante Prueba BDD");
        BaseSistemaBDD.init();

        cliente = BaseSistemaBDD.getClient();
        target = cliente.target(BaseSistemaBDD.getBaseUrl());

    }

    @Given("existe un aspirante con perfil creado y una carrera asociada")
    public void existe_un_aspirante_con_perfil_creado_y_una_carrera_asociada(){
        System.out.println("CREAMOS un aspirante de prueba");

        // Construir un aspirante
        AspirantesDato nuevoAspirante = new AspirantesDato();
        nuevoAspirante.setNombres("Ana");
        nuevoAspirante.setApellidos("Garcia");
        nuevoAspirante.setDui("12345678-1");
        nuevoAspirante.setUsaSillaRuedas(false);

        /**
         *  Resolvemos llaves foráneas (Relaciones)
         *  La tabla 'aspirantes_datos' necesita el ID de un 'usuario_sistema'.
         *  No creamos un usuario desde cero, usamos nuestro ID_USUARIO "semilla" (Desde el script)
         *  Y lo enlazamos a nuestro nuevo aspirante.
         */
        UsuariosSistema usuario = new UsuariosSistema();
        usuario.setId(ID_USUARIO);
        nuevoAspirante.setIdUsuario(usuario);

        // Enviar a la API, Validar y Guardar el estado
        // Enviamos nuestro "formulario" (el objeto nuevoAspirante) al servidor mediante un POST.
        Response respuestaAspirante = hacerPost("aspirantes_datos", nuevoAspirante);

        // Verificamos estrictamente que el servidor nos responda con HTTP 201 (Created).
        // Si no la prueba se detiene aquí.
        Assertions.assertEquals(201, respuestaAspirante.getStatus());

        /**
         * Atrapamos el ID autogenerado por la base de datos y lo guardamos
         * en nuestra variable estática 'idAspirante' para poder usarlo en el @When.
         */
        idAspirante = extraerIdDelHeader(respuestaAspirante, "aspirantes_datos");
    }


    @Given("existe una prueba de admision disponible con un turno habilitado")
    public void existe_una_prueba_de_admision_disponible_con_un_turno_habilitado(){
        System.out.println("CREAMOS un TURNO de Examen");

        // Construir el objeto principal (INCSCRIPCION DE TURNO)

        TurnosExaman nuevoTurno = new TurnosExaman();
        nombreTurno = "Turno MATUTINO";
        fechaTurno = LocalDate.now().plusDays(10);
        horaInicioTurno = LocalTime.of(9,0);
        horaFinTurno = LocalTime.of(11,0);

        nuevoTurno.setNombreTurno(nombreTurno);
        nuevoTurno.setFecha(fechaTurno);
        nuevoTurno.setHoraInicio(horaInicioTurno);
        nuevoTurno.setHoraFin(horaFinTurno);

        /**
         * Resolver llaves foráneas
         * LOs turno pertenece a una prueba de admisión específica.
         * Por lo tanto, instanciamos una prueba y le seteamos nuestra semilla ID_PRUEBA_SEMILLA
         */
        PruebasAdmision pruebasAdmision = new PruebasAdmision();
        pruebasAdmision.setId(ID_PRUEBA_SEMILLA);
        nuevoTurno.setIdPrueba(pruebasAdmision);

        /**
         *  Enviar a la API, Validar y Guardar el estado
         */
        Response respuestaTurno = hacerPost("turnos_examen", nuevoTurno);

        // Validamos que se creó correctamente.
        Assertions.assertEquals(201, respuestaTurno.getStatus());

        /**
         * Extraemos y guardamos el ID del turno recién creado en nuestra variable 'idTurno'
         * para tenerlo disponible cuando el aspirante se vaya a inscribir.
         */
        idTurno = extraerIdDelHeader(respuestaTurno, "turnos_examen");
    }

    @When("solicito inscribir al aspirante en la prueba de admision y turno seleccionados")
    public void solicito_inscribir_al_aspirante_en_la_prueba_de_admision_y_turno_seleccionados(){
        System.out.println("INSCRIBIENDO al aspirante");

        // CREAR LA INSCRIPCION
        InscripcionesPrueba inscrpciones = new InscripcionesPrueba();

        // Enlazamos al aspirante que creamos en el @Given anterior.
        AspirantesDato nuevoAspirante = new AspirantesDato();
        nuevoAspirante.setId(idAspirante);
        inscrpciones.setIdAspirante(nuevoAspirante);

        // Enlazamos la prueba de admisión
        PruebasAdmision pruebasAdmision = new PruebasAdmision();
        pruebasAdmision.setId(ID_PRUEBA_SEMILLA);
        inscrpciones.setIdPrueba(pruebasAdmision);

        inscrpciones.setEstado("INSCRITO");

        // Enviamos la petición POST para guardar la inscripción en la base de datos
        Response respuestaDeInscripcion = hacerPost("inscripciones_prueba", inscrpciones);
        Assertions.assertEquals(201, respuestaDeInscripcion.getStatus());

        // Guardamos el ID generado para usarlo en las validaciones (@Then)
        idInscripcion = extraerIdDelHeader(respuestaDeInscripcion, "inscripciones_prueba");

        // ASOCIAR LA CARRERA A LA INSCRIPCIÓN

        // Utilizamos la tabla intermedia (PK compuesta)
        CarrerasElegidaId carrerasElegida = new CarrerasElegidaId();
        carrerasElegida.setIdInscripcion(idInscripcion);
        carrerasElegida.setIdCarrera(ID_CARRERA_SEMILLA);

        // Creamos la entidad principal de la carrera elegida y le asignamos su llave compuesta
        CarrerasElegida carreras = new CarrerasElegida();
        carreras.setId(carrerasElegida);

        // Resolvemos la relación hacia la inscripción
        InscripcionesPrueba inscripcionRef = new InscripcionesPrueba();
        inscripcionRef.setId(idInscripcion);
        carreras.setIdInscripcion(inscripcionRef);

        // Resolvemos la llave foránea hacia el catálogo de carreras
        CatalogoCarrera catalogoCarrera = new CatalogoCarrera();
        catalogoCarrera.setIdCarrera(ID_CARRERA_SEMILLA);
        carreras.setIdCarrera(catalogoCarrera);

        carreras.setPrioridad((short) 1);

        // Enviamos la petición POST para registrar la elección de carrera
        Response respuestaCarrera = hacerPost("carreras_elegidas", carreras);
        Assertions.assertEquals(201, respuestaCarrera.getStatus());
    }


    @When("vuelvo a solicitar la inscripcion del mismo aspirante a la misma prueba")
    public void vuelvo_a_solicitar_la_inscripcion_del_mismo_aspirante_a_la_misma_prueba(){
        System.out.println("Intentando inscribir por segunda vez al aspirante (DUPLICADO)");

        // Armamos exactamente la misma inscripción que en el paso anterior
        InscripcionesPrueba inscripcionDuplicada = new InscripcionesPrueba();

        AspirantesDato aspirante = new AspirantesDato();
        aspirante.setId(idAspirante);
        inscripcionDuplicada.setIdAspirante(aspirante);

        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setId(ID_PRUEBA_SEMILLA);
        inscripcionDuplicada.setIdPrueba(prueba);

        inscripcionDuplicada.setEstado("INSCRITO");

        // Hacemos el POST, pero esta vez guardamos la respuesta en respuestaDuplicada
        respuestaDuplicada = hacerPost("inscripciones_prueba", inscripcionDuplicada);
    }

    @Then("el sistema rechaza la solicitud por duplicidad")
    public void el_sistema_rechaza_la_solicitud_por_duplicidad(){
        System.out.println("Validando el rechazo de la inscripción duplicada");

        // Verificamos que el servidor haya devuelto 422
        Assertions.assertEquals(422, respuestaDuplicada.getStatus(), "El servidor no devolvió error 422");

        // Verificamos que el Header especial venga en la respuesta
        Assertions.assertEquals("true", respuestaDuplicada.getHeaderString("REGISTRO-DUPLICADO"), "No se encontró el header Duplicate-registration");

        // Verificamos el mensaje del cuerpo
        String cuerpoRespuesta = respuestaDuplicada.readEntity(String.class);
        Assertions.assertEquals("El aspirante ya está inscrito en esta prueba", cuerpoRespuesta);
    }


    @Then("se registra una nueva inscripcion a la prueba para ese aspirante")
    public void se_registra_una_nueva_inscripcion_a_la_prueba_para_ese_aspirante(){
        System.out.println("Verificamos que se haya creado un ID en la inscripcion");

        // Si el POST falló, idInscripcion sería nulo.
        Assertions.assertNotNull(idInscripcion,"El ID de la inscripcion no deberia de ser null");
    }

    @Then("puedo consultar la inscripcion de ese aspirante a la prueba")
    public void puedo_consultar_la_inscripcion_de_ese_aspirante_a_la_prueba(){
        System.out.println("Consultamos la inscripcion generada");

        /**
         * Vamos a la API (simulando un cliente) para verificar que el dato
         * realmente existe en la base de datos y no fue un falso positivo.
         */
        Response respuesta = hacerGet("inscripciones_prueba", idInscripcion);

        // Verificamos que el servidor la encuentre (HTTP 200 OK)
        Assertions.assertEquals(200, respuesta.getStatus());

        // Transformamos el JSON de respuesta de vuelta a un objeto Java
        InscripcionesPrueba inscripcionesConsulatada = respuesta.readEntity(InscripcionesPrueba.class);

        // Validamos la integridad de los datos:
        Assertions.assertEquals(idInscripcion, inscripcionesConsulatada.getId());
        Assertions.assertEquals(idAspirante, inscripcionesConsulatada.getIdAspirante().getId());
    }

    @Then("el turno de examen existe y la carrera elegida queda registrada correctamente")
    public void el_turno_de_examen_existe_y_la_carrera_elegida_queda_registrada_correctamente(){
        System.out.println("Verificamos que el turno existe y que la carrera elegida quedó registrada");

        // 1) Verificar que el turno creado exista (la inscripción NO referencia turno en el modelo actual)
        Assertions.assertNotNull(idTurno, "El ID del turno no deberia de ser null");
        Response respTurno = hacerGet("turnos_examen", idTurno);
        Assertions.assertEquals(200, respTurno.getStatus());
        TurnosExaman turnoConsultado = respTurno.readEntity(TurnosExaman.class);
        Assertions.assertNotNull(turnoConsultado);
        Assertions.assertEquals(idTurno, turnoConsultado.getId());
        Assertions.assertEquals(nombreTurno, turnoConsultado.getNombreTurno());
        Assertions.assertEquals(fechaTurno, turnoConsultado.getFecha());
        Assertions.assertEquals(horaInicioTurno, turnoConsultado.getHoraInicio());
        Assertions.assertEquals(horaFinTurno, turnoConsultado.getHoraFin());

        if (turnoConsultado.getIdPrueba() != null && turnoConsultado.getIdPrueba().getId() != null) {
            Assertions.assertEquals(ID_PRUEBA_SEMILLA, turnoConsultado.getIdPrueba().getId());
        }

        // 2) Verificar que la carrera elegida se haya registrado para la inscripción (PK compuesta + prioridad)
        Assertions.assertNotNull(idInscripcion, "El ID de la inscripcion no deberia de ser null");
        Response respCarrera = target.path("carreras_elegidas/{idInscripcion}/{idCarrera}")
                .resolveTemplate("idInscripcion", idInscripcion)
                .resolveTemplate("idCarrera", ID_CARRERA_SEMILLA)
                .request(MediaType.APPLICATION_JSON)
                .get();
        Assertions.assertEquals(200, respCarrera.getStatus());
        CarrerasElegida carreraEncontrada = respCarrera.readEntity(CarrerasElegida.class);
        Assertions.assertNotNull(carreraEncontrada);
        Assertions.assertNotNull(carreraEncontrada.getId());
        Assertions.assertEquals(idInscripcion, carreraEncontrada.getId().getIdInscripcion());
        Assertions.assertEquals(ID_CARRERA_SEMILLA, carreraEncontrada.getId().getIdCarrera());
        Assertions.assertEquals(Short.valueOf((short) 1), carreraEncontrada.getPrioridad());
    }



}
