package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.EtapasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.util.UUID;

/**
 * Recurso REST para la gestión de Etapas de Admisión.
 * Hereda el endpoint GET paginado de AbstractResource.
 * Expone operaciones CRUD completas bajo /resources/v1/etapas_admision
 */
@Path("etapas_admision")
public class EtapasAdmisionResource extends AbstractResource<EtapasAdmision> {

    @Inject
    EtapasAdmisionDAO etapasAdmisionDAO;

    @Override
    protected IngresoDefaultDataAccess<EtapasAdmision> getDAO() {
        return etapasAdmisionDAO;
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                EtapasAdmision resp = etapasAdmisionDAO.leer(id);
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
    public Response create(EtapasAdmision entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null) {
            try {
                etapasAdmisionDAO.crear(entity);
                return Response.created(
                        uriInfo.getAbsolutePathBuilder()
                                .path(String.valueOf(entity.getId()))
                                .build())
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
    public Response update(@PathParam("id") UUID id, EtapasAdmision entity) {
        if (id != null && entity != null) {
            try {
                EtapasAdmision existing = etapasAdmisionDAO.leer(id);
                if (existing != null) {
                    entity.setId(id);
                    etapasAdmisionDAO.actualizar(entity);
                    return Response.ok(entity).build();
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
                .header(MISSING_PARAMETER, "id and entity must not be null")
                .build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                EtapasAdmision resp = etapasAdmisionDAO.leer(id);
                if (resp != null) {
                    etapasAdmisionDAO.eliminar(resp);
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
