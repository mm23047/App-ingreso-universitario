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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd.BaseSistemaST;

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

    /**
     * Variables de estado del escenario para guardar los IDS generados en la prueba
     */
    static UUID idAspirante;
    static UUID idTurno;
    static UUID idInscripcion;

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
        System.out.println("Iniciando infraestructura (SINGLETON BaseSistemaST) para Inscribir Aspirante Prueba BDD");
        BaseSistemaST.init();

        cliente = BaseSistemaST.getClient();
        target = cliente.target(BaseSistemaST.getBaseUrl());

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
        nuevoTurno.setNombreTurno("Turno MATUTINO");
        nuevoTurno.setFecha(LocalDate.now().plusDays(10));
        nuevoTurno.setHoraInicio(LocalTime.of(9,0));
        nuevoTurno.setHoraFin(LocalTime.of(11,0));

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

        // Utilizamos la tabla intermedia
        CarrerasElegidaId carrerasElegida = new CarrerasElegidaId();
        carrerasElegida.setIdInscripcion(idInscripcion);
        carrerasElegida.setIdCarrera(ID_CARRERA_SEMILLA);

        // Creamos la entidad principal de la carrera elegida y le asignamos su llave compuesta
        CarrerasElegida carreras = new CarrerasElegida();
        carreras.setId(carrerasElegida);
        carreras.setPrioridad((short) 1);
cd
        // Resolvemos la llave foránea hacia el catálogo de carreras
        CatalogoCarrera catalogoCarrera = new CatalogoCarrera();
        catalogoCarrera.setIdCarrera(ID_CARRERA_SEMILLA);
        carreras.setIdCarrera(catalogoCarrera);

        // Enviamos la petición POST para registrar la elección de carrera
        Response respuestaCarrera = hacerPost("carreras_elegidas", carreras);
        Assertions.assertEquals(201, respuestaCarrera.getStatus());
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

    @Then("la inscripcion muestra el turno de examen y la carrera elegida correctamente")
    public void la_inscripcion_muestra_el_turno_de_examen_y_la_carrera_elegida_correctamente(){
        System.out.println("La inscripcion muestra el turno de examen Y la carrera elegida por el aspirante");

        // Consultamos el endpoint usando la llave compuesta (ID de la inscripción + ID de la carrera)
        Response respuesta = target.path("carreras_elegidas/{idInscripcion}/{idCarrera}")
                .resolveTemplate("idInscripcion", idInscripcion)
                .resolveTemplate("idCarrera", ID_CARRERA_SEMILLA)
                .request(MediaType.APPLICATION_JSON)
                .get();

        // Verificamos que la relación se haya guardado y pueda ser consultada (HTTP 200 OK)
        Assertions.assertEquals(200, respuesta.getStatus());

        // Extraemos la entidad y verificamos que el código de carrera coincida con nuestra semilla
        CarrerasElegida carreraEncontrada = respuesta.readEntity(CarrerasElegida.class);
        Assertions.assertEquals(ID_CARRERA_SEMILLA, carreraEncontrada.getId().getIdCarrera());
    }

}
