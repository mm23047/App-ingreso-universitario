package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntasPorClaveDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClaveId;

import java.util.UUID;

/**
 * Recurso REST para la gestión de Preguntas asignadas por Clave de Examen.
 * Hereda el endpoint GET paginado de AbstractResource.
 * Expone operaciones CRUD completas bajo /resources/v1/preguntas_por_clave
 * <p>
 * PK compuesta: {idClave}/{idPregunta}
 * </p>
 */
@Path("preguntas_por_clave")
public class PreguntasPorClaveResource extends AbstractResource<PreguntasPorClave> {

    @Inject
    PreguntasPorClaveDAO preguntasPorClaveDAO;

    @Override
    protected IngresoDefaultDataAccess<PreguntasPorClave> getDAO() {
        return preguntasPorClaveDAO;
    }

    @GET
    @Path("{idClave}/{idPregunta}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("idClave") UUID idClave,
                             @PathParam("idPregunta") UUID idPregunta) {
        if (idClave != null && idPregunta != null) {
            try {
                PreguntasPorClaveId pk = new PreguntasPorClaveId();
                pk.setIdClave(idClave);
                pk.setIdPregunta(idPregunta);
                PreguntasPorClave resp = preguntasPorClaveDAO.leer(pk);
                if (resp != null) {
                    return Response.ok(resp).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with idClave=" + idClave + " idPregunta=" + idPregunta + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "idClave and idPregunta")
                .build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response create(PreguntasPorClave entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getIdPreguntaPorClave() != null
                && entity.getIdPreguntaPorClave().getIdClave() != null
                && entity.getIdPreguntaPorClave().getIdPregunta() != null
                && entity.getIdClave() != null
                && entity.getIdPregunta() != null) {
            try {
                preguntasPorClaveDAO.crear(entity);
                return Response.created(
                        uriInfo.getAbsolutePathBuilder()
                                .path(String.valueOf(entity.getIdPreguntaPorClave().getIdClave()))
                                .path(String.valueOf(entity.getIdPreguntaPorClave().getIdPregunta()))
                                .build())
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "entity must not be null; id.idClave, id.idPregunta, idClave and idPregunta must not be null")
                .build();
    }

    @PUT
    @Path("{idClave}/{idPregunta}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response update(@PathParam("idClave") UUID idClave,
                           @PathParam("idPregunta") UUID idPregunta,
                           PreguntasPorClave entity) {
        if (idClave != null && idPregunta != null && entity != null) {
            try {
                PreguntasPorClaveId pk = new PreguntasPorClaveId();
                pk.setIdClave(idClave);
                pk.setIdPregunta(idPregunta);
                PreguntasPorClave existing = preguntasPorClaveDAO.leer(pk);
                if (existing != null) {
                    entity.setIdPreguntaPorClave(pk);
                    preguntasPorClaveDAO.actualizar(entity);
                    return Response.ok(entity).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with idClave=" + idClave + " idPregunta=" + idPregunta + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "idClave, idPregunta and entity must not be null")
                .build();
    }

    @DELETE
    @Path("{idClave}/{idPregunta}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response delete(@PathParam("idClave") UUID idClave,
                           @PathParam("idPregunta") UUID idPregunta) {
        if (idClave != null && idPregunta != null) {
            try {
                PreguntasPorClaveId pk = new PreguntasPorClaveId();
                pk.setIdClave(idClave);
                pk.setIdPregunta(idPregunta);
                PreguntasPorClave existing = preguntasPorClaveDAO.leer(pk);
                if (existing != null) {
                    preguntasPorClaveDAO.eliminar(existing);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with idClave=" + idClave + " idPregunta=" + idPregunta + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "idClave and idPregunta")
                .build();
    }
}
