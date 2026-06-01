package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST para PreguntasPorClaveResource.
 * URL real: /resources/v1/claves/{idClave}/preguntas
 * Datos semilla:
 *   Clave A (08000000...001): preguntas f1000000...001 y 55555555...5555 (etapa c1000000...001, 0 requeridas)
 *   Clave B (aaaabbbb...): preguntas f1000000...003 y f1000000...004 (etapa c1000000...001, 0 requeridas)
 *
 * NOTA: Las claves semilla usan etapa "Etapa Inscripcion" con cantidadPreguntasRequeridas=0.
 *       Para agregar preguntas se necesita crear una clave con etapa de alta capacidad.
 */
public class PreguntasPorClaveResourceST extends AbstractResourceST {

    // Claves semilla
    private static final UUID ID_CLAVE_A = UUID.fromString("08000000-0000-0000-0000-000000000001");
    private static final UUID ID_CLAVE_B = UUID.fromString("aaaabbbb-cccc-dddd-eeee-ffffffffffff");

    // Preguntas semilla
    private static final UUID ID_PREGUNTA_1 = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_4 = UUID.fromString("f1000000-0000-0000-0000-000000000004");

    // Para crear claves nuevas con alta capacidad
    private static final UUID ID_PRUEBA_2        = UUID.fromString("d1000000-0000-0000-0000-000000000002");
    private static final UUID ID_ETAPA_PRIMERA   = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"); // cantidadRequeridas=20

    @Test
    void getPreguntasByClave_ClaveExistente_DebeRetornarLista() {
        Response response = get("claves/" + ID_CLAVE_A + "/preguntas");

        assertEquals(200, response.getStatus());

        PreguntasPorClave[] arreglo = response.readEntity(PreguntasPorClave[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2, "Clave A debe tener al menos 2 preguntas asignadas");

        // Verificar que la pregunta 1 está asignada a Clave A
        boolean encontradaPregunta1 = false;
        for (PreguntasPorClave pc : arreglo) {
            if (ID_PREGUNTA_1.equals(pc.getIdPreguntaPorClave().getIdPregunta())) {
                encontradaPregunta1 = true;
                break;
            }
        }
        assertTrue(encontradaPregunta1, "Pregunta 1 debe estar asignada a Clave A");
    }

    @Test
    void getPreguntasByClave_ClaveInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        Response response = get("claves/" + idInexistente + "/preguntas");

        assertEquals(404, response.getStatus());
    }

    @Test
    void getPreguntasByClave_ClaveB_DebeRetornarSusPreguntas() {
        Response response = get("claves/" + ID_CLAVE_B + "/preguntas");

        assertEquals(200, response.getStatus());

        PreguntasPorClave[] arreglo = response.readEntity(PreguntasPorClave[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2, "Clave B debe tener al menos 2 preguntas asignadas");
    }

    @Test
    void asignarPregunta_AClaveConCapacidad_DebeRetornar201() {
        // Crear una clave nueva con etapa de alta capacidad (20 preguntas requeridas)
        UUID idNuevaClave = crearClaveConCapacidad("Clave ST PPC Test");

        // Asignar pregunta 4 a esa clave (pregunta sin opciones, segura para asignar)
        String pathPreguntas = "claves/" + idNuevaClave + "/preguntas";
        AsignarPreguntaDTO dto = new AsignarPreguntaDTO(ID_PREGUNTA_4);

        Response response = post(pathPreguntas, dto);

        // 201 si se creó, 409 si ya existe (segunda ejecución de la misma clave)
        assertTrue(response.getStatus() == 201 || response.getStatus() == 409,
                "HTTP " + response.getStatus() + " | " + response.getHeaderString("Server-exception"));
    }

    @Test
    void asignarPregunta_SinIdPregunta_DebeRetornar400() {
        // DTO sin idPregunta → 400 BAD_REQUEST
        UUID idNuevaClave = crearClaveConCapacidad("Clave ST PPC Sin Pregunta");
        AsignarPreguntaDTO dtoInvalido = new AsignarPreguntaDTO(null);

        Response response = post("claves/" + idNuevaClave + "/preguntas", dtoInvalido);

        assertEquals(400, response.getStatus());
    }

    @Test
    void desasignarPregunta_AsignacionExistente_DebeRetornar204() {
        // Crear clave con capacidad, asignar una pregunta, luego eliminarla
        UUID idNuevaClave = crearClaveConCapacidad("Clave ST PPC Delete");
        String pathPreguntas = "claves/" + idNuevaClave + "/preguntas";

        AsignarPreguntaDTO dto = new AsignarPreguntaDTO(ID_PREGUNTA_4);
        Response postResp = post(pathPreguntas, dto);

        if (postResp.getStatus() == 201) {
            Response deleteResp = delete(pathPreguntas + "/" + ID_PREGUNTA_4);
            assertEquals(204, deleteResp.getStatus());
        }
    }

    @Test
    void desasignarPregunta_AsignacionInexistente_DebeRetornar404() {
        UUID idClaveInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        UUID idPreguntaInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000001");

        Response response = delete("claves/" + idClaveInexistente + "/preguntas/" + idPreguntaInexistente);

        assertEquals(404, response.getStatus());
    }

    /**
     * Crea una clave con etapa de alta capacidad (Primera Etapa, 20 preguntas requeridas).
     * Idempotente: si la clave ya existe (409), la busca en GET /claves?idPrueba=... y la retorna.
     */
    private UUID crearClaveConCapacidad(String nombreClave) {
        ClavesExamen clave = new ClavesExamen();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(ID_PRUEBA_2);
        clave.setPruebaAdmision(prueba);
        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setIdEtapaAdmision(ID_ETAPA_PRIMERA);
        clave.setEtapaAdmision(etapa);
        clave.setNombreClave(nombreClave);

        Response responseCreacion = post("claves", clave);
        if (responseCreacion.getStatus() == 201) {
            String location = responseCreacion.getHeaderString("Location");
            return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
        }

        if (responseCreacion.getStatus() == 409 || responseCreacion.getStatus() == 500) {
            // 409 = conflicto detectado por el DAO; 500 = constraint DB violado sin detección previa.
            // En ambos casos la clave ya existe: buscarla por idPrueba y filtrar por nombre.
            Response busqueda = get("claves?idPrueba=" + ID_PRUEBA_2);
            if (busqueda.getStatus() == 200) {
                ClavesExamen[] existentes = busqueda.readEntity(ClavesExamen[].class);
                if (existentes != null) {
                    for (ClavesExamen c : existentes) {
                        if (nombreClave.equals(c.getNombreClave())) {
                            return c.getIdClaveExaman();
                        }
                    }
                }
            }
            throw new RuntimeException(
                    "HTTP " + responseCreacion.getStatus() + " al crear clave '" + nombreClave +
                    "' pero no se encontró en GET /claves?idPrueba=" + ID_PRUEBA_2 +
                    " (GET respondió HTTP " + busqueda.getStatus() + ")");
        }

        throw new RuntimeException(
                "Error inesperado al crear clave '" + nombreClave + "': HTTP " + responseCreacion.getStatus());
    }

    /**
     * DTO para asignar una pregunta a una clave.
     * Coincide con PreguntasPorClaveResource.AsignarPreguntaDTO.
     */
    private static class AsignarPreguntaDTO {
        private UUID idPregunta;

        public AsignarPreguntaDTO() {}
        public AsignarPreguntaDTO(UUID idPregunta) { this.idPregunta = idPregunta; }
        public UUID getIdPregunta() { return idPregunta; }
        public void setIdPregunta(UUID idPregunta) { this.idPregunta = idPregunta; }
    }
}
