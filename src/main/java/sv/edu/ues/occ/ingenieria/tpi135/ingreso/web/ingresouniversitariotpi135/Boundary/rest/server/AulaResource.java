package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AulaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Aula;

import java.net.URI;
import java.util.UUID;

/**
 * Recurso REST para gestionar Aulas.
 * 
 * Base: /resources/v1/aulas
 * Nota: La disponibilidad de aulas por turno es responsabilidad del Desarrollador B.
 */
@Path("aulas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AulaResource extends AbstractResource<Aula> {

    @Inject
    private AulaDAO aulaDAO;

    @Override
    protected AulaDAO getDAO() {
        return aulaDAO;
    }

    /**
     * GET /aulas
     * Retorna lista paginada de aulas.
     */
    @GET
    public Response listAulas(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        return findRange(first, max);
    }

    /**
     * POST /aulas
     * Crea una nueva aula.
     */
    @POST
    public Response createAula(Aula aula, @Context UriInfo uriInfo) {
        if (aula == null || aula.getCodigoAulaApi() == null || aula.getCodigoAulaApi().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El código del aula es obligatorio")
                    .header(RestHeaders.MISSING_PARAMETER, "codigoAulaApi")
                    .build();
        }

        try {
            aulaDAO.crear(aula);
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(aula.getIdAula().toString())
                    .build();
            return Response.created(location)
                    .entity(aula)
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
     * GET /aulas/{idAula}
     * Obtiene un aula específica.
     */
    @GET
    @Path("{idAula}")
    public Response getAula(@PathParam("idAula") String idAulaStr) {
        try {
            UUID idAula = UUID.fromString(idAulaStr);
            Aula aula = aulaDAO.leer(idAula);
            if (aula == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idAulaStr)
                        .build();
            }
            return Response.ok(aula).build();
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
     * PUT /aulas/{idAula}
     * Actualiza un aula.
     */
    @PUT
    @Path("{idAula}")
    public Response updateAula(@PathParam("idAula") String idAulaStr, Aula aula) {
        try {
            UUID idAula = UUID.fromString(idAulaStr);
            Aula existente = aulaDAO.leer(idAula);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idAulaStr)
                        .build();
            }

            aula.setIdAula(idAula);
            Aula actualizada = aulaDAO.actualizar(aula);
            return Response.ok(actualizada).build();
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
     * DELETE /aulas/{idAula}
     * Elimina un aula.
     */
    @DELETE
    @Path("{idAula}")
    public Response deleteAula(@PathParam("idAula") String idAulaStr) {
        try {
            UUID idAula = UUID.fromString(idAulaStr);
            Aula existente = aulaDAO.leer(idAula);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idAulaStr)
                        .build();
            }

            aulaDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }
}
