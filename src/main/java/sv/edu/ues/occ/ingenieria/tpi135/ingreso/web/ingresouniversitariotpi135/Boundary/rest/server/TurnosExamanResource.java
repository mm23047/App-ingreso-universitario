package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TurnosExamenDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExamen;

import java.net.URI;
import java.util.UUID;

/**
 * Recurso REST para gestionar Turnos de Examen.
 * 
 * Base: /resources/v1/turnos
 */
@Path("turnos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TurnosExamanResource extends AbstractResource<TurnosExamen> {

    @Inject
    private TurnosExamenDAO turnosExamenDAO;

    @Override
    protected TurnosExamenDAO getDAO() {
        return turnosExamenDAO;
    }

    /**
     * GET /turnos
     * Retorna lista paginada de turnos.
     */
    @GET
    public Response listTurnos(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        return findRange(first, max);
    }

    /**
     * POST /turnos
     * Crea un nuevo turno de examen.
     * Validaciones: hora_inicio < hora_fin, no hay traslapes en la misma prueba.
     */
    @POST
    public Response createTurno(TurnosExamen turno, @Context UriInfo uriInfo) {
        if (turno == null || turno.getPruebaAdmision() == null || turno.getNombreTurno() == null || turno.getNombreTurno().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La prueba y el nombre del turno son obligatorios")
                    .header(RestHeaders.MISSING_PARAMETER, "pruebaAdmision, nombreTurno")
                    .build();
        }

        // Validar que hora_inicio < hora_fin
        if (turno.getHoraInicio() != null && turno.getHoraFin() != null && 
            !turno.getHoraInicio().isBefore(turno.getHoraFin())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La hora de inicio debe ser menor que la hora de fin")
                    .header(RestHeaders.CONFLICT_REASON, "hora_inicio >= hora_fin")
                    .build();
        }

        try {
            turnosExamenDAO.crear(turno);
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(turno.getIdTurnoExamen().toString())
                    .build();
            return Response.created(location)
                    .entity(turno)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .header(RestHeaders.CONFLICT_REASON, e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /turnos/{idTurno}
     * Obtiene un turno específico.
     */
    @GET
    @Path("{idTurno}")
    public Response getTurno(@PathParam("idTurno") String idTurnoStr) {
        try {
            UUID idTurno = UUID.fromString(idTurnoStr);
            TurnosExamen turno = turnosExamenDAO.leer(idTurno);
            if (turno == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idTurnoStr)
                        .build();
            }
            return Response.ok(turno).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("UUID inválido")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * PUT /turnos/{idTurno}
     * Actualiza un turno.
     */
    @PUT
    @Path("{idTurno}")
    public Response updateTurno(@PathParam("idTurno") String idTurnoStr, TurnosExamen turno) {
        try {
            UUID idTurno = UUID.fromString(idTurnoStr);
            TurnosExamen existente = turnosExamenDAO.leer(idTurno);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idTurnoStr)
                        .build();
            }

            turno.setIdTurnoExamen(idTurno);
            TurnosExamen actualizado = turnosExamenDAO.actualizar(turno);
            return Response.ok(actualizado).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .header(RestHeaders.CONFLICT_REASON, e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /turnos/{idTurno}
     * Elimina un turno.
     */
    @DELETE
    @Path("{idTurno}")
    public Response deleteTurno(@PathParam("idTurno") String idTurnoStr) {
        try {
            UUID idTurno = UUID.fromString(idTurnoStr);
            TurnosExamen existente = turnosExamenDAO.leer(idTurno);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idTurnoStr)
                        .build();
            }

            turnosExamenDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }
}
