package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ClavesExamanDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;

import java.net.URI;
import java.util.UUID;

/**
 * Recurso REST para gestionar las Claves de Examen directamente.
 * * Base: /resources/v1/claves
 */
@Path("claves")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClaveExamenResource extends AbstractResource<ClavesExamen> {

    @Inject
    private ClavesExamanDAO clavesExamanDAO;

    // TODO: Inyectar ExamenesDAO cuando exista para validar el borrado
    // @Inject
    // private ExamenesDAO examenesDAO;

    // TODO: Inyectar PreguntasDAO cuando exista para gestionar la asignación
    // @Inject
    // private PreguntasDAO preguntasDAO;

    @Override
    protected ClavesExamanDAO getDAO() {
        // Corregido usando covarianza para mantener tu estándar
        return clavesExamanDAO;
    }

    /**
     * GET /claves/{idClave}
     * Obtiene una clave específica por su ID.
     */
    @GET
    @Path("{idClave}")
    public Response getClave(@PathParam("idClave") String idClaveStr) {
        try {
            UUID idClave = UUID.fromString(idClaveStr);
            ClavesExamen clave = clavesExamanDAO.leer(idClave);

            if (clave == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Clave de examen no encontrada.")
                        .header(RestHeaders.NOT_FOUND_ID, idClaveStr)
                        .build();
            }
            return Response.ok(clave).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formato de UUID inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * PUT /claves/{idClave}
     * Actualiza el nombre de una clave.
     */
    @PUT
    @Path("{idClave}")
    public Response updateClave(@PathParam("idClave") String idClaveStr, ClavesExamen claveActualizada) {
        if (claveActualizada == null || claveActualizada.getNombreClave() == null || claveActualizada.getNombreClave().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El nuevo nombre de la clave es obligatorio.")
                    .build();
        }

        try {
            UUID idClave = UUID.fromString(idClaveStr);
            ClavesExamen existente = clavesExamanDAO.leer(idClave);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idClaveStr)
                        .build();
            }

            // Preservamos la prueba original (regla de negocio: no cambiar la clave a otra prueba)
            claveActualizada.setIdClaveExaman(idClave);
            claveActualizada.setPruebaAdmision(existente.getPruebaAdmision());

            ClavesExamen guardada = clavesExamanDAO.actualizar(claveActualizada);
            return Response.ok(guardada).build();

        } catch (IllegalArgumentException e) {
            // Atrapa la regla de negocio del DAO (duplicidad de nombre en la misma prueba)
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .header(RestHeaders.CONFLICT_REASON, "Nombre duplicado")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /claves/{idClave}
     * Elimina una clave (Solo si no tiene exámenes generados).
     */
    @DELETE
    @Path("{idClave}")
    public Response deleteClave(@PathParam("idClave") String idClaveStr) {
        try {
            UUID idClave = UUID.fromString(idClaveStr);
            ClavesExamen existente = clavesExamanDAO.leer(idClave);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idClaveStr)
                        .build();
            }

            // TODO: Validación de integridad según tu tabla
            // long examenesAsociados = examenesDAO.countByClave(idClave);
            // if (examenesAsociados > 0) {
            //     return Response.status(Response.Status.CONFLICT)
            //             .entity("No se puede eliminar la clave porque ya fue asignada a uno o más exámenes generados.")
            //             .build();
            // }

            clavesExamanDAO.eliminar(existente);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formato de UUID inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    // =========================================================================
    // SUB-RECURSOS: GESTIÓN DE PREGUNTAS POR CLAVE (Dejados listos para Fase 2)
    // =========================================================================

    /**
     * GET /claves/{idClave}/preguntas
     * Lista las preguntas asignadas a esta clave.
     */
    @GET
    @Path("{idClave}/preguntas")
    public Response getPreguntasDeClave(@PathParam("idClave") String idClaveStr) {
        // TODO: Implementar usando PreguntaDAO.findByClave(idClave)
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("Endpoint en construcción").build();
    }

    /**
     * POST /claves/{idClave}/preguntas
     * Asigna una pregunta existente a esta clave.
     */
    @POST
    @Path("{idClave}/preguntas")
    public Response asignarPreguntaAClave(@PathParam("idClave") String idClaveStr, /* Entity de mapeo */ Object payload) {
        // TODO: Validar existencia de clave, existencia de pregunta e insertar en tabla intermedia.
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("Endpoint en construcción").build();
    }

    /**
     * DELETE /claves/{idClave}/preguntas/{idPregunta}
     * Desasigna una pregunta de esta clave.
     */
    @DELETE
    @Path("{idClave}/preguntas/{idPregunta}")
    public Response quitarPreguntaDeClave(@PathParam("idClave") String idClaveStr, @PathParam("idPregunta") String idPreguntaStr) {
        // TODO: Eliminar de la tabla intermedia
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("Endpoint en construcción").build();
    }
}