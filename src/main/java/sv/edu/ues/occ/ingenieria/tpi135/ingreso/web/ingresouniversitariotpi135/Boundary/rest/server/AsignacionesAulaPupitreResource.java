package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AsignacionesAulaPupitreDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;

import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para la gestión de Asignaciones de Aula y Pupitre.
 * Hereda el endpoint GET paginado de AbstractResource.
 * Expone operaciones CRUD completas bajo /resources/v1/asignaciones_aula_pupitre
 */
@Path("asignaciones_aula_pupitre")
public class AsignacionesAulaPupitreResource extends AbstractResource<AsignacionesAulaPupitre> {

    @Inject
    AsignacionesAulaPupitreDAO asignacionesAulaPupitreDAO;

    @QueryParam("inscripcionId")
    String inscripcionIdParam;

    @QueryParam("aspiranteId")
    String aspiranteIdParam;

    @Override
    protected IngresoDefaultDataAccess<AsignacionesAulaPupitre> getDAO() {
        return asignacionesAulaPupitreDAO;
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response findRange(
            @DefaultValue("0")  @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max")   int max) {

        try {

            if (inscripcionIdParam != null && !inscripcionIdParam.isBlank()) {
                try {
                    UUID inscripcionId = UUID.fromString(inscripcionIdParam);
                    List<AsignacionesAulaPupitre> list = asignacionesAulaPupitreDAO.findByInscripcionId(inscripcionId);
                    return Response.ok(list).build();
                } catch (IllegalArgumentException e) {
                    return Response.status(422)
                            .header(MISSING_PARAMETER, "inscripcionId must be a valid UUID")
                            .build();
                }
            }

            if (aspiranteIdParam != null && !aspiranteIdParam.isBlank()) {
                try {
                    UUID aspiranteId = UUID.fromString(aspiranteIdParam);
                    List<AsignacionesAulaPupitre> list = asignacionesAulaPupitreDAO.findByAspiranteId(aspiranteId);
                    return Response.ok(list).build();
                } catch (IllegalArgumentException e) {
                    return Response.status(422)
                            .header(MISSING_PARAMETER, "aspiranteId must be a valid UUID")
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
                AsignacionesAulaPupitre resp = asignacionesAulaPupitreDAO.leer(id);
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
    public Response create(AsignacionesAulaPupitre entity, @Context UriInfo uriInfo) {
        if (entity != null && entity.getId() == null
                && entity.getIdInscripcion() != null
                && entity.getIdAula() != null
                && entity.getPupitre() != null) {
            try {
                AsignacionesAulaPupitre creada = asignacionesAulaPupitreDAO.crearConCupo(entity);
                return Response.created(
                        uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(creada.getId()))
                                .build())
                        .build();
            } catch (IllegalStateException ex) {
                String mensaje = ex.getMessage() != null ? ex.getMessage() : "";
                if ("AULA_SIN_CUPO".equals(mensaje)) {
                    return Response.status(Response.Status.CONFLICT)
                                .header(CONFLICT_REASON, "El aula ya alcanzo su capacidad maxima")
                        .build();
                }
                if ("AULA_NO_ENCONTRADA".equals(mensaje)) {
                    return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Aula no encontrada")
                        .build();
                }
                if ("AULA_CAPACIDAD_INVALIDA".equals(mensaje)) {
                    return Response.status(422)
                        .header(MISSING_PARAMETER, "capacidad")
                        .build();
                }
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(SERVER_EXCEPTION, "Cannot access db: " + mensaje)
                    .build();
            } catch (RuntimeException ex) {
                String mensaje = ex.getMessage() != null ? ex.getMessage() : "";
                if (mensaje.contains("AULA_SIN_CUPO")) {
                    return Response.status(Response.Status.CONFLICT)
                                .header(CONFLICT_REASON, "El aula ya alcanzo su capacidad maxima")
                        .build();
                }
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db: " + mensaje)
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db: " + ex.getMessage())
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "entity must not be null; id must be null; idInscripcion, idAula and pupitre must not be null")
                .build();
    }

    @PUT
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response update(@PathParam("id") UUID id, AsignacionesAulaPupitre entity) {
        if (id != null && entity != null) {
            try {
                AsignacionesAulaPupitre existing = asignacionesAulaPupitreDAO.leer(id);
                if (existing != null) {
                    entity.setId(id);
                    asignacionesAulaPupitreDAO.actualizar(entity);
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
    @Produces({MediaType.APPLICATION_JSON})
    public Response delete(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                AsignacionesAulaPupitre existing = asignacionesAulaPupitreDAO.leer(id);
                if (existing != null) {
                    asignacionesAulaPupitreDAO.eliminar(existing);
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
