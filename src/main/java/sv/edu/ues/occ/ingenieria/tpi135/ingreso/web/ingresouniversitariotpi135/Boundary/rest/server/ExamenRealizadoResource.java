package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenRealizado;

import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar los Exámenes Realizados.
 * * Base: /resources/v1/examenes
 */
@Path("examenes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExamenRealizadoResource extends AbstractResource<ExamenRealizado> {

    @Inject
    private ExamenRealizadoDAO examenRealizadoDAO;

    // TODO: Inyectar RespuestaExamenDAO cuando esté creado para el endpoint de respuestas

    @Override
    protected ExamenRealizadoDAO getDAO() {
        return examenRealizadoDAO;
    }

    /**
     * GET /examenes/{idExamen}
     * Obtiene un examen específico (incluye relaciones gracias al JOIN FETCH en el DAO).
     */
    @GET
    @Path("{idExamen}")
    public Response getExamen(@PathParam("idExamen") String idExamenStr) {
        try {
            UUID idExamen = UUID.fromString(idExamenStr);
            ExamenRealizado examen = examenRealizadoDAO.leer(idExamen);

            if (examen == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Examen no encontrado")
                        .header(RestHeaders.NOT_FOUND_ID, idExamenStr)
                        .build();
            }
            return Response.ok(examen).build();
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
     * POST /examenes/{idExamen}/calificar
     * Califica el examen (actualiza el puntaje y cambia estado de inscripción). Es idempotente.
     */
    @POST
    @Path("{idExamen}/calificar")
    public Response calificarExamen(@PathParam("idExamen") String idExamenStr) {
        try {
            UUID idExamen = UUID.fromString(idExamenStr);

            // Validamos existencia antes de mandar al DAO
            ExamenRealizado existente = examenRealizadoDAO.leer(idExamen);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Examen no encontrado")
                        .header(RestHeaders.NOT_FOUND_ID, idExamenStr)
                        .build();
            }

            // Ejecutamos la regla de negocio
            ExamenRealizado calificado = examenRealizadoDAO.calificarExamen(idExamen);

            return Response.ok(calificado).build();

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Captura las excepciones de negocio lanzadas por tu DAO (ej. "El examen no tiene una clave asignada")
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .header(RestHeaders.CONFLICT_REASON, "Error de validación al calificar")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * ENDPOINT EXTRA BASADO EN TU DAO: GET /examenes/ranking
     * Obtiene el ranking de calificaciones por Prueba y Etapa.
     */
    @GET
    @Path("ranking")
    public Response getRanking(
            @QueryParam("idPrueba") String idPruebaStr,
            @QueryParam("idEtapa") String idEtapaStr,
            @DefaultValue("100") @QueryParam("max") int max) {

        if (idPruebaStr == null || idEtapaStr == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Los parámetros idPrueba e idEtapa son obligatorios.")
                    .build();
        }

        try {
            UUID idPrueba = UUID.fromString(idPruebaStr);
            UUID idEtapa = UUID.fromString(idEtapaStr);

            List<ExamenRealizado> ranking = examenRealizadoDAO.findRankingByPruebaAndEtapa(idPrueba, idEtapa, max);

            return Response.ok(ranking)
                    .header(RestHeaders.TOTAL_RECORDS, ranking.size())
                    .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formato de UUID inválido en los parámetros.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }
}