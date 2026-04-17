package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.*;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class CuposCarreraResourceIT extends AbstractResourceIT {

    // IDs semilla
    private static final UUID ID_PRUEBA_1 = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final String ID_CARRERA_ICS = "ICS";
    private static final String ID_CARRERA_MAT = "MAT";

    // Etapas
    private static final UUID ID_ETAPA_1_MATEMATICAS = UUID.fromString("c1000000-0000-0000-0000-000000000001");
    private static final UUID ID_ETAPA_2_CIENCIAS = UUID.fromString("c1000000-0000-0000-0000-000000000002");
    private static final UUID ID_ETAPA_3_FINAL = UUID.fromString("c1000000-0000-0000-0000-000000000003");

    // Construimos la ruta parcial reutilizable para los GET/PUT/DELETE
    private static final String PATH_CUPO_1 = ID_PRUEBA_1 + "/" + ID_CARRERA_ICS + "/" + ID_ETAPA_3_FINAL;

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de LECTURA (GET)

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("cupos_carrera");

        assertEquals(200, response.getStatus());

        CuposCarrera[] arreglo = response.readEntity(CuposCarrera[].class);
        assertNotNull(arreglo);

        // Tenemos 3 registros en el script
        assertTrue(arreglo.length >= 3);

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 3);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("cupos_carrera/" + PATH_CUPO_1);

        assertEquals(200, response.getStatus());

        CuposCarrera entidad = response.readEntity(CuposCarrera.class);
        assertNotNull(entidad);

        // Validamos la llave primaria compuesta
        assertEquals(ID_PRUEBA_1, entidad.getId().getIdPrueba());
        assertEquals(ID_CARRERA_ICS, entidad.getId().getIdCarrera());
        assertEquals(ID_ETAPA_3_FINAL, entidad.getId().getIdEtapa());

        // ICS tiene 50 cupos
        assertEquals(50, entidad.getCupos());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        // Inventamos una ruta compuesta INEXISTENTE
        String pathInexistente = UUID.randomUUID() + "/XYZ/" + UUID.randomUUID();
        Response response = get("cupos_carrera/" + pathInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de CREAR (POST)

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        CuposCarrera nuevoCupo = construirPayload(ID_PRUEBA_1, ID_CARRERA_MAT, ID_ETAPA_3_FINAL, 35);

        Response responseCreacion = post("cupos_carrera", nuevoCupo);

        assertEquals(201, responseCreacion.getStatus());
        assertNotNull(responseCreacion.getHeaderString("Location"));

        // Verificamos persistencia
        String pathNuevo = ID_PRUEBA_1 + "/" + ID_CARRERA_MAT + "/" + ID_ETAPA_3_FINAL;
        Response responseConsulta = get("cupos_carrera/" + pathNuevo);

        assertEquals(200, responseConsulta.getStatus());
        CuposCarrera creado = responseConsulta.readEntity(CuposCarrera.class);
        assertEquals(35, creado.getCupos());
    }

    @Test
    void create_ConEntidadInvalida_SinDatos_DebeRetornar422() {
        // Enviamos una entidad vacía
        CuposCarrera invalida = new CuposCarrera();

        Response response = post("cupos_carrera", invalida);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de ACTUALIZAION (PUT)

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        // Creamos cupo temporal en ICS en Etapa 1
        String pathTemporal = crearCupoReal(ID_PRUEBA_1, ID_CARRERA_ICS, ID_ETAPA_1_MATEMATICAS, 10);

        // Actualizamos a 100 cupos
        CuposCarrera actualizada = construirPayload(ID_PRUEBA_1, ID_CARRERA_ICS, ID_ETAPA_1_MATEMATICAS, 100);

        // Hacemos PUT
        Response responseUpdate = put("cupos_carrera/" + pathTemporal, actualizada);
        assertEquals(200, responseUpdate.getStatus());

        CuposCarrera cuerpo = responseUpdate.readEntity(CuposCarrera.class);
        assertEquals(100, cuerpo.getCupos());

        // Verificamos persistencia
        Response responseConsulta = get("cupos_carrera/" + pathTemporal);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals(100, responseConsulta.readEntity(CuposCarrera.class).getCupos());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        String pathInexistente = UUID.randomUUID() + "/ERR/" + UUID.randomUUID();

        // Construimos un payload pero que apunta a un ID INEXISTENTE
        CuposCarrera payload = construirPayload(UUID.randomUUID(), "ERR", UUID.randomUUID(), 5);

        Response response = put("cupos_carrera/" + pathInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de DELETE

    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        // Creamos un cupo temporal en ICS en Etapa 2
        String pathTemporal = crearCupoReal(ID_PRUEBA_1, ID_CARRERA_ICS, ID_ETAPA_2_CIENCIAS, 99);

        // Eliminamos
        Response responseDelete = delete("cupos_carrera/" + pathTemporal);
        assertEquals(204, responseDelete.getStatus());

        // Verificamos que ya no existe
        Response responseConsulta = get("cupos_carrera/" + pathTemporal);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de HELPER

    /**
     * Construye una entidad CuposCarrera.
     * Inicializa tanto la llave compuesta (CuposCarreraId) como los objetos de relación con sus respectivos IDs.
     */
    private CuposCarrera construirPayload(UUID idPrueba, String idCarrera, UUID idEtapa, int cantidadCupos) {
        CuposCarrera entidad = new CuposCarrera();

        // Inicializar la Llave Primaria Compuesta
        CuposCarreraId pk = new CuposCarreraId();
        pk.setIdPrueba(idPrueba);
        pk.setIdCarrera(idCarrera);
        pk.setIdEtapa(idEtapa);
        entidad.setId(pk);

        // Inicializamos los objetos relacionales SETEANDO SUS IDs para evadir el 422 del Resource
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setId(idPrueba);
        entidad.setIdPrueba(prueba);

        CatalogoCarrera carrera = new CatalogoCarrera();
        carrera.setIdCarrera(idCarrera);
        entidad.setIdCarrera(carrera);

        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setId(idEtapa);
        entidad.setIdEtapa(etapa);

        // Setear el valor nativo
        entidad.setCupos(cantidadCupos);

        return entidad;
    }

    /**
     * Crea un registro real en BD y retorna el path URL listo para usarse.
     */
    private String crearCupoReal(UUID idPrueba, String idCarrera, UUID idEtapa, int cantidadCupos) {
        CuposCarrera nueva = construirPayload(idPrueba, idCarrera, idEtapa, cantidadCupos);

        Response responseCreacion = post("cupos_carrera", nueva);

        // Validamos que se creo antes de devolver la ruta
        assertEquals(201, responseCreacion.getStatus(), "Fallo al crear registro temporal en el helper");

        // Retornamos la ruta
        return idPrueba + "/" + idCarrera + "/" + idEtapa;
    }

}