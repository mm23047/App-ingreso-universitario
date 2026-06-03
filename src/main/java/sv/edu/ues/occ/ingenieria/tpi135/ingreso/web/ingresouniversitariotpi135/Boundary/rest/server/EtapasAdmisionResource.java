package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.EtapasAdmisionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.net.URI;
import java.util.UUID;

/**
 * Recurso REST para gestionar Etapas de Admisión.
 * 
 * Base: /resources/v1/etapas
 */
@Path("etapas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EtapasAdmisionResource extends AbstractResource<EtapasAdmision> {

    @Inject
    private EtapasAdmisionDAO etapasAdmisionDAO;

    @Override
    protected EtapasAdmisionDAO getDAO() {
        return etapasAdmisionDAO;
    }

    /**
     * GET /etapas
     * Retorna lista paginada de etapas.
     */
    @GET
    public Response listEtapas(
            @DefaultValue("0") @QueryParam("first") int first,
            @DefaultValue("50") @QueryParam("max") int max) {
        return findRange(first, max);
    }

    /**
     * POST /etapas
     * Crea una nueva etapa de admisión.
     */
    @POST
    public Response createEtapa(EtapasAdmision etapa, @Context UriInfo uriInfo) {
        if (etapa == null || etapa.getNombre() == null || etapa.getNombre().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El nombre de la etapa es obligatorio")
                    .header(RestHeaders.MISSING_PARAMETER, "nombre")
                    .build();
        }
        // --- NUEVA VALIDACIÓN DE FRONTERA ---
        if (etapa.getCantidadPreguntasRequeridas() == null || etapa.getCantidadPreguntasRequeridas() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Debe especificar una cantidad válida (mayor a 0) de preguntas requeridas para esta etapa.")
                    .header(RestHeaders.MISSING_PARAMETER, "cantidadPreguntasRequeridas")
                    .build();
        }

        try {
            etapasAdmisionDAO.crear(etapa);
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(etapa.getIdEtapaAdmision().toString())
                    .build();
            return Response.created(location)
                    .entity(etapa)
                    .build();
        } catch (IllegalArgumentException e) {
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
     * GET /etapas/{idEtapa}
     * Obtiene una etapa específica.
     */
    @GET
    @Path("{idEtapa}")
    public Response getEtapa(@PathParam("idEtapa") String idEtapaStr) {
        try {
            UUID idEtapa = UUID.fromString(idEtapaStr);
            EtapasAdmision etapa = etapasAdmisionDAO.leer(idEtapa);
            if (etapa == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idEtapaStr)
                        .build();
            }
            return Response.ok(etapa).build();
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
     * PUT /etapas/{idEtapa}
     * Actualiza una etapa.
     */
    @PUT
    @Path("{idEtapa}")
    public Response updateEtapa(@PathParam("idEtapa") String idEtapaStr, EtapasAdmision etapa) {
        try {
            UUID idEtapa = UUID.fromString(idEtapaStr);
            EtapasAdmision existente = etapasAdmisionDAO.leer(idEtapa);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idEtapaStr)
                        .build();
            }

            etapa.setIdEtapaAdmision(idEtapa);
            EtapasAdmision actualizada = etapasAdmisionDAO.actualizar(etapa);
            return Response.ok(actualizada).build();
        } catch (IllegalArgumentException e) {
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
     * DELETE /etapas/{idEtapa}
     * Elimina una etapa.
     */
    @DELETE
    @Path("{idEtapa}")
    public Response deleteEtapa(@PathParam("idEtapa") String idEtapaStr) {
        try {
            UUID idEtapa = UUID.fromString(idEtapaStr);
            EtapasAdmision existente = etapasAdmisionDAO.leer(idEtapa);
            if (existente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .header(RestHeaders.NOT_FOUND_ID, idEtapaStr)
                        .build();
            }

            etapasAdmisionDAO.eliminar(existente);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header(RestHeaders.SERVER_EXCEPTION, e.getMessage())
                    .build();
        }
    }
}
