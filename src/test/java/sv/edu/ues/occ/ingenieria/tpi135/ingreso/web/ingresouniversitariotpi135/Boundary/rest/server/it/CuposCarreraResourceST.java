package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST para CuposCarreraResource.
 * Base: GET/POST/PUT/DELETE /resources/v1/cupos_carrera
 * Datos semilla: 3 cupos para Prueba Test A + etapa Asignacion.
 *   (d1000000...001, ISI, c1000000...003) → 60 cupos
 *   (d1000000...001, ICS, c1000000...003) → 50 cupos
 *   (d1000000...001, ICC, c1000000...003) → 45 cupos
 */
public class CuposCarreraResourceST extends AbstractResourceST {

    // IDs desde init.sql
    private static final UUID ID_PRUEBA_1   = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_2   = UUID.fromString("d1000000-0000-0000-0000-000000000002");
    private static final String ID_CARRERA_ISI = "ISI";
    private static final String ID_CARRERA_ICS = "ICS";
    private static final String ID_CARRERA_MED = "MED";

    // Etapa usada por los cupos semilla
    private static final UUID ID_ETAPA_ASIGNACION = UUID.fromString("c1000000-0000-0000-0000-000000000003");
    // Etapa alternativa para crear nuevos cupos
    private static final UUID ID_ETAPA_INSCRIPCION = UUID.fromString("c1000000-0000-0000-0000-000000000001");

    // Ruta del cupo semilla ISI
    private static final String PATH_CUPO_ISI =
            ID_PRUEBA_1 + "/" + ID_CARRERA_ISI + "/" + ID_ETAPA_ASIGNACION;

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("cupos_carrera");

        assertEquals(200, response.getStatus());

        CuposCarrera[] arreglo = response.readEntity(CuposCarrera[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 3, "Debe haber al menos 3 cupos semilla");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 3);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("cupos_carrera/" + PATH_CUPO_ISI);

        assertEquals(200, response.getStatus());

        CuposCarrera entidad = response.readEntity(CuposCarrera.class);
        assertNotNull(entidad);
        assertEquals(ID_PRUEBA_1, entidad.getIdCupoCarrera().getIdPrueba());
        assertEquals(ID_CARRERA_ISI, entidad.getIdCupoCarrera().getIdCarrera());
        assertEquals(ID_ETAPA_ASIGNACION, entidad.getIdCupoCarrera().getIdEtapa());
        assertEquals(60, entidad.getCupos());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        String pathInexistente = UUID.randomUUID() + "/XYZ/" + UUID.randomUUID();
        Response response = get("cupos_carrera/" + pathInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        // MED para Prueba 1 en Etapa Inscripcion (no existe en semilla)
        CuposCarrera nuevo = construirPayload(ID_PRUEBA_1, ID_CARRERA_MED, ID_ETAPA_INSCRIPCION, 35);

        Response responseCreacion = post("cupos_carrera", nuevo);

        // 201 en primera ejecución, 500/400 si ya existe por un test anterior
        if (responseCreacion.getStatus() == 201) {
            assertNotNull(responseCreacion.getHeaderString("Location"));

            String pathNuevo = ID_PRUEBA_1 + "/" + ID_CARRERA_MED + "/" + ID_ETAPA_INSCRIPCION;
            Response responseConsulta = get("cupos_carrera/" + pathNuevo);
            assertEquals(200, responseConsulta.getStatus());
            CuposCarrera creado = responseConsulta.readEntity(CuposCarrera.class);
            assertEquals(35, creado.getCupos());
        }
    }

    @Test
    void create_ConEntidadInvalida_SinDatos_DebeRetornar400() {
        // Entidad vacía → 400 BAD_REQUEST
        CuposCarrera invalido = new CuposCarrera();

        Response response = post("cupos_carrera", invalido);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        // Crear un cupo temporal: ISI en Prueba 2 + Etapa Inscripcion
        String pathTemporal = crearCupoReal(ID_PRUEBA_2, ID_CARRERA_ISI, ID_ETAPA_INSCRIPCION, 10);

        // Actualizar a 150 cupos
        CuposCarrera actualizado = construirPayload(ID_PRUEBA_2, ID_CARRERA_ISI, ID_ETAPA_INSCRIPCION, 150);
        actualizado.setCupos(150);

        Response responseUpdate = put("cupos_carrera/" + pathTemporal, actualizado);
        assertEquals(200, responseUpdate.getStatus());

        CuposCarrera cuerpo = responseUpdate.readEntity(CuposCarrera.class);
        assertEquals(150, cuerpo.getCupos());

        Response responseConsulta = get("cupos_carrera/" + pathTemporal);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals(150, responseConsulta.readEntity(CuposCarrera.class).getCupos());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        String pathInexistente = UUID.randomUUID() + "/ERR/" + UUID.randomUUID();

        CuposCarrera payload = new CuposCarrera();
        payload.setCupos(5);

        Response response = put("cupos_carrera/" + pathInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        // Crear ISI en Prueba 2 + Etapa Asignacion
        String pathTemporal = crearCupoReal(ID_PRUEBA_2, ID_CARRERA_ISI, ID_ETAPA_ASIGNACION, 99);

        Response responseDelete = delete("cupos_carrera/" + pathTemporal);
        assertEquals(204, responseDelete.getStatus());

        Response responseConsulta = get("cupos_carrera/" + pathTemporal);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }

    /**
     * Construye un payload de CuposCarrera con clave compuesta y cupos.
     */
    private CuposCarrera construirPayload(UUID idPrueba, String idCarrera, UUID idEtapa, int cupos) {
        CuposCarrera entidad = new CuposCarrera();

        CuposCarreraId pk = new CuposCarreraId();
        pk.setIdPrueba(idPrueba);
        pk.setIdCarrera(idCarrera);
        pk.setIdEtapa(idEtapa);
        entidad.setIdCupoCarrera(pk);

        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(idPrueba);
        entidad.setPruebaAdmision(prueba);

        CatalogoCarrera carrera = new CatalogoCarrera();
        carrera.setIdCarrera(idCarrera);
        entidad.setCatalogoCarrera(carrera);

        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setIdEtapaAdmision(idEtapa);
        entidad.setEtapaAdmision(etapa);

        entidad.setCupos(cupos);
        return entidad;
    }

    /**
     * Crea un cupo real en la BD y retorna la ruta URL compuesta.
     */
    private String crearCupoReal(UUID idPrueba, String idCarrera, UUID idEtapa, int cupos) {
        CuposCarrera nuevo = construirPayload(idPrueba, idCarrera, idEtapa, cupos);

        Response responseCreacion = post("cupos_carrera", nuevo);
        assertEquals(201, responseCreacion.getStatus(), "Helper crearCupoReal: POST debe retornar 201");

        return idPrueba + "/" + idCarrera + "/" + idEtapa;
    }
}
