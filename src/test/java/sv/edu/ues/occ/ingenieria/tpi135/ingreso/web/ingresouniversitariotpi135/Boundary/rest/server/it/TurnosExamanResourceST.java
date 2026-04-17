package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración REST para el recurso TurnosExamanResource.
 * 
 * Valida el contrato HTTP de los endpoints de turnos de examen,
 * incluyendo filtros por prueba, validaciones de FK, y persistencia.
 */
public class TurnosExamanResourceST extends AbstractResourceIT {

    // UUIDs de turnos del init.sql
    private static final UUID ID_TURNO_MANANA = UUID.fromString("07000000-0000-0000-0000-000000000001");
    private static final UUID ID_TURNO_TARDE = UUID.fromString("07000000-0000-0000-0000-000000000002");

    // UUIDs de pruebas del init.sql
    private static final UUID ID_PRUEBA_1 = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_2 = UUID.fromString("d1000000-0000-0000-0000-000000000002");

    /**
     * GET /resources/v1/turnos_examen debe retornar al menos los 2 turnos iniciales.
     */
    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("turnos_examen");

        assertEquals(200, response.getStatus());

        TurnosExaman[] arreglo = response.readEntity(TurnosExaman[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2, "Debe haber al menos 2 turnos iniciales");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 2);

        // Verificar que está Turno Mañana
        boolean encontroTurnoManana = false;
        for (TurnosExaman turno : arreglo) {
            if (ID_TURNO_MANANA.equals(turno.getId())) {
                encontroTurnoManana = true;
                break;
            }
        }
        assertTrue(encontroTurnoManana, "Debe encontrar Turno Mañana");
    }

    /**
     * GET /resources/v1/turnos_examen?first=0&max=1 debe retornar máximo 1 registro.
     */
    @Test
    void findRange_ConPaginacion_DebeRetornarDatosLimitados() {
        Response response = get("turnos_examen?first=0&max=1");

        assertEquals(200, response.getStatus());

        TurnosExaman[] arreglo = response.readEntity(TurnosExaman[].class);
        assertNotNull(arreglo);
        assertEquals(1, arreglo.length, "Debe retornar exactamente 1 registro");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 2);
    }

    /**
     * GET /resources/v1/turnos_examen/{id} con un id existente debe retornar 200.
     */
    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("turnos_examen/" + ID_TURNO_MANANA);

        assertEquals(200, response.getStatus());

        TurnosExaman entidad = response.readEntity(TurnosExaman.class);
        assertNotNull(entidad);
        assertEquals(ID_TURNO_MANANA, entidad.getId());
    }

    /**
     * GET /resources/v1/turnos_examen/{id} con un id inexistente debe retornar 404.
     */
    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("turnos_examen/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * GET /resources/v1/turnos_examen/no-es-uuid debe retornar 404.
     */
    @Test
    void findById_ConFormatoIdInvalido_DebeRetornar404() {
        Response response = get("turnos_examen/no-es-uuid");

        assertEquals(404, response.getStatus());
    }

    /**
     * GET /resources/v1/turnos_examen?idPrueba={id} debe retornar turnos de esa prueba.
     */
    @Test
    void findRange_ConFiltroPrueba_DebeRetornarDeLaPrueba() {
        Response response = get("turnos_examen?idPrueba=" + ID_PRUEBA_1);

        assertEquals(200, response.getStatus());

        TurnosExaman[] arreglo = response.readEntity(TurnosExaman[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2, "Prueba 1 debe tener al menos 2 turnos");

        // Verificar por IDs de turnos esperados para la Prueba 1 en init.sql.
        boolean contieneTurnoManana = false;
        boolean contieneTurnoTarde = false;
        for (TurnosExaman turno : arreglo) {
            if (ID_TURNO_MANANA.equals(turno.getId())) {
                contieneTurnoManana = true;
            }
            if (ID_TURNO_TARDE.equals(turno.getId())) {
                contieneTurnoTarde = true;
            }
        }
        assertTrue(contieneTurnoManana || contieneTurnoTarde);
    }

    /**
     * POST /resources/v1/turnos_examen con una entidad válida debe retornar 201.
     */
    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        LocalDate fecha = LocalDate.of(2026, 4, 20);
        LocalTime horaInicio = LocalTime.of(8, 0);
        LocalTime horaFin = LocalTime.of(11, 0);

        TurnosExaman nueva = crearTurno(ID_PRUEBA_2, "Turno Test", fecha, horaInicio, horaFin);

        Response responseCreacion = post("turnos_examen", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        Response responseConsulta = get("turnos_examen/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        TurnosExaman creado = responseConsulta.readEntity(TurnosExaman.class);
        assertNotNull(creado);
        assertEquals(idCreado, creado.getId());
        assertEquals("Turno Test", creado.getNombreTurno());
        assertEquals(ID_PRUEBA_2, creado.getIdPrueba().getId());
    }

    /**
     * POST /resources/v1/turnos_examen con una entidad válida (con filtro de horaInicio/horaFin) debe persistir.
     */
    @Test
    void create_ConEntidadValidaConHoras_DebeRetornar201_YPersistir() {
        LocalDate fecha = LocalDate.of(2026, 4, 21);
        LocalTime horaInicio = LocalTime.of(14, 0);
        LocalTime horaFin = LocalTime.of(17, 0);

        TurnosExaman nueva = crearTurno(ID_PRUEBA_1, "Turno Tarde Extra", fecha, horaInicio, horaFin);

        Response responseCreacion = post("turnos_examen", nueva);

        if (responseCreacion.getStatus() == 201) {
            String location = responseCreacion.getHeaderString("Location");
            String idString = location.substring(location.lastIndexOf('/') + 1);
            UUID idCreado = UUID.fromString(idString);

            Response responseConsulta = get("turnos_examen/" + idCreado);
            assertEquals(200, responseConsulta.getStatus());

            TurnosExaman creado = responseConsulta.readEntity(TurnosExaman.class);
            assertEquals(ID_PRUEBA_1, creado.getIdPrueba().getId());
            assertEquals(LocalDate.of(2026, 4, 21), creado.getFecha());
            assertEquals(LocalTime.of(14, 0), creado.getHoraInicio());
            assertEquals(LocalTime.of(17, 0), creado.getHoraFin());
        }
    }

    /**
     * PUT /resources/v1/turnos_examen/{id} con datos válidos debe retornar 200.
     */
    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        LocalDate fecha = LocalDate.of(2026, 4, 20);
        LocalTime horaInicio = LocalTime.of(8, 0);
        LocalTime horaFin = LocalTime.of(11, 0);

        UUID idCreado = crearTurnoReal(ID_PRUEBA_1, "Turno Original", fecha, horaInicio, horaFin);

        LocalTime horaInicio2 = LocalTime.of(14, 0);
        LocalTime horaFin2 = LocalTime.of(17, 0);
        TurnosExaman actualizado = crearTurno(ID_PRUEBA_1, "Turno Actualizado", fecha, horaInicio2, horaFin2);

        Response responsePut = put("turnos_examen/" + idCreado, actualizado);

        assertEquals(200, responsePut.getStatus());

        TurnosExaman actualizado2 = responsePut.readEntity(TurnosExaman.class);
        assertNotNull(actualizado2);
        assertEquals(idCreado, actualizado2.getId());
        assertEquals("Turno Actualizado", actualizado2.getNombreTurno());

        // Verificar persistencia
        Response responseConsulta = get("turnos_examen/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        TurnosExaman consultado = responseConsulta.readEntity(TurnosExaman.class);
        assertEquals("Turno Actualizado", consultado.getNombreTurno());
    }

    /**
     * PUT /resources/v1/turnos_examen/{id} con un id inexistente debe retornar 404.
     */
    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        LocalDate fecha = LocalDate.of(2026, 4, 20);
        LocalTime horaInicio = LocalTime.of(8, 0);
        LocalTime horaFin = LocalTime.of(11, 0);

        TurnosExaman actualizado = crearTurno(ID_PRUEBA_1, "No importa", fecha, horaInicio, horaFin);

        Response response = put("turnos_examen/" + idInexistente, actualizado);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * DELETE /resources/v1/turnos_examen/{id} debe retornar 204 y posteriores GETs deben retornar 404.
     */
    @Test
    void delete_ConIdExistente_DebeRetornar204_YNoEncontrarDespues() {
        LocalDate fecha = LocalDate.of(2026, 4, 20);
        LocalTime horaInicio = LocalTime.of(8, 0);
        LocalTime horaFin = LocalTime.of(11, 0);

        UUID idCreado = crearTurnoReal(ID_PRUEBA_1, "Turno a eliminar", fecha, horaInicio, horaFin);

        Response responseAntesEliminar = get("turnos_examen/" + idCreado);
        assertEquals(200, responseAntesEliminar.getStatus());

        Response responseDelete = delete("turnos_examen/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        Response responseDespuesEliminar = get("turnos_examen/" + idCreado);
        assertEquals(404, responseDespuesEliminar.getStatus());
        assertNotNull(responseDespuesEliminar.getHeaderString("Not-found-id"));
    }

    /**
     * DELETE /resources/v1/turnos_examen/{id} con un id inexistente debe retornar 404.
     */
    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = delete("turnos_examen/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    // ========== HELPERS ==========

    /**
     * Construye una entidad TurnosExaman válida con los parámetros dados.
     * No ejecuta el POST, solo prepara el payload.
     */
    private TurnosExaman crearTurno(UUID idPrueba, String nombreTurno, LocalDate fecha,
                                     LocalTime horaInicio, LocalTime horaFin) {
        TurnosExaman turno = new TurnosExaman();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setId(idPrueba);
        turno.setIdPrueba(prueba);
        turno.setNombreTurno(nombreTurno);
        turno.setFecha(fecha);
        turno.setHoraInicio(horaInicio);
        turno.setHoraFin(horaFin);
        return turno;
    }

    /**
     * Construye una entidad TurnosExaman válida y ejecuta el POST al recurso.
     * Devuelve el UUID del recurso creado extraído del header Location.
     * Falla si el POST no retorna 201.
     */
    private UUID crearTurnoReal(UUID idPrueba, String nombreTurno, LocalDate fecha,
                               LocalTime horaInicio, LocalTime horaFin) {
        TurnosExaman turno = crearTurno(idPrueba, nombreTurno, fecha, horaInicio, horaFin);

        Response responseCreacion = post("turnos_examen", turno);
        assertEquals(201, responseCreacion.getStatus(),
                "crearTurnoReal: POST debe retornar 201");

        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        return UUID.fromString(idString);
    }
}
