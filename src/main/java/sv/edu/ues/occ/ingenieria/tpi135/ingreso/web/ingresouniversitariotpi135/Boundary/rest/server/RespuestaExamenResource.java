package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.RespuestaExamenDAO;
// TODO: Importar los DAOs complementarios cuando los tengas creados
// import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
// import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestaExamen;
// import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar las Respuestas del Examen.
 * Base: /resources/v1/respuestas
 */
@Path("respuestas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RespuestaExamenResource extends AbstractResource<RespuestaExamen> {

    @Inject
    private RespuestaExamenDAO respuestaExamenDAO;

    // Dependencias necesarias para validar las reglas de negocio
    // @Inject
    // private ExamenRealizadoDAO examenDAO;
    // @Inject
    // private PreguntaOpcionDAO opcionDAO;

    @Override
    protected RespuestaExamenDAO getDAO() {
        return respuestaExamenDAO;
    }

    /**
     * GET /respuestas
     * Retorna lista paginada de respuestas (Uso administrativo).
     */
    @GET
    public Response listRespuestas(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        return findRange(first, max);
    }

    /**
     * POST /respuestas
     * Registra una respuesta. Si el aspirante ya había respondido esa pregunta,
     * actualiza la opción elegida (Regla de negocio: Upsert).
     */
    @POST
    public Response submitRespuesta(RespuestaExamen respuesta, @Context UriInfo uriInfo) {
        if (respuesta == null || respuesta.getExamenRealizado() == null || respuesta.getPreguntaOpcion() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El ID del examen y el ID de la opción seleccionada son obligatorios.")
                    .build();
        }

        try {
            UUID idExamen = respuesta.getExamenRealizado().getIdExamenRealizado();
            UUID idOpcion = respuesta.getPreguntaOpcion().getIdPreguntaOpcion();

            // 1. TODO: Validar que el examen exista y no esté ya finalizado
            // ExamenRealizado examen = examenDAO.leer(idExamen);
            // if(examen == null || examen.isFinalizado()) { return 409 Conflict }

            // 2. TODO: Obtener la opción para saber a qué pregunta pertenece
            // PreguntaOpcion opcion = opcionDAO.leer(idOpcion);
            // UUID idPregunta = opcion.getBancoPregunta().getIdBancoPregunta();

            // --- SIMULACIÓN HASTA TENER EL PreguntaOpcionDAO ---
            // UUID idPregunta = ...

            // 3. REGLA DE NEGOCIO: ¿Ya respondió esta pregunta?
            // Si ya la respondió, en lugar de crear una nueva (y causar duplicados), actualizamos la existente.
            /* RespuestaExamen respuestaPrevia = respuestaExamenDAO.findByExamenAndPregunta(idExamen, idPregunta);
            if (respuestaPrevia != null) {
                respuestaPrevia.setPreguntaOpcion(opcion); // Cambia de opinión
                respuestaExamenDAO.actualizar(respuestaPrevia);
                return Response.ok(respuestaPrevia).build();
            }
            */

            // 4. Si es nueva, la creamos normalmente
            respuestaExamenDAO.crear(respuesta);
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(respuesta.getIdRespuestaExamen().toString())
                    .build();

            return Response.created(location).entity(respuesta).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /respuestas/{idRespuesta}
     * Obtiene una respuesta específica.
     */
    @GET
    @Path("{idRespuesta}")
    public Response getRespuesta(@PathParam("idRespuesta") String idRespuestaStr) {
        try {
            UUID idRespuesta = UUID.fromString(idRespuestaStr);
            RespuestaExamen respuesta = respuestaExamenDAO.leer(idRespuesta);
            if (respuesta == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idRespuestaStr)
                        .build();
            }
            return Response.ok(respuesta).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("UUID inválido").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /respuestas/examen/{idExamen}
     * Devuelve todas las respuestas de un examen específico.
     * Útil para recuperar el progreso si se va la luz o el estudiante refresca la página.
     */
    @GET
    @Path("examen/{idExamen}")
    public Response getRespuestasByExamen(@PathParam("idExamen") String idExamenStr) {
        try {
            UUID idExamen = UUID.fromString(idExamenStr);
            List<RespuestaExamen> respuestas = respuestaExamenDAO.findByExamenId(idExamen);
            return Response.ok(respuestas).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("UUID inválido").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /respuestas/examen/{idExamen}/conteo
     * REGLA DE NEGOCIO: Validar cuántas preguntas ha respondido antes de permitirle entregar.
     */
    @GET
    @Path("examen/{idExamen}/conteo")
    public Response getConteoRespuestas(@PathParam("idExamen") String idExamenStr) {
        try {
            UUID idExamen = UUID.fromString(idExamenStr);
            Long conteo = respuestaExamenDAO.countRespuestasByExamen(idExamen);
            return Response.ok(conteo).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("UUID inválido").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /respuestas/{idRespuesta}
     * Elimina una respuesta (Por si el aspirante decide dejarla en blanco).
     */
    @DELETE
    @Path("{idRespuesta}")
    public Response deleteRespuesta(@PathParam("idRespuesta") String idRespuestaStr) {
        try {
            UUID idRespuesta = UUID.fromString(idRespuestaStr);
            RespuestaExamen existente = respuestaExamenDAO.leer(idRespuesta);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // TODO: Validar que el examen no esté ya finalizado/calificado antes de borrar

            respuestaExamenDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}