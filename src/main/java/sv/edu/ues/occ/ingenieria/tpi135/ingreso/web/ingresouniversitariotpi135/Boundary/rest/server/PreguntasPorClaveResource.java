package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntasPorClaveDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClaveId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar las preguntas asignadas a una clave de examen.
 * original para evitar colisiones de rutas.
 * Base: /resources/v1/claves
 */
@Path("claves")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PreguntasPorClaveResource extends AbstractResource<PreguntasPorClave> {

    @Inject
    private PreguntasPorClaveDAO preguntasPorClaveDAO;

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

            ClavesExamen clave = clavesDAO.leer(idClave);
            if (clave == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("La clave de examen especificada no existe.")
                        .build();
            }

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
     * Asigna una pregunta existente a una clave usando un DTO limpio.
     */
    @POST
    @Path("{idClave}/preguntas")
    public Response asignarPreguntaAClave(
            @PathParam("idClave") String idClaveStr,
            AsignarPreguntaDTO payload,
            @Context UriInfo uriInfo) {

        if (payload == null || payload.getIdPregunta() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Debe proporcionar el ID de la pregunta a asignar.")
                    .build();
        }

        try {
            UUID idClave = UUID.fromString(idClaveStr);
            UUID idPregunta = payload.getIdPregunta();

            // CORRECCIÓN: Usar el método que carga la Etapa para evitar LazyInitializationException
            ClavesExamen clave = clavesDAO.findByIdWithEtapa(idClave);
            if (clave == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Clave de examen no encontrada.")
                        .build();
            }

            // REGLA DE NEGOCIO: Validar el límite de preguntas de la etapa
            long preguntasActuales = preguntasPorClaveDAO.countPreguntasByClave(idClave);
            int limiteRequerido = clave.getEtapaAdmision().getCantidadPreguntasRequeridas();

            if (preguntasActuales >= limiteRequerido) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Límite alcanzado: La etapa permite un máximo de " + limiteRequerido + " preguntas.")
                        .build();
            }

            // Validar existencia en el banco y evitar duplicados
            if (preguntasDAO.leer(idPregunta) == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("La pregunta especificada no existe en el banco.")
                        .build();
            }

            if (preguntasPorClaveDAO.existsByClaveAndPregunta(idClave, idPregunta)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Esta pregunta ya se encuentra asignada a esta clave.")
                        .build();
            }

            // Persistir la relación
            PreguntasPorClaveId compositeId = new PreguntasPorClaveId();
            compositeId.setIdClave(idClave);
            compositeId.setIdPregunta(idPregunta);

            PreguntasPorClave nuevaAsignacion = new PreguntasPorClave();
            nuevaAsignacion.setIdPreguntaPorClave(compositeId);

            preguntasPorClaveDAO.crear(nuevaAsignacion);

            URI location = uriInfo.getAbsolutePathBuilder().path(idPregunta.toString()).build();
            return Response.created(location).entity(nuevaAsignacion).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error en el formato del UUID.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * POST /claves/{idClave}/preguntas/masivo
     * NUEVO: Asigna un lote completo de preguntas a la vez, garantizando no superar el límite.
     */
    @POST
    @Path("{idClave}/preguntas/masivo")
    public Response asignarPreguntasMasivamente(
            @PathParam("idClave") String idClaveStr,
            AsignacionMasivaDTO payload) {

        if (payload == null || payload.getIdsPreguntas() == null || payload.getIdsPreguntas().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Debe proporcionar una lista de IDs de preguntas a asignar.")
                    .build();
        }

        try {
            UUID idClave = UUID.fromString(idClaveStr);

            ClavesExamen clave = clavesDAO.findByIdWithEtapa(idClave);
            if (clave == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Clave de examen no encontrada.")
                        .build();
            }

            // Validar límite global
            long preguntasActuales = preguntasPorClaveDAO.countPreguntasByClave(idClave);
            int limiteRequerido = clave.getEtapaAdmision().getCantidadPreguntasRequeridas();
            int cantidadAInsertar = payload.getIdsPreguntas().size();

            if ((preguntasActuales + cantidadAInsertar) > limiteRequerido) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("No se puede completar el lote. El límite es de " + limiteRequerido +
                                " preguntas. Actualmente hay " + preguntasActuales + " y estás intentando añadir " + cantidadAInsertar + ".")
                        .build();
            }

            List<PreguntasPorClave> insertadas = new ArrayList<>();

            // Iterar y guardar
            for (UUID idPregunta : payload.getIdsPreguntas()) {
                // Ignorar si ya existe (para evitar que falle todo el lote por un duplicado)
                if (!preguntasPorClaveDAO.existsByClaveAndPregunta(idClave, idPregunta)) {

                    PreguntasPorClaveId compositeId = new PreguntasPorClaveId();
                    compositeId.setIdClave(idClave);
                    compositeId.setIdPregunta(idPregunta);

                    PreguntasPorClave nuevaAsignacion = new PreguntasPorClave();
                    nuevaAsignacion.setIdPreguntaPorClave(compositeId);

                    preguntasPorClaveDAO.crear(nuevaAsignacion);
                    insertadas.add(nuevaAsignacion);
                }
            }

            return Response.ok(insertadas).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error en el formato de uno o más UUIDs.")
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

            PreguntasPorClaveId compositeId = new PreguntasPorClaveId();
            compositeId.setIdClave(idClave);
            compositeId.setIdPregunta(idPregunta);

            PreguntasPorClave asignacionExistente = preguntasPorClaveDAO.leer(compositeId);

            if (asignacionExistente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("La asignación entre esta clave y esta pregunta no existe.")
                        .build();
            }

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

    // =========================================================================
    // DTOs PARA EL MANEJO LIMPIO DE PAYLOADS
    // =========================================================================

    public static class AsignarPreguntaDTO {
        private UUID idPregunta;
        public AsignarPreguntaDTO() {}
        public UUID getIdPregunta() { return idPregunta; }
        public void setIdPregunta(UUID idPregunta) { this.idPregunta = idPregunta; }
    }

    public static class AsignacionMasivaDTO {
        private List<UUID> idsPreguntas;
        public AsignacionMasivaDTO() {}
        public List<UUID> getIdsPreguntas() { return idsPreguntas; }
        public void setIdsPreguntas(List<UUID> idsPreguntas) { this.idsPreguntas = idsPreguntas; }
    }
}