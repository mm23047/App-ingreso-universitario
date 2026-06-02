package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.dto.RespuestasLoteDTO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.RespuestaExamenDAO;
// TODO: Importar los DAOs complementarios cuando los tengas creados
// import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
// import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestaExamen;
// import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar las Respuestas del Examen.
 * Base: /resources/v1/respuestas_examen
 */
@Path("respuestas_examen")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RespuestaExamenResource extends AbstractResource<RespuestaExamen> {

    @Inject
    private RespuestaExamenDAO respuestaExamenDAO;

     //Dependencias necesarias para validar las reglas de negocio
     @Inject
     private ExamenRealizadoDAO examenDAO;
     @Inject
     private PreguntaOpcionDAO opcionDAO;

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
    /**
     * POST /respuestas
     * Registra UNA respuesta (Autoguardado al hacer clic en el frontend virtual).
     */
    @POST
    public Response submitRespuesta(RespuestaExamen respuesta, @Context UriInfo uriInfo) {
        if (respuesta == null || respuesta.getExamenRealizado() == null || respuesta.getPreguntaOpcion() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Datos incompletos").build();
        }

        try {
            UUID idExamen = respuesta.getExamenRealizado().getIdExamenRealizado();
            UUID idOpcion = respuesta.getPreguntaOpcion().getIdPreguntaOpcion();

            // 1. Validar que el examen exista y no esté calificado/finalizado
            ExamenRealizado examen = examenDAO.leer(idExamen);
            if (examen == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Examen no encontrado").build();
            }
            if (examen.getPuntajeFinal() != null) { // Si ya tiene nota, está cerrado
                return Response.status(Response.Status.CONFLICT).entity("El examen ya fue finalizado y calificado.").build();
            }

            // 2. Obtener la opción para saber a qué pregunta pertenece
            PreguntaOpcion opcion = opcionDAO.leer(idOpcion);
            if (opcion == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Opción no válida").build();
            }
            UUID idPregunta = opcion.getBancoPregunta().getIdBancoPregunta();

            // 3. REGLA DE NEGOCIO (Upsert): Si ya la respondió, actualizamos
            RespuestaExamen respuestaPrevia = respuestaExamenDAO.findByExamenAndPregunta(idExamen, idPregunta);
            if (respuestaPrevia != null) {
                respuestaPrevia.setPreguntaOpcion(opcion);
                respuestaExamenDAO.actualizar(respuestaPrevia);
                return Response.ok(respuestaPrevia).build();
            }

            // 4. Si es nueva, la creamos
            respuesta.setPreguntaOpcion(opcion); // Aseguramos usar la entidad completa
            respuestaExamenDAO.crear(respuesta);
            URI location = uriInfo.getAbsolutePathBuilder().path(respuesta.getIdRespuestaExamen().toString()).build();
            return Response.created(location).entity(respuesta).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * POST /respuestas/lote
     * Registra múltiples respuestas de un solo golpe (Máquina OMR o "Enviar Todo").
     */
    /**
     * POST /respuestas/lote
     * Registra múltiples respuestas procesando el DTO en bloque.
     */
    @POST
    @Path("lote")
    public Response submitRespuestasBatch(RespuestasLoteDTO payload) {
        if (payload == null || payload.getIdExamen() == null ||
                payload.getOpcionesSeleccionadas() == null || payload.getOpcionesSeleccionadas().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Payload inválido. Se requiere el ID del examen y la lista de opciones.")
                    .build();
        }

        try {
            // 1. Validar estado del examen
            ExamenRealizado examen = examenDAO.leer(payload.getIdExamen());
            if (examen == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Examen no encontrado.").build();
            }
            if (examen.getPuntajeFinal() != null) {
                return Response.status(Response.Status.CONFLICT).entity("El examen ya está finalizado y calificado.").build();
            }

            // 2. Delegar el guardado optimizado al DAO
            respuestaExamenDAO.guardarLoteMejorado(payload.getIdExamen(), payload.getOpcionesSeleccionadas());

            return Response.status(Response.Status.CREATED)
                    .entity("Respuestas procesadas correctamente.")
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
            if (existente.getExamenRealizado().getPuntajeFinal() != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("No se puede eliminar una respuesta de un examen ya finalizado.")
                        .build();
            }
            respuestaExamenDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}