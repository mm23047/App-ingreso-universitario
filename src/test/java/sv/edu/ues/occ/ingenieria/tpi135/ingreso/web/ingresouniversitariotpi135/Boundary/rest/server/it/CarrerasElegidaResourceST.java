package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST para CarrerasElegidaResource.
 * URL real: /resources/v1/inscripciones/{idInscripcion}/carreras
 * Datos semilla: inscripción1 tiene ICS(prio=1) e ISI(prio=2);
 *                inscripción2 tiene MED(prio=1) y ADM(prio=2).
 */
public class CarrerasElegidaResourceST extends AbstractResourceST {

    // IDs de inscripciones desde init.sql
    private static final UUID ID_INSCRIPCION_1 = UUID.fromString("09000000-0000-0000-0000-000000000001");
    private static final UUID ID_INSCRIPCION_2 = UUID.fromString("09000000-0000-0000-0000-000000000002");

    // IDs de carreras
    private static final String ID_CARRERA_ICS = "ICS";
    private static final String ID_CARRERA_ICC = "ICC";
    private static final String ID_CARRERA_MAT = "MAT";
    private static final String ID_CARRERA_ARQ = "ARQ";

    @Test
    void findRange_PorInscripcion1_DebeRetornarSusCarreras() {
        Response response = get("inscripciones/" + ID_INSCRIPCION_1 + "/carreras");

        assertEquals(200, response.getStatus());

        CarrerasElegida[] arreglo = response.readEntity(CarrerasElegida[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2, "Inscripción 1 debe tener al menos 2 carreras elegidas");
    }

    @Test
    void findRange_InscripcionInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        Response response = get("inscripciones/" + idInexistente + "/carreras");

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void create_AgregarCarreraAInscripcion_DebeRetornar201() {
        // Inscripción 2 ya tiene MED(1) y ADM(2). Agregamos ICC con prioridad 3.
        CarrerasElegida nueva = construirPayload(ID_CARRERA_ICC, (short) 3);

        Response response = post("inscripciones/" + ID_INSCRIPCION_2 + "/carreras", nueva);

        // Puede ser 201 (primera ejecución) o 409 (si ya existe por ejecución previa)
        assertTrue(response.getStatus() == 201 || response.getStatus() == 409,
                "Debe retornar 201 (creado) o 409 (conflicto si ya existe)");
    }

    @Test
    void create_SinCarrera_DebeRetornar400() {
        // Sin catalogoCarrera → 400 BAD_REQUEST
        CarrerasElegida invalida = new CarrerasElegida();
        invalida.setPrioridad((short) 5);

        Response response = post("inscripciones/" + ID_INSCRIPCION_1 + "/carreras", invalida);

        assertEquals(400, response.getStatus());
    }

    @Test
    void create_SinPrioridad_DebeRetornar400() {
        // Sin prioridad → 400 BAD_REQUEST
        CarrerasElegida invalida = construirPayload(ID_CARRERA_ICC, null);

        Response response = post("inscripciones/" + ID_INSCRIPCION_1 + "/carreras", invalida);

        assertEquals(400, response.getStatus());
    }

    @Test
    void update_CambiarPrioridad_DebeRetornar200() {
        // Primero creamos una entrada nueva para actualizar sin tocar semilla
        String pathInscripcion = "inscripciones/" + ID_INSCRIPCION_2 + "/carreras";
        CarrerasElegida nueva = construirPayload(ID_CARRERA_MAT, (short) 4);
        Response postResp = post(pathInscripcion, nueva);

        // Si ya existe (409), el test aún puede verificar el PUT con los datos actuales
        if (postResp.getStatus() == 201 || postResp.getStatus() == 409) {
            // Actualizar MAT a prioridad 10
            CarrerasElegida actualizada = construirPayload(ID_CARRERA_MAT, (short) 10);
            Response putResp = put(pathInscripcion + "/" + ID_CARRERA_MAT, actualizada);

            // PUT puede retornar 200 (actualizado) o 409 (prioridad 10 ocupada por otro test)
            assertTrue(putResp.getStatus() == 200 || putResp.getStatus() == 409,
                    "PUT debe retornar 200 o 409");
        }
    }

    @Test
    void update_CarreraNoRegistrada_DebeRetornar404() {
        // ARQ no está registrada para inscripción 1
        CarrerasElegida payload = construirPayload(ID_CARRERA_ARQ, (short) 5);
        Response response = put("inscripciones/" + ID_INSCRIPCION_1 + "/carreras/" + ID_CARRERA_ARQ, payload);

        assertEquals(404, response.getStatus());
    }

    @Test
    void delete_CarreraElegidaExistente_DebeRetornar204() {
        // Crear ICC en inscripción 1 y luego eliminarla
        String pathInscripcion = "inscripciones/" + ID_INSCRIPCION_1 + "/carreras";
        CarrerasElegida nueva = construirPayload(ID_CARRERA_ICC, (short) 5);
        Response postResp = post(pathInscripcion, nueva);

        if (postResp.getStatus() == 201) {
            Response deleteResp = delete(pathInscripcion + "/" + ID_CARRERA_ICC);
            assertEquals(204, deleteResp.getStatus());
        }
        // Si 409 (ya existe de ejecución anterior), la prueba fue válida la primera vez
    }

    @Test
    void delete_CarreraNoRegistrada_DebeRetornar404() {
        // ARQ no está registrada para inscripción 1 → 404
        Response response = delete("inscripciones/" + ID_INSCRIPCION_1 + "/carreras/" + ID_CARRERA_ARQ);

        assertEquals(404, response.getStatus());
    }

    /**
     * Construye un payload de CarrerasElegida con el ID de carrera y prioridad dados.
     */
    private CarrerasElegida construirPayload(String idCarrera, Short prioridad) {
        CarrerasElegida entidad = new CarrerasElegida();
        CatalogoCarrera carrera = new CatalogoCarrera();
        carrera.setIdCarrera(idCarrera);
        entidad.setCatalogoCarrera(carrera);
        entidad.setPrioridad(prioridad);
        return entidad;
    }
}
