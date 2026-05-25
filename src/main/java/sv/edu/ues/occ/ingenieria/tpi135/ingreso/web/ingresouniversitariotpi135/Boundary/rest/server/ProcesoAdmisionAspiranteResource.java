package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ProcesoAdmisionAspiranteDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ProcesoAdmisionAspirante;

import java.io.Serializable;
import java.util.UUID;

/**
 * Recurso REST para gestionar el Proceso de Admisión de los Aspirantes.
 * Base: /resources/v1/proceso-admision
 */
@Path("proceso-admision")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcesoAdmisionAspiranteResource extends AbstractResource<ProcesoAdmisionAspirante> {

    @Inject
    private ProcesoAdmisionAspiranteDAO procesoAspiranteDAO;

    @Override
    protected ProcesoAdmisionAspiranteDAO getDAO() {
        return procesoAspiranteDAO;
    }

    /**
     * GET /proceso-admision
     * Retorna la lista de procesos de admisión con soporte para paginación (y futuros filtros).
     */
    @GET
    public Response listProcesos(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        return findRange(first, max);
    }

    /**
     * GET /proceso-admision/{idInscripcion}
     * Obtiene el proceso de admisión asociado a una inscripción específica.
     */
    @GET
    @Path("{idInscripcion}")
    public Response getProceso(@PathParam("idInscripcion") String idInscripcionStr) {
        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);
            ProcesoAdmisionAspirante proceso = procesoAspiranteDAO.leer(idInscripcion);

            if (proceso == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Proceso de admisión no encontrado para la inscripción especificada.")
                        .header(RestHeaders.NOT_FOUND_ID, idInscripcionStr)
                        .build();
            }
            return Response.ok(proceso).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El UUID de la inscripción es inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * POST /proceso-admision/asignar-masivo
     * Ejecuta la asignación masiva de cupos a carreras por nota de examen.
     * Payload esperado: {"idPrueba": "uuid", "idEtapa": "uuid"}
     */
    @POST
    @Path("asignar-masivo")
    public Response asignarMasivo(AsignacionMasivaPayload payload) {
        if (payload == null || payload.getIdEtapa() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El cuerpo de la petición debe contener obligatoriamente 'idEtapa'.")
                    .build();
        }

        try {
            UUID idEtapa = UUID.fromString(payload.getIdEtapa());

            // Ejecutamos la lógica de negocio pesada en el DAO
            procesoAspiranteDAO.procesarAsignacionMasiva(idEtapa);

            return Response.ok()
                    .entity("Asignación masiva procesada exitosamente.")
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formato de UUID inválido en el payload.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error durante la ejecución del lote de asignaciones.")
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * POST /proceso-admision/{idInscripcion}/asignar
     * Recalcula o ejecuta la asignación de cupo manualmente para un único aspirante.
     */
    @POST
    @Path("{idInscripcion}/asignar")
    public Response asignarAspiranteIndividual(@PathParam("idInscripcion") String idInscripcionStr) {
        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);

            // Verificamos existencia antes de operar (Orquestación del Resource)
            ProcesoAdmisionAspirante existente = procesoAspiranteDAO.leer(idInscripcion);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No se puede asignar: Proceso de inscripción no encontrado.")
                        .build();
            }

            // Ejecutamos la regla de negocio atómica
            ProcesoAdmisionAspirante resultado = procesoAspiranteDAO.asignarCarreraFinal(idInscripcion);

            return Response.ok(resultado).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El UUID proporcionado es inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al procesar la asignación del aspirante.")
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    // =================================================================================
    // CLASES DE APOYO (PAYLOADS)
    // =================================================================================

    /**
     * DTO interno para recibir los datos de configuración de la asignación masiva.
     * Mapea perfectamente la petición de la API Table: payload: {idPrueba, idEtapa}
     */
    public static class AsignacionMasivaPayload implements Serializable {
        private String idPrueba;
        private String idEtapa;

        public String getIdPrueba() {
            return idPrueba;
        }

        public void setIdPrueba(String idPrueba) {
            this.idPrueba = idPrueba;
        }

        public String getIdEtapa() {
            return idEtapa;
        }

        public void setIdEtapa(String idEtapa) {
            this.idEtapa = idEtapa;
        }
    }
}