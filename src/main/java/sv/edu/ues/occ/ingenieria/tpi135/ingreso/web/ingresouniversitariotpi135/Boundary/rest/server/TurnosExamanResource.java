package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TurnosExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExaman;

import java.util.UUID;

/**
 * Recurso REST para la gestión de Turnos de Examen.
 * Hereda el endpoint GET paginado de AbstractResource.
 * Expone operaciones CRUD completas bajo /resources/v1/turnos_examen
 */
@Path("turnos_examen")
public class TurnosExamanResource extends AbstractResource<TurnosExaman> {

    @Inject
    TurnosExamanDAO turnosExamanDAO;

    @Override
    protected IngresoDefaultDataAccess<TurnosExaman> getDAO() {
        return turnosExamanDAO;
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                TurnosExaman resp = turnosExamanDAO.leer(id);
                if (resp != null) {
                    return Response.ok(resp).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "id")
                .build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response create(TurnosExaman entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null) {
            try {
                entity.validarHorario();
                turnosExamanDAO.crear(entity);
                return Response.created(
                        uriInfo.getAbsolutePathBuilder()
                                .path(String.valueOf(entity.getId()))
                                .build())
                        .build();
            } catch (IllegalArgumentException ex) {
                return Response.status(422)
                        .header(MISSING_PARAMETER, "horaInicio must be before horaFin")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "entity must not be null and entity.id must be null")
                .build();
    }

    @PUT
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response update(@PathParam("id") UUID id, TurnosExaman entity) {
        if (id != null && entity != null) {
            try {
                entity.validarHorario();
                TurnosExaman existing = turnosExamanDAO.leer(id);
                if (existing != null) {
                    entity.setId(id);
                    turnosExamanDAO.actualizar(entity);
                    return Response.ok(entity).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with id " + id + " not found")
                        .build();
            } catch (IllegalArgumentException ex) {
                return Response.status(422)
                        .header(MISSING_PARAMETER, "horaInicio must be before horaFin")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "id and entity must not be null")
                .build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                TurnosExaman resp = turnosExamanDAO.leer(id);
                if (resp != null) {
                    turnosExamanDAO.eliminar(resp);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "id")
                .build();
    }
}
