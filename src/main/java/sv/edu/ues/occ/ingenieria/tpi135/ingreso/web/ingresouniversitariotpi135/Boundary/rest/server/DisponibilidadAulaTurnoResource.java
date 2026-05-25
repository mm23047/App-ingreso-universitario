package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.DisponibilidadAulaTurnoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurnoId;

// ⚠️ Se asume la existencia de estos DAOs para validar la existencia de los padres
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AulaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TurnosExamenDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Aula;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.TurnosExamen;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para gestionar la Disponibilidad de Aulas por Turno.
 * Base dinámica para soportar rutas multi-raíz.
 */
@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DisponibilidadAulaTurnoResource extends AbstractResource<DisponibilidadAulaTurno> {

    @Inject
    private DisponibilidadAulaTurnoDAO disponibilidadDAO;

    @Inject
    private AulaDAO aulaDAO;

    @Inject
    private TurnosExamenDAO turnosDAO;

    @Override
    protected DisponibilidadAulaTurnoDAO getDAO() {
        return disponibilidadDAO;
    }

    /**
     * GET /disponibilidad
     * Listar disponibilidad filtrada opcionalmente por Aula y/o Turno.
     */
    @GET
    @Path("disponibilidad")
    public Response listDisponibilidad(
            @QueryParam("idAula") String idAulaStr,
            @QueryParam("idTurno") String idTurnoStr) {
        try {
            UUID idAula = (idAulaStr != null && !idAulaStr.isBlank()) ? UUID.fromString(idAulaStr) : null;
            UUID idTurno = (idTurnoStr != null && !idTurnoStr.isBlank()) ? UUID.fromString(idTurnoStr) : null;

            List<DisponibilidadAulaTurno> lista = disponibilidadDAO.findFiltrado(idAula, idTurno);
            return Response.ok(lista).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Uno de los UUIDs proporcionados para el filtro es inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * POST /aulas/{idAula}/disponibilidad/{idTurno}
     * Crea una nueva asignación de disponibilidad (Sin Payload corporal).
     */
    @POST
    @Path("aulas/{idAula}/disponibilidad/{idTurno}")
    public Response createDisponibilidad(
            @PathParam("idAula") String idAulaStr,
            @PathParam("idTurno") String idTurnoStr,
            @Context UriInfo uriInfo) {
        try {
            UUID idAula = UUID.fromString(idAulaStr);
            UUID idTurno = UUID.fromString(idTurnoStr);

            // 1. REGLA DE NEGOCIO: Validar existencia de las entidades Padre
            Aula aula = aulaDAO.leer(idAula);
            TurnosExamen turno = turnosDAO.leer(idTurno);

            if (aula == null || turno == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No se puede crear disponibilidad: El aula o el turno especificado no existen.")
                        .build();
            }

            // 2. REGLA DE NEGOCIO: Impedir registros duplicados de llave compuesta activa
            if (disponibilidadDAO.existsByAulaAndTurno(idAula, idTurno)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Conflicto: Ya existe una programación de disponibilidad registrada para este Aula en el Turno indicado.")
                        .header(RestHeaders.CONFLICT_REASON, "Registro duplicado")
                        .build();
            }

            // 3. Instanciar y persistir (El método @PrePersist de la entidad generará el ID compuesto)
            DisponibilidadAulaTurno nuevaDisponibilidad = new DisponibilidadAulaTurno();
            nuevaDisponibilidad.setAula(aula);
            nuevaDisponibilidad.setTurnoExamen(turno);

            disponibilidadDAO.crear(nuevaDisponibilidad);

            // Generar Location apuntando al recurso unificado de lectura
            URI location = uriInfo.getBaseUriBuilder()
                    .path("disponibilidad")
                    .queryParam("idAula", idAulaStr)
                    .queryParam("idTurno", idTurnoStr)
                    .build();

            return Response.created(location)
                    .entity(nuevaDisponibilidad)
                    .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Parámetros de ruta inválidos (Formato UUID incorrecto).")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }

    /**
     * DELETE /aulas/{idAula}/disponibilidad/{idTurno}
     * Elimina un registro de disponibilidad.
     */
    @DELETE
    @Path("aulas/{idAula}/disponibilidad/{idTurno}")
    public Response deleteDisponibilidad(
            @PathParam("idAula") String idAulaStr,
            @PathParam("idTurno") String idTurnoStr) {
        try {
            UUID idAula = UUID.fromString(idAulaStr);
            UUID idTurno = UUID.fromString(idTurnoStr);

            // 1. Instanciar la llave compuesta para realizar la lectura de control
            DisponibilidadAulaTurnoId idCompuesto = new DisponibilidadAulaTurnoId();
            idCompuesto.setIdAula(idAula);
            idCompuesto.setIdTurno(idTurno);

            DisponibilidadAulaTurno existente = disponibilidadDAO.leer(idCompuesto);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("La disponibilidad solicitada no existe.")
                        .build();
            }

            // 2. REGLA DE INTEGRIDAD (Similitud con Temas/Áreas):
            // TODO: Cuando desarrolles el AsignacionesAulaDAO, debes validar que la lista de alumnos citados esté vacía
            // List<Asignacion> asignaciones = asignacionDAO.findByAulaAndTurno(idAula, idTurno);
            // if (!asignaciones.isEmpty()) { return Response.status(Status.CONFLICT)... }

            disponibilidadDAO.eliminar(existente);
            return Response.noContent().build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("UUID inválido.")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }
}