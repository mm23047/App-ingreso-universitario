package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogoCarreraResourceST extends AbstractResourceIT {
    //ID de Ingeniería en Ciencias de la Computación
    private static final String ID_CARRERA_1 = "ICS";

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de LECTURA (GET)

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("catalogo_carreras");

        assertEquals(200, response.getStatus());

        CatalogoCarrera[] arreglo = response.readEntity(CatalogoCarrera[].class);
        assertNotNull(arreglo);

        // Tenemos 4 carreras en el script
        assertTrue(arreglo.length >= 4);

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 4);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("catalogo_carreras/" + ID_CARRERA_1);

        assertEquals(200, response.getStatus());

        CatalogoCarrera entidad = response.readEntity(CatalogoCarrera.class);
        assertNotNull(entidad);

        assertEquals(ID_CARRERA_1, entidad.getIdCarrera());
        assertEquals("Ingeniería en Ciencias de la Computación", entidad.getNombre());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        // Un ID de carrera INEXISTENTE
        String idInexistente = "XYZ";
        Response response = get("catalogo_carreras/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }


    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de CREAR (POST)

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        CatalogoCarrera nuevaCarrera = new CatalogoCarrera();
        // El ID es String, debemos enviarlo en el payload
        nuevaCarrera.setIdCarrera("IND");
        nuevaCarrera.setNombre("Ingeniería Industrial");

        Response responseCreacion = post("catalogo_carreras", nuevaCarrera);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        // Extraemos el ID de la URL
        String idCreado = location.substring(location.lastIndexOf('/') + 1);
        assertEquals("IND", idCreado);

        // Verificar persistencia
        Response responseConsulta = get("catalogo_carreras/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        CatalogoCarrera creado = responseConsulta.readEntity(CatalogoCarrera.class);
        assertEquals("IND", creado.getIdCarrera());
        assertEquals("Ingeniería Industrial", creado.getNombre());
    }

    @Test
    void create_ConEntidadInvalida_SinDatos_DebeRetornar422() {
        // Enviamos una entidad vacia
        CatalogoCarrera invalida = new CatalogoCarrera();

        Response response = post("catalogo_carreras", invalida);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de ACTUALIZAION (PUT)
    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        // Creamos carrera temporal (ID: "TMP", Nombre: "Carrera Temporal")
        String idCreado = crearCarreraReal("TMP", "Carrera Temporal para Update");

        // Construimos el payload actualizado
        CatalogoCarrera actualizada = new CatalogoCarrera();
        actualizada.setNombre("Carrera Modificada IT");

        // Hacemos PUT
        Response responseUpdate = put("catalogo_carreras/" + idCreado, actualizada);
        assertEquals(200, responseUpdate.getStatus());

        CatalogoCarrera cuerpo = responseUpdate.readEntity(CatalogoCarrera.class);
        assertEquals("Carrera Modificada IT", cuerpo.getNombre());

        // Verificamos la persistencia
        Response responseConsulta = get("catalogo_carreras/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals("Carrera Modificada IT", responseConsulta.readEntity(CatalogoCarrera.class).getNombre());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        String idInexistente = "ERR";
        CatalogoCarrera payload = new CatalogoCarrera();
        payload.setNombre("Intento de actualización");

        Response response = put("catalogo_carreras/" + idInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de DELETE

    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        // Creamos una carrera temporal
        String idCreado = crearCarreraReal("DEL", "Carrera Temporal para Delete");

        // Eliminamos la carrera temporal
        Response responseDelete = delete("catalogo_carreras/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        // Verificamos que ya no existe
        Response responseConsulta = get("catalogo_carreras/" + idCreado);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de HELPER


    /**
     * Crea una carrera real en la BD usando el endpoint REST.
     * Asi poder controlar los tests de PUT y DELETE sin afectar los datos semilla.
     */
    private String crearCarreraReal(String idCarrera, String nombre) {
        CatalogoCarrera nueva = new CatalogoCarrera();
        nueva.setIdCarrera(idCarrera);
        nueva.setNombre(nombre);

        Response responseCreacion = post("catalogo_carreras", nueva);
        assertEquals(201, responseCreacion.getStatus());

        String location = responseCreacion.getHeaderString("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }


}
