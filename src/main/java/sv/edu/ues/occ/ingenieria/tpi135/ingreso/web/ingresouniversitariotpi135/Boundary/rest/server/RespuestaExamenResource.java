package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.*;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.ExamenRealizadoDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.IngresoDefaultDataAccess;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntasPorClaveDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.RespuestaExamenDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestaExamen;

import java.util.List;
import java.util.UUID;

/**
 * Recurso REST para la gestion de respuestas de examen.
 * Hereda el endpoint GET paginado de AbstractResource.
 * Expone operaciones de creacion y consulta bajo /resources/v1/respuesta_examen.
 */
@Path("respuesta_examen")
public class RespuestaExamenResource extends AbstractResource<RespuestaExamen> {

    @Inject
    RespuestaExamenDAO respuestaExamenDAO;

    @Inject
    ExamenRealizadoDAO examenRealizadoDAO;

    @Inject
    PreguntaOpcionDAO preguntaOpcionDAO;

    @Inject
    PreguntasPorClaveDAO preguntasPorClaveDAO;

    @QueryParam("examenId")
    String examenIdParam;

    @Override
    protected IngresoDefaultDataAccess<RespuestaExamen> getDAO() {
        return respuestaExamenDAO;
    }

    @Override
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response findRange(
            @DefaultValue("0")  @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max")   int max) {
        try {
            if (examenIdParam != null && !examenIdParam.isBlank()) {
                UUID examenId;
                try {
                    examenId = UUID.fromString(examenIdParam);
                } catch (IllegalArgumentException ex) {
                    return Response.status(422)
                            .header(MISSING_PARAMETER, "examenId must be a valid UUID")
                            .build();
                }

                List<RespuestaExamen> list = respuestaExamenDAO.findByExamenId(examenId);
                return Response.ok(list).build();
            }

            // Sin examenId: comportamiento paginado normal heredado
            return super.findRange(first, max);
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(SERVER_EXCEPTION, "Cannot access db")
                    .build();
        }
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findById(@PathParam("id") UUID id) {
        if (id != null) {
            try {
                RespuestaExamen resp = respuestaExamenDAO.leer(id);
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
    public Response create(RespuestaExamen entity, @Context UriInfo uriInfo) {
        if (entity != null
                && entity.getIdRespuestaExamen() == null
                && entity.getIdExamen() != null
                && entity.getIdExamen().getIdExamenRealizado() != null
                && entity.getIdPreguntaOpcion() != null
                && entity.getIdPreguntaOpcion().getIdPreguntaOpcion() != null) {
            try {
                UUID examenId = entity.getIdExamen().getIdExamenRealizado();
                UUID opcionId = entity.getIdPreguntaOpcion().getIdPreguntaOpcion();

                ExamenRealizado examen = examenRealizadoDAO.leer(examenId);
                if (examen == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .header(NOT_FOUND_ID, "Examen with id " + examenId + " not found")
                            .build();
                }

                PreguntaOpcion opcion = preguntaOpcionDAO.leer(opcionId);
                if (opcion == null || opcion.getIdPregunta() == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .header(NOT_FOUND_ID, "PreguntaOpcion with id " + opcionId + " not found")
                            .build();
                }

                UUID preguntaId = opcion.getIdPregunta().getIdBancoPregunta();
                if (!preguntasPorClaveDAO.existsByClaveAndPregunta(examen.getIdClave().getIdClaveExaman(), preguntaId)) {
                    return Response.status(422)
                            .header(MISSING_PARAMETER, "pregunta does not belong to examen.clave")
                            .build();
                }

                if (respuestaExamenDAO.existsByExamenAndPregunta(examenId, preguntaId)) {
                    return Response.status(Response.Status.CONFLICT)
                            .header(CONFLICT_REASON, "duplicate response for examen and pregunta")
                            .build();
                }

                entity.setIdExamen(examen);
                entity.setIdPreguntaOpcion(opcion);
                respuestaExamenDAO.crear(entity);
                return Response.created(
                                uriInfo.getAbsolutePathBuilder()
                                        .path(String.valueOf(entity.getIdRespuestaExamen()))
                                        .build())
                        .build();
            } catch (Exception ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .header(SERVER_EXCEPTION, "Cannot access db")
                        .build();
            }
        }
        return Response.status(422)
                .header(MISSING_PARAMETER, "entity must not be null; id must be null; idExamen and idPreguntaOpcion must not be null")
                .build();
    }
}
