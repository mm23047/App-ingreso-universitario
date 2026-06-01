package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExamen;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST para TurnosExamanResource.
 * Base: GET/POST/PUT/DELETE /resources/v1/turnos
 * Datos semilla: init.sql tiene 2 turnos (Mañana y Tarde), ambos para Prueba Nacional UES.
 */
public class TurnosExamenResourceST extends AbstractResourceST {

    // UUIDs de turnos desde init.sql
    private static final UUID ID_TURNO_MANANA = UUID.fromString("ffff0001-0001-0001-0001-000000000001");
    private static final UUID ID_TURNO_TARDE  = UUID.fromString("ffff0002-0002-0002-0002-000000000002");

    // Prueba bajo la cual están los turnos semilla (Prueba Nacional UES)
    private static final UUID ID_PRUEBA_NACIONAL = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    // Prueba Test B para crear nuevos turnos (no tiene turnos semilla, evita conflictos)
    private static final UUID ID_PRUEBA_2 = UUID.fromString("d1000000-0000-0000-0000-000000000002");

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("turnos");

        assertEquals(200, response.getStatus());

        TurnosExamen[] arreglo = response.readEntity(TurnosExamen[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2, "Debe haber al menos 2 turnos semilla");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 2);

        boolean encontroTurnoManana = false;
        for (TurnosExamen turno : arreglo) {
            if (ID_TURNO_MANANA.equals(turno.getIdTurnoExamen())) {
                encontroTurnoManana = true;
                break;
            }
        }
        assertTrue(encontroTurnoManana, "Debe encontrar Turno Mañana en la lista");
    }

    @Test
    void findRange_ConPaginacion_DebeRetornarDatosLimitados() {
        Response response = get("turnos?first=0&max=1");

        assertEquals(200, response.getStatus());

        TurnosExamen[] arreglo = response.readEntity(TurnosExamen[].class);
        assertNotNull(arreglo);
        assertEquals(1, arreglo.length, "Debe retornar exactamente 1 registro");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 2);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("turnos/" + ID_TURNO_MANANA);

        assertEquals(200, response.getStatus());

        TurnosExamen entidad = response.readEntity(TurnosExamen.class);
        assertNotNull(entidad);
        assertEquals(ID_TURNO_MANANA, entidad.getIdTurnoExamen());
        assertEquals("Turno Mañana", entidad.getNombreTurno());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("turnos/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConFormatoIdInvalido_DebeRetornar400() {
        Response response = get("turnos/no-es-uuid");

        assertEquals(400, response.getStatus());
    }

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        LocalDate fecha = LocalDate.of(2026, 5, 10);
        LocalTime horaInicio = LocalTime.of(8, 0);
        LocalTime horaFin = LocalTime.of(11, 0);

        TurnosExamen nueva = crearTurno(ID_PRUEBA_2, "Turno ST Test", fecha, horaInicio, horaFin);

        Response responseCreacion = post("turnos", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        UUID idCreado = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));

        Response responseConsulta = get("turnos/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        TurnosExamen creado = responseConsulta.readEntity(TurnosExamen.class);
        assertEquals(idCreado, creado.getIdTurnoExamen());
        assertEquals("Turno ST Test", creado.getNombreTurno());
    }

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        LocalDate fecha = LocalDate.of(2026, 5, 11);
        LocalTime horaInicio = LocalTime.of(8, 0);
        LocalTime horaFin = LocalTime.of(11, 0);

        UUID idCreado = crearTurnoReal(ID_PRUEBA_2, "Turno ST Original", fecha, horaInicio, horaFin);

        TurnosExamen actualizado = crearTurno(ID_PRUEBA_2, "Turno ST Actualizado",
                fecha, LocalTime.of(14, 0), LocalTime.of(17, 0));

        Response responsePut = put("turnos/" + idCreado, actualizado);
        assertEquals(200, responsePut.getStatus());

        TurnosExamen cuerpo = responsePut.readEntity(TurnosExamen.class);
        assertEquals(idCreado, cuerpo.getIdTurnoExamen());
        assertEquals("Turno ST Actualizado", cuerpo.getNombreTurno());

        Response responseConsulta = get("turnos/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals("Turno ST Actualizado", responseConsulta.readEntity(TurnosExamen.class).getNombreTurno());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        TurnosExamen payload = crearTurno(ID_PRUEBA_2, "No importa",
                LocalDate.of(2026, 5, 10), LocalTime.of(8, 0), LocalTime.of(11, 0));

        Response response = put("turnos/" + idInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConIdExistente_DebeRetornar204_YNoEncontrarDespues() {
        LocalDate fecha = LocalDate.of(2026, 5, 12);
        LocalTime horaInicio = LocalTime.of(8, 0);
        LocalTime horaFin = LocalTime.of(11, 0);

        UUID idCreado = crearTurnoReal(ID_PRUEBA_2, "Turno ST Delete", fecha, horaInicio, horaFin);

        Response responseDelete = delete("turnos/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        Response responseDespues = get("turnos/" + idCreado);
        assertEquals(404, responseDespues.getStatus());
        assertNotNull(responseDespues.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = delete("turnos/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    private TurnosExamen crearTurno(UUID idPrueba, String nombreTurno,
                                    LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        TurnosExamen turno = new TurnosExamen();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(idPrueba);
        turno.setPruebaAdmision(prueba);
        turno.setNombreTurno(nombreTurno);
        turno.setFecha(fecha);
        turno.setHoraInicio(horaInicio);
        turno.setHoraFin(horaFin);
        return turno;
    }

    private UUID crearTurnoReal(UUID idPrueba, String nombreTurno,
                                LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        TurnosExamen turno = crearTurno(idPrueba, nombreTurno, fecha, horaInicio, horaFin);

        Response responseCreacion = post("turnos", turno);
        assertEquals(201, responseCreacion.getStatus(), "Helper crearTurnoReal: POST debe retornar 201");

        String location = responseCreacion.getHeaderString("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }
}
