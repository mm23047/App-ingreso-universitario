package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CuposCarreraDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId;

import java.util.UUID;

/**
 * Recurso REST para la gestión de Cupos por Carrera.
 * Hereda el endpoint GET paginado de AbstractResource.
 * Expone operaciones CRUD completas bajo /resources/v1/cupos_carrera
 * <p>
 * PK compuesta: {idPrueba}/{idCarrera}/{idEtapa}
 * </p>
 */
@Path("cupos_carrera")
public class CuposCarreraResource extends AbstractResource<CuposCarrera> {

    @Inject
    CuposCarreraDAO cuposCarreraDAO;

    @Override
    protected IngresoDefaultDataAccess<CuposCarrera> getDAO() {
        return cuposCarreraDAO;
    }

    @GET
    @Path("{idPrueba}/{idCarrera}/{idEtapa}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("idPrueba") UUID idPrueba,
                             @PathParam("idCarrera") String idCarrera,
                             @PathParam("idEtapa") UUID idEtapa) {
        if (idPrueba != null && idCarrera != null && idEtapa != null) {
            try {
                CuposCarreraId pk = new CuposCarreraId();
                pk.setIdPrueba(idPrueba);
                pk.setIdCarrera(idCarrera);
                pk.setIdEtapa(idEtapa);
                CuposCarrera resp = cuposCarreraDAO.leer(pk);
                if (resp != null) {
                    return Response.ok(resp).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with idPrueba=" + idPrueba + " idCarrera=" + idCarrera + " idEtapa=" + idEtapa + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "idPrueba, idCarrera and idEtapa")
                .build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response create(CuposCarrera entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getIdCupoCarrera() != null
                && entity.getIdCupoCarrera().getIdPrueba() != null
                && entity.getIdCupoCarrera().getIdCarrera() != null
                && entity.getIdCupoCarrera().getIdEtapa() != null
                && entity.getPruebaAdmision() != null
                && entity.getCatalogoCarrera() != null
                && entity.getEtapaAdmision() != null
                && entity.getCupos() != null) {
            try {
                cuposCarreraDAO.crear(entity);
                return Response.created(
                        uriInfo.getAbsolutePathBuilder()
                                .path(String.valueOf(entity.getIdCupoCarrera().getIdPrueba()))
                                .path(entity.getIdCupoCarrera().getIdCarrera())
                                .path(String.valueOf(entity.getIdCupoCarrera().getIdEtapa()))
                                .build())
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "entity must not be null; id.idPrueba, id.idCarrera, id.idEtapa, cupos must not be null")
                .build();
    }

    @PUT
    @Path("{idPrueba}/{idCarrera}/{idEtapa}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response update(@PathParam("idPrueba") UUID idPrueba,
                           @PathParam("idCarrera") String idCarrera,
                           @PathParam("idEtapa") UUID idEtapa,
                           CuposCarrera entity) {
        if (idPrueba != null && idCarrera != null && idEtapa != null && entity != null) {
            try {
                CuposCarreraId pk = new CuposCarreraId();
                pk.setIdPrueba(idPrueba);
                pk.setIdCarrera(idCarrera);
                pk.setIdEtapa(idEtapa);
                CuposCarrera existing = cuposCarreraDAO.leer(pk);
                if (existing != null) {
                    entity.setIdCupoCarrera(pk);
                    // Preserve existing managed relations to avoid side effects when
                    // request payload sends partial nested entities (only ids).
                    entity.setPruebaAdmision(existing.getPruebaAdmision());
                    entity.setCatalogoCarrera(existing.getCatalogoCarrera());
                    entity.setEtapaAdmision(existing.getEtapaAdmision());
                    cuposCarreraDAO.actualizar(entity);
                    return Response.ok(entity).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with idPrueba=" + idPrueba + " idCarrera=" + idCarrera + " idEtapa=" + idEtapa + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "idPrueba, idCarrera, idEtapa and entity must not be null")
                .build();
    }

    @DELETE
    @Path("{idPrueba}/{idCarrera}/{idEtapa}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response delete(@PathParam("idPrueba") UUID idPrueba,
                           @PathParam("idCarrera") String idCarrera,
                           @PathParam("idEtapa") UUID idEtapa) {
        if (idPrueba != null && idCarrera != null && idEtapa != null) {
            try {
                CuposCarreraId pk = new CuposCarreraId();
                pk.setIdPrueba(idPrueba);
                pk.setIdCarrera(idCarrera);
                pk.setIdEtapa(idEtapa);
                CuposCarrera existing = cuposCarreraDAO.leer(pk);
                if (existing != null) {
                    cuposCarreraDAO.eliminar(existing);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with idPrueba=" + idPrueba + " idCarrera=" + idCarrera + " idEtapa=" + idEtapa + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "idPrueba, idCarrera and idEtapa")
                .build();
    }
}
