package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import java.util.UUID;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import static org.junit.jupiter.api.Assertions.*;

public class EtapasAdmisionResourceST extends AbstractResourceST{

    // ID semilla
    private static final UUID ID_ETAPA_1 = UUID.fromString("c1000000-0000-0000-0000-000000000001");

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de LECTURA (GET)

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("etapas_admision");

        assertEquals(200, response.getStatus());

        EtapasAdmision[] arreglo = response.readEntity(EtapasAdmision[].class);
        assertNotNull(arreglo);

        // Tenemos 3 etapas en el script
        assertTrue(arreglo.length >= 3);

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 3);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("etapas_admision/" + ID_ETAPA_1);

        assertEquals(200, response.getStatus());

        EtapasAdmision entidad = response.readEntity(EtapasAdmision.class);
        assertNotNull(entidad);

        assertEquals(ID_ETAPA_1, entidad.getId());
        assertEquals("Etapa 1 - Matemáticas", entidad.getNombre());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.randomUUID();
        Response response = get("etapas_admision/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de CREAR (POST)

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        EtapasAdmision nuevaEtapa = new EtapasAdmision();
        nuevaEtapa.setNombre("Vuelta 3");
        nuevaEtapa.setDescripcion("Ultima Oportunidad");

        Response responseCreacion = post("etapas_admision", nuevaEtapa);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        // Extraemos el UUID de la URL autogenerada
        String idCreadoStr = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idCreadoStr);

        // Verificar persistencia
        Response responseConsulta = get("etapas_admision/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        EtapasAdmision creado = responseConsulta.readEntity(EtapasAdmision.class);
        assertEquals(idCreado, creado.getId());
        assertEquals("Vuelta 3", creado.getNombre());
    }

    @Test
    void create_ConEntidadInvalida_ConIdIncluido_DebeRetornar422() {
        // Enviamos una entidad no valida
        EtapasAdmision invalida = new EtapasAdmision();
        invalida.setId(UUID.randomUUID());
        invalida.setNombre("Etapa Inválida");

        Response response = post("etapas_admision", invalida);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de ACTUALIZAION (PUT)

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        // Creamos etapa temporal
        UUID idCreado = crearEtapaReal("Etapa Temporal Update");

        // Construimos el payload actualizado
        EtapasAdmision actualizada = new EtapasAdmision();
        actualizada.setNombre("Etapa Modificada IT");

        // Hacemos PUT
        Response responseUpdate = put("etapas_admision/" + idCreado, actualizada);
        assertEquals(200, responseUpdate.getStatus());

        EtapasAdmision cuerpo = responseUpdate.readEntity(EtapasAdmision.class);
        assertEquals("Etapa Modificada IT", cuerpo.getNombre());

        // Verificamos la persistencia
        Response responseConsulta = get("etapas_admision/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals("Etapa Modificada IT", responseConsulta.readEntity(EtapasAdmision.class).getNombre());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.randomUUID();
        EtapasAdmision payload = new EtapasAdmision();
        payload.setNombre("Intento de actualización");

        Response response = put("etapas_admision/" + idInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de DELETE

    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        // Creamos una etapa temporal
        UUID idCreado = crearEtapaReal("Etapa Temporal para Delete");

        // Eliminamos la etapa temporal
        Response responseDelete = delete("etapas_admision/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        // Verificamos que ya no existe
        Response responseConsulta = get("etapas_admision/" + idCreado);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de HELPER

    /**
     * Crea una etapa real en la BD usando el endpoint REST.
     * Retorna el UUID generado automáticamente por el servidor.
     */
    private UUID crearEtapaReal(String nombre) {
        EtapasAdmision nueva = new EtapasAdmision();
        nueva.setNombre(nombre);

        Response responseCreacion = post("etapas_admision", nueva);
        assertEquals(201, responseCreacion.getStatus(), "No se pudo crear la etapa temporal");

        String location = responseCreacion.getHeaderString("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }

}
