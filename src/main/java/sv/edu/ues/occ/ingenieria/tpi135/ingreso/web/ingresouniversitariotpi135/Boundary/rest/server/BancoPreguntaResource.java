package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar Preguntas del Banco de Preguntas.
 * 
 * Base: /resources/v1/preguntas
 */
@Path("preguntas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BancoPreguntaResource extends AbstractResource<BancoPregunta> {

    @Inject
    private BancoPreguntaDAO bancoPreguntaDAO;

    @Inject
    private PreguntaOpcionDAO preguntaOpcionDAO;

    @Override
    protected BancoPreguntaDAO getDAO() {
        return bancoPreguntaDAO;
    }

    /**
     * GET /preguntas
     * Retorna lista paginada de preguntas.
     */
    @GET
    public Response listPreguntas(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        return findRange(first, max);
    }

    /**
     * GET /preguntas/{idPregunta}
     * Obtiene una pregunta específica.
     */
    @GET
    @Path("{idPregunta}")
    public Response getPregunta(@PathParam("idPregunta") String idPreguntaStr) {
        try {
            UUID idPregunta = UUID.fromString(idPreguntaStr);
            BancoPregunta pregunta = bancoPreguntaDAO.leer(idPregunta);
            if (pregunta == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idPreguntaStr)
                        .build();
            }
            return Response.ok(pregunta).build();
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
     * PUT /preguntas/{idPregunta}
     * Actualiza una pregunta.
     */
    @PUT
    @Path("{idPregunta}")
    public Response updatePregunta(@PathParam("idPregunta") String idPreguntaStr, BancoPregunta pregunta) {
        try {
            UUID idPregunta = UUID.fromString(idPreguntaStr);
            BancoPregunta existente = bancoPreguntaDAO.leer(idPregunta);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idPreguntaStr)
                        .build();
            }

            pregunta.setIdBancoPregunta(idPregunta);
            BancoPregunta actualizada = bancoPreguntaDAO.actualizar(pregunta);
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
     * DELETE /preguntas/{idPregunta}
     * Elimina una pregunta. Solo permite si no tiene opciones asociadas.
     */
    @DELETE
    @Path("{idPregunta}")
    public Response deletePregunta(@PathParam("idPregunta") String idPreguntaStr) {
        try {
            UUID idPregunta = UUID.fromString(idPreguntaStr);
            BancoPregunta existente = bancoPreguntaDAO.leer(idPregunta);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idPreguntaStr)
                        .build();
            }

            // Validación: No permitir eliminar si tiene opciones
            List<PreguntaOpcion> opciones = preguntaOpcionDAO.findByPregunta(idPregunta);
            if (!opciones.isEmpty()) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("No se puede eliminar una pregunta con opciones asociadas")
                        .header(RestHeaders.CONFLICT_REASON, "Pregunta tiene " + opciones.size() + " opciones")
                        .build();
            }

            bancoPreguntaDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /preguntas/{idPregunta}/opciones
     * Retorna todas las opciones de una pregunta.
     */
    @GET
    @Path("{idPregunta}/opciones")
    public Response getOpcionesOfPregunta(
            @PathParam("idPregunta") String idPreguntaStr,
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        try {
            UUID idPregunta = UUID.fromString(idPreguntaStr);
            BancoPregunta pregunta = bancoPreguntaDAO.leer(idPregunta);
            if (pregunta == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idPreguntaStr)
                        .build();
            }

            List<PreguntaOpcion> opciones = preguntaOpcionDAO.findByPregunta(idPregunta);
            int fromIndex = Math.min(first, opciones.size());
            int toIndex = Math.min(first + max, opciones.size());
            List<PreguntaOpcion> paginadas = opciones.subList(fromIndex, toIndex);

            return Response.ok(paginadas)
                    .header(RestHeaders.TOTAL_RECORDS, opciones.size())
                    .build();
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
     * POST /preguntas/{idPregunta}/opciones
     * Crea una nueva opción para una pregunta.
     * El payload NO debe incluir idPregunta (se obtiene de la URI).
     */
    @POST
    @Path("{idPregunta}/opciones")
    public Response createOpcionInPregunta(@PathParam("idPregunta") String idPreguntaStr, PreguntaOpcion opcion, @Context UriInfo uriInfo) {
        if (opcion == null || opcion.getIdRespuestaGlobal() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("La respuesta global es obligatoria")
                    .header(RestHeaders.MISSING_PARAMETER, "idRespuestaGlobal")
                    .build();
        }

        try {
            UUID idPregunta = UUID.fromString(idPreguntaStr);
            BancoPregunta pregunta = bancoPreguntaDAO.leer(idPregunta);
            if (pregunta == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idPreguntaStr)
                        .build();
            }

            opcion.setBancoPregunta(pregunta);
            preguntaOpcionDAO.crear(opcion);

                URI location = uriInfo.getBaseUriBuilder()
                    .path("opciones/{idOpcion}")
                    .build(opcion.getIdPreguntaOpcion());
            return Response.created(location)
                    .entity(opcion)
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
}
