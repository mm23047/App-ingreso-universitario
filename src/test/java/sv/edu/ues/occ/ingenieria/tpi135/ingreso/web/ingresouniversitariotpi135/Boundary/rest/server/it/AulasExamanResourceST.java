package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AulasExamanResourceST extends AbstractResourceIT{
    // IDs semilla
    private static final UUID ID_AULA_1 = UUID.fromString("0a000000-0000-0000-0000-000000000001");
    private static final UUID ID_TURNO_1 = UUID.fromString("07000000-0000-0000-0000-000000000001");

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de LECTURA (GET)

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("aulas_examen");

        assertEquals(200, response.getStatus());

        AulasExaman[] arreglo = response.readEntity(AulasExaman[].class);
        assertNotNull(arreglo);

        // Tenemos 2 registros en el script
        assertTrue(arreglo.length >= 2);

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 2);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("aulas_examen/" + ID_AULA_1);

        assertEquals(200, response.getStatus());

        AulasExaman entidad = response.readEntity(AulasExaman.class);
        assertNotNull(entidad);

        assertEquals(ID_AULA_1, entidad.getId());
        assertNotNull(entidad.getId());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.randomUUID();
        Response response = get("aulas_examen/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de CREAR (POST)

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {

        AulasExaman nueva = construirPayload("AULA-NEW-POST", ID_TURNO_1);

        Response responseCreacion = post("aulas_examen", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        // Extraemos el UUID autogenerado
        String idCreadoStr = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idCreadoStr);

        // Verificamos persistencia
        Response responseConsulta = get("aulas_examen/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        AulasExaman creado = responseConsulta.readEntity(AulasExaman.class);
        assertEquals("AULA-NEW-POST", creado.getIdAulaApi());
        assertEquals(30, creado.getCapacidad());
    }

    @Test
    void create_ConEntidadInvalida_FaltaTurno_DebeRetornar422() {
        // Enviamos entidad INVALIDA
        AulasExaman invalida = new AulasExaman();
        invalida.setIdAulaApi("AULA-ERROR");

        Response response = post("aulas_examen", invalida);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }


    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de ACTUALIZAION (PUT)

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        // Creamos un registro temporal
        UUID idTemporal = crearRegistroReal("AULA-TEMP-PUT", ID_TURNO_1);

        // Construimos el payload actualizado- cambiamos el nombre y la capacidad
        AulasExaman actualizada = construirPayload("AULA-MODIFICADA", ID_TURNO_1);
        actualizada.setCapacidad(50);

        // Hacemos PUT
        Response responseUpdate = put("aulas_examen/" + idTemporal, actualizada);
        assertEquals(200, responseUpdate.getStatus());

        AulasExaman cuerpo = responseUpdate.readEntity(AulasExaman.class);
        assertEquals("AULA-MODIFICADA", cuerpo.getIdAulaApi());
        assertEquals(50, cuerpo.getCapacidad());

        // Verificamos la persistencia
        Response responseConsulta = get("aulas_examen/" + idTemporal);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals("AULA-MODIFICADA", responseConsulta.readEntity(AulasExaman.class).getIdAulaApi());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.randomUUID();
        AulasExaman payload = construirPayload("AULA-ERR", ID_TURNO_1);

        Response response = put("aulas_examen/" + idInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }


    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de DELETE

    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        // Creamos un registro temporal
        UUID idTemporal = crearRegistroReal("AULA-TEMP-DEL", ID_TURNO_1);

        // Eliminamos
        Response responseDelete = delete("aulas_examen/" + idTemporal);
        assertEquals(204, responseDelete.getStatus());

        // Verificamos que ya no existe
        Response responseConsulta = get("aulas_examen/" + idTemporal);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }

    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    //Casos de HELPER

    /**
     * Construye una entidad AulasExamen lista para enviar por POST/PUT.
     * Asigna el objeto relacional (Turno) para evadir el Error 422 del backend.
     */
    private AulasExaman construirPayload(String idAulaApi, UUID idTurno) {
        AulasExaman entidad = new AulasExaman();

        entidad.setIdAulaApi(idAulaApi);
        entidad.setCapacidad(30);
        entidad.setCuposOcupados(0);
        entidad.setAccesibleSillaRuedas(true);

        // Inicializar objeto relacional.
        TurnosExaman turno = new TurnosExaman();
        turno.setId(idTurno);
        entidad.setIdTurno(turno);

        return entidad;
    }

    /**
     * Crea un registro real en BD y retorna su UUID generado.
     */
    private UUID crearRegistroReal(String idAulaApi, UUID idTurno) {
        AulasExaman nueva = construirPayload(idAulaApi, idTurno);

        Response responseCreacion = post("aulas_examen", nueva);

        assertEquals(201, responseCreacion.getStatus(), "Fallo al crear registro temporal en el helper");

        String location = responseCreacion.getHeaderString("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }

}
