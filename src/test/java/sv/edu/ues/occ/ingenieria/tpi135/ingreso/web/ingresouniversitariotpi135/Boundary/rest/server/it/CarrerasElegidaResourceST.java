package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegidaId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
public class CarrerasElegidaResourceST extends AbstractResourceST{
    // IDs semilla desde el script
    private static final UUID ID_INSCRIPCION_1 = UUID.fromString("09000000-0000-0000-0000-000000000001");
    private static final UUID ID_INSCRIPCION_2 = UUID.fromString("09000000-0000-0000-0000-000000000002");

    // Carreras del catálogo
    private static final String ID_CARRERA_ICS = "ICS";
    private static final String ID_CARRERA_ISI = "ISI";
    private static final String ID_CARRERA_ICC = "ICC";
    private static final String ID_CARRERA_MAT = "MAT";

    // Ruta base
    private static final String PATH_CARRERA_ELEGIDA_1 = ID_INSCRIPCION_1 + "/" + ID_CARRERA_ICS;

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de LECTURA (GET)

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("carreras_elegidas");

        assertEquals(200, response.getStatus());

        CarrerasElegida[] arreglo = response.readEntity(CarrerasElegida[].class);
        assertNotNull(arreglo);

        // Tenemos 4 registros en el script
        assertTrue(arreglo.length >= 4);

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 4);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("carreras_elegidas/" + PATH_CARRERA_ELEGIDA_1);

        assertEquals(200, response.getStatus());

        CarrerasElegida entidad = response.readEntity(CarrerasElegida.class);
        assertNotNull(entidad);

        // Validamos la llave primaria compuesta
        assertEquals(ID_INSCRIPCION_1, entidad.getIdCarreraElegida().getIdInscripcion());
        assertEquals(ID_CARRERA_ICS, entidad.getIdCarreraElegida().getIdCarrera());

        // La prioridad de ICS para la inscripción 1 es 1
        assertEquals((short) 1, entidad.getPrioridad());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        // Inventamos una ruta compuesta INEXISTENTE
        String pathInexistente = UUID.randomUUID() + "/XYZ";
        Response response = get("carreras_elegidas/" + pathInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de CREAR (POST)

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        // Enviamos (short) 3
        CarrerasElegida nueva = construirPayload(ID_INSCRIPCION_1, ID_CARRERA_ICC, (short) 3);
        Response responseCreacion = post("carreras_elegidas", nueva);

        assertEquals(201, responseCreacion.getStatus());
        assertNotNull(responseCreacion.getHeaderString("Location"));

        // Verificamos persistencia
        String pathNuevo = ID_INSCRIPCION_1 + "/" + ID_CARRERA_ICC;
        Response responseConsulta = get("carreras_elegidas/" + pathNuevo);

        assertEquals(200, responseConsulta.getStatus());
        CarrerasElegida creado = responseConsulta.readEntity(CarrerasElegida.class);
        assertEquals((short) 3, creado.getPrioridad());
    }

    @Test
    void create_ConEntidadInvalida_SinDatos_DebeRetornar422() {
        // Enviamos una entidad vacía
        CarrerasElegida invalida = new CarrerasElegida();

        Response response = post("carreras_elegidas", invalida);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }


    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de ACTUALIZAION (PUT)

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        // Enviamos (short) 4 y (short) 10
        String pathTemporal = crearRegistroReal(ID_INSCRIPCION_2, ID_CARRERA_ICS, (short) 4);
        CarrerasElegida actualizada = construirPayload(ID_INSCRIPCION_2, ID_CARRERA_ICS, (short) 10);
        // Hacemos PUT
        Response responseUpdate = put("carreras_elegidas/" + pathTemporal, actualizada);
        assertEquals(200, responseUpdate.getStatus());

        CarrerasElegida cuerpo = responseUpdate.readEntity(CarrerasElegida.class);
        assertEquals((short) 10, cuerpo.getPrioridad());

        // Verificamos la persistencia
        Response responseConsulta = get("carreras_elegidas/" + pathTemporal);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals((short) 10, responseConsulta.readEntity(CarrerasElegida.class).getPrioridad());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        String pathInexistente = UUID.randomUUID() + "/ERR";
        // Construimos un payload pero que apunta a un ID INEXISTENTE
        CarrerasElegida payload = construirPayload(UUID.randomUUID(), "ERR", (short) 1);

        Response response = put("carreras_elegidas/" + pathInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de DELETE

    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        // Creamos un registro temporal: Inscripción 2 con la carrera "MAT"
        String pathTemporal = crearRegistroReal(ID_INSCRIPCION_2, ID_CARRERA_MAT, (short) 5);

        // Eliminamos
        Response responseDelete = delete("carreras_elegidas/" + pathTemporal);
        assertEquals(204, responseDelete.getStatus());

        // Verificamos que ya no existe
        Response responseConsulta = get("carreras_elegidas/" + pathTemporal);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }


    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de HELPER

    /**
     * Construye una entidad CarrerasElegida lista para enviar por POST/PUT.
     * Inicializa tanto la llave compuesta como los objetos relacionales para evadir el Error 422.
     */
    private CarrerasElegida construirPayload(UUID idInscripcion, String idCarrera, short prioridad) {
        CarrerasElegida entidad = new CarrerasElegida();

        //Inicializar la Llave Primaria Compuesta
        CarrerasElegidaId pk = new CarrerasElegidaId();
        pk.setIdInscripcion(idInscripcion);
        pk.setIdCarrera(idCarrera);
        entidad.setIdCarreraElegida(pk);

        // Inicializamos los objetos relacionales asignándoles su ID respectivo
        InscripcionesPrueba inscripcion = new InscripcionesPrueba();
        inscripcion.setIdInscripcionPrueba(idInscripcion);
        entidad.setIdInscripcion(inscripcion);

        CatalogoCarrera carrera = new CatalogoCarrera();
        carrera.setIdCarrera(idCarrera);
        entidad.setIdCarrera(carrera);

        // Setear el valor nativo
        entidad.setPrioridad(prioridad);

        return entidad;
    }

    /**
     * Crea un registro real en BD y retorna el path URL listo para usarse en PUT/DELETE.
     */
    private String crearRegistroReal(UUID idInscripcion, String idCarrera, short prioridad) {
        CarrerasElegida nueva = construirPayload(idInscripcion, idCarrera, prioridad);

        Response responseCreacion = post("carreras_elegidas", nueva);

        // Validamos que se creó correctamente antes de devolver la ruta
        assertEquals(201, responseCreacion.getStatus(), "Fallo al crear registro temporal en el helper");

        // Retornamos la ruta
        return idInscripcion + "/" + idCarrera;
    }

}
