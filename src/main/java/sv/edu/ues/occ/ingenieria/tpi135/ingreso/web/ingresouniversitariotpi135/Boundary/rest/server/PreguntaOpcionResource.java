package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;

import java.util.UUID;

/**
 * Recurso REST para gestionar Opciones de Preguntas.
 * 
 * Base: /resources/v1/opciones
 * Nota: La mayoría de operaciones se hacen vía /preguntas/{id}/opciones,
 *       pero esta resource maneja GET, PUT, DELETE de opciones individuales.
 */
@Path("opciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PreguntaOpcionResource extends AbstractResource<PreguntaOpcion> {

    @Inject
    private PreguntaOpcionDAO preguntaOpcionDAO;

    @Override
    protected PreguntaOpcionDAO getDAO() {
        return preguntaOpcionDAO;
    }

    /**
     * GET /opciones/{idOpcion}
     * Obtiene una opción específica.
     */
    @GET
    @Path("{idOpcion}")
    public Response getOpcion(@PathParam("idOpcion") String idOpcionStr) {
        try {
            UUID idOpcion = UUID.fromString(idOpcionStr);
            PreguntaOpcion opcion = preguntaOpcionDAO.leer(idOpcion);
            if (opcion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idOpcionStr)
                        .build();
            }
            return Response.ok(opcion).build();
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
     * PUT /opciones/{idOpcion}
     * Actualiza una opción (principalmente el campo esCorrecta).
     */
    @PUT
    @Path("{idOpcion}")
    public Response updateOpcion(@PathParam("idOpcion") String idOpcionStr, PreguntaOpcion opcion) {
        try {
            UUID idOpcion = UUID.fromString(idOpcionStr);
            PreguntaOpcion existente = preguntaOpcionDAO.leer(idOpcion);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idOpcionStr)
                        .build();
            }

            opcion.setIdPreguntaOpcion(idOpcion);
            PreguntaOpcion actualizada = preguntaOpcionDAO.actualizar(opcion);
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
     * DELETE /opciones/{idOpcion}
     * Elimina una opción.
     */
    @DELETE
    @Path("{idOpcion}")
    public Response deleteOpcion(@PathParam("idOpcion") String idOpcionStr) {
        try {
            UUID idOpcion = UUID.fromString(idOpcionStr);
            PreguntaOpcion existente = preguntaOpcionDAO.leer(idOpcion);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idOpcionStr)
                        .build();
            }

            preguntaOpcionDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }
}
