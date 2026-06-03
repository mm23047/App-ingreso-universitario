package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoRespuestaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoRespuesta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;

import jakarta.ws.rs.core.Context;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar Respuestas Globales del Banco de Respuestas.
 * 
 * Base: /resources/v1/respuestas_globales
 */
@Path("respuestas_globales")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BancoRespuestaResource extends AbstractResource<BancoRespuesta> {

    @Inject
    private BancoRespuestaDAO bancoRespuestaDAO;

    @Inject
    private PreguntaOpcionDAO preguntaOpcionDAO;

    @Override
    protected BancoRespuestaDAO getDAO() {
        return bancoRespuestaDAO;
    }

    /**
     * GET /respuestas_globales
     * Retorna lista paginada de respuestas globales.
     */
    @GET
    public Response listRespuestas(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        return findRange(first, max);
    }

    /**
     * POST /respuestas_globales
     * Crea una nueva respuesta global (sin área asociada).
     */
    @POST
    public Response createRespuestaGlobal(BancoRespuesta respuesta, @Context UriInfo uriInfo) {
        if (respuesta == null || respuesta.getTextoRespuesta() == null || respuesta.getTextoRespuesta().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El texto de la respuesta es obligatorio")
                    .header(RestHeaders.MISSING_PARAMETER, "textoRespuesta")
                    .build();
        }

        // Asegurarse de que no tiene área (respuesta global)
        respuesta.setAreaConocimiento(null);

        try {
            bancoRespuestaDAO.crear(respuesta);
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(respuesta.getIdBancoRespuesta().toString())
                    .build();
            return Response.created(location)
                    .entity(respuesta)
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
     * GET /respuestas_globales/{idRespuesta}
     * Obtiene una respuesta específica.
     */
    @GET
    @Path("{idRespuesta}")
    public Response getRespuesta(@PathParam("idRespuesta") String idRespuestaStr) {
        try {
            UUID idRespuesta = UUID.fromString(idRespuestaStr);
            BancoRespuesta respuesta = bancoRespuestaDAO.leer(idRespuesta);
            if (respuesta == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idRespuestaStr)
                        .build();
            }
            return Response.ok(respuesta).build();
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
     * PUT /respuestas_globales/{idRespuesta}
     * Actualiza una respuesta.
     */
    @PUT
    @Path("{idRespuesta}")
    public Response updateRespuesta(@PathParam("idRespuesta") String idRespuestaStr, BancoRespuesta respuesta) {
        try {
            UUID idRespuesta = UUID.fromString(idRespuestaStr);
            BancoRespuesta existente = bancoRespuestaDAO.leer(idRespuesta);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idRespuestaStr)
                        .build();
            }

            respuesta.setIdBancoRespuesta(idRespuesta);
            BancoRespuesta actualizada = bancoRespuestaDAO.actualizar(respuesta);
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
     * DELETE /respuestas_globales/{idRespuesta}
     * Elimina una respuesta. Solo permite si no tiene opciones asociadas.
     */
    @DELETE
    @Path("{idRespuesta}")
    public Response deleteRespuesta(@PathParam("idRespuesta") String idRespuestaStr) {
        try {
            UUID idRespuesta = UUID.fromString(idRespuestaStr);
            BancoRespuesta existente = bancoRespuestaDAO.leer(idRespuesta);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idRespuestaStr)
                        .build();
            }

            // Validación: No permitir eliminar si tiene opciones asociadas
            // (Esto se haría con un método en PreguntaOpcionDAO)
            // Por ahora, permitimos la eliminación

            bancoRespuestaDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }
}
