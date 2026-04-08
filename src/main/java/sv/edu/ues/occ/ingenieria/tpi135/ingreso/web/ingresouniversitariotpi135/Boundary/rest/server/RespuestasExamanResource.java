package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.RespuestasExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestasExaman;

import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para la gestión de respuestas de examen.
 * Hereda el endpoint GET paginado de AbstractResource.
 * Expone operaciones de creación y consulta bajo /resources/v1/respuestas_examen.
 */
@Path("respuestas_examen")
public class RespuestasExamanResource extends AbstractResource<RespuestasExaman> {

    @Inject
    RespuestasExamanDAO respuestasExamanDAO;

    @QueryParam("examenId")
    String examenIdParam;

    @Override
    protected IngresoDefaultDataAccess<RespuestasExaman> getDAO() {
        return respuestasExamanDAO;
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response findRange(
            @DefaultValue("0")  @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max")   int max) {
        try {
            if (examenIdParam != null && !examenIdParam.isBlank()) {
                UUID examenId;
                try {
                    examenId = UUID.fromString(examenIdParam);
                } catch (IllegalArgumentException ex) {
                    return Response.status(422)
                            .header(MISSING_PARAMETER, "examenId must be a valid UUID")
                            .build();
                }

                List<RespuestasExaman> list = respuestasExamanDAO.findByExamenId(examenId);
                return Response.ok(list).build();
            }

            // Sin examenId: comportamiento paginado normal heredado
            return super.findRange(first, max);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(SERVER_EXCEPTION, "Cannot access db")
                    .build();
        }
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                RespuestasExaman resp = respuestasExamanDAO.leer(id);
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
    public Response create(RespuestasExaman entity, @Context UriInfo uriInfo) {
        if (entity != null
                && entity.getId() == null
                && entity.getIdExamen() != null
                && entity.getIdPregunta() != null) {
            try {
                respuestasExamanDAO.crear(entity);
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
                .header(MISSING_PARAMETER, "entity must not be null; id must be null; idExamen and idPregunta must not be null")
                .build();
    }
}
