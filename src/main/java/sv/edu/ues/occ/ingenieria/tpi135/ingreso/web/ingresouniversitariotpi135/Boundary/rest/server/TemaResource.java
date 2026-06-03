package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TemaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar Temas.
 * 
 * Base: /resources/v1/temas
 * Soporta jerarquía de temas (padre-hijo).
 */
@Path("temas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TemaResource extends AbstractResource<Tema> {

    @Inject
    private TemaDAO temaDAO;

    @Inject
    private BancoPreguntaDAO bancoPreguntaDAO;

    @Override
    protected TemaDAO getDAO() {
        return temaDAO;
    }

    /**
     * GET /temas
     * Retorna lista paginada de temas.
     */
    @GET
    public Response listTemas(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        return findRange(first, max);
    }

    /**
     * GET /temas/{idTema}
     * Obtiene un tema específico por ID.
     */
    @GET
    @Path("{idTema}")
    public Response getTema(@PathParam("idTema") String idTemaStr) {
        try {
            UUID idTema = UUID.fromString(idTemaStr);
            Tema tema = temaDAO.leer(idTema);
            if (tema == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idTemaStr)
                        .build();
            }
            return Response.ok(tema).build();
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
     * PUT /temas/{idTema}
     * Actualiza un tema existente.
     */
    @PUT
    @Path("{idTema}")
    public Response updateTema(@PathParam("idTema") String idTemaStr, Tema tema) {
        try {
            UUID idTema = UUID.fromString(idTemaStr);
            Tema existente = temaDAO.leer(idTema);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idTemaStr)
                        .build();
            }

            tema.setIdTema(idTema);
            Tema actualizado = temaDAO.actualizar(tema);
            return Response.ok(actualizado).build();
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
     * DELETE /temas/{idTema}
     * Elimina un tema. Solo permite si no tiene preguntas ni hijos asociados.
     */
    @DELETE
    @Path("{idTema}")
    public Response deleteTema(@PathParam("idTema") String idTemaStr) {
        try {
            UUID idTema = UUID.fromString(idTemaStr);
            Tema existente = temaDAO.leer(idTema);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idTemaStr)
                        .build();
            }

            // Validación: No permitir eliminar si tiene preguntas
            List<BancoPregunta> preguntas = bancoPreguntaDAO.findByTema(idTema);
            if (!preguntas.isEmpty()) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("No se puede eliminar un tema con preguntas asociadas")
                        .header(RestHeaders.CONFLICT_REASON, "Tema tiene " + preguntas.size() + " preguntas")
                        .build();
            }

            // Validación: No permitir eliminar si tiene temas hijos
            List<Tema> hijos = temaDAO.findByTemaPadre(idTema);
            if (!hijos.isEmpty()) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("No se puede eliminar un tema que tiene subtemas")
                        .header(RestHeaders.CONFLICT_REASON, "Tema tiene " + hijos.size() + " subtemas")
                        .build();
            }

            temaDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /temas/{idTema}/preguntas
     * Retorna todas las preguntas de un tema.
     */
    @GET
    @Path("{idTema}/preguntas")
    public Response getPreguntasByTema(
            @PathParam("idTema") String idTemaStr,
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        try {
            UUID idTema = UUID.fromString(idTemaStr);
            Tema tema = temaDAO.leer(idTema);
            if (tema == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idTemaStr)
                        .build();
            }

            List<BancoPregunta> preguntas = bancoPreguntaDAO.findByTema(idTema);
            // Paginación manual
            int fromIndex = Math.min(first, preguntas.size());
            int toIndex = Math.min(first + max, preguntas.size());
            List<BancoPregunta> paginadas = preguntas.subList(fromIndex, toIndex);

            return Response.ok(paginadas)
                    .header(RestHeaders.TOTAL_RECORDS, preguntas.size())
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
     * POST /temas/{idTema}/preguntas
     * Crea una nueva pregunta dentro de un tema.
     * El payload NO debe incluir idTema (se obtiene de la URI).
     */
    @POST
    @Path("{idTema}/preguntas")
    public Response createPreguntaInTema(@PathParam("idTema") String idTemaStr, BancoPregunta pregunta, @Context UriInfo uriInfo) {
        if (pregunta == null || pregunta.getEnunciado() == null || pregunta.getEnunciado().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El enunciado de la pregunta es obligatorio")
                    .header(RestHeaders.MISSING_PARAMETER, "enunciado")
                    .build();
        }

        try {
            UUID idTema = UUID.fromString(idTemaStr);
            Tema tema = temaDAO.leer(idTema);
            if (tema == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idTemaStr)
                        .build();
            }

            // Asignar tema a la pregunta (inyectada desde la URI)
            pregunta.setTema(tema);
            bancoPreguntaDAO.crear(pregunta);

                URI location = uriInfo.getBaseUriBuilder()
                    .path("preguntas/{idPregunta}")
                    .build(pregunta.getIdBancoPregunta());
            return Response.created(location)
                    .entity(pregunta)
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
