package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntasPorClaveDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;

import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar el ciclo de vida de los Exámenes Realizados.
 * Base: /resources/v1/examenes
 */
@Path("examenes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExamenRealizadoResource extends AbstractResource<ExamenRealizado> {

    @Inject
    private ExamenRealizadoDAO examenRealizadoDAO;

    @Inject
    private PreguntasPorClaveDAO preguntasPorClaveDAO;

    @Override
    protected ExamenRealizadoDAO getDAO() {
        return examenRealizadoDAO;
    }

    /**
     * GET /examenes/{idExamen}
     * Obtiene un examen específico (utiliza el leer optimizado con JOIN FETCH).
     */
    @GET
    @Path("{idExamen}")
    public Response getExamen(@PathParam("idExamen") String idExamenStr) {
        try {
            UUID idExamen = UUID.fromString(idExamenStr);
            ExamenRealizado examen = examenRealizadoDAO.leer(idExamen);

            if (examen == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Examen no encontrado.")
                        .header(RestHeaders.NOT_FOUND_ID, idExamenStr)
                        .build();
            }
            return Response.ok(examen).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("UUID de examen inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * POST /examenes
     * Permite a un aspirante iniciar su examen. Ejecuta las validaciones de turno
     * y realiza la asignación aleatoria/aleuda de la clave en el servidor.
     */
    @POST
    public Response iniciarExamen(ExamenInicioDTO dto, @Context UriInfo uriInfo) {
        if (dto == null || dto.getIdInscripcion() == null || dto.getIdEtapa() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Missing-Parameter", "idInscripcion and idEtapa are mandatory")
                    .entity("Los parámetros de inscripción y etapa son obligatorios.")
                    .build();
        }

        try {
            ExamenRealizado nuevoExamen = examenRealizadoDAO.iniciarExamenAspirante(
                    dto.getIdInscripcion(),
                    dto.getIdEtapa()
            );

            UriBuilder builder = uriInfo.getAbsolutePathBuilder();
            builder.path(nuevoExamen.getIdExamenRealizado().toString());

            return Response.created(builder.build())
                    .entity(nuevoExamen)
                    .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .header(RestHeaders.CONFLICT_REASON, e.getMessage())
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * POST /examenes/{idExamen}/calificar
     * Califica el examen calculando la nota proporcional en base a las respuestas correctas.
     */
    @POST
    @Path("{idExamen}/calificar")
    public Response calificarExamen(@PathParam("idExamen") String idExamenStr) {
        try {
            UUID idExamen = UUID.fromString(idExamenStr);

            ExamenRealizado existente = examenRealizadoDAO.leer(idExamen);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Examen no encontrado para calificar.")
                        .header(RestHeaders.NOT_FOUND_ID, idExamenStr)
                        .build();
            }

            ExamenRealizado calificado = examenRealizadoDAO.calificarExamen(idExamen);
            return Response.ok(calificado).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .header(RestHeaders.CONFLICT_REASON, "Error en estado del examen")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /examenes/ranking
     * Obtiene el ranking de calificaciones ordenadas de mayor a menor rendimiento.
     */
    @GET
    @Path("ranking")
    public Response getRanking(
            @QueryParam("idPrueba") String idPruebaStr,
            @QueryParam("idEtapa") String idEtapaStr,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("20") @QueryParam("limit") int limit) {

        if (idPruebaStr == null || idEtapaStr == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Los parámetros idPrueba e idEtapa son requeridos de forma obligatoria.")
                    .build();
        }

        try {
            UUID idPrueba = UUID.fromString(idPruebaStr);
            UUID idEtapa = UUID.fromString(idEtapaStr);

            // Pasamos offset y limit al DAO
            List<ExamenRealizado> ranking = examenRealizadoDAO.findRankingByPruebaAndEtapa(idPrueba, idEtapa, offset, limit);

            return Response.ok(ranking)
                    .header(RestHeaders.TOTAL_RECORDS, ranking.size()) // Idealmente, aquí harías un count total real
                    .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formato de parámetros UUID inválido para el ranking.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /examenes/aspirante/{idAspirante}
     * Permite consultar el historial de exámenes de un estudiante específico.
     */
    @GET
    @Path("aspirante/{idAspirante}")
    public Response getExamenesPorAspirante(@PathParam("idAspirante") String idAspiranteStr) {
        try {
            UUID idAspirante = UUID.fromString(idAspiranteStr);
            List<ExamenRealizado> examenes = examenRealizadoDAO.findByAspiranteId(idAspirante);

            return Response.ok(examenes)
                    .header(RestHeaders.TOTAL_RECORDS, examenes.size())
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("UUID de aspirante no válido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /examenes/prueba/{idPrueba}
     * Lista todos los exámenes asociados a una prueba de admisión específica.
     */
    @GET
    @Path("prueba/{idPrueba}")
    public Response getExamenesPorPrueba(@PathParam("idPrueba") String idPruebaStr) {
        try {
            UUID idPrueba = UUID.fromString(idPruebaStr);
            List<ExamenRealizado> examenes = examenRealizadoDAO.findByPruebaId(idPrueba);

            return Response.ok(examenes)
                    .header(RestHeaders.TOTAL_RECORDS, examenes.size())
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("UUID de prueba no válido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /examenes/{idExamen}/preguntas
     * ACOPLAMIENTO DE NEGOCIO: Permite al frontend obtener el juego de preguntas exacto
     * que debe responder el alumno, basándose en la clave automática que le asignó el sistema.
     */
    @GET
    @Path("{idExamen}/preguntas")
    public Response getPreguntasDelExamen(@PathParam("idExamen") String idExamenStr) {
        try {
            UUID idExamen = UUID.fromString(idExamenStr);

            // 1. Cargamos el examen (trae la relación ClaveExamen por el JOIN FETCH del leer)
            ExamenRealizado examen = examenRealizadoDAO.leer(idExamen);
            if (examen == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Examen no encontrado.")
                        .build();
            }

            // 2. Extraemos el ID de la clave que el sistema seleccionó para él
            UUID idClave = examen.getClaveExamen().getIdClaveExaman();

            // 3. Consumimos tu método del PreguntasPorClaveDAO
            List<PreguntasPorClave> preguntas = preguntasPorClaveDAO.findPreguntasByClave(idClave);

            return Response.ok(preguntas)
                    .header(RestHeaders.TOTAL_RECORDS, preguntas.size())
                    .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Identificador de examen inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * DTO estático para recibir los parámetros estrictos de inicialización.
     */
    public static class ExamenInicioDTO {
        private UUID idInscripcion;
        private UUID idEtapa;

        public ExamenInicioDTO() {}

        public UUID getIdInscripcion() {
            return idInscripcion;
        }

        public void setIdInscripcion(UUID idInscripcion) {
            this.idInscripcion = idInscripcion;
        }

        public UUID getIdEtapa() {
            return idEtapa;
        }

        public void setIdEtapa(UUID idEtapa) {
            this.idEtapa = idEtapa;
        }
    }
}