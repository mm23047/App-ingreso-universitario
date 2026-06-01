package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST para ClaveExamenResource.
 * Base: GET/POST/PUT/DELETE /resources/v1/claves
 * Nota: GET /claves (list) también disponible con filtro ?idPrueba=.
 * Datos semilla: Clave A (08000000...001) y Clave B (aaaabbbb...), ambas bajo Prueba Test A.
 */
public class ClavesExamenResourceST extends AbstractResourceST {

    // UUIDs desde init.sql
    private static final UUID ID_CLAVE_A = UUID.fromString("08000000-0000-0000-0000-000000000001");
    private static final UUID ID_CLAVE_B = UUID.fromString("aaaabbbb-cccc-dddd-eeee-ffffffffffff");

    // Prueba y etapa que usan las claves semilla
    private static final UUID ID_PRUEBA_1 = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_2 = UUID.fromString("d1000000-0000-0000-0000-000000000002");

    // Etapa con cantidadPreguntasRequeridas=20 (para crear nuevas claves con capacidad)
    private static final UUID ID_ETAPA_PRIMERA = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    // Etapa usada por claves semilla
    private static final UUID ID_ETAPA_INSCRIPCION = UUID.fromString("c1000000-0000-0000-0000-000000000001");

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("claves");

        assertEquals(200, response.getStatus());

        ClavesExamen[] arreglo = response.readEntity(ClavesExamen[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2, "Debe haber al menos 2 claves semilla");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 2);

        // Verificar que está Clave A
        boolean encontroClaveA = false;
        for (ClavesExamen clave : arreglo) {
            if (ID_CLAVE_A.equals(clave.getIdClaveExaman())) {
                encontroClaveA = true;
                assertEquals("Clave A", clave.getNombreClave());
                break;
            }
        }
        assertTrue(encontroClaveA, "Debe encontrar Clave A en la lista");
    }

    @Test
    void findRange_ConPaginacion_DebeRetornarDatosLimitados() {
        Response response = get("claves?first=0&max=1");

        assertEquals(200, response.getStatus());

        ClavesExamen[] arreglo = response.readEntity(ClavesExamen[].class);
        assertNotNull(arreglo);
        assertEquals(1, arreglo.length, "Debe retornar exactamente 1 registro");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 2);
    }

    @Test
    void findRange_ConFiltroPrueba_DebeRetornarClavesDeEsaPrueba() {
        Response response = get("claves?idPrueba=" + ID_PRUEBA_1);

        assertEquals(200, response.getStatus());

        ClavesExamen[] arreglo = response.readEntity(ClavesExamen[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2, "Prueba 1 debe tener al menos 2 claves");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 2);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("claves/" + ID_CLAVE_A);

        assertEquals(200, response.getStatus());

        ClavesExamen entidad = response.readEntity(ClavesExamen.class);
        assertNotNull(entidad);
        assertEquals(ID_CLAVE_A, entidad.getIdClaveExaman());
        assertEquals("Clave A", entidad.getNombreClave());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("claves/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConFormatoIdInvalido_DebeRetornar400() {
        Response response = get("claves/no-es-uuid");

        assertEquals(400, response.getStatus());
    }

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        // Usar Prueba 2 y Primera Etapa para no duplicar claves semilla
        ClavesExamen nueva = crearClave(ID_PRUEBA_2, ID_ETAPA_PRIMERA, "Clave ST Test");

        Response responseCreacion = post("claves", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        UUID idCreado = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));

        Response responseConsulta = get("claves/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        ClavesExamen creada = responseConsulta.readEntity(ClavesExamen.class);
        assertEquals(idCreado, creada.getIdClaveExaman());
        assertEquals("Clave ST Test", creada.getNombreClave());
    }

    @Test
    void create_ConEntidadInvalida_SinNombreNiPrueba_DebeRetornar400() {
        // El recurso valida nombreClave y pruebaAdmision → 400 BAD_REQUEST (sin header MISSING_PARAMETER)
        ClavesExamen invalida = new ClavesExamen();

        Response response = post("claves", invalida);

        assertEquals(400, response.getStatus());
    }

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        UUID idCreado = crearClaveReal(ID_PRUEBA_2, ID_ETAPA_PRIMERA, "Clave ST Original");

        ClavesExamen actualizada = new ClavesExamen();
        actualizada.setNombreClave("Clave ST Actualizada");

        Response responsePut = put("claves/" + idCreado, actualizada);
        assertEquals(200, responsePut.getStatus());

        ClavesExamen cuerpo = responsePut.readEntity(ClavesExamen.class);
        assertEquals(idCreado, cuerpo.getIdClaveExaman());
        assertEquals("Clave ST Actualizada", cuerpo.getNombreClave());

        Response responseConsulta = get("claves/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals("Clave ST Actualizada", responseConsulta.readEntity(ClavesExamen.class).getNombreClave());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        ClavesExamen payload = new ClavesExamen();
        payload.setNombreClave("No importa");

        Response response = put("claves/" + idInexistente, payload);

        // ClaveExamenResource.updateClave devuelve 404 sin header Not-found-id para este caso
        assertEquals(404, response.getStatus());
    }

    @Test
    void delete_ConIdExistente_DebeRetornar204_YNoEncontrarDespues() {
        UUID idCreado = crearClaveReal(ID_PRUEBA_2, ID_ETAPA_PRIMERA, "Clave ST Delete");

        Response responseAntesEliminar = get("claves/" + idCreado);
        assertEquals(200, responseAntesEliminar.getStatus());

        Response responseDelete = delete("claves/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        Response responseDespues = get("claves/" + idCreado);
        assertEquals(404, responseDespues.getStatus());
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = delete("claves/" + idInexistente);

        // ClaveExamenResource.deleteClave devuelve 404 sin header Not-found-id para este caso
        assertEquals(404, response.getStatus());
    }

    private ClavesExamen crearClave(UUID idPrueba, UUID idEtapa, String nombreClave) {
        ClavesExamen clave = new ClavesExamen();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(idPrueba);
        clave.setPruebaAdmision(prueba);
        EtapasAdmision etapa = new EtapasAdmision();
        etapa.setIdEtapaAdmision(idEtapa);
        clave.setEtapaAdmision(etapa);
        clave.setNombreClave(nombreClave);
        return clave;
    }

    private UUID crearClaveReal(UUID idPrueba, UUID idEtapa, String nombreClave) {
        ClavesExamen clave = crearClave(idPrueba, idEtapa, nombreClave);

        Response responseCreacion = post("claves", clave);
        assertEquals(201, responseCreacion.getStatus(), "Helper crearClaveReal: POST debe retornar 201");

        String location = responseCreacion.getHeaderString("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }
}
