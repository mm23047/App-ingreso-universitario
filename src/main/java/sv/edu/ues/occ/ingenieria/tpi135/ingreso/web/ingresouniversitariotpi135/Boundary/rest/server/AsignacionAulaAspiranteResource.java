package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AsignacionAulaAspiranteDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.AulaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.DisponibilidadAulaTurnoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionAulaAspirante;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Aula;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.DisponibilidadAulaTurno;

import java.util.UUID;

@Path("asignacion_aula_aspirante")
public class AsignacionAulaAspiranteResource extends AbstractResource<AsignacionAulaAspirante> {

    @Inject
    AsignacionAulaAspiranteDAO asignacionAulaAspiranteDAO;

    @Inject
    AulaDAO aulaDAO;

    @Inject
    DisponibilidadAulaTurnoDAO disponibilidadAulaTurnoDAO;

    @Override
    protected IngresoDefaultDataAccess<AsignacionAulaAspirante> getDAO() {
        return asignacionAulaAspiranteDAO;
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                AsignacionAulaAspirante resp = asignacionAulaAspiranteDAO.leer(id);
                if (resp != null) {
                    return Response.ok(resp).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "id")
                .build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response create(AsignacionAulaAspirante entity, @Context UriInfo uriInfo) {
        if (entity != null
                && entity.getId() == null
                && entity.getIdInscripcion() != null
                && entity.getIdInscripcion().getId() != null
                && entity.getDisponibilidad() != null
                && entity.getDisponibilidad().getIdAula() != null
                && entity.getDisponibilidad().getIdAula().getId() != null
                && entity.getDisponibilidad().getIdTurno() != null
                && entity.getDisponibilidad().getIdTurno().getId() != null) {
            try {
                UUID idAula = entity.getDisponibilidad().getIdAula().getId();
                UUID idTurno = entity.getDisponibilidad().getIdTurno().getId();
                UUID idInscripcion = entity.getIdInscripcion().getId();

                if (!disponibilidadAulaTurnoDAO.existsByAulaAndTurno(idAula, idTurno)) {
                    return Response.status(Response.Status.CONFLICT)
                            .header(CONFLICT_REASON, "disponibilidad aula-turno no existe")
                            .build();
                }

                if (asignacionAulaAspiranteDAO.existsByInscripcionAndTurno(idInscripcion, idTurno)) {
                    return Response.status(Response.Status.CONFLICT)
                            .header(CONFLICT_REASON, "inscripcion ya asignada en este turno")
                            .build();
                }

                Aula aula = aulaDAO.leer(idAula);
                if (aula == null || aula.getCapacidadFisica() == null) {
                    return Response.status(Response.Status.CONFLICT)
                            .header(CONFLICT_REASON, "aula no disponible o capacidad invalida")
                            .build();
                }

                long asignaciones = asignacionAulaAspiranteDAO.countByAulaAndTurno(idAula, idTurno);
                if (asignaciones >= aula.getCapacidadFisica()) {
                    return Response.status(Response.Status.CONFLICT)
                            .header(CONFLICT_REASON, "aula sin capacidad disponible")
                            .build();
                }

                asignacionAulaAspiranteDAO.crear(entity);
                return Response.created(
                                uriInfo.getAbsolutePathBuilder()
                                        .path(String.valueOf(entity.getId()))
                                        .build())
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "entity must not be null; id must be null; idInscripcion.id, disponibilidad.idAula.id and disponibilidad.idTurno.id must not be null")
                .build();
    }

    @PUT
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response update(@PathParam("id") UUID id, AsignacionAulaAspirante entity) {
        if (id != null && entity != null) {
            try {
                AsignacionAulaAspirante existing = asignacionAulaAspiranteDAO.leer(id);
                if (existing != null) {
                    entity.setId(id);
                    asignacionAulaAspiranteDAO.actualizar(entity);
                    return Response.ok(entity).build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "id and entity must not be null")
                .build();
    }

    @DELETE
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response delete(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                AsignacionAulaAspirante existing = asignacionAulaAspiranteDAO.leer(id);
                if (existing != null) {
                    asignacionAulaAspiranteDAO.eliminar(existing);
                    return Response.noContent().build();
                }
                return Response.status(Response.Status.NOT_FOUND)
                        .header(NOT_FOUND_ID, "Record with id " + id + " not found")
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "id")
                .build();
    }
}