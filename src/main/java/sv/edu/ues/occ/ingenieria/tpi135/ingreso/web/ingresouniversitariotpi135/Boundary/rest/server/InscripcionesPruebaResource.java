package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar Inscripciones de manera global.
 * Base: /resources/v1/inscripciones
 */
@Path("inscripciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InscripcionesPruebaResource extends AbstractResource<InscripcionesPrueba> {

    @Inject
    private InscripcionesPruebaDAO inscripcionesPruebaDAO;

    @Override
    protected IngresoDefaultDataAccess<InscripcionesPrueba> getDAO() {
        return inscripcionesPruebaDAO;
    }

    /**
     * GET /inscripciones
     * Lista las inscripciones. Permite filtrar por idPrueba y estado,
     * aprovechando el método de negocio de tu DAO (FASE 1).
     */
    @GET
    public Response listInscripciones(
            @QueryParam("idPrueba") String idPruebaStr,
            @QueryParam("estado") String estado,
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {

        try {
            // Si el Frontend envía idPrueba y estado, usamos la consulta especializada
            if (idPruebaStr != null && !idPruebaStr.isBlank() && estado != null && !estado.isBlank()) {
                UUID idPrueba = UUID.fromString(idPruebaStr);
                List<InscripcionesPrueba> filtradas = inscripcionesPruebaDAO.findByPruebaAndEstado(idPrueba, estado);

                // Aplicamos paginación manual en memoria para los resultados filtrados
                int fromIndex = Math.min(first, filtradas.size());
                int toIndex = Math.min(first + max, filtradas.size());
                List<InscripcionesPrueba> paginadas = filtradas.subList(fromIndex, toIndex);

                return Response.ok(paginadas)
                        .header(RestHeaders.TOTAL_RECORDS, filtradas.size())
                        .build();
            }

            // Si no hay filtros, retorna todas paginadas usando el método heredado
            return findRange(first, max);

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Parámetros de filtrado inválidos: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /inscripciones/{idInscripcion}
     * Obtiene una inscripción. Utiliza el método leer() sobrescrito en el DAO
     * que trae las relaciones con JOIN FETCH.
     */
    @GET
    @Path("{idInscripcion}")
    public Response getInscripcion(@PathParam("idInscripcion") String idInscripcionStr) {
        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);
            InscripcionesPrueba inscripcion = inscripcionesPruebaDAO.leer(idInscripcion);

            if (inscripcion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idInscripcionStr)
                        .build();
            }
            return Response.ok(inscripcion).build();

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
     * PUT /inscripciones/{idInscripcion}
     * Actualiza una inscripción (ej. cambio de estado a CANCELADO, APROBADO, etc.).
     */
    @PUT
    @Path("{idInscripcion}")
    public Response updateInscripcion(
            @PathParam("idInscripcion") String idInscripcionStr,
            InscripcionesPrueba inscripcionActualizada) {

        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);

            // 1. Verificar que exista
            InscripcionesPrueba existente = inscripcionesPruebaDAO.leer(idInscripcion);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idInscripcionStr)
                        .build();
            }

            // 2. Asegurar que el ID del payload sea el mismo de la URL
            inscripcionActualizada.setIdInscripcionPrueba(idInscripcion);

            // 3. Evitar que se modifiquen los IDs de las relaciones vitales accidentalmente
            if(inscripcionActualizada.getAspiranteDato() == null) {
                inscripcionActualizada.setAspiranteDato(existente.getAspiranteDato());
            }
            if(inscripcionActualizada.getPruebaAdmision() == null) {
                inscripcionActualizada.setPruebaAdmision(existente.getPruebaAdmision());
            }

            // 4. Actualizar. Si viola la regla de negocio, el DAO lanzará IllegalArgumentException
            InscripcionesPrueba guardada = inscripcionesPruebaDAO.actualizar(inscripcionActualizada);

            return Response.ok(guardada).build();

        } catch (IllegalArgumentException e) {
            // Captura las reglas de negocio de tu DAO (Colisiones, nulos, etc.)
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
     * DELETE /inscripciones/{idInscripcion}
     * Cancela/Elimina la inscripción.
     */
    @DELETE
    @Path("{idInscripcion}")
    public Response deleteInscripcion(@PathParam("idInscripcion") String idInscripcionStr) {
        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);
            InscripcionesPrueba existente = inscripcionesPruebaDAO.leer(idInscripcion);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idInscripcionStr)
                        .build();
            }

            // Eliminación física (Si tu negocio exige eliminación lógica,
            // aquí deberías hacer un setEstado("CANCELADO") y un actualizar).
            inscripcionesPruebaDAO.eliminar(existente);

            return Response.noContent().build();

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
}