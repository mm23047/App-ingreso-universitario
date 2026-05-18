package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para la gestión de Inscripciones a Prueba.
 * Hereda el endpoint GET paginado de AbstractResource.
 * Expone operaciones CRUD completas bajo /resources/v1/inscripciones_prueba
 */
@Path("inscripciones_prueba")
public class InscripcionesPruebaResource extends AbstractResource<InscripcionesPrueba> {

    @Inject
    InscripcionesPruebaDAO inscripcionesPruebaDAO;

    @QueryParam("aspiranteId")
    String aspiranteIdParam;

    @QueryParam("pruebaId")
    String pruebaIdParam;

    @Override
    protected IngresoDefaultDataAccess<InscripcionesPrueba> getDAO() {
        return inscripcionesPruebaDAO;
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response findRange(
            @DefaultValue("0")  @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max")   int max) {

        try {

            if (aspiranteIdParam != null && !aspiranteIdParam.isBlank()) {
                try {
                    UUID aspiranteId = UUID.fromString(aspiranteIdParam);
                    List<InscripcionesPrueba> list = inscripcionesPruebaDAO.findByAspiranteId(aspiranteId);
                    return Response.ok(list).build();
                } catch (IllegalArgumentException e) {
                    return Response.status(422)
                            .header(MISSING_PARAMETER, "aspiranteId must be a valid UUID")
                            .build();
                }
            }

            if (pruebaIdParam != null && !pruebaIdParam.isBlank()) {
                try {
                    UUID pruebaId = UUID.fromString(pruebaIdParam);
                    List<InscripcionesPrueba> list = inscripcionesPruebaDAO.findByPruebaId(pruebaId);
                    return Response.ok(list).build();
                } catch (IllegalArgumentException e) {
                    return Response.status(422)
                            .header(MISSING_PARAMETER, "pruebaId must be a valid UUID")
                            .build();
                }
            }

            // Sin filtros: comportamiento paginado normal heredado
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
                InscripcionesPrueba resp = inscripcionesPruebaDAO.leer(id);
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
    public Response create(InscripcionesPrueba entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null
                && entity.getIdAspirante() != null
                && entity.getIdPrueba() != null) {

            try {
                //Verificamos si ya existe la inscripcion en la prueba
                boolean existe = inscripcionesPruebaDAO.existsByAspiranteAndPrueba(
                        entity.getIdAspirante().getId(),
                        entity.getIdPrueba().getId()
                );
                if (existe) {
                return Response.status(Response.Status.CONFLICT)
                    .header(CONFLICT_REASON, "duplicate inscription for aspirante and prueba")
                    .entity("El aspirante ya está inscrito en esta prueba")
                            .build();
                }
                // Camino por el cual no existe el registro
                inscripcionesPruebaDAO.crear(entity);
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
                .header(MISSING_PARAMETER, "entity must not be null; id must be null; idAspirante and idPrueba must not be null")
                .build();
    }

    @PUT
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response update(@PathParam("id") UUID id, InscripcionesPrueba entity) {
        if (id != null && entity != null) {
            try {
                InscripcionesPrueba existing = inscripcionesPruebaDAO.leer(id);
                if (existing != null) {
                    if (entity.getIdAspirante() != null) {
                        existing.setIdAspirante(entity.getIdAspirante());
                    }
                    if (entity.getIdPrueba() != null) {
                        existing.setIdPrueba(entity.getIdPrueba());
                    }
                    if (existing.getIdAspirante() != null && existing.getIdPrueba() != null
                            && inscripcionesPruebaDAO.existsByAspiranteAndPruebaExcludingId(
                            existing.getIdAspirante().getId(),
                            existing.getIdPrueba().getId(),
                            id)) {
                        return Response.status(Response.Status.CONFLICT)
                                .header(CONFLICT_REASON, "duplicate inscription for aspirante and prueba")
                                .build();
                    }
                    if (entity.getEstado() != null) {
                        existing.setEstado(entity.getEstado());
                    }
                    inscripcionesPruebaDAO.actualizar(existing);
                    return Response.ok(existing).build();
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
    @Produces({MediaType.APPLICATION_JSON})
    public Response delete(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                InscripcionesPrueba existing = inscripcionesPruebaDAO.leer(id);
                if (existing != null) {
                    inscripcionesPruebaDAO.eliminar(existing);
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
