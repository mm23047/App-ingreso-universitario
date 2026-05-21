package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.CarrerasElegidaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CarrerasElegida;

import java.util.UUID;

/**
 * Recurso REST para la gestión de Carreras Elegidas por el Aspirante.
 * Hereda el endpoint GET paginado de AbstractResource.
 * Expone operaciones CRUD completas bajo /resources/v1/carreras_elegidas
 * <p>
 * PK compuesta: {idInscripcion}/{idCarrera}
 * </p>
 */
@Path("carreras_elegidas")
public class CarrerasElegidaResource extends AbstractResource<CarrerasElegida> {

    @Inject
    CarrerasElegidaDAO carrerasElegidaDAO;

    @Override
    protected IngresoDefaultDataAccess<CarrerasElegida> getDAO() {
        return carrerasElegidaDAO;
    }

    @GET
    @Path("{idInscripcion}/{idCarrera}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("idInscripcion") UUID idInscripcion,
                             @PathParam("idCarrera") String idCarrera) {
        if (idInscripcion != null && idCarrera != null) {
            try {
                CarrerasElegida resp = carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, idCarrera);
                if (resp != null) {
                    return Response.ok(resp).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with idInscripcion=" + idInscripcion + " idCarrera=" + idCarrera + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "idInscripcion and idCarrera")
                .build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response create(CarrerasElegida entity, @Context UriInfo uriInfo) {
        if (entity != null
            && entity.getInscripcionesPrueba() != null
            && entity.getInscripcionesPrueba().getIdInscripcionPrueba() != null
                && entity.getCatalogoCarrera() != null
            && entity.getCatalogoCarrera().getIdCarrera() != null
                && entity.getPrioridad() != null) {
            try {
            if (carrerasElegidaDAO.existsByInscripcionAndPrioridad(entity.getInscripcionesPrueba().getIdInscripcionPrueba(), entity.getPrioridad())) {
                return Response.status(Response.Status.CONFLICT)
                            .header(CONFLICT_REASON, "prioridad already exists for inscripcion")
                    .build();
            }
                carrerasElegidaDAO.crear(entity);
                return Response.created(
                        uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(entity.getInscripcionesPrueba().getIdInscripcionPrueba()))
                    .path(entity.getCatalogoCarrera().getIdCarrera())
                                .build())
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
            .header(MISSING_PARAMETER, "entity must not be null; idInscripcion.id, idCarrera.idCarrera and prioridad must not be null")
                .build();
    }

    @PUT
    @Path("{idInscripcion}/{idCarrera}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response update(@PathParam("idInscripcion") UUID idInscripcion,
                           @PathParam("idCarrera") String idCarrera,
                           CarrerasElegida entity) {
        if (idInscripcion != null && idCarrera != null && entity != null) {
            try {
                CarrerasElegida existing = carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, idCarrera);
                if (existing != null) {
                    if (entity.getPrioridad() != null
                            && !entity.getPrioridad().equals(existing.getPrioridad())
                            && carrerasElegidaDAO.existsByInscripcionAndPrioridad(idInscripcion, entity.getPrioridad())) {
                        return Response.status(Response.Status.CONFLICT)
                                .header(CONFLICT_REASON, "prioridad already exists for inscripcion")
                                .build();
                    }
                    if (entity.getPrioridad() != null) {
                        existing.setPrioridad(entity.getPrioridad());
                    }
                    CarrerasElegida updated = carrerasElegidaDAO.actualizar(existing);
                    return Response.ok(updated).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with idInscripcion=" + idInscripcion + " idCarrera=" + idCarrera + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "idInscripcion, idCarrera and entity must not be null")
                .build();
    }

    @DELETE
    @Path("{idInscripcion}/{idCarrera}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response delete(@PathParam("idInscripcion") UUID idInscripcion,
                           @PathParam("idCarrera") String idCarrera) {
        if (idInscripcion != null && idCarrera != null) {
            try {
                CarrerasElegida existing = carrerasElegidaDAO.findByInscripcionAndCarrera(idInscripcion, idCarrera);
                if (existing != null) {
                    carrerasElegidaDAO.eliminar(existing);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with idInscripcion=" + idInscripcion + " idCarrera=" + idCarrera + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "idInscripcion and idCarrera")
                .build();
    }
}
