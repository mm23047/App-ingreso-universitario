package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AreasConocimientoResourceST extends AbstractResourceIT{

    //ID desde el script
    private static final UUID ID_AREA_1 = UUID.fromString("a1000000-0000-0000-0000-000000000001");

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de LECTURA (GET)

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista(){
        Response response = get("areas_conocimiento");

        assertEquals(200, response.getStatus());

        AreasConocimiento[] arreglo = response.readEntity(AreasConocimiento[].class);
        assertNotNull(arreglo);
        //Hay 3 arreglos en nuestra BD
        assertTrue(arreglo.length == 3);
        // Ver por consola lo que nos han enviado...
        if (arreglo.length > 0) {
            System.out.println("El ID del primer registro es: " + arreglo[0].getId());
            System.out.println("El nombre del primer registro es: " + arreglo[0].getNombreArea());
        }

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        //
        assertTrue(Integer.parseInt(totalHeader) >=3);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200(){
        Response response = get("areas_conocimiento/" + ID_AREA_1);

        assertEquals(200, response.getStatus());

        AreasConocimiento entidad = response.readEntity(AreasConocimiento.class);
        assertNotNull(entidad);
        assertEquals(ID_AREA_1, entidad.getId());
        assertEquals("Matemáticas", entidad.getNombreArea());
        System.out.println("El Nombre del primer registro es: " + entidad.getNombreArea());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404(){
        UUID idNotExistente = UUID.randomUUID();

        Response response = get("areas_conocimiento/" + idNotExistente);
        assertEquals(404, response.getStatus());

        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar(){
        AreasConocimiento nuevaAreaConocimiento =  new AreasConocimiento();

        nuevaAreaConocimiento.setNombreArea("Humanidades");

        Response responseCreacion = post("areas_conocimiento", nuevaAreaConocimiento);

        assertEquals(201, responseCreacion.getStatus());
        String Location = responseCreacion.getHeaderString("Location");
        assertNotNull(Location);

        String idString = Location.substring(Location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        Response responseConsultando = get("areas_conocimiento/" + idCreado);
        assertEquals(200, responseConsultando.getStatus());

        AreasConocimiento creadado = responseConsultando.readEntity(AreasConocimiento.class);
        assertEquals(idCreado, creadado.getId());
        assertEquals("Humanidades", creadado.getNombreArea());
    }

    @Test
    void create_ConEntidadInvalida_ConIdYaAsignado_DebeRetornar422(){
        AreasConocimiento areasConocimientoInvalida = new AreasConocimiento();
        // EL ID debe de ser NULL al momento de crear
        areasConocimientoInvalida.setId(UUID.randomUUID());
        areasConocimientoInvalida.setNombreArea("Área Inválida");

        Response response = post("areas_conocimiento", areasConocimientoInvalida);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        // creamos una area temporal
        UUID idCreado = crearAreaReal("Área Temporal para Update");

        // COnstruimos el payload actualizado
        AreasConocimiento actualizada = new AreasConocimiento();
        actualizada.setNombreArea("Área Modificada IT");

        // Hacemos PUT
        Response responseUpdate = put("areas_conocimiento/" + idCreado, actualizada);
        assertEquals(200, responseUpdate.getStatus());

        AreasConocimiento cuerpo = responseUpdate.readEntity(AreasConocimiento.class);
        assertEquals("Área Modificada IT", cuerpo.getNombreArea());

        // verificamos persistencia
        Response responseConsulta = get("areas_conocimiento/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals("Área Modificada IT", responseConsulta.readEntity(AreasConocimiento.class).getNombreArea());

    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        AreasConocimiento payload = new AreasConocimiento();
        payload.setNombreArea("Intento de actualización");

        Response response = put("areas_conocimiento/" + idInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de DELETE

    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        // creamos un area temporal
        UUID idCreado = crearAreaReal("Área Temporal para Delete");

        // ELiminamos el area temporal
        Response responseDelete = delete("areas_conocimiento/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        // Verofocamos que ya no existe
        Response responseConsulta = get("areas_conocimiento/" + idCreado);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de HELPER

    /**
     * Creamo un area real en la BD usando el endpoint REST.
     * Para poder generar precondiciones en los tests de PUT y DELETE.
     */
    private UUID crearAreaReal(String nombreArea) {
        AreasConocimiento nueva = new AreasConocimiento();
        nueva.setNombreArea(nombreArea);

        Response responseCreacion = post("areas_conocimiento", nueva);
        assertEquals(201, responseCreacion.getStatus());

        String location = responseCreacion.getHeaderString("Location");
        String idString = location.substring(location.lastIndexOf('/') + 1);
        return UUID.fromString(idString);
    }


}
