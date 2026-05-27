package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AsignacionAulaAspiranteDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.InscripcionesPruebaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.DisponibilidadAulaTurnoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionAulaAspirante;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("asignaciones-aula")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AsignacionAulaAspiranteResource extends AbstractResource<AsignacionAulaAspirante> {

    @Inject
    private AsignacionAulaAspiranteDAO asignacionAulaAspiranteDAO;

    @Inject
    private InscripcionesPruebaDAO inscripcionesDAO;

    @Inject
    private DisponibilidadAulaTurnoDAO disponibilidadDAO; // DAO de tu compañero

    @Override
    protected AsignacionAulaAspiranteDAO getDAO() {
        return asignacionAulaAspiranteDAO;
    }

    /**
     * POST /inscripciones/{idInscripcion}/asignacion-aula
     */
    @POST
    @Path("inscripciones/{idInscripcion}")
    public Response asignarAulaAspirante(
            @PathParam("idInscripcion") String idInscripcionStr,
            AsignacionRequest payload, // Uso del DTO para simplificar la entrada
            @Context UriInfo uriInfo) {

        if (payload == null || payload.getIdAula() == null || payload.getIdTurno() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Los parámetros idAula e idTurno son obligatorios.")
                    .build();
        }

        try {
            UUID idInscripcion = UUID.fromString(idInscripcionStr);

            // 1. Validar la existencia de la inscripción
            InscripcionesPrueba inscripcion = inscripcionesDAO.leer(idInscripcion);
            if (inscripcion == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("La inscripción provista no existe.")
                        .build();
            }

            // 2. REUTILIZACIÓN: Usamos el método 'findFiltrado' del DAO de tu compañero
            // DESPUÉS (Llamada corregida solicitando exactamente 1 registro)
            List<DisponibilidadAulaTurno> listaDisp = disponibilidadDAO.findFiltrado(payload.getIdAula(), payload.getIdTurno(), 0, 1);
            if (listaDisp.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("No existe una asignación de disponibilidad para el Aula y Turno especificados.")
                        .build();
            }
            // Como sabemos que existe, extraemos el registro único de la lista
            DisponibilidadAulaTurno disponibilidadAsignable = listaDisp.get(0);

            // 3. Armar la entidad completa para persistir
            AsignacionAulaAspirante nuevaAsignacion = new AsignacionAulaAspirante();
            nuevaAsignacion.setInscripcionPrueba(inscripcion);
            nuevaAsignacion.setDisponibilidad(disponibilidadAsignable);

            // 4. Guardar (Aquí el DAO ejecutará el conteo de aforo y choques de horario)
            asignacionAulaAspiranteDAO.crear(nuevaAsignacion);

            URI location = uriInfo.getBaseUriBuilder()
                    .path("asignaciones-aula")
                    .path(nuevaAsignacion.getIdAsignacionAulaAspirante().toString())
                    .build();

            return Response.created(location).entity(nuevaAsignacion).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Formato UUID inválido.")
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage()) // Retorna "El aula seleccionada ha alcanzado su capacidad máxima", etc.
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * GET /disponibilidad/{idAula}/{idTurno}/asignaciones
     */
    @GET
    @Path("disponibilidad/{idAula}/{idTurno}") // Limpiamos el "/asignaciones" del final
    public Response getAsignacionesPorAulaYTurno(
            @PathParam("idAula") String idAulaStr,
            @PathParam("idTurno") String idTurnoStr,
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        try {
            UUID idAula = UUID.fromString(idAulaStr);
            UUID idTurno = UUID.fromString(idTurnoStr);

            // Llama al método propio de AsignacionAulaAspiranteDAO
            List<AsignacionAulaAspirante> paginados = asignacionAulaAspiranteDAO.findByAulaAndTurno(idAula, idTurno, first, max);
            long totalRecords = asignacionAulaAspiranteDAO.countByAulaAndTurno(idAula, idTurno);

            return Response.ok(paginados)
                    .header(RestHeaders.TOTAL_RECORDS, totalRecords)
                    .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Los parámetros de ruta deben ser UUIDs válidos.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /asignaciones-aula/{idAsignacion}
     */
    @DELETE
    @Path("{idAsignacion}")
    public Response deleteAsignacion(@PathParam("idAsignacion") String idAsignacionStr) {
        try {
            UUID idAsignacion = UUID.fromString(idAsignacionStr);
            AsignacionAulaAspirante existente = asignacionAulaAspiranteDAO.leer(idAsignacion);

            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idAsignacionStr)
                        .build();
            }

            asignacionAulaAspiranteDAO.eliminar(existente);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("UUID de asignación inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * Clase DTO (Se queda aquí en la frontera Web, protegiendo los DAOs)
     */
    public static class AsignacionRequest {
        private UUID idAula;
        private UUID idTurno;

        public UUID getIdAula() { return idAula; }
        public void setIdAula(UUID idAula) { this.idAula = idAula; }

        public UUID getIdTurno() { return idTurno; }
        public void setIdTurno(UUID idTurno) { this.idTurno = idTurno; }
    }
}