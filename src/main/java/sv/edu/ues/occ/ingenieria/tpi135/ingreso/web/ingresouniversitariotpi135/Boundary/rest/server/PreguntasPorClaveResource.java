package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntasPorClaveDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO; // Asumiendo que existe
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO; // Asumiendo que existe
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClaveId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar las preguntas asignadas a una clave de examen.
 * Base: /resources/v1/claves
 */
@Path("claves")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PreguntasPorClaveResource extends AbstractResource<PreguntasPorClave> {

    @Inject
    private PreguntasPorClaveDAO preguntasPorClaveDAO;

    // Se inyectan para validar que los padres existan antes de asociarlos
    @Inject
    private ClavesExamanDAO clavesDAO;

    @Inject
    private BancoPreguntaDAO preguntasDAO;

    @Override
    protected PreguntasPorClaveDAO getDAO() {
        return preguntasPorClaveDAO;
    }

    /**
     * GET /claves/{idClave}/preguntas
     * Retorna todas las preguntas asignadas a una clave específica.
     */
    @GET
    @Path("{idClave}/preguntas")
    public Response getPreguntasByClave(@PathParam("idClave") String idClaveStr) {
        try {
            UUID idClave = UUID.fromString(idClaveStr);

            // Validamos que la clave exista antes de buscar sus preguntas
            ClavesExamen clave = clavesDAO.leer(idClave);
            if (clave == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("La clave de examen especificada no existe.")
                        .header(RestHeaders.NOT_FOUND_ID, idClaveStr)
                        .build();
            }

            // Usamos tu método optimizado con JOIN FETCH
            List<PreguntasPorClave> preguntas = preguntasPorClaveDAO.findPreguntasByClave(idClave);

            return Response.ok(preguntas).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El UUID de la clave es inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * POST /claves/{idClave}/preguntas
     * Asigna una pregunta existente a una clave.
     * Payload esperado: {"bancoPregunta": {"idPregunta": "uuid-aqui"}}
     */
    @POST
    @Path("{idClave}/preguntas")
    public Response asignarPreguntaAClave(
            @PathParam("idClave") String idClaveStr,
            PreguntasPorClave nuevaAsignacion,
            @Context UriInfo uriInfo) {

        if (nuevaAsignacion == null || nuevaAsignacion.getBancoPregunta() == null || nuevaAsignacion.getBancoPregunta().getIdBancoPregunta() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Debe proporcionar el ID de la pregunta a asignar.")
                    .build();
        }

        try {
            UUID idClave = UUID.fromString(idClaveStr);
            UUID idPregunta = nuevaAsignacion.getBancoPregunta().getIdBancoPregunta();

            // 1. Validar existencia de la Clave
            ClavesExamen clave = clavesDAO.leer(idClave);
            if (clave == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Clave de examen no encontrada.")
                        .build();
            }

            // 2. Validar existencia de la Pregunta
            BancoPregunta pregunta = preguntasDAO.leer(idPregunta);
            if (pregunta == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("La pregunta especificada no existe en el banco.")
                        .build();
            }

            // 3. Regla de negocio: Evitar duplicados (usando tu método del DAO)
            if (preguntasPorClaveDAO.existsByClaveAndPregunta(idClave, idPregunta)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Esta pregunta ya se encuentra asignada a esta clave.")
                        .build();
            }

            // 4. Construir la llave compuesta e inyectar dependencias
            PreguntasPorClaveId compositeId = new PreguntasPorClaveId();
            compositeId.setIdClave(idClave);
            compositeId.setIdPregunta(idPregunta);

            nuevaAsignacion.setIdPreguntaPorClave(compositeId);
            nuevaAsignacion.setClaveExamen(clave);
            nuevaAsignacion.setBancoPregunta(pregunta);

            // 5. Persistir
            preguntasPorClaveDAO.crear(nuevaAsignacion);

            // 6. Retornar Created
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(idPregunta.toString())
                    .build();

            return Response.created(location).entity(nuevaAsignacion).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error en los datos de los identificadores.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /claves/{idClave}/preguntas/{idPregunta}
     * Desasigna (elimina) una pregunta de una clave.
     */
    @DELETE
    @Path("{idClave}/preguntas/{idPregunta}")
    public Response desasignarPregunta(
            @PathParam("idClave") String idClaveStr,
            @PathParam("idPregunta") String idPreguntaStr) {

        try {
            UUID idClave = UUID.fromString(idClaveStr);
            UUID idPregunta = UUID.fromString(idPreguntaStr);

            // 1. Construir el ID compuesto para buscar el registro exacto
            PreguntasPorClaveId compositeId = new PreguntasPorClaveId();
            compositeId.setIdClave(idClave);
            compositeId.setIdPregunta(idPregunta);

            // 2. Buscar si existe la asignación
            PreguntasPorClave asignacionExistente = preguntasPorClaveDAO.leer(compositeId);

            if (asignacionExistente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("La asignación entre esta clave y esta pregunta no existe.")
                        .build();
            }

            // 3. Eliminar
            preguntasPorClaveDAO.eliminar(asignacionExistente);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("UUID inválido proporcionado en la ruta.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }
}